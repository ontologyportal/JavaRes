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
        child1 = c1.deepCopy();
    }
    
    /** ***************************************************************
     */
    public BareFormula(String s, Literal l1) {
     
        op = s;
        lit1 = l1.deepCopy();
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
    private boolean logOp(String s) {
        
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

        if (isLiteral())
            return true;
        if (op == "|")
            return child1.isLiteralDisjunction() &&
                   child2.isLiteralDisjunction();
        return false;
    }
    
    /** ***************************************************************
     * Return True if the formula is a conjunction of disjunction of
     * literals.
     */
    public boolean isClauseConjunction() {

        if (isLiteral())
            return true;
        if (op == "|")
            return isLiteralDisjunction();
        if (op == "&")
            return child1.isClauseConjunction() &&
                   child2.isClauseConjunction();
        return false;
    }
     
    /** ***************************************************************
     * Return True if the formula is in conjunctive normal form.
     */
    public boolean isCNF() {

        if (op == "!")
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
     * Return True if self has a proper subformula as the first
     * argument. This is the case for quantified formulas and binary
     * formulas.
     */
    public boolean hasSubform2() {
        
        return isQuantified() || isBinary();
    }  
    
    /** ***************************************************************
     * If a BareFormula has no operator, but does have a child rather
     * than a literal, promote its first child. Recursively call on 
     * each child first. Return null if not modified.               
     */
    public BareFormula promoteChildren() {
    	
    	System.out.println("INFO in BareFormula.promoteChildren(): " + this);
    	System.out.println("INFO in BareFormula.promoteChildren(): op: " + op);
    	BareFormula newf = deepCopy();
    	BareFormula tempf = null;
    	boolean modified = false;
    	if (child1 != null) {
    		tempf = child1.promoteChildren();
    		if (tempf != null) {
    			modified = true;
    			newf.child1 = tempf;
    	    	System.out.println("INFO in BareFormula.promoteChildren(): child1 modified");
    		}
    	}
    	if (child2 != null) {
    		tempf = child2.promoteChildren();
    		if (tempf != null) {
    			modified = true;
    			newf.child2 = tempf;
    	    	System.out.println("INFO in BareFormula.promoteChildren(): child2 modified");
    		}
    	}
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
		if (modified) {
	    	System.out.println("INFO in BareFormula.promoteChildren(): returning: " + newf);
			return newf;
		}
		else
			return null;    	
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

        String arg1 = null;
        if (child1 != null)        
            arg1 = child1.toKIFString();
        if (lit1 != null)
            arg1 = lit1.toKIFString();
        String arg2 = null;
        if (child2 != null)        
            arg2 = child2.toKIFString();
        if (lit2 != null)
            arg2 = lit2.toKIFString();
        
        if (Term.emptyString(op))      
            return arg1;        
        if (op.equals(Lexer.Negation))      
            return "(" + KIF.opMap.get(Lexer.Negation) + " " + arg1 + ")";        
        if (logOp(op)) 
            return "(" + KIF.opMap.get(op) + " " + arg1 + " " + arg2 + ")";        
        else {
            if (!op.equals("!") && !op.equals("?")) {
                System.out.println("Error in BareFormula.toString(): bad operator: " + op);
                return null;
            }
            return "(" + KIF.opMap.get(op) + " "  + "(" + arg1 + ") " + arg2 + ")"; 
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
     * Return True if self is structurally equal to other.
     */
    public boolean equals(BareFormula other) {

        if (op != null) {
            if (!op.equals(other.op))
                return false;
        }
        else
            if (other.op != null)
                return false;
        
        if (child1 != null) {
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
            System.out.println("Error in BareFormula.deepCopy(): lit1 & child1 are both non-null");
            return null;
        }
        if (lit2 != null && child2 != null) {
            System.out.println("Error in BareFormula.deepCopy(): lit2 & child2 are both non-null");
            return null;
        }
        BareFormula result = new BareFormula();
        result.op = op;
        if (lit1 != null)            
            result.lit1 = lit1.deepCopy();
        if (lit2 != null)
            result.lit2 = lit2.deepCopy(); 
        if (child1 != null)            
            result.child1 = child1.deepCopy();
        if (child2 != null)            
            result.child2 = child2.deepCopy();
        return result;
    }

    /** ***************************************************************
     * Return the set of all variables in self.
     */
    public ArrayList<Term> collectVars() {

    	ArrayList<Term> res = null;
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
    public ArrayList<Term> collectFreeVars() {

    	//System.out.println("INFO in BareFormula.collectFreeVars(): " + this + "  op: '" + op + "'");
        ArrayList<Term> res = new ArrayList<Term>();
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
            result.lit1 = lit1.substitute(subst);
        if (lit2 != null)
            result.lit2 = lit2.substitute(subst);        
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
        //System.out.println("INFO in  BareFormula.parseQuantified(): returning: " + result);
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
            lex.next();
            lex.acceptTok(Lexer.OpenSquare);
            res = parseQuantified(lex, quantor);
        }
        else if (lex.testTok(Lexer.OpenPar)) {
            lex.acceptTok(Lexer.OpenPar);                      
            res = BareFormula.parse(lex);
            lex.acceptTok(Lexer.ClosePar);                
        }
        else if (lex.testTok(Lexer.Negation)) {
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
        //System.out.println("INFO in BareFormula.parseUnitaryFormula(): returning: " + res);
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
        return head;
    }

    /** ***************************************************************
     * Parse a (naked) formula.  Stream pointer must be at the start 
     * of the expression.
     */
    public static BareFormula parse(Lexer lex) throws IOException, ParseException {
      
        //System.out.println("INFO in BareFormula.parseRecurse(): token: " + lex.literal);        
        BareFormula res = parseUnitaryFormula(lex);
        //System.out.println("INFO in BareFormula.parseRecurse(): returned from unitary with token: " + lex.literal);
        
        if (lex.testTok(Lexer.andOr)) 
            res = parseAssocFormula(lex, res.deepCopy());           
        else if (lex.testTok(Lexer.binaryRel)) {
            String op = lex.lookLit();
            lex.next();
            BareFormula rest = parseUnitaryFormula(lex);
            BareFormula lhs = res.deepCopy();
            res = new BareFormula(op,lhs,rest);  
        }
        return res;
    }

    /** ***************************************************************
     * Convert a string to a BareFormula
     */
    public static BareFormula string2form(String s) {

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
