package atp;

/*
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

import java.io.*;
import java.text.ParseException;
import java.util.*;

/** ***************************************************************
    A simple implementation of first-order formulas and their associated
    meta-information. 

    See literals.py for the definition of atoms.

    A formula is either a first-order-atom, or build from pre-existing
    formulas using the various logical connectives and quantifiers.

    Assume F,G are arbitrary formulas and X is an arbitrary variable. Then

    (~F)
    (F&G)
    (F|G)
    (F->G)
    (F<=>G)
    (F<~>G)
    (F<-G)
    (F~&G)
    (F~|G)
    (![X]:F)
    (?[X]:F)

    are formulas.

    The set of all formulas for a given signature is denoted as
    Formulas(P,F,V).

    In the external representation, some parentheses can be omitted. Lists
    of either conjunctively or disjunctively connected subformula are
    assumed to associate left. (F & G & H) is equivalent to ((F&G)&H)

    Formulas are represented on two levels: The actual logical formula is
    a recursive data structure. This is wrapped in a container that
    associates the formula with its meta-information. The implementation
    uses literals as the base case, not atoms. That allows us to reuse
    some code for parsing and printing infix equality, but also to
    represent a formula in Negation Normal Form without any negations in
    the frame of the formula.
    
        This is a class representing a naked first-order formula
        formula. Operators are represented as strings, an empty operator
        indicates an atomic formula. child1 and child2 are the subformulas
        (child2 may be empty). In the case of atomic formula, child1 is an
        atom (representing a term). In the case of quantified formulae,
        child1 is a plain string (i.e. the term representing the variable)
        and child2 is the formula quantified over.
 */
public class BareFormula {

    public String op = "";
    public BareFormula child1 = null;
    public Literal lit1 = null;        // either child1 or lit1 must be null
    public BareFormula child2 = null;
    public Literal lit2 = null;        // either child2 or lit2 (or both) must be null
        
    public static int level = 0;

    /** ***************************************************************
     */
    public BareFormula() {     
    }
    
    /** ***************************************************************
     */
    public BareFormula(String s, BareFormula c1) {
     
        op = s;
        if (c1 != null)
            child1 = c1.deepCopy();
        else
            Thread.dumpStack();
    }
    
    /** ***************************************************************
     */
    public BareFormula(String s, Literal l1) {
     
        op = s;
        if (l1 != null)
            lit1 = l1.deepCopy();
        else
            Thread.dumpStack();
    }
    
    /** ***************************************************************
     */
    public BareFormula(String s, Literal l1, BareFormula c2) {
     
        op = s;
        if (l1 != null)
        	lit1 = l1.deepCopy();
        if (c2 != null)
        	child2 = c2.deepCopy();
    }
            
    /** ***************************************************************
     */
    public BareFormula(String s, BareFormula c1, BareFormula c2) {
     
        op = s;
        if (c1 != null)
        	child1 = c1.deepCopy();
        if (c2 != null)
        	child2 = c2.deepCopy();
    }
    
    /** ***************************************************************
     */
    public BareFormula(String s, BareFormula c1, BareFormula c2, 
    		Literal l1, Literal l2) {

        if (l1 != null && c1 != null) {
            System.out.println("Error in BareFormula(): lit1 & child1 are both non-null");
            System.out.println("Error in BareFormula(): lit1: " + l1);
            System.out.println("Error in BareFormula(): child1: " + c1);
            Thread.dumpStack();
        }
        if (l2 != null && c2 != null) {
            System.out.println("Error in BareFormula(): lit2 & child2 are both non-null");
            System.out.println("Error in BareFormula(): lit2: " + l2);
            System.out.println("Error in BareFormula(): child2: " + c2);
            Thread.dumpStack();
        }
        op = s;
        if (c1 != null)
        	child1 = c1.deepCopy();
        if (c2 != null)
        	child2 = c2.deepCopy();
        if (l1 != null)
        	lit1 = l1.deepCopy();
        if (l2 != null)
        	lit2 = l2.deepCopy();
    }
    
    /** ***************************************************************
     * a logical operator other than a quantifier or negation
     */
    private static boolean logOp(String s) {
        
        return s.equals("&") || s.equals("|") || s.equals("->") || 
            s.equals("<-") || s.equals("<=>") || s.equals( "<~>") || 
            s.equals("=>") || s.equals("<=") || s.equals("~|") || s.equals("~&");
    }
    
    /** ***************************************************************
     */
    public boolean isQuantified() {
        
        return op.equals("?") || op.equals("!");
    }    

    /** ***************************************************************
     */
    public static boolean isQuantifier(String s) {
        
        return s.equals("?") || s.equals("!");
    }    
    
    /** ***************************************************************
     */
    public boolean isLiteral() {
        
        return Term.emptyString(op) && lit1 != null;
    }

    /** ***************************************************************
     * allow equations as literals (since they are allowed)
     */
    public boolean isEqLiteral() {

        if (Term.emptyString(op) && lit1 != null)
            return true;
        if (op.equals("~") && lit1 != null)
            return true;
        if (op.equals("=") && lit1 != null && lit2 != null)
            return true;
        return false;
    }

