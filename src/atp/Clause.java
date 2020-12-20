/*
A simple implementation of first-order clauses.

See Literal.java for the definition of atoms and literals.

A logical clause in our sense is a multi-set of literals, implicitly
representing the universally quantified disjunction of these literals.

The set of all clauses for a given signature is denoted as
Clauses(P,F,V).

We represent a clause as a list of literals. The actual clause data
structure contains additional information that is useful, but not
strictly necessary from a logic/calculus point of view.

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
import java.text.*;

/** ***************************************************************
*/
public class Clause {

    public static int clauseIDcounter = 0;
    public ArrayList<Literal> literals = new ArrayList<Literal>(); 
    public String type = "plain";
    public String name = "";
    public ArrayList<String> support = new ArrayList<String>();  // Clauses or Formulas from which this clause is derived.
    public ArrayList<String> supportsClauses = new ArrayList<String>();  // Clauses this clause supports.     
    public int depth = 0;                                        // Depth from input
    public String rationale = "input";                           // If not input, reason for derivation.
    public ArrayList<Integer> evaluation = null;                 // Must be the same order as clause evaluation 
                                                                 // function list in EvalStructure.
    public Substitutions subst = new Substitutions();            // The substitutions that support any derived clause.

    /** ***************************************************************
     */
    public Clause() {

    }
    
    /** ***************************************************************
     * Print for use by GraphViz.  Convert vertical bar to HTML code and
     * just print the formula with no informational wrapper.
     */
    public Clause(ArrayList<Literal> litlist) {
        literals = litlist;
    }
    
    /** ***************************************************************
     * Print for use by GraphViz.  Convert vertical bar to HTML code and
     * just print the formula with no informational wrapper.
     */
    public String toString(boolean forDot) {
            
        if (!forDot)
            return toString();
        else {
            String temp = Literal.literalList2String(literals);
            return temp.replaceAll("\\|", "&#124;");
        }
    }

