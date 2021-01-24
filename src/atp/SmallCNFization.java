/*
    Clausification of first-order formulas. Clausification is done in
    several steps","

    1) Simplification

       Exhaustively apply the simplifiction rules described in the header
       to FormulaSimplify

    2) Construction of the Negation Normal Form

    3) Miniscoping

    4) Variable renaming

    5) Skolemization

    6) Shift out universal quantors

    7) Distribute disjunctions

    8) Extract clauses

    This basically follows [NW","SmallCNF-2001], albeit with some minor
    changes. The first version does not use formula renaming.

    @InCollection{NW","SmallCNF-2001,
      author =       {A. Nonnengart and C. Weidenbach},
      title =        {{Computing Small Clause Normal Forms}},
      booktitle =    {Handbook of Automated Reasoning},
      publisher =    {Elsevier Science and MIT Press},
      year =         {2001},
      editor =       {A. Robinson and A. Voronkov},
      volume =       {I},
      chapter =      {5},
      pages =        {335--367}
    }

This code is a translation to Java of the PyRes system written by Stephan Schulz

Copyright 2010-2011 Adam Pease, apease@articulatesoftware.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program ; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston,
MA  02111-1307 USA 
*/

package atp;

import java.io.*;
import java.util.*;

public class SmallCNFization extends Clausifier {

    private static int skolemCount = 0;
    
    /** ***************************************************************
     * Return a new skolem symbol. This is a simple version, not 
     * suitable for a real production system. The symbol is not
     * guaranteed to be globally fresh. It's the user's
     * responsibility to ensure that no symbols of the form
     * "skolemXXXX" are in the input.
     */
    public static String newSkolemSymbol() {

        skolemCount += 1;
        return String.format("skolem%04d",skolemCount);
    }

    /** ***************************************************************
     *  Return a new skolem term for the given (list of) variables.
     */
    public static Term newSkolemTerm(SortedSet<Term> varlist) {

        Term result = new Term();
        result.t = newSkolemSymbol();
        for (Term v : varlist)
            result.subterms.add(v);
        return result;
    }
           
    /** ***************************************************************
     * Simplify the formula by eliminating the <=, ~|, ~& and <~>. This
        is not strictly necessary, but means fewer cases to consider
        later. The following rules are applied exhaustively:
        F~|G  -> ~(F|G)
        F~&G  -> ~(F&G)
        F<=G  -> G=>F
        F<~>G -> ~(F<=>G)

        @return f' or null if not modified
     */
    public static BareFormula formulaOpSimplify(BareFormula input) {

    	//System.out.println("INFO in SmallCNFization.formulaOpSimplify(): " + input.toStructuredString());
        BareFormula f = input.deepCopy();
        if (f.isLiteral()) {
            return null;
        }
        boolean modified = false;
        
        // First simplify the subformulas
        if (f.child1 != null) {
            BareFormula child1 = formulaOpSimplify(f.child1);
            modified = modified || (child1 != null);
            if (child1 != null)
            	f.child1 = child1;
        }
        if (f.child2 != null) {
            BareFormula child2 = formulaOpSimplify(f.child2);
            modified = modified || (child2 != null);
            if (child2 != null)
            	f.child2 = child2;
        }        

        if (f.op.equals("<~>")) {
            BareFormula handle = f.deepCopy();
            handle.op = "<=>";
            BareFormula newform = new BareFormula("~", handle);
            return newform;
        }
        else if (f.op.equals("<=")) {
        	BareFormula newform = new BareFormula("=>", f.child2, f.child1, f.lit2, f.lit1);
            return newform;
        }
        else if (f.op.equals("~|")) {
            BareFormula handle = f.deepCopy();
            handle.op = "|";
            BareFormula newform = new BareFormula("~", handle);
            return newform;
        }
        else if (f.op.equals("~&")) {
            BareFormula handle = f.deepCopy();
            handle.op = "&";
            BareFormula newform = new BareFormula("~", handle);
            return newform;
        }
        if (!modified)
            return null;
        //System.out.println("INFO in SmallCNFization.formulaOpSimplify(): returning: " + f.toStructuredString());
        return f;
    }
    