    /** ***************************************************************
     */
    private boolean isConstFalse(Literal lit, BareFormula child) {

        //System.out.println("BareFormula.isConstFalse(): " + lit + "  " + child);
        if (lit != null && lit.atomIsConstFalse())
            return true;
        //System.out.println("BareFormula.isConstFalse(): lit null or not true");
        if (child != null) {
            //System.out.println("BareFormula.isConstFalse(): childop :" + child.op);
            if (Term.emptyString(child.op)) {
                //System.out.println("BareFormula.isConstFalse(): child op null");
                if (child.lit1.atomIsConstFalse()) {
                    //System.out.println("BareFormula.isConstFalse(): child:" + child.lit1);
                    //System.out.println("BareFormula.isConstFalse(): " + child.lit1.atomIsConstFalse());
                    return true;
                }
            }
        }
        return false;
    }

    /** ***************************************************************
     */
    private boolean isConstTrue(Literal lit, BareFormula child) {

        //System.out.println("BareFormula.isConstTrue(): " + lit + "  " + child);
        if (lit != null && lit.atomIsConstTrue())
            return true;
        //System.out.println("BareFormula.isConstTrue(): lit null or not true");
        if (child != null) {
            //System.out.println("BareFormula.isConstTrue(): childop :" + child.op);
            if (Term.emptyString(child.op)) {
                //System.out.println("BareFormula.isConstTrue(): child op null");
                if (child.lit1.atomIsConstTrue()) {
                    //System.out.println("BareFormula.isConstTrue(): child:" + child.lit1);
                    //System.out.println("BareFormula.isConstTrue(): " + child.lit1.atomIsConstTrue());
                    return true;
                }
            }
        }
        return false;
    }

    /** ***************************************************************
     */
    public boolean lhsIsConstTrue() {

        return isConstTrue(lit1,child1);
    }

    /** ***************************************************************
     */
    public boolean rhsIsConstTrue() {

        return isConstTrue(lit2,child2);
    }

    /** ***************************************************************
     */
    public boolean lhsIsConstFalse() {

        return isConstFalse(lit1,child1);
    }

    /** ***************************************************************
     */
    public boolean rhsIsConstFalse() {

        return isConstFalse(lit2,child2);
    }


    /** ***************************************************************
     * a logical operator other than a quantifier,  negation, 'and' or
     * 'or'
     * @return null if not one of these operators and the operator 
     * otherwise
     */
    private static String isBinaryConnective(String s) {

        if (s.equals(Lexer.Nand) || s.equals(Lexer.Nor) || s.equals(Lexer.BImplies) || 
            s.equals(Lexer.Implies) || s.equals(Lexer.Equiv) || s.equals(Lexer.Xor))
            return s;
        else
            return null;
    }
    
    /** ***************************************************************
     */
    public boolean isBinary() {
        
        if (op.equals(Lexer.Nand) || op.equals(Lexer.Nor) || op.equals(Lexer.BImplies) || 
            op.equals(Lexer.Implies) || op.equals(Lexer.Equiv) || op.equals(Lexer.Xor) ||
        	op.equals(Lexer.And) || op.equals(Lexer.Or) )
            return true;
        else
            return false;
    }
    

    /** ***************************************************************
     */
    public boolean isNoOp() {
        
        return Term.emptyString(op);
    }  

    /** ***************************************************************
     */
    public boolean isUnary() {
        
        return op.equals("~");
    }  
    
    /** ***************************************************************
     * Return True if self is a propositional constant of the given
     * polarity.
     */
    public boolean isPropConst(boolean polarity) {

        if (isLiteral())
            if (polarity && lit1 != null)
                return lit1.isPropTrue();
            else
                return lit1.isPropFalse();
        else
            return false;
    }
    
    /** ***************************************************************
     * Return True if the first child is a propositional constant of the given
     * polarity.
     */
    public boolean is1PropConst(boolean polarity) {

        if (lit1 != null)
            if (polarity)
                return lit1.isPropTrue();
            else
                return lit1.isPropFalse();
        else
            return false;
    }
    
    /** ***************************************************************
     * Return True if the second child is a propositional constant of the given
     * polarity.
     */
    public boolean is2PropConst(boolean polarity) {

        if (lit2 != null)
            if (polarity)
                return lit2.isPropTrue();
            else
                return lit2.isPropFalse();
        else
            return false;
    }
    
    /** ***************************************************************
     * Return True iff the formula is a disjunction of literals.
     */
    public boolean isLiteralDisjunction() {

        //System.out.println("BareFormula.isLiteralDisjunction(): op: " + op);
        if (isLiteral())
            return true;
        if (op.equals("|")) {
            boolean isLitDis1 = false;
            if (child1 != null)
                isLitDis1 = child1.isLiteralDisjunction();
            else
                isLitDis1 = true;
            boolean isLitDis2 = false;
            if (child2 != null)
                isLitDis2 = child2.isLiteralDisjunction();
            else
                isLitDis2 = true;
            return isLitDis1 && isLitDis2;
        }
        return false;
    }
    