    /** ***************************************************************
     */
    public ArrayList<String> getConstantStrings() {
        
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < literals.size(); i++) 
            result.addAll(literals.get(i).getConstantStrings());
        return result;
    }
    
    /** ***************************************************************
     */
    public String toString() {
            
        StringBuffer result = new StringBuffer();
        result.append("cnf(" + name + "," + type + "," + 
                Literal.literalList2String(literals) + ").");
        return result.toString();
    }
    
    /** ***************************************************************
     * Create a string representation of the Clause with reference to
     * an inference rule and its supporting axioms if it was generated
     * in inference.
     */
    public String toStringJustify() {
            
        StringBuffer result = new StringBuffer();
        result.append("cnf(" + name + "," + type + "," + 
                Literal.literalList2String(literals) + ").");
        if (support.size() > 0) {
            result.append(" : " + rationale + "[");
            result.append(support.get(0));
            for (int i = 1; i < support.size(); i++) {
                result.append(",");
                result.append(support.get(i));
            }
            result.append("]");
        }
        if (subst != null && subst.subst.keySet().size() > 0) {
            result.append(";");
            result.append(subst.toString());
        }
        return result.toString();
    }
    
    /** ***************************************************************
     * Create a string representation of the Clause's
     * inference rule and its supporting axioms if it was generated
     * in inference, in TSTP format.
     */
    public String toStringTSTPJustify() {
            
        StringBuffer result = new StringBuffer();
        if (support.size() > 0) {
            result.append("inference(" + rationale + ",[");
            result.append(support.get(0));
            for (int i = 1; i < support.size(); i++) {
                result.append(",");
                result.append(support.get(i));
            }
            result.append("])");
        }

        return result.toString();
    }
    
    /** ***************************************************************
     * Print all info about a clause
     */
    public String toStringDiag() {
            
        StringBuffer result = new StringBuffer();
        result.append(toStringJustify());
        result.append(":" + Integer.toString(depth));
        return result.toString();
    }
    
    /** ***************************************************************
     */
    public void createName() {
            
        name = "c" + Integer.toString(clauseIDcounter);
        clauseIDcounter++;
    }
    
    /** ***************************************************************
     */
    public void addEval(ArrayList<Integer> e) {
            
        evaluation = e;
    }

    /** ***************************************************************
     */
    public Clause deepCopy() {
                
        return deepCopy(0);
    }
    
    /** ***************************************************************
     * @param start is the starting index of the literal list to copy
     */
    public Clause deepCopy(int start) {
        
        Clause result = new Clause();
        result.name = name;
        result.type = type;
        result.rationale = rationale;
        for (int i = 0; i < support.size(); i++)  
            result.support.add(support.get(i));
        for (int i = start; i < literals.size(); i++) 
            result.literals.add(literals.get(i).deepCopy());
        if (subst != null)
            result.subst = subst.deepCopy();
        return result;
    }
    
    /** ***************************************************************
     * Check to see if the contents of two clauses are equal.  Ignore
     * all meta-information such as clause name, type and information
     * that tracks how it was created.  Note that normalizeVariables()
     * should first be called so that identical clauses will have
     * syntactically equal variable names.
     */
    public boolean equals(Object c_obj) {
       
        assert !c_obj.getClass().getName().equals("Clause") : "Clause() passed object not of type Clause"; 
        Clause c = (Clause) c_obj;
        if (literals.size() != c.literals.size())
            return false;
        for (int i = 0; i < literals.size(); i++)
            if (!literals.get(i).equals(c.literals.get(i)))
                return false;
        return true;
    }
    
    /** ***************************************************************
     * should never be called so throw an error.
     */   
    public int hashCode() {
        assert false : "Clause not designed";
        return 0;
    }
    
    /** ***************************************************************
     */
    public int length() {
               
        return literals.size();
    }
    
    /** ***************************************************************
     */
    public void add(Literal l) {
        
        literals.add(l);
    }
    
    /** ***************************************************************
     */
    public void addAll(ArrayList<Literal> l) {
        
        literals.addAll(l);
    }
    
    /** ***************************************************************
     * Parse a clause. A clause in (slightly simplified) TPTP-3 syntax 
     * is written as
     *    cnf(<name>, <type>, <literal list>).
     * where <name> is a lower-case ident, type is a lower-case ident
     * from a specific list, and <literal list> is a "|" separated list
     * of literals, optionally enclosed in parenthesis.
     * For us, all clause types are essentially the same, so we only
     * distinguish "axiom", "negated_conjecture", and map everything else
     * to "plain".  
     * @return the parsed clause.  Note also that this is the side effect 
     * on the clause instance
     */
    public static Clause parse(Lexer lex) {
               
    	Clause result = new Clause();
        try {
            //System.out.println("INFO in Clause.parse(): " + lex.literal);
            lex.next(); 
            //if (st.ttype == '%')
            //    return this;
            if (!lex.literal.equals("cnf"))
                throw new Exception("\"cnf\" expected.  Instead found '" + lex.literal + "' with clause so far " + result);
            lex.next(); 
            if (!lex.type.equals(Lexer.OpenPar))
                throw new Exception("Open paren expected. Instead found '" + lex.literal + "' with clause so far " + result);
            lex.next(); 
            if (lex.type == Lexer.IdentLower)
            	result.name = lex.literal;
            else {
                System.out.println("Warning in Clause.parse(): Identifier expected.  Instead found '" + lex.literal + "' with clause so far " + result);
                System.out.println("Accepting non-identifier anyway.");
                result.name = lex.literal;
            }
            lex.next(); 
            if (!lex.type.equals(Lexer.Comma))
                throw new Exception("Comma expected. Instead found '" + lex.literal + "' with clause so far " + result);
            lex.next(); 
            if (lex.type == Lexer.IdentLower) {                 
            	result.type = lex.literal;
                //if (!type.equals("axiom") && !type.equals("negated_conjecture"))
                //   type = "plain";
            }
            else 
                throw new Exception("Clause type enumeration expected.  Instead found '" + lex.literal + "' with clause so far " + result);
            lex.next(); 
            if (!lex.type.equals(Lexer.Comma))
                throw new Exception("Comma expected. Instead found '" + lex.literal + "' with clause so far " + result);
            String s = lex.look(); 
            //System.out.println("INFO in Clause.parse() (2): found token: " + s);
            if (s.equals(Lexer.OpenPar)) {
                //System.out.println("INFO in Clause.parse(): found open paren at start of bare clause");
                lex.next();
                result.literals = Literal.parseLiteralList(lex);
                lex.next(); 
                if (!lex.type.equals(Lexer.ClosePar))
                    throw new Exception("Literal list close paren expected. Instead found '" + lex.literal + "' with clause so far " + result);
            }
            else {
            	result.literals = Literal.parseLiteralList(lex);
            }

            lex.next();
            if (!lex.type.equals(Lexer.ClosePar)) {
                //System.out.println("Warning in Clause.parse(): Clause close paren expected. Instead found '" + lex.literal + "' with clause so far " + this);
                //System.out.println("Discarding remainder of line.");
                while (lex.type != Lexer.FullStop && lex.type != Lexer.EOFToken)
                	lex.next();
                return result;
            }
            lex.next(); 
            if (!lex.type.equals(Lexer.FullStop))
                throw new Exception("Period expected. Instead found '" + lex.literal + "' with clause so far " + result);
            //System.out.println("INFO in Clause.parse(): completed parsing: " + this);
            return result;
        }
        catch (Exception ex) {
            Prover2.errors = "input error";
            if (lex.type == Lexer.EOFToken)
                return result;
            System.out.println("Error in Clause.parse(): " + ex.getMessage());
            System.out.println("Error in Term.parseTermList(): token:" + lex.literal);  
            ex.printStackTrace();
        }
        return null;
    }  
    
    /** ***************************************************************
     */
    public static Clause string2Clause(String s) {
    
        Lexer lex = new Lexer(s);
        return Clause.parse(lex);
    }
    
    /** ***************************************************************
     * Return true if the clause is empty.
     */
    public boolean isEmpty() {

        return literals.size() == 0;
    }

    /** ***************************************************************
     * Return true if the clause is a unit clause.
     */
    public boolean isUnit() {

        return literals.size() == 1;
    }
    
    /** ***************************************************************
     * Return true if the clause is a Horn clause.
     */
    public boolean isHorn() {

        ArrayList<Literal> tmp = new ArrayList<Literal>();
        for (int i = 0; i < literals.size(); i++) 
            if (literals.get(i).isPositive())
                tmp.add(literals.get(i));
        return tmp.size() <= 1;
    }
    
    /** ***************************************************************
     * Return the indicated literal of the clause. Position is an
     * integer from 0 to litNumber (exclusive).
     */
    public Literal getLiteral(int position) {

        if (position >= 0 && position < literals.size())
            return literals.get(position);
        else
            return null;
    }
    
    /** ***************************************************************
     * Insert all variables in self into the set res and return it. 
     */
    public ArrayList<Term> collectVars() {

        ArrayList<Term> res = new ArrayList<Term>();
        for (int i = 0; i < literals.size(); i++)
            res.addAll(literals.get(i).collectVars());
        return res;
    }
    
    /** ***************************************************************
     * Collect function- and predicate symbols into the signature. 
     */
    public Signature collectSig(Signature sig) {

        for (Literal l : literals)
            sig = l.collectSig(sig);
        return sig;
    }

    /** ***************************************************************
     * Return the symbol-count weight of the clause.
     */
    public int weight(int fweight, int vweight) {

        int res = 0;
        for (int i = 0; i < literals.size(); i++)
            res = res + literals.get(i).weight(fweight, vweight);
        return res;
    }
    
    /** ***************************************************************
     * Return an instantiated copy of self. Name and type are copied
     * and need to be overwritten if that is not desired.
     */
    public Clause substitute(Substitutions subst) {

        //System.out.println("INFO in Clause.instantiate(): " + subst);
        //System.out.println("INFO in Clause.instantiate(): " + this);
        Clause newC = deepCopy();
        newC.literals = new ArrayList<Literal>();
        for (int i = 0; i < literals.size(); i++)
            newC.literals.add(literals.get(i).substitute(subst));
        //System.out.println("INFO in Clause.instantiate(): " + newC);
        return newC;
    }
    
    /** ***************************************************************
     * Return a copy of self with fresh variables.
     */
    public Clause freshVarCopy() {

        ArrayList<Term> vars = collectVars();
        Substitutions s = Substitutions.freshVarSubst(vars);
        subst.addAll(s);
        return substitute(s);
    }

    /** ***************************************************************
     * Return a copy of self with variables that are renumbered from 0,
     * which will make clauses that are equal except for their variable
     * names, syntactically equal. 
     */
    public Clause normalizeVarCopy() {

        ArrayList<Term> vars = collectVars();
        int varCounter = 0;
        Substitutions s = new Substitutions();
        for (int i = 0; i < vars.size(); i++) {
            Term newTerm = new Term();
            newTerm.t = "VAR" + Integer.toString(varCounter++);
            s.addSubst(vars.get(i), newTerm);
        }
        //System.out.println("INFO in Clause.normalizeVarCopy(): subst: " + s);
        Clause c = deepCopy();
        subst.addAll(s);
        return c.substitute(s);
    }
    
    /** ***************************************************************
     * Remove duplicated literals from clause.
     */
    public void removeDupLits() {

        ArrayList<Literal> res = new ArrayList<Literal>();
        for (int i = 0; i < literals.size(); i++) {
            if (!Literal.litInLitList(literals.get(i),res))
                res.add(literals.get(i));
        }
        literals = res;
    }

    /** ***************************************************************
     * Check if a clause is a simple tautology, i.e. if it contains
     * two literals with the same atom, but different signs.
     */
    public boolean isTautology() {

        if (literals.size() < 2)
            return false;
        for (int i = 0; i < literals.size(); i++) {
            for (int j = 1; j < literals.size(); j++) {
                if (literals.get(i).isOpposite(literals.get(j)))
                    return true;
            }
        }
        return false;     
    }
    

    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {

    }

}