    /** ***************************************************************
     * Try to apply the following simplification rules to f at the top
     * level. Return (f',m), where f' is the result of simplification,
     * and m indicates if f'!=f.
     */
    public static BareFormula formulaTopSimplify(BareFormula f) {

    	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): " + f.toStructuredString());
    	//System.out.println(" with op " + f.op);
    	if (Term.emptyString(f.op)) {
    		if (f.child1 != null)
    			return formulaTopSimplify(f.child1);
    		return null;
    	}
    	else if (f.op.equals("~")) {
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): not");
            if (f.lit1 != null)
                // Push ~ into literals if possible. This covers the case
                // of ~~P -> P if one of the negations is in the literal.
                return new BareFormula("", f.lit1.negate());
        }
        else if (f.op.equals("|")) {
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): or");
            if (f.lit1 != null && f.lit1.atomIsConstTrue())
                // T | P -> T. Note that child1 is $true or
                // equivalent. This applies to several other cases where we
                // can reuse a $true or $false child instead of creating a
                // new formula.
                return new BareFormula ("",f.child2,null,f.lit2,null);
            else if (f.rhsIsConstTrue()) {
                // P | T -> T
            	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): child1 or lit1 is $true");
                return new BareFormula ("",f.child1,null,f.lit1,null);
            }
            else if (f.lhsIsConstTrue()) {
                // T | P -> T
                //System.out.println("INFO in SmallCNFization.formulaTopSimplify(): lit2 is $true");
                return new BareFormula ("",f.child2,null,f.lit2,null);
            }
            else if (f.lhsIsConstFalse())
                // F | P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.rhsIsConstFalse())
                // P | F -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.childrenEqual())
                // P | P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.childrenEqual()) {
                // P | P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            }
        }
        else if (f.op.equals("&")) {
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): and");
            if (f.lhsIsConstTrue())
                // T & P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.rhsIsConstTrue())
                // P & T -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.lhsIsConstFalse())
                // F & P -> F
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.rhsIsConstFalse())
                // P & F -> F
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.childrenEqual())
                // P & P -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
        }
        else if (f.op.equals("<=>")) {
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): equiv");
            if (f.lhsIsConstTrue())
                // T <=> P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.rhsIsConstTrue())
                // P <=> T -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.rhsIsConstFalse()) {
                // P <=> F -> ~P
                BareFormula newform = new BareFormula("~", f.child2, null, f.lit2, null);        
                newform = formulaSimplify(newform);
                return newform;
            }
            else if (f.lhsIsConstFalse()) {
                // F <=> P -> ~P
                BareFormula newform = new BareFormula("~", f.child1, null, f.lit1, null);            
                newform = formulaSimplify(newform);
                return newform;
            }
            else if (f.childrenEqual())
                // P <=> P -> T
                return new BareFormula("", Literal.string2lit("$true"));
        }
        else if (f.op.equals("=>")) {
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): implication");
            if (f.lhsIsConstTrue())
                // T => P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.lhsIsConstFalse())
                // F => P -> T
                return new BareFormula("", Literal.string2lit("$true"));
            else if (f.rhsIsConstTrue())
                // P => T -> T
                return new BareFormula("",Literal.string2lit("$true"));
            else if (f.rhsIsConstFalse()) {
                // P => F -> ~P
                BareFormula newform = new BareFormula("~", f.child1, null, f.lit1, null);
                BareFormula simplified = newform.promoteChildren();
                if (simplified != null)
                    newform = simplified;
                //System.out.println("INFO in SmallCNFization.formulaTopSimplify(): newform: " + newform);
                simplified = formulaSimplify(newform);
                if (simplified != null) // formulaSimplified returns null if formula is unchanged
                    newform = simplified;
                //System.out.println("INFO in SmallCNFization.formulaTopSimplify(): result: " + newform);
                return newform;
            }
            else if (f.childrenEqual())
                // P => P -> T
                return new BareFormula("", Literal.string2lit("$true"));
        }
        else if (f.isQuantified()) {
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): quantified");
            // ![X] F -> F if X is not free in F
            // ?[X] F -> F if X is not free in F
            SortedSet<Term> vars = f.collectFreeVars();
        	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): vars"  + vars);
            if (!vars.contains(f.lit1.atom)) {
            	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): no free vars: " + f);
            	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): lit2: " + f.lit2);
            	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): child2: " + f.child2);
                BareFormula bfnew = new BareFormula("",f.child2,null,f.lit2,null);
            	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): bfnew: " + bfnew);
                return bfnew;
            }
        }
        else
        	System.out.println("Error in SmallCNFization.formulaTopSimplify(): unexpected operator");
        return null; // false
    }                

    /** ***************************************************************
     * Exhaustively apply simplification to f, creating the simplified
     * version f'. See formulaTopSimplify() above for the individual rules.
     * Returns (f', True) if f'!=f, (f', False) otherwise (i.e. if no
     * simplification happened because the formula already was completely
     * simplified.
     */
    public static BareFormula formulaSimplify(BareFormula input) {

    	BareFormula f = input.deepCopy();
    	//System.out.println("INFO in SmallCNFization.formulaSimplify(): " + f.toStructuredString());
        if (f.isLiteral())
            return null;

        boolean modified = false;        
        // First simplify the subformulas
        if (f.child1 != null) {
            BareFormula child1 = formulaOpSimplify(f.child1);
            modified = modified || (child1 != null);
            if (child1 != null)
            	f.child1 = child1;
        }
        if (f.child2 != null) {
            BareFormula child2 = formulaOpSimplify(f.child2);
            modified = modified || (child2 != null);
            if (child2 != null)
            	f.child2 = child2;
        }    
    	//System.out.println("INFO in SmallCNFization.formulaSimplify(): before promote children");
        BareFormula simpf = f.promoteChildren();
    	if (simpf != null) {
    		f = simpf;
    		modified = true;
    	}
    	//System.out.println("INFO in SmallCNFization.formulaSimplify(): after promote children: " + f);
        boolean topmod = true;
        while (topmod) {
            //System.out.println("INFO in SmallCNFization.formulaSimplify(): in loop before topsimplify: " + f);
            BareFormula newf = formulaTopSimplify(f);
            //System.out.println("INFO in SmallCNFization.formulaSimplify(): after topsimplify: " + newf);
            if (newf != null) {
            	simpf = newf.promoteChildren();
            	if (simpf != null) {
            		newf = simpf;
            		modified = true;
            	}
            }
            if (newf != null) {
                f = newf;
            }
            else
            	topmod = false;
            modified = modified || (newf != null);
        	//System.out.println("INFO in SmallCNFization.formulaSimplify(): in loop: " + f);
        }
        if (!modified)
            return null;
        //System.out.println("INFO in SmallCNFization.formulaSimplify(): returning: " + f.toStructuredString());
        return f;
    }

    /** ***************************************************************
     * Apply all NNF transformation rules that can be applied at top
     * level. Return result and null if not modified.
     */
    public static BareFormula rootFormulaNNF(BareFormula f, boolean polarity) {

//       	System.out.println("INFO in SmallCNFization.rootFormulaNNF(): " + f.toStructuredString());
       	//if (f.child1 != null) {
//            System.out.println("INFO in SmallCNFization.rootFormulaNNF(): child1: " + f.child1.toStructuredString());
//            System.out.println("INFO in SmallCNFization.rootFormulaNNF(): child1.op: " + f.child1.op);
//        }
       	//System.out.println("INFO in SmallCNFization.rootFormulaNNF(): op: " + f.op);
        //System.out.println("INFO in SmallCNFization.rootFormulaNNF(): op is ~: " + f.op.equals("~"));
        //System.out.println("INFO in SmallCNFization.rootFormulaNNF(): has literal: " + (f.lit1 != null));
        boolean normalform = false;
        boolean modified = false;

        while (!normalform) {
            normalform = true;
            boolean m = false;

            if (f.op.equals("~")) {
                if (f.lit1 != null) {
                    // Move negations into literals
                    f = new BareFormula("", f.lit1.negate());
                    //System.out.println("INFO in SmallCNFization.rootFormulaNNF(): negation result: " + f);
                    m = true;
                }
                else if (f.child1.op.equals("|")) {
                    // De Morgan: ~(P|Q) -> ~P & ~Q
                    f = new BareFormula("&",
                        new BareFormula("~", f.child1.child1,null,f.child1.lit1,null),
                        new BareFormula("~", f.child1.child2,null,f.child1.lit2,null));
                    m = true;
                }
                else if  (f.child1.op.equals("&")) {
                    // De Morgan: ~(P&Q) -> ~P | ~Q
                    f = new BareFormula("|",
                            new BareFormula("~", f.child1.child1,null,f.child1.lit1,null),
                            new BareFormula("~", f.child1.child2,null,f.child1.lit2,null));
                    m = true;
                }
                else if (f.child1.op.equals("!")) {
                    // ~(![X]:P) -> ?[X]:~P
                    f = new BareFormula("?",
                                f.child1.lit1,
                                new BareFormula("~", f.child1.child2,null,f.child1.lit2,null));
                    m = true;
                }
                else if (f.child1.op.equals("?")) {
                    // ~(?[X]:P) -> ![X]:~P
                    f = new BareFormula("!",
                                f.child1.lit1,
                                new BareFormula("~", f.child1.child2,null,f.child1.lit2,null));
                    m = true;
                }
            }
            else if (f.op.equals("=>")) {
                // Expand P=>Q into ~P|Q
                f = new BareFormula("|",
                        new BareFormula("~", f.child1, null, f.lit1, null),
                        new BareFormula("", f.child2, null, f.lit2, null));
                m = true;
            }
            else if  (f.op.equals("<=>")) {
                if (polarity == true) {
                    // P<=>Q -> (P=>Q)&(Q=>P)
                    f = new BareFormula("&",
                            new BareFormula("=>", f.child1, f.child2, f.lit1, f.lit2),
                            new BareFormula("=>", f.child2, f.child1, f.lit2, f.lit1));
                    m = true;
                }
                else {                                   
                    // P<=>Q -> (P & Q) | (~P & ~Q)
                    f = new BareFormula("|",
                            new BareFormula("&", f.child1, f.child2, f.lit1, f.lit2),
                            new BareFormula("&",
                                    new BareFormula("~", f.child1, null, f.lit1, null),
                                    new BareFormula("~", f.child2, null, f.lit2, null)));
                    m = true;
                }
            }            
            normalform = !m;
           	modified = modified || m;
        }

        if (!modified) {
            //System.out.println("INFO in SmallCNFization.rootFormulaNNF(): not modified, returning null");
            return null;
        }
        //System.out.println("INFO in SmallCNFization.rootFormulaNNF(): returning: " + f);
        return f;
    }

    /** ***************************************************************
     * Convert f into a NNF. Equivalences (<=>) are eliminated
     * polarity-dependend, top to bottom. Returns null if formula 
     * is not modified
     */
    public static BareFormula formulaNNF(BareFormula f, boolean polarity) {

    	//System.out.println();
       	//System.out.println("INFO in SmallCNFization.formulaNNF(): " + f);
        //System.out.println("INFO in SmallCNFization.formulaNNF():polarity: " + polarity);

        boolean normalform = false;
        boolean modified   = false;
        boolean m = false;
        BareFormula fprime = f.deepCopy();
        BareFormula newf = fprime.promoteChildren();
        if (newf == null)
        	newf = fprime;
       	//System.out.println("INFO in SmallCNFization.formulaNNF(): after promotion: " + newf.toStructuredString());
        while (!normalform) {
            //System.out.println("INFO in SmallCNFization.formulaNNF(): top of loop: " + newf.toStructuredString());
            normalform = true;
            BareFormula fnew = rootFormulaNNF(newf, polarity);
            if (fnew == null)
                fnew = newf;
            else
                m = true;
            //System.out.println("INFO in SmallCNFization.formulaNNF(): after root formula nnf: " + fnew.toStructuredString());
            modified = modified || m;
           	//System.out.println("INFO in SmallCNFization.formulaNNF(): op: " + fnew.op);
            if (Term.emptyString(fnew.op)) {
                BareFormula promoted = fnew.promoteChildren();
                if (promoted != null) {
                    fnew = promoted;
                    m = true;
                }
            }
            else if (fnew.op.equals("~")) {
                if (fnew.child1 != null) {
                    BareFormula handle = formulaNNF(fnew.child1, !polarity);
                    if (handle != null) {
                        m = true;
                        normalform = false;
                        fnew = new BareFormula("~", handle);
                    }
                }
            }
            else if (fnew.op.equals("!") || fnew.op.equals("?")) {
                if (fnew.child2 != null) {
                    BareFormula handle = formulaNNF(fnew.child2, polarity);
                    if (handle != null) {
                        m = true;
                        normalform = false;
                        fnew = new BareFormula(fnew.op, fnew.lit1, handle);
                    }
                }
            }
            else if (fnew.op.equals("|") || fnew.op.equals("&")) {
                //System.out.println("INFO in SmallCNFization.formulaNNF(): fnew: " + fnew.toStructuredString());
                //System.out.println("INFO in SmallCNFization.formulaNNF(): op (2): " + fnew.op);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): child1: " + fnew.child1);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): child2: " + fnew.child2);
                BareFormula handle1 = null;
                BareFormula handle2 = null;
                if (fnew.child1 != null)
                    handle1 = formulaNNF(fnew.child1, polarity);
                if (fnew.child2 != null)
                    handle2 = formulaNNF(fnew.child2, polarity);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): handle1: " + handle1);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): handle2: " + handle2);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): lit1: " + fnew.lit1);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): lit2: " + fnew.lit2);
                //System.out.println("INFO in SmallCNFization.formulaNNF(): op: " + fnew.op);
                m = (handle1 != null) || (handle2 != null);
                if (m) {
                    if (handle1 == null)
                        handle1 = fnew.child1;
                    if (handle2 == null)
                        handle2 = fnew.child2;
                    normalform = false;
                    fnew = new BareFormula(fnew.op, handle1, handle2,fnew.lit1,fnew.lit2);
                    //System.out.println("INFO in SmallCNFization.formulaNNF(): modified" );
                }
                //else
                    //System.out.println("INFO in SmallCNFization.formulaNNF(): not modified" );
            }
           	modified = modified || m;
            newf = fnew;
        }
        if (!modified)
            return null;
        //System.out.println("INFO in SmallCNFization.formulaNNF(): returning: " + newf);
        //System.out.println("INFO in SmallCNFization.formulaNNF(): from input: " + f);
        return newf;
    }
    
    /** ***************************************************************
     * Perform miniscoping, i.e. move quantors in as far as possible, so
     * that their scope is only the smallest subformula in which the
     * variable occurs. 
     */
    public static BareFormula formulaMiniScope(BareFormula f) {

        BareFormula newf = f.deepCopy();
        //System.out.println();
        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): f: " + f.toStructuredString());
        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): op: " + f.op);
        //if (f.child1 != null) System.out.println("INFO in SmallCNFization.formulaMiniScope(): child1: " + f.child1);
        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): lit1: " + f.lit1);
        //if (f.child2 != null) System.out.println("INFO in SmallCNFization.formulaMiniScope(): child2: " + f.child2);
        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): lit2: " + f.lit2);
        boolean res = false;
        if (f.isQuantified()) {
            if (f.child2 == null)
                return null;
            String op = f.child2.op;
            String quant = f.op;
            //System.out.println("INFO in SmallCNFization.formulaMiniScope(): quant : " + quant);
            //System.out.println("INFO in SmallCNFization.formulaMiniScope(): op : " + op);
            Term var   = f.lit1.atom;
            BareFormula subf  = f.child2;
            if (op.equals("&") || op.equals("|")) {
                if (subf.child1 != null && !subf.child1.collectFreeVars().contains(var)) {
                    //System.out.println("INFO in SmallCNFization.formulaMiniScope(): quantified var not in child1 free vars: ");
                    // q[X]:(P op Q)  -> P op (q[X]:Q) if X not free in P
                    BareFormula arg1 = subf.child1;
                    BareFormula arg2 = new BareFormula(quant, null, subf.child2, f.lit1, subf.lit2);
                    newf = new BareFormula(op, arg1, arg2);
                    res = true;
                }
                else if (subf.child2 != null && !subf.child2.collectFreeVars().contains(var)) {
                    //System.out.println("INFO in SmallCNFization.formulaMiniScope(): quantified var not in child2 free vars: ");
                    // q[X]:(P op Q)  -> (q[X]:P) op Q if X not free in Q
                    BareFormula arg1 = new BareFormula(quant, null, subf.child1, f.lit1, subf.lit1);
                    BareFormula arg2 = subf.child2;
                    newf = new BareFormula(op, arg1, arg2, null, subf.lit2);
                    res = true;
                }
                else {
                    if (op.equals("&") && quant.equals("!")) {
                        // ![X]:(P&Q) -> ![X]:P & ![X]:Q
                        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): pattern ![X]:(P&Q)in f : " + f);
                        BareFormula arg1 = new BareFormula("!", null, subf.child1, f.lit1, subf.lit1);
                        BareFormula arg2 = new BareFormula("!", null, subf.child2, f.lit1, subf.lit2);
                        newf = new BareFormula("&" , arg1, arg2);
                        res = true;
                    }
                    else if (op.equals("|") && quant.equals("?")) {
                        // ?[X]:(P|Q) -> ?[X]:P | ?[X]:Q
                        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): pattern ?[X]:(P|Q) in f : " + f);
                        BareFormula arg1 = new BareFormula("?", null, subf.child1, f.lit1, subf.lit1);
                        BareFormula arg2 = new BareFormula("?", null, subf.child2, f.lit1, subf.lit2);
                        newf = new BareFormula("|", arg1, arg2);
                        res = true;
                    }
                }
            }
        }
        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): after patterns: " + newf);
        BareFormula arg1 = newf.child1;
        BareFormula arg2 = newf.child2;
        boolean modified = false;
        if (newf.hasSubform1() && newf.child1 != null) {
            arg1 = formulaMiniScope(newf.child1);
            modified = modified || (arg1 != null);
            if (arg1 == null)
                arg1 = newf.child1;
        }
        if (newf.hasSubform2() && newf.child2 != null) {
            arg2 = formulaMiniScope(newf.child2);
            modified = modified || (arg2 != null);
            if (arg2 == null)
                arg2 = newf.child2;
        }
        if (modified) {
            newf = new BareFormula(newf.op, arg1, arg2, newf.lit1, newf.lit2);
            newf = formulaMiniScope(newf);
            res = true;
        }
        if (res == false || newf == null) {
            //System.out.println("INFO in SmallCNFization.formulaMiniScope(): returning: null ");
            return null;
        }
        //System.out.println("INFO in SmallCNFization.formulaMiniScope(): returning: " + newf.toStructuredString());
        return newf;
    }

    /** ***************************************************************
     * Perform Skolemization of f, which is assumed to be in the scope of
     * the list of variables provided.
     */
    public static BareFormula formulaRekSkolemize(BareFormula f, SortedSet<Term> variables, Substitutions subst) {

        //System.out.println("INFO in SmallCNFization.formulaRekSkolemize(): f: " + f.toStructuredString());
        //System.out.println("INFO in SmallCNFization.formulaRekSkolemize(): f.op: " + f.op);
        //System.out.println("INFO in SmallCNFization.formulaRekSkolemize(): vars: " + variables);
        if (f.isLiteral()) {
            if (f.child1 != null) {
                BareFormula child = f.child1.substitute(subst);
                f = new BareFormula("", child);
            }
            else if (f.lit1 != null) {
                Literal lit = f.lit1.instantiate(subst);
                f = new BareFormula("", lit);
            }
        }
        else if (f.op.equals("?")) {
            Term var = f.lit1.atom;
            Term skTerm = newSkolemTerm(variables);
            Term oldbinding = subst.modifyBinding(var,skTerm);
            if (f.child2 != null) {
                BareFormula newf = formulaRekSkolemize(f.child2, variables, subst);
                if (newf != null)
                    f = newf;
            }
            else {
                Literal lit = f.lit2.instantiate(subst);
                f = new BareFormula("", lit);
            }
            subst.modifyBinding(var, oldbinding);
        }
        else if (f.op.equals("!")) {
            Term var = f.lit1.atom;
            variables.add(var);
            if (f.child2 != null) {
                BareFormula handle = formulaRekSkolemize(f.child2, variables, subst);
                if (handle == null)
                    handle = f.child2;
                f = new BareFormula("!", new Literal(var), handle);
            }
            else {
                Literal lit = f.lit2.instantiate(subst);
                f = new BareFormula("!", null,null,new Literal(var), lit);
            }
            variables.remove(var);
        }
        else {
            //System.out.println("INFO in SmallCNFization.formulaRekSkolemize(): no quantifier and not a literal f: " + f.toStructuredString());
            BareFormula arg1 = f.child1;
            BareFormula arg2 = f.child2;
            Literal lit1 = f.lit1;
            Literal lit2 = f.lit2;
            if (f.hasSubform1()) {
                if (f.child1 != null) {
                    arg1 = formulaRekSkolemize(f.child1, variables, subst);
                    if (arg1 == null)
                        arg1 = f.child1;
                }
                else
                    lit1 = f.lit1.instantiate(subst);
            }
            if (f.hasSubform2()) {
                if (f.child2 != null) {
                    arg2 = formulaRekSkolemize(f.child2, variables, subst);
                    if (arg2 == null)
                        arg2 = f.child2;
                }
                else
                    lit2 = f.lit2.instantiate(subst);
            }
            f = new BareFormula(f.op, arg1, arg2, lit1, lit2);
        }
        //System.out.println("INFO in SmallCNFization.formulaRekSkolemize(): returning f: " + f.toStructuredString());
        return f;
    }
        
    /** ***************************************************************
     * Perform an outermost Skolemization of f, removing all existential
     * quantifiers. Formulas are considered to be universally closed,
     * i.e. free variables (which should not occur) are treated as
     * universally quantified.
     */
    public static BareFormula formulaSkolemize(BareFormula f) {

        SortedSet<Term> vars = f.collectFreeVars();
        return formulaRekSkolemize(f, vars, new Substitutions());        
    }

    /** ***************************************************************
     * Remove all quantors from f, returning the quantor-free core of the
     * formula and a list of quantified variables. This will only be
     * applied to Skolemized formulas, thus finding an existential
     * quantor is an error. To be useful, the input formula also has to be
     * variable-normalized. 
     */
    public static BareFormula separateQuantors(BareFormula f, ArrayList<Term> varlist) {

        //System.out.println("INFO in SmallCNFization.separateQuantors(): f: " + f.toStructuredString());
        BareFormula result = f.deepCopy();
        if (varlist == null)
            varlist = new ArrayList<Term>();

        if (f.isQuantified()) {
            assert f.op.equals("!");
            varlist.add(f.lit1.atom);
            result = separateQuantors(f.child2, varlist);
        }
        else if (!f.isLiteral()) {
            BareFormula arg1 = f.child1;
            BareFormula arg2 = f.child2;
            if (f.hasSubform1() && f.child1 != null)
                arg1 = separateQuantors(f.child1, varlist);
            if (f.hasSubform2()&& f.child2 != null)
                arg2 = separateQuantors(f.child2, varlist);
            result = new BareFormula(f.op, arg1, arg2, f.lit1,f.lit2);
        }
        //System.out.println("INFO in SmallCNFization.separateQuantors(): returning: " + result.toStructuredString());
        return result;
    }
    
    /** ***************************************************************
     * Shift all (universal) quantor to the outermost level.
     */
    public static BareFormula formulaShiftQuantorsOut(BareFormula f) {

        //System.out.println("INFO in SmallCNFization.formulaShiftQuantorsOut(): f: " + f.toStructuredString());
        ArrayList<Term> varlist = null;
        BareFormula newf = separateQuantors(f,varlist);
        if (newf != null)
            f = newf;
        if (varlist == null)
            return f;
        while (varlist.size() > 0) {
            Term t = varlist.get(varlist.size() - 1);
            varlist.remove(t);
            f = new BareFormula("!", new Literal(t), f);
        }
        return f;
    }

    /** ***************************************************************
     * Convert a Skolemized formula in prefix-NNF form into Conjunctive
     * Normal Form.
     */
    public static BareFormula formulaDistributeDisjunctions(BareFormula f) {

        //System.out.println("SmallCNFization.formulaDistributeDisjunctions(): f: " + f.toStructuredString());
        BareFormula arg1 = f.child1;
        BareFormula arg2 = f.child2;
        if (f.isQuantified()) {
            arg1 = f.child1;
            arg2 = formulaDistributeDisjunctions(f.child2);
            f = new BareFormula(f.op, arg1, arg2,f.lit1,f.lit2);
        }
        else if (f.isLiteral()) { }
        else {
            if (f.hasSubform1()) {
                if (f.child1 != null) {
                    arg1 = formulaDistributeDisjunctions(f.child1);
                    if (arg1 == null)
                        arg1 = f.child1;
                }
            }
            if (f.hasSubform2()) {
                if (f.child2 != null) {
                    arg2 = formulaDistributeDisjunctions(f.child2);
                    if (arg2 == null)
                        arg2 = f.child2;
                }
            }
            f = new BareFormula(f.op, arg1, arg2, f.lit1, f.lit2);
        }
        if (f.op.equals("|")) {
            if (f.child1 != null && f.child1.op.equals("&")) {
                // (P&Q)|R -> (P|R) & (Q|R)
                arg1 = new BareFormula("|", f.child1.child1, f.child2, f.child1.lit1, f.lit2);
                arg2 = new BareFormula("|", f.child1.child2, f.child2, f.child1.lit2, f.lit2);
                f    = new BareFormula("&", arg1, arg2,null,null);
                BareFormula newf = formulaDistributeDisjunctions(f);
                if (newf != null)
                    f = newf;
            }
            else if (f.child2 != null && f.child2.op.equals("&")) {
                // (R|(P&Q) -> (R|P) & (R|Q)
                arg1 = new BareFormula("|", f.child1, f.child2.child1, f.lit1, f.child2.lit1);
                arg2 = new BareFormula("|", f.child1, f.child2.child2, f.lit1, f.child2.lit2);
                f    = new BareFormula("&", arg1, arg2,null,null);
                BareFormula newf = formulaDistributeDisjunctions(f);
                if (newf != null)
                    f = newf;
            }
        }
        //System.out.println("SmallCNFization.formulaDistributeDisjunctions(): returning: " + f.toStructuredString());
        return f;
    }

    /** ***************************************************************
     * Given a formula in CNF, convert it to a set of clauses.
     */
    public static ArrayList<Clause> formulaCNFSplit(Formula f) {

        //System.out.println("SmallCNFization.formulaCNFSplit(): f: " + f.form.toStructuredString());
        BareFormula matrix = f.form.getMatrix();
        ArrayList<Clause> res = new ArrayList<Clause>();
        ArrayList<BareFormula> conjuncts = matrix.conj2List();
        //System.out.println("SmallCNFization.formulaCNFSplit(): conjuncts: " + conjuncts);
        for (BareFormula c : conjuncts) {
            ArrayList<BareFormula> list = c.disj2List();
            ArrayList<Literal> litlist = new ArrayList<Literal>();
            for (BareFormula bf : list) {
                if (bf.child1 != null || bf.child2 != null)
                    System.out.println("Error in SmallCNFization.formulaCNFSplit(): Non-literal element: " + bf);
                litlist.add(bf.lit1);
                Clause clause = new Clause(litlist,f.type,"");
                res.add(clause);
            }
        }        
        return res;  
    }
        
    /** ***************************************************************
     * Convert a (wrapped) formula to Conjunctive Normal Form.
     */
    public static Formula wFormulaCNF(Formula wfinput) {

        //System.out.println("wFormulaCNF(): input: " + wfinput);
        Formula wf = wfinput.deepCopy();
        BareFormula f = formulaOpSimplify(wf.form);
        //System.out.println("wFormulaCNF(): after op simplify: " + f);
        boolean m0 = (f != null);
        if (f == null)
            f = wf.form;
        BareFormula newf = formulaSimplify(f);
        //System.out.println("wFormulaCNF(): after  simplify: " + newf);
        boolean m1 = (f != null);
        if (newf != null)
            f = newf;
        if (m0 || m1) {
            Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "fof_simplification";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        newf = formulaNNF(f,true);
        //System.out.println("wFormulaCNF(): after  nnf: " + newf);
        if (newf != null) {
            f = newf;
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "fof_nnf";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        newf = f.promoteChildren();
        if (newf != null)
            f = newf;
        newf = formulaMiniScope(f);
        //System.out.println("wFormulaCNF(): after  miniscope: " + newf);
        if (newf != null) {
            f = newf;
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "shift_quantors";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        // f = formulaVarRename(f);
        newf = Clausifier.standardizeVariables(f);
        //System.out.println("wFormulaCNF(): after  standardize vars: " + newf);
        if (!newf.equals(wf.form)) {
            f = newf;
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "variable_rename";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        newf = formulaSkolemize(f);
        //System.out.println("wFormulaCNF(): after  skolemize: " + newf);
        if (!newf.equals(wf.form)) {
            f = newf;
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "skolemize";
            tmp.support.add(wf.name);
            tmp.status = "status(esa)";
            wf = tmp;
        }
        newf = formulaShiftQuantorsOut(f);
        //System.out.println("wFormulaCNF(): after  shift quantors: " + newf);
        if (!newf.equals(wf.form)) {
            f = newf;
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "shift_quantors";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        newf = f.promoteChildren();
        if (newf != null)
            f = newf;
        newf = formulaDistributeDisjunctions(f);
        //System.out.println("wFormulaCNF(): after  distribute disjunctions: " + newf);
        if (!newf.equals(wf.form)) {
            f = newf;
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "distribute";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        //System.out.println("wFormulaCNF(): before promote children: " + wf.form);
        BareFormula promoted = wf.form.promoteChildren();
        if (promoted != null)
            wf.form = promoted;
        return wf;
    }
    
    /** ***************************************************************
     * Convert a formula into Clause Normal Form.
     */
    public static ArrayList<Clause> wFormulaClausify(Formula wf) {

        Formula newf = wFormulaCNF(wf);
        ArrayList<Clause> clauses = formulaCNFSplit(newf);
        for (Clause c : clauses) {
            c.rationale = "split_conjunct";
            c.support.add(wf.name);
        }
        return clauses;
    }

}