    /** ***************************************************************
     * Return True if the formula is a conjunction of disjunction of
     * literals.
     */
    public boolean isClauseConjunction() {

        //System.out.println("BareFormula.isClauseConjuction(): op: " + op);
        if (isLiteral())
            return true;
        if (op.equals("|"))
            return isLiteralDisjunction();
        if (op.equals("&"))
            return child1.isClauseConjunction() &&
                   child2.isClauseConjunction();
        return false;
    }
     
    /** ***************************************************************
     * Return True if the formula is in conjunctive normal form.
     */
    public boolean isCNF() {

        //System.out.println("BareFormula.isCNF(): this: " + toStructuredString());
        //System.out.println("BareFormula.isCNF(): op: " + op);
        if (op.equals("!"))
            return child2.isCNF();
        return isClauseConjunction();   
    }

    /** ***************************************************************
     * Return a list of all non-logical symbols in the formula
     */
    public ArrayList<String> getConstantStrings() {
    	
    	ArrayList<String> result = new ArrayList<String>();
    	HashSet<String> resultSet = new HashSet<String>();
    	if (child1 != null)
    		resultSet.addAll(child1.getConstantStrings());
    	if (child2 != null)
    		resultSet.addAll(child2.getConstantStrings());
    	if (lit1 != null)
    		resultSet.addAll(lit1.getConstantStrings());
    	if (lit2 != null)
    		resultSet.addAll(lit2.getConstantStrings());
    	result.addAll(resultSet);
    	return result;
    }
    
    /** ***************************************************************
     * Return a list of the subformulas connected by top-level "&".
     */
    public ArrayList<BareFormula> conj2List() {

        ArrayList<BareFormula> result = new ArrayList<BareFormula>();
        if (op.equals("&")) {
            if (child1 != null)
                result.addAll(child1.conj2List());
            else
                result.add(new BareFormula("", lit1));
            if (child2 != null)
                result.addAll(child2.conj2List());
            else
                result.add(new BareFormula("", lit2));
            return result;
        }
        result.add(this);
        return result;
    }
    
    /** ***************************************************************
     * Return a list of the subformulas connected by top-level "|".
     */
    public ArrayList<BareFormula> disj2List() {

        ArrayList<BareFormula> result = new ArrayList<BareFormula>();
        if (op.equals("|")) {
            if (child1 != null)
                result.addAll(child1.disj2List());
            else
                result.add(new BareFormula("", lit1));
            if (child2 != null)
                result.addAll(child2.disj2List());
            else
                result.add(new BareFormula("", lit2));
            return result;
        }
        result.add(this);
        return result;
    }

    /** ***************************************************************
     * Return True if self has a proper subformula as the first
     * argument. This is false for quantified formulas and literals.
     */
    public boolean hasSubform1() {
        
        return isUnary() || isBinary();
    }  
    
    /** ***************************************************************
     * Return True if self has a proper subformula as the second
     * argument. This is the case for quantified formulas and binary
     * formulas.
     */
    public boolean hasSubform2() {
        
        return isQuantified() || isBinary();
    }

    /** ***************************************************************
     * If this formula is just a literal, return it as a Literal
     */
    public Literal toLiteral() {

        //System.out.println("BareFormula.toLiteral(): " + this.toStructuredString());
        if (!isEqLiteral())
            return null;
        String thisString = this.toString();
        if (thisString.charAt(0) == '(' && thisString.charAt(thisString.length()-1) == ')')
            thisString = thisString.substring(1,thisString.length()-1);
        if (thisString.startsWith("~~"))
            thisString = thisString.substring(2);
        Lexer lex = new Lexer(thisString);
        return Literal.parseLiteral(lex);
    }

