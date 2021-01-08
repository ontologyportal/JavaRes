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

@Deprecated
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
    public static Term newSkolemTerm(ArrayList<Term> varlist) {

        Term result = new Term();
        result.t = newSkolemSymbol();
        for (int i = 0; i < varlist.size(); i++) {
            Term v = varlist.get(i);
            result.subterms.add(v);
        }
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

    	//System.out.println("INFO in SmallCNFization.formulaOpSimplify(): " + input);
        BareFormula f = input.deepCopy();
        if (f.isLiteral()) {
            return null;
        }
        boolean modified = false;
        
        // First simplify the subformulas
        if (f.child1 != null) {
            BareFormula child1 = formulaOpSimplify(f.child1);
            modified = (child1 != null);
            if (child1 != null)
            	f.child1 = child1;
        }
        if (f.child2 != null) {
            BareFormula child2 = formulaOpSimplify(f.child2);
            modified = (child2 != null);
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
        return f;
    }
    
    /** ***************************************************************
     * Try to apply the following simplification rules to f at the top
     * level. Return (f',m), where f' is the result of simplification,
     * and m indicates if f'!=f.
     */
    public static BareFormula formulaTopSimplify(BareFormula f) {

    	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): " + f);
    	System.out.println(" with op " + f.op);
    	if (Term.emptyString(f.op)) {
    		if (f.child1 != null)
    			return formulaTopSimplify(f.child1);
    		return null;
    	}
    	else if (f.op.equals("~")) {
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): not");
            if (f.lit1 != null)
                // Push ~ into literals if possible. This covers the case
                // of ~~P -> P if one of the negations is in the literal.
                return new BareFormula("", f.lit1.negate());
        }
        else if (f.op.equals("|")) {
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): or");
      		System.out.println(f.lit1);
      		System.out.println(f.lit2);
      		System.out.println(f.child1);
      		System.out.println(f.child2);
            if (f.lit1 != null && f.lit1.atomIsConstTrue())
                // T | P -> T. Note that child1 is $true or
                // equivalent. This applies to several other cases where we
                // can reuse a $true or $false child instead of creating a
                // new formula.
                return new BareFormula ("",f.child2,null,f.lit2,null);
            else if (f.lit2 != null && f.lit2.atomIsConstTrue()) {
                // P | T -> T
            	//System.out.println("INFO in SmallCNFization.formulaTopSimplify(): lit2 is $true");
                return new BareFormula ("",f.child1,null,f.lit1,null);
            }
            else if (f.lit1 != null && f.lit1.atomIsConstFalse())
                // F | P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.lit2 != null && f.lit2.atomIsConstFalse())
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
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): and");
            if (f.is1PropConst(true))
                // T & P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.is2PropConst(true))
                // P & T -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.is1PropConst(false))
                // F & P -> F
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.is2PropConst(false))
                // P & F -> F
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.childrenEqual())
                // P & P -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
        }
        else if (f.op.equals("<=>")) {
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): equiv");
            if (f.is1PropConst(true))
                // T <=> P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.is2PropConst(true))
                // P <=> T -> P
                return new BareFormula("", f.child1, null, f.lit1, null); 
            else if (f.is1PropConst(false)) {
                // P <=> F -> ~P
                BareFormula newform = new BareFormula("~", f.child2, null, f.lit2, null);        
                newform = formulaSimplify(newform);
                return newform;
            }
            else if (f.is2PropConst(false)) {
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
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): implication");
            if (f.is1PropConst(true))
                // T => P -> P
                return new BareFormula("", f.child2, null, f.lit2, null); 
            else if (f.is1PropConst(false))
                // F => P -> T
                return new BareFormula("", Literal.string2lit("$true"));
            else if (f.is2PropConst(true))
                // P => T -> T
                return new BareFormula("",Literal.string2lit("$true"));
            else if (f.is2PropConst(false)) {
                // P => F -> ~P
                BareFormula newform = new BareFormula("~", f.child1, null, f.lit1, null);            
                newform = formulaSimplify(newform);
                return newform;
            }
            else if (f.childrenEqual())
                // P => P -> T
                return new BareFormula("", Literal.string2lit("$true"));
        }
        else if (f.isQuantified()) {
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): quantified");
            // ![X] F -> F if X is not free in F
            // ?[X] F -> F if X is not free in F
            ArrayList<Term> vars = f.child1.collectFreeVars();   // but test for null and test lit1
        	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): vars"  + vars);
            if (!vars.contains(f.lit1.atom)) {
            	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): no free vars: " + f);
            	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): lit2: " + f.lit2);
            	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): child2: " + f.child2);
                BareFormula bfnew = new BareFormula("",f.child2,null,f.lit2,null);
            	System.out.println("INFO in SmallCNFization.formulaTopSimplify(): bfnew: " + bfnew);
                return bfnew;
            }
        }
        else
        	System.out.println("Error in SmallCNFization.formulaTopSimplify(): unexpected operator");
        return null; // false
    }                

    /** ***************************************************************
     *  Exhaustively apply simplification to f. See formulaTopSimplify()
        above for the 

        Returns (f', True) if f!=f, (f', False) otherwise. 
     */
    public static BareFormula formulaSimplify(BareFormula input) {

    	BareFormula f = input.deepCopy();
    	//System.out.println("INFO in SmallCNFization.formulaSimplify(): " + f);
        if (f.isLiteral())
            return null;

        boolean modified = false;        
        // First simplify the subformulas
        if (f.child1 != null) {
            BareFormula child1 = formulaOpSimplify(f.child1);
            modified = (child1 != null);
            if (child1 != null)
            	f.child1 = child1;
        }
        if (f.child2 != null) {
            BareFormula child2 = formulaOpSimplify(f.child2);
            modified = (child2 != null);
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
            BareFormula newf = formulaTopSimplify(f);
            if (newf != null) {
            	simpf = newf.promoteChildren();
            	if (simpf != null) {
            		newf = simpf;
            		modified = true;
            	}
            }
            if (newf != null) {
                modified = true;
                f = newf;
            }
            else
            	topmod = false;
        	//System.out.println("INFO in SmallCNFization.formulaSimplify(): in loop: " + f);
        }
        if (!modified)
            return null;
        return f;
    }

    /** ***************************************************************
     * Apply all NNF transformation rules that can be applied at top
     * level. Return result and null if not modified.
     */
    public static BareFormula rootFormulaNNF(BareFormula f, boolean polarity) {

       	System.out.println("INFO in SmallCNFization.rootFormulaNNF(): " + f);
       	System.out.println("INFO in SmallCNFization.rootFormulaNNF(): op: " + f.op);
        boolean normalform = false;
        boolean modified = false;

        while (!normalform) {
            normalform = true;
            boolean m = false;

            if (f.op.equals("~")) {
                if (f.isLiteral()) {
                    // Move negations into literals
                    f = new BareFormula("", f.lit1.negate());
                    m = true;
                }
                else if (f.child1.op.equals("|")) {
                    // De Morgan: ~(P|Q) -> ~P & ~Q
                    f = new BareFormula("&",
                            new BareFormula("~", f.child1.child1),
                            new BareFormula("~", f.child1.child2));
                    m = true;
                }
                else if  (f.child1.op.equals("&")) {
                    // De Morgan: ~(P&Q) -> ~P | ~Q
                    f = new BareFormula("|",
                            new BareFormula("~", f.child1.child1),
                            new BareFormula("~", f.child1.child2));
                    m = true;
                }
                else if (f.child1.op.equals("!")) {
                    // ~(![X]:P) -> ?[X]:~P
                    f = new BareFormula("?",
                                f.child1.child1,
                                new BareFormula("~", f.child1.child2));
                    m = true;
                }
                else if (f.child1.op.equals("?")) {
                    // ~(?[X]:P) -> ![X]:~P
                    f = new BareFormula("!",
                                f.child1.child1,
                                new BareFormula("~", f.child1.child2));
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
            if (m)
            	modified = true;
        }
       	System.out.println("INFO in SmallCNFization.rootFormulaNNF(): returning: " + f);
        if (!modified)
            return null;
        return f;
    }

    /** ***************************************************************
     * Convert f into a NNF. Equivalences (<=>) are eliminated
     * polarity-dependend, top to bottom. Returns null if formula 
     * is not modified
     */
    public static BareFormula formulaNNF(BareFormula f, boolean polarity) {

    	System.out.println();
       	System.out.println("INFO in SmallCNFization.formulaNNF(): " + f);

        boolean normalform = false;
        boolean modified   = false;
        boolean m = false;
        BareFormula newf = f.promoteChildren();
        if (newf == null)
        	newf = f;
       	System.out.println("INFO in SmallCNFization.formulaNNF(): after promotion: " + newf);
        while (!normalform) {
            normalform = true;
            newf = rootFormulaNNF(newf, polarity);
            if (newf != null)
                modified = true;
            else
            	newf = f;
           	System.out.println("INFO in SmallCNFization.formulaNNF(): op: " + newf.op);
            if (Term.emptyString(newf.op)) {
            	newf = newf.promoteChildren();
            }
            else if (newf.op.equals("~")) {
                BareFormula handle = formulaNNF(newf.child1, !polarity);
                if (handle != null) {
                    m = true;
                    normalform = false;
                    newf = new BareFormula("~", handle);
                }
            }
            else if (newf.op.equals("!") || newf.op.equals("?")) {
                BareFormula handle = formulaNNF(newf.child2, polarity);
                if (handle != null) {
                    m = true;
                    normalform = false;
                    newf = new BareFormula(newf.op, newf.child1, handle);
                }
            }
            else if (newf.isQuantified()) {
                BareFormula handle1 = formulaNNF(newf.child1, polarity);
                BareFormula handle2 = formulaNNF(newf.child2, polarity);
                m = (handle1 != null) || (handle2 != null);
                if (m) {
                    normalform = false;
                    newf = new BareFormula(newf.op, handle1, handle2);
                }
            }
            if (m = true)
            	modified = true;
        }
        if (!modified)
            return null;
        return newf;
    }
    
    /** ***************************************************************
     * Perform miniscoping, i.e. move quantors in as far as possible, so
     * that their scope is only the smallest subformula in which the
     * variable occurs. 
     */
    public static BareFormula formulaMiniScope(BareFormula f) {

        System.out.println("INFO in SmallCNFization.formulaMiniScope(): f: " + f);
        System.out.println("INFO in SmallCNFization.formulaMiniScope(): child1: " + f.child1);
        System.out.println("INFO in SmallCNFization.formulaMiniScope(): lit1: " + f.lit1);
        System.out.println("INFO in SmallCNFization.formulaMiniScope(): child2: " + f.child2);
        System.out.println("INFO in SmallCNFization.formulaMiniScope(): lit2: " + f.lit2);
        boolean res = false;
        if (f.isQuantified()) {
            String op    = f.child2.op;
            String quant = f.op;
            BareFormula var   = f.child1;
            BareFormula subf  = f.child2;
            if (op.equals("&") || op.equals("|")) {
                if (!subf.child1.collectFreeVars().contains(var)) {
                    // q[X]:(P op Q)  -> P op (q[X]:Q) if X not free in P
                    BareFormula arg2 = new BareFormula(quant, var, subf.child2);
                    BareFormula arg1 = subf.child1;
                    f = new BareFormula(op, arg1, arg2);
                    res = true;
                }
                else if (!subf.child2.collectFreeVars().contains(var)) {
                    // q[X]:(P op Q)  -> (q[X]:P) op Q if X not free in Q
                    BareFormula arg1 = new BareFormula(quant, var, subf.child1);
                    BareFormula arg2 = subf.child2;
                    f = new BareFormula(op, arg1, arg2);
                    res = true;
                }
                else {
                    if (op.equals("&") && quant.equals("!")) {
                        // ![X]:(P&Q) -> ![X]:P & ![X]:Q
                        BareFormula arg1 = new BareFormula("!", var, subf.child1);
                        BareFormula arg2 = new BareFormula("!", var, subf.child2);
                        f = new BareFormula("&" , arg1, arg2);
                        res = true;
                    }
                    else if (op.equals("|") && quant.equals("?")) {
                        // ?[X]:(P|Q) -> ?[X]:P | ?[X]:Q
                        BareFormula arg1 = new BareFormula("?", var, subf.child1);
                        BareFormula arg2 = new BareFormula("?", var, subf.child2);
                        f = new BareFormula("|", arg1, arg2);
                        res = true;
                    }
                }
            }
        }
        BareFormula arg1 = f.child1;
        BareFormula arg2 = f.child2;
        boolean modified = false;
        if (f.hasSubform1()) {
            arg1 = formulaMiniScope(f.child1);
            if (arg1 == null)
                modified = false;
            else
                modified = true;
        }
        if (f.hasSubform2()) {
            arg2 = formulaMiniScope(f.child2);
            if (arg2 == null)
                modified = false;
            else
                modified = true;
        }
        if (modified) {
            f = new BareFormula(f.op, arg1, arg2);
            f = formulaMiniScope(f);
            res = true;
        }
        if (res = false)
            return null;
        return f;
    }
    
    /** ***************************************************************
     * Rename variables in f so that all bound variables are unique
     
    public BareFormula formulaVarRename(BareFormula f, Substitutions subst) {

        if (subst == null)
            subst = new Substitutions();
        Term newvar = null;
        if (f.isQuantified()) {
            // New scope of a variable -> add a new binding to a new
            // variable. Store potential old binding to restore when
            // leaving the scope later
            Term var = f.lit1.lhs;
            newvar = Substitutions.freshVar();
            Term oldbinding = subst.modifyBinding(var,newvar);
        }
        if (f.isLiteral()) {
            // Create copy with the new variables recorded in subst
            BareFormula child = f.child1.substitute(subst);
            f = new BareFormula("", child);
        }
        else {
            // This is a composite formula. Rename it...
            Term arg1 = null;
            BareFormula arg2 = null;
            if (f.isQuantified()) {
                // Apply new renaming locally to the bound variable and
                // recursively to the subformula
                arg1 = newvar;
                arg2 = formulaVarRename(f.child2, subst);
            }
            else {
                // Apply renaming to all subformulas
                if (f.hasSubform1())
                    arg1 = formulaVarRename(f.child1, subst);
                if (f.hasSubform2())
                    arg2 = formulaVarRename(f.child2, subst);
            }
            f = new BareFormula(f.op, arg1, arg2);
        }
        if (f.isQuantified()) {
            // We are leaving the scope of the quantifier, so restore
            // substitution.
            subst.modifyBinding(var, oldbinding);
        }
        return f;
    }
*/
    /** ***************************************************************
     * Perform Skolemization of f, which is assumed to be in the scope of
     * the list of variables provided.
     */
    public static BareFormula formulaRekSkolemize(BareFormula f, ArrayList<Term> variables, Substitutions subst) {

        if (f.isLiteral()) {
            BareFormula child = f.child1.substitute(subst);
            f = new BareFormula("", child);
        }
        else if (f.op.equals("?")) {
            Term var = f.lit1.atom;
            Term skTerm = newSkolemTerm(variables);
            Term oldbinding = subst.modifyBinding(var,skTerm);
            f = formulaRekSkolemize(f.child2, variables, subst);
            subst.modifyBinding(var, oldbinding);
        }
        else if (f.op.equals("!")) {
            Term var = f.lit1.atom;
            variables.add(var);
            BareFormula handle = formulaRekSkolemize(f.child2, variables, subst);
            f = new BareFormula("!", new Literal(var), handle);
            variables.remove(variables.get(variables.size()-1));
        }
        else {
            BareFormula arg1 = null;
            BareFormula arg2 = null;
            if (f.hasSubform1())
                arg1 = formulaRekSkolemize(f.child1, variables, subst);
            if (f.hasSubform2())
                arg2 = formulaRekSkolemize(f.child2, variables, subst);
            f = new BareFormula(f.op, arg1, arg2);
        }
        return f;
    }
        
    /** ***************************************************************
     * Perform an outermost Skolemization of f, removing all existential
     * quantifiers. Formulas are considered to be universally closed,
     * i.e. free variables (which should not occur) are treated as
     * universally quantified.
     */
    public static BareFormula formulaSkolemize(BareFormula f) {

        ArrayList<Term> vars = f.collectFreeVars();
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

        BareFormula result = f.deepCopy();
        if (varlist == null)
            varlist = new ArrayList<Term>();

        if (f.isQuantified()) {
            assert f.op.equals("!");
            varlist.add(f.lit1.atom);
            result = separateQuantors(f.child2, varlist);
        }
        else if (!f.isLiteral()) {
            BareFormula arg1 = null;
            BareFormula arg2 = null;
            if (f.hasSubform1())
                arg1 = separateQuantors(f.child1, varlist);
            if (f.hasSubform2())
                arg2 = separateQuantors(f.child2, varlist);
            result = new BareFormula(f.op, arg1, arg2);
        }
        return result;
    }
    
    /** ***************************************************************
     * Shift all (universal) quantor to the outermost level.
     */
    public static BareFormula formulaShiftQuantorsOut(BareFormula f) {

        ArrayList<Term> varlist = null;
        f = separateQuantors(f,varlist);

        while (varlist.size() > 0) {
            Term t  = varlist.get(varlist.size() - 1);
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

        BareFormula arg1 = null;
        BareFormula arg2 = null;
        if (f.isQuantified()) {
            arg1 = f.child1;
            arg2 = formulaDistributeDisjunctions(f.child2);
            f = new BareFormula(f.op, arg1, arg2);
        }
        else if (f.isLiteral()) { }
        else {
            if (f.hasSubform1())
                arg1 = formulaDistributeDisjunctions(f.child1);
            if (f.hasSubform2())
                arg2 = formulaDistributeDisjunctions(f.child2);
            f = new BareFormula(f.op, arg1, arg2);
        }
        if (f.op.equals("|")) {
            if (f.child1.op.equals("&")) {
                // (P&Q)|R -> (P|R) & (Q|R)
                arg1 = new BareFormula("|", f.child1.child1, f.child2);
                arg2 = new BareFormula("|", f.child1.child2, f.child2);
                f    = new BareFormula("&", arg1, arg2);
                f    = formulaDistributeDisjunctions(f);
            }
            else if (f.child2.op.equals("&")) {
                // (R|(P&Q) -> (R|P) & (R|Q)
                arg1 = new BareFormula("|", f.child1, f.child2.child1);
                arg2 = new BareFormula("|", f.child1, f.child2.child2);
                f    = new BareFormula("&", arg1, arg2);
                f    = formulaDistributeDisjunctions(f);
            }
        }
        return f;
    }

    /** ***************************************************************
     * Given a formula in CNF, convert it to a set of clauses.
     */
    public static ArrayList<Clause> formulaCNFSplit(BareFormula f) {

        System.out.println("SmallCNFization.formulaCNFSplit(): f: " + f);
        BareFormula matrix = f.getMatrix();
        ArrayList<Clause> res = new ArrayList<Clause>();
        ArrayList<BareFormula> conjuncts = matrix.conj2List();
        for (BareFormula c : conjuncts) {
            ArrayList<BareFormula> list = c.disj2List();
            ArrayList<Literal> litlist = new ArrayList<Literal>();
            for (int i = 0; i < list.size(); i++) {
                BareFormula bf = list.get(i);
                if (bf.child1 != null || bf.lit2 != null || bf.child2 != null)
                    System.out.println("Error in SmallCNFization.formulaCNFSplit(): Non-literal element: " + bf);
                litlist.add(bf.lit1);
                Clause clause = new Clause(litlist);
                res.add(clause);
            }
        }        
        return res;  
    }
        
    /** ***************************************************************
     * Convert a (wrapped) formula to Conjunctive Normal Form.
     */
    public static Formula wFormulaCNF(Formula wf) {
 
        BareFormula f = formulaOpSimplify(wf.form);
        boolean m0 = (f != null);
        f = formulaSimplify(f);
        boolean m1 = (f != null);
        if (m0 || m1) {
            Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "fof_simplification";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        f = formulaNNF(f,true);
        if (f != null) {
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "fof_nnf";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        f = formulaMiniScope(f);
        if (f != null) {
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "shift_quantors";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        // f = formulaVarRename(f);
        f = Clausifier.standardizeVariables(f);
        if (!f.equals(wf.form)) {
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "variable_rename";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        f = formulaSkolemize(f);
        if (!f.equals(wf.form)) {
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "skolemize";
            tmp.support.add(wf.name);
            tmp.status = "status(esa)";
            wf = tmp;
        }
        f = formulaShiftQuantorsOut(f);
        if (!f.equals(wf.form)) {
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "shift_quantors";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        f = formulaDistributeDisjunctions(f);
        if (!f.equals(wf.form)) {
        	Formula tmp = new Formula(f, wf.type);
            tmp.rationale = "distribute";
            tmp.support.add(wf.name);
            wf = tmp;
        }
        return wf;
    }
    
    /** ***************************************************************
     * Convert a formula into Clause Normal Form.
     */
    public static ArrayList<Clause> wFormulaClausify(Formula wf) {

        BareFormula newwf = wf.form.deepCopy();
        ArrayList<Clause> clauses = formulaCNFSplit(newwf);
        for (Clause c : clauses) {
            c.rationale = "split_conjunct";
            c.support.add(wf.name);
        }
        return clauses;
    }

}