    /** ***************************************************************
     * If a BareFormula has no operator, but does have a child rather
     * than a literal, promote its first child. Recursively call on 
     * each child first. Return null if not modified.               
     */
    public BareFormula promoteChildren() {

        //System.out.println("++++++++");
    	//System.out.println("INFO in BareFormula.promoteChildren(): input (structured print): " + this.toStructuredString());
    	//System.out.println("INFO in BareFormula.promoteChildren(): op: " + op);
    	if (op.equals("~") && lit1 != null) {
            BareFormula result = new BareFormula("", lit1.negate());
            //System.out.println("INFO in BareFormula.promoteChildren(): returning : " + result.toStructuredString());
            return result;
        }
    	//if (child1 != null)
        //     System.out.println("INFO in BareFormula.promoteChildren(): child1.isLiteral(): " + child1.isEqLiteral());
    	BareFormula newf = deepCopy();
    	BareFormula tempf = null;
    	boolean modified = false;
    	if (newf.child1 != null) {
    		tempf = newf.child1.promoteChildren();
            //System.out.println("INFO in BareFormula.promoteChildren(): after promote children tempf: " + tempf);
    		if (tempf != null) {
    			modified = true;
    			newf.child1 = tempf;
    	    	//System.out.println("INFO in BareFormula.promoteChildren(): child1 modified");
    		}
    		else
    		    tempf = newf.child1;
    		if (Term.emptyString(op)) {
                //System.out.println("INFO in BareFormula.promoteChildren(): empty op result (structured print): " + newf.toStructuredString());
                return tempf;
            }
            else if (op.equals("~") && child1.isLiteral()) {
                newf = this.deepCopy();
                newf.lit1 = newf.child1.toLiteral();
                newf.lit1 = newf.lit1.negate();
                newf.child1 = null;
                newf.op = "";
                modified = true;
                //System.out.println("INFO in BareFormula.promoteChildren(): returning negated literal (structured print): " + newf.toStructuredString());
                return newf;
            }
    	}
        //if (newf.child2 != null)
        //    System.out.println("INFO in BareFormula.promoteChildren(): child2.isLiteral(): " + newf.child2.isEqLiteral());
    	if (newf.child2 != null) {
    		tempf = newf.child2.promoteChildren();
    		if (tempf != null) {
    			modified = true;
    			newf.child2 = tempf;
    	    	//System.out.println("INFO in BareFormula.promoteChildren(): child2 modified");
    		}
    	}
    	if (newf.child1 != null && newf.child1.isEqLiteral()) {
            newf.lit1 = newf.child1.toLiteral();
            if (newf.op.equals("~")) {
                newf.op = "";
                newf.lit1.negate();
            }
            newf.child1 = null;
            modified = true;
            //System.out.println("INFO in BareFormula.promoteChildren(): newf modified: " + newf.toStructuredString());
        }
        if (newf.child2 != null && newf.child2.isEqLiteral()) {
            newf.lit2 = newf.child2.toLiteral();
            newf.child2 = null;
            modified = true;
        }
        /*
		if (lit1 == null && child1 != null && newf.isNoOp()) {
			BareFormula newnewf = new BareFormula();
			newnewf.op = newf.child1.op;
			newnewf.lit1 = newf.child1.lit1;			
			newnewf.child1 = newf.child1.child1;
			newnewf.lit2 = newf.child1.lit2;
			newnewf.child2 = newf.child1.child2;
			newf = newnewf;
			modified = true;
	    	System.out.println("INFO in BareFormula.promoteChildren(): promoting children");
		}
         */
		if (modified) {
	    	//System.out.println("INFO in BareFormula.promoteChildren(): returning (structured print): " + newf.toStructuredString());
			return newf;
		}
		else {
            //System.out.println("INFO in BareFormula.promoteChildren(): " + this.toStructuredString() + " not modified, returning");
            return null;
        }
    }
    
    /** ***************************************************************
     * Return the formula without any leading quantifiers (if the
     * formula is in prefix normal form, this is the matrix of the
     * formula).
     */
    public BareFormula getMatrix() {

        BareFormula f = deepCopy();
        while (f.isQuantified()) {
            if (f.child2 != null)
                f = f.child2;
            else {
                f.op = "";
                f.lit1 = f.lit2;
                f.lit2 = null;
            }
        }
        return f;
    }

    /** ***************************************************************
     * Return a string representation of the formula.
     */
    public String toKIFString() {

        //System.out.println("BareFormula.toKIFString(): " + this);
        //System.out.println("BareFormula.toKIFString(): op: " + op);
        //System.out.println("BareFormula.toKIFString(): KIF: " + KIF.opMap.get(op));
        //System.out.println("BareFormula.toKIFString(): child1: " + child1);
        //System.out.println("BareFormula.toKIFString(): lit1: " + lit1);
        //System.out.println("BareFormula.toKIFString(): child2: " + child2);
        //System.out.println("BareFormula.toKIFString(): lit2: " + lit2);
        String arg1 = null;
        if (child1 != null) {
            arg1 = child1.toKIFString();
        }
        if (lit1 != null) {
            arg1 = lit1.toKIFString();
        }
        String arg2 = null;
        if (child2 != null)        
            arg2 = child2.toKIFString();
        if (lit2 != null) {
            arg2 = lit2.toKIFString();
        }

        if (Term.emptyString(op)) {
            String result = arg1;
            //System.out.println("BareFormula.toKIFString(): result: " + result);
            return result;
        }
        if (op.equals(Lexer.Negation)) {
            String result = "(" + KIF.opMap.get(Lexer.Negation) + " " + arg1 + ")";
            //System.out.println("BareFormula.toKIFString(): result: " + result);
            return result;
        }
        if (logOp(op)) {
            String result = "(" + KIF.opMap.get(op) + " " + arg1 + " " + arg2 + ")";
            //System.out.println("BareFormula.toKIFString(): result: " + result);
            return result;
        }
        else {
            if (!op.equals("!") && !op.equals("?")) {
                System.out.println("Error in BareFormula.toString(): bad operator: " + op);
                return null;
            }
            if (KIF.opMap.get(op) == null) {
                String result = "(" + op + " " + "(" + arg1 + ") " + arg2 + ")";
                //System.out.println("BareFormula.toKIFString(): result: " + result);
                return result;
            }
            else {
                String result = "(" + KIF.opMap.get(op) + " " + "(" + arg1 + ") " + arg2 + ")";
                //System.out.println("BareFormula.toKIFString(): result: " + result);
                return result;
            }
        }
    }

    /** ***************************************************************
     */
    public static BareFormula parseKIFNegation(Term term) {

        if (term.subterms.size() != 1) {
            System.out.println("Error in parseKIFNegation(): wrong number of arguments to negation in " + term);
            return null;
        }
        BareFormula bf = parseKIF(term.subterms.get(0),true);
        if (bf.lit1 != null) {
            bf.lit1.negated = !bf.lit1.negated;
            return bf;
        }
        else {
            BareFormula bf2 = new BareFormula();
            bf2.child1 = bf.child1;
            bf2.op = "~";
            return bf2;
        }
    }

    /** ***************************************************************
     */
    public static BareFormula parseKIFEquality(Term term) {

        if (term.subterms.size() != 2) {
            System.out.println("Error in parseKIFEquality(): wrong number of arguments to equal in " + term);
            return null;
        }
        BareFormula bf1 = parseKIF(term.subterms.get(0),false);
        BareFormula bf2 = parseKIF(term.subterms.get(1),false);
        if (bf1.lit1.atom.subterms.size() == 0 && bf2.lit1.atom.subterms.size() == 0) {  // must be an equality literal
            Term newterm = new Term();
            newterm.t = "=";
            newterm.subterms.add(new Term(bf1.lit1.atom.t));
            newterm.subterms.add(new Term(bf2.lit1.atom.t));
            bf1.lit1.atom = newterm;
            return bf1;
        }
        return null;
    }

    /** ***************************************************************
     */
    public static BareFormula parseKIFQuantifier(Term term) {

        BareFormula child = parseKIF(term.subterms.get(1),true);
        BareFormula bf = null;
        //System.out.println("BareFormula.parseKIFQuantifier(): term as quantifier expression: " + term);
        //System.out.println("BareFormula.parseKIFQuantifier(): subterms: " + term.subterms);
        //System.out.println("BareFormula.parseKIFQuantifier(): first subterm: " + term.subterms.get(0));
        for (Term var : term.subterms.get(0).subterms) {
            bf = new BareFormula();
            bf.lit1 = new Literal();
            bf.lit1.atom = var;
            if (term.t.equals("exists"))
                bf.op = "?";
            else
                bf.op = "!";
            if (child.isLiteral())
                bf.lit2 = child.lit1;
            else
                bf.child2 = child;
            child = bf;

        }
        //System.out.println("BareFormula.parseKIFQuantifier(): returning quantifier expression: " + bf.toStructuredString());
        return bf;
    }

    /** ***************************************************************
     */
    public static BareFormula parseKIFImpEquiv(Term term) {

        BareFormula bf1 = parseKIF(term.subterms.get(0),true);
        BareFormula bf2 = parseKIF(term.subterms.get(1),true);
        BareFormula bf = new BareFormula();
        bf.op = term.t;
        if (bf1.isLiteral())
            bf.lit1 = bf1.lit1;
        else
            bf.child1 = bf1;
        if (bf2.isLiteral())
            bf.lit2 = bf2.lit1;
        else
            bf.child2 = bf2;
        return bf;
    }

    /** ***************************************************************
     */
    public static BareFormula parseKIFConjDisj(Term term) {

        BareFormula bf = new BareFormula();
        bf.op = term.t;
        BareFormula bf1 = parseKIF(term.subterms.get(0),true);
        if (bf1.isLiteral())
            bf.lit1 = bf1.lit1;
        else
            bf.child1 = bf1;
        BareFormula rhs = bf;
        for (int i = 1; i < term.subterms.size(); i++) {      // 'and' and 'or' can have several arguments in SUO-KIF
            BareFormula bf2 = parseKIF(term.subterms.get(1),true); // convert them to a tree of binary ones
            if (bf2.isLiteral())
                rhs.lit1 = bf2.lit1;
            else
                bf.child1 = bf1;
            if (bf2.isLiteral())
                bf.lit2 = bf2.lit1;
            else
                bf.child2 = bf2;
        }
        return bf;
    }

    /** ***************************************************************
     * Take a Term, with subTerms and convert it to a BareFormula.
     * @param truthValue is true if this term must be a sentence, with
     *                   a truth value, rather than a term.  Note that if
     *                   false, it could still be for an equality that needs
     *                   compares variables that are truth values
     */
    public static BareFormula parseKIF(Term term, boolean truthValue) {

        //System.out.println("BareFormula.parseKIF(): input: " + term);
        //System.out.println("BareFormula.parseKIF(): t: " + term.t);
        if (term == null || term.emptyString(term.t)) {
            System.out.println("Error in BareFormula.parseKIF(): null term");
            return null;
        }
        else if (term.t.equals("not"))
            return parseKIFNegation(term);
        else if (term.t.equals("equal"))
            return parseKIFEquality(term);
        else if (term.t.equals("exists") || term.t.equals("forall"))
            return parseKIFQuantifier(term);
        else if (term.t.equals(KIFLexer.Implies) || term.t.equals(KIFLexer.Equiv))
            return parseKIFImpEquiv(term);
        else if (term.t.equals(KIFLexer.And) || term.t.equals(KIFLexer.Or))
            return parseKIFConjDisj(term);
        else { // non-logical relation
            //System.out.println("BareFormula.parseKIF(): non logical predicate input: " + term.t);
            BareFormula bf = new BareFormula();
            bf.lit1 = new Literal();
            bf.lit1.atom = term;
            return bf;
        }
    }

    /** ***************************************************************
     * Return a string representation of the formula.
     */
    public String toString() {

        String arg1 = null;
        if (child1 != null)        
            arg1 = child1.toString();
        if (lit1 != null)
            arg1 = lit1.toString();
        String arg2 = null;
        if (child2 != null)        
            arg2 = child2.toString();
        if (lit2 != null)
            arg2 = lit2.toString();
        
        if (Term.emptyString(op))      
            return arg1;        
        if (op.equals("~"))      
            return "(~" + arg1 + ")";        
        if (logOp(op)) 
            return "(" + arg1 + op + arg2 + ")";        
        else {
            if (!op.equals("!") && !op.equals("?")) {
                System.out.println("Error in BareFormula.toString(): bad operator: " + op);
                return null;
            }
            return "(" + op + "[" + arg1 + "]:" + arg2 + ")"; 
        }  
    }

    /** ***************************************************************
     * Return a string representation of the formula with detailed
     * information to support debugging.
     */
    public String toStructuredString() {

        String arg1 = null;
        if (child1 != null)
            arg1 = "(child1:" + child1.toStructuredString() + ")";
        if (lit1 != null)
            arg1 = lit1.toString();
        String arg2 = null;
        if (child2 != null)
            arg2 = "(child2:" + child2.toStructuredString() + ")";
        if (lit2 != null)
            arg2 = lit2.toString();

        if (Term.emptyString(op))
            return arg1;
        if (op.equals("~"))
            return "(~" + arg1 + ")";
        if (logOp(op))
            return "(" + arg1 + op + arg2 + ")";
        else {
            if (!op.equals("!") && !op.equals("?")) {
                System.out.println("Error in BareFormula.toString(): bad operator: " + op);
                return null;
            }
            return "(" + op + "[" + arg1 + "]:" + arg2 + ")";
        }
    }

    /** ***************************************************************
     * Return True if self is structurally equal to other.
     */
    public boolean equals(BareFormula other) {

        if (other == null) {
            System.out.println("BareFormula.equals(): null argument other");
            Thread.dumpStack();
            return false;
        }
        if (op != null) {
            if (!op.equals(other.op))
                return false;
        }
        else
            if (other.op != null)
                return false;
        
        if (child1 != null && other.child1 != null) {
            if (!child1.equals(other.child1))
                return false;
        }
        else
            if (other.child1 != null)
                return false;
        
        if (lit1 != null) {
            if (!lit1.equals(other.lit1))
                return false;
        }
        else
            if (other.lit1 != null)
                return false;        

        if (lit2 != null) {
            if (!lit2.equals(other.lit2))
                return false;
        }
        else
            if (other.lit2 != null)
                return false;
        
        if (child2 != null) {
            if (!child2.equals(other.child2))
                return false;
        }
        else
            if (other.child2 != null)
                return false;
        
        return true;
    }

    
    /** ***************************************************************
     */
    public boolean childrenEqual() {
    
    	if (child1 != null) {
    		if (child2 == null)
        		return false;
    		return child1.equals(child2);
    	}
    	else if (child2 != null)
    		return false;
    	
    	if (lit1 != null) {
    		if (lit2 == null)
        		return false;
    		return lit1.equals(lit2);
    	}
    	else if (lit2 != null)
    		return false;
    	return false;
    }
    
    /** ***************************************************************
     */
    public BareFormula deepCopy() {
        
        if (lit1 != null && child1 != null) {
            System.out.println("Error in BareFormula.deepCopy(): lit1 & child1 are both non-null : " + this.toStructuredString());
            System.out.println("Error in BareFormula.deepCopy(): lit1: " + lit1);
            System.out.println("Error in BareFormula.deepCopy(): child1: " + child1);
            Thread.dumpStack();

        }
        if (lit2 != null && child2 != null) {
            System.out.println("Error in BareFormula.deepCopy(): lit2 & child2 are both non-null: " + this.toStructuredString());
            System.out.println("Error in BareFormula.deepCopy(): lit2: " + lit2);
            System.out.println("Error in BareFormula.deepCopy(): child2: " + child2);
            Thread.dumpStack();
        }
        BareFormula result = new BareFormula();
        result.op = op;
        if (child1 != null)
            result.child1 = child1.deepCopy();
        else if (lit1 != null)
            result.lit1 = lit1.deepCopy();
        if (child2 != null)
            result.child2 = child2.deepCopy();
        else if (lit2 != null)
            result.lit2 = lit2.deepCopy();
        return result;
    }

    /** ***************************************************************
     * Return the set of all function and predicate symbols used in
     *         the formula.
     */
    public Signature collectSig(Signature sig) {

        ArrayDeque<BareFormula> todo = new ArrayDeque();
        todo.push(this);
        while (todo.size() > 0) {
            BareFormula f = todo.pop();
            if (f.isLiteral())
                f.lit1.collectSig(sig);
            else if (f.isUnary())
                todo.push(f.child1);
            else if (f.isBinary()) {
                todo.push(f.child1);
                todo.push(f.child2);
            }
            else {
                assert f.isQuantified();
                todo.push(f.child2);
            }
        }
        return sig;
    }

    /** ***************************************************************
     * Return the set of all function and predicate symbols used in
     *         the formula.
     */
    public Signature collectSig() {

        Signature sig = new Signature();
        return collectSig(sig);
    }

    /** ***************************************************************
     * Return the set of all variables in self.
     */
    public LinkedHashSet<Term> collectVars() {

        LinkedHashSet<Term> res = null;
        if (isLiteral())
            res = lit1.collectVars();
        else if (isUnary())
	    	if (child1 != null)
	    		res = child1.collectVars();
	    	else
	    		res = lit1.collectVars(); 
        else if (isBinary()) {
            res = child1.collectVars();
            res.addAll(child2.collectVars());
        }
        else {
            assert isQuantified();
            res = lit1.collectVars();
            res.addAll(child2.collectVars());
        }
        return res;   
    }
    
    /** ***************************************************************
     * Return the set of all free variables in self.
     */
    public LinkedHashSet<Term> collectFreeVars() {

    	//System.out.println("INFO in BareFormula.collectFreeVars(): " + this + "  op: '" + op + "'");
        LinkedHashSet<Term> res = new LinkedHashSet<Term>();
        if (isLiteral())
            res = lit1.collectVars();
        else if (isUnary() || Term.emptyString(op)) {
           	//System.out.println("INFO in BareFormula.collectFreeVars(): unary");
        	if (child1 != null)
        		res = child1.collectFreeVars();
        	else
        		res = lit1.collectVars();
        }
        else if (isBinary()) {
           	//System.out.println("INFO in BareFormula.collectFreeVars(): binary");
        	if (child1 != null)
        		res = child1.collectFreeVars();
        	else
        		res = lit1.collectVars();  
        	if (child2 != null)
        		res.addAll(child2.collectFreeVars());
        	else
        		res.addAll(lit2.collectVars());  
        }
        else {
            // Quantor case. We first collect all free variables in
            // the quantified formula, then remove the one bound by the
            // quantifier. 
           	//System.out.println("INFO in BareFormula.collectFreeVars(): quantified");
            if (!isQuantified())
               	System.out.println("Error in BareFormula.collectFreeVars(): expected quantified statement");
        	if (child2 != null)
        		res = child2.collectFreeVars();
        	else
        		res = lit2.collectVars();             
            res.removeAll(lit1.collectVars());
        }
       	//System.out.println("INFO in BareFormula.collectFreeVars(): returning vars: " + res);
        return res; 
    }
    
    /** ***************************************************************
     * Return the set of all (first-order) operators and quantors
     * used in the formula. This is mostly for unit-testing
     * transformations later on. 
     */
    public ArrayList<String> collectOps() {
        
        ArrayList<String> res = new ArrayList<String>();
        if (!Term.emptyString(op))
            res.add(op);
    	if (child1 != null)
    		res.addAll(child1.collectOps());
    	if (child2 != null)
    		res.addAll(child2.collectOps());
        return res;         
    }

    /** ***************************************************************
     * Return the set of all function and predicate symbols used in
     * the formula. 
     */
    public ArrayList<String> collectFuns() {

    	ArrayList<String> res = new ArrayList<String>();
        if (isQuantified()) {
        	if (child2 != null)
        		res.addAll(child2.collectFuns());
        	else
        		res.addAll(lit2.collectFuns());
        }
        else {
        	if (child1 != null)
                res.addAll(child1.collectFuns());
        	if (lit1 != null)
                res.addAll(lit1.collectFuns());
        	if (child2 != null)
                res.addAll(child2.collectFuns());
        	if (lit2 != null)
                res.addAll(lit2.collectFuns());
        }
        return res;   
    }
    
    /** ***************************************************************
     * Substitute one variable for another
     */
    public BareFormula substitute(Substitutions subst) {
        
        //System.out.println("INFO in BareFormula.substitute(): "  + this + " " + subst);
        BareFormula result = deepCopy();
        if (child1 != null)
            result.child1 = child1.substitute(subst);
        if (child2 != null)
            result.child2 = child2.substitute(subst);
        if (lit1 != null)
            result.lit1 = lit1.instantiate(subst);
        if (lit2 != null)
            result.lit2 = lit2.instantiate(subst);
        return result;
    }
    
    /** ***************************************************************
     * Parse the "remainder" of a formula starting with the given quantor.
     * Stream tokenizer will be pointing on the opening square bracket to start. 
     */
    public static BareFormula parseQuantified(Lexer lex, String quantor) 
        throws ParseException, IOException {

        //System.out.println("INFO in BareFormula.parseQuantified(): " + lex.literal);
        lex.checkTok(Lexer.IdentUpper);
        Literal var = new Literal();
        var = var.parseLiteral(lex);
        //System.out.println("INFO in BareFormula.parseQuantified(): var: " + var);
        BareFormula rest = null;
        if (lex.testTok(Lexer.Comma)) {
            lex.acceptTok(Lexer.Comma);
            rest = parseQuantified(lex, quantor);
        }
        else {
            lex.acceptTok(Lexer.CloseSquare);
            lex.acceptTok(Lexer.Colon);
            rest = parseUnitaryFormula(lex);            
        }
        BareFormula result = new BareFormula(quantor,var);
        result.child2 = rest;
        //System.out.println("INFO in  BareFormula.parseQuantified(): returning: " + result.toStructuredString());
        return result;
    }

    /** ***************************************************************
     * Parse a "unitary" formula (following TPTP-3 syntax terminology). 
     * This can be the first unitary formula of a binary formula, of course.
     * It expects stream pointer to be on the first token.
     */
    public static BareFormula parseUnitaryFormula(Lexer lex)
        throws IOException, ParseException {

        //System.out.println("INFO in BareFormula.parseUnitaryFormula(): token: " + lex.literal);
        BareFormula res = null;
        if (lex.testTok(Lexer.quant)) {
            String quantor = lex.lookLit();
            //System.out.println("INFO in BareFormula.parseUnitaryFormula(): quantifier: " + quantor);
            lex.next();
            lex.acceptTok(Lexer.OpenSquare);
            res = parseQuantified(lex, quantor);
        }
        else if (lex.testTok(Lexer.OpenPar)) {
            //System.out.println("INFO in BareFormula.parseUnitaryFormula(): open par ");
            lex.acceptTok(Lexer.OpenPar);                      
            res = BareFormula.parse(lex);
            lex.acceptTok(Lexer.ClosePar);                
        }
        else if (lex.testTok(Lexer.Negation)) {
            //System.out.println("INFO in BareFormula.parseUnitaryFormula(): negation ");
            lex.acceptTok(Lexer.Negation);
            BareFormula subform = parseUnitaryFormula(lex);
            res = new BareFormula("~",subform);
        }
        else {
            //System.out.println("INFO in BareFormula.parseUnitaryFormula(): (2) token: " + lex.literal);
            Literal lit = new Literal();
            lit = lit.parseLiteral(lex);  // stream pointer looks at token after literal
            res = new BareFormula("",lit);
        }
        //System.out.println("INFO in BareFormula.parseUnitaryFormula(): returning: " + res.toStructuredString());
        return res;
    }

    /** ***************************************************************
     * Parse the rest of the associative formula that starts with head
     * and continues ([&|] form *).
     * It expects stream to be pointing at the operator.
     */
    public static BareFormula parseAssocFormula(Lexer lex, BareFormula head) 
        throws IOException, ParseException {

        //System.out.println("INFO in BareFormula.parseAssocFormula(): token: " + lex.literal);
        String op = lex.lookLit();
        while (lex.testLit(op)) {
            lex.acceptLit(op);
            BareFormula next = parseUnitaryFormula(lex);
            BareFormula newhead = new BareFormula(op,head,next);
            head = newhead;
        }
        //System.out.println("INFO in BareFormula.parseAssocFormula(): returning: " + head.toStructuredString());
        return head;
    }

    /** ***************************************************************
     * Parse a (naked) formula.  Stream pointer must be at the start 
     * of the expression.
     */
    public static BareFormula parse(Lexer lex) throws IOException, ParseException {

        if (level > 25) { // trap pathological cases of nested formulas
            System.out.println("Error in BareFormula.parse(): too much nesting at line: " + lex.pos + " in " + lex.line);
            return null;
        }
        level++;
        //System.out.println("INFO in BareFormula.parse(): token: " + lex.literal);
        BareFormula res = parseUnitaryFormula(lex);
        //System.out.println("INFO in BareFormula.parse(): returned from unitary with token: " + lex.literal);
        
        if (lex.testTok(Lexer.andOr)) 
            res = parseAssocFormula(lex, res.deepCopy());           
        else if (lex.testTok(Lexer.binaryRel)) {
            String op = lex.lookLit();
            lex.next();
            BareFormula rest = parseUnitaryFormula(lex);
            BareFormula lhs = res.deepCopy();
            res = new BareFormula(op,lhs,rest);  
        }
        //System.out.println("INFO in BareFormula.parse(): returning: " + res.toStructuredString());
        return res;
    }

    /** ***************************************************************
     * Convert a string to a BareFormula
     */
    public static BareFormula string2form(String s) {

        BareFormula.level = 0;
        try {
            Lexer lex = new Lexer(s);
            return BareFormula.parse(lex);
        }
        catch (Exception e) {
            System.out.println("Error in BareFormula.string2form()");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
