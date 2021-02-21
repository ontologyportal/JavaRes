/*
 A simple implementation of first-order terms. 
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
A composite term f(t1, ..., tn) is represented by the list
[f lt1, ..., ltn], where lt1, ..., ltn are lists representing the
subterms.
"X"          -> "X"
"g(X, f(Y))" -> ["g", "X", ["f", "Y"]]
"g(a,b)"      -> ["g", ["a"], ["b"]]
*/
public class Term implements Comparable {
    
public String t = "";  // lowercase is a constant, uppercase is a variable
public ArrayList<Term> subterms = new ArrayList<Term>();    // empty if not composite

	/** ***************************************************************
	 */
	public Term() {

	}

    /** ***************************************************************
     */
    public Term(Term newTerm) {
        this.t = newTerm.t;
        if (newTerm.subterms != null && newTerm.subterms.size() > 0) {
            for (Term subT : newTerm.subterms)
                subterms.add(new Term(subT));
        }
    }

	/** ***************************************************************
	 */
	public Term(String op, Term t1, Term t2) {
		t = op;
		if (t1 != null)
		    subterms.add(t1);
        if (t2 != null)
		    subterms.add(t2);
	}
	
    /** ***************************************************************
     * @param s An input Object, expected to be a String.
     * @return true if s == null or s is an empty String, else false.
     */
    public static boolean emptyString(Object s) {
    
        return ((s == null) || 
                ((s instanceof String) && s.equals("")));
    }
    
    /** ***************************************************************
     */
    public String toString() {
            
        StringBuffer result = new StringBuffer();
        result.append(t);
        if (subterms.size() > 0) {
            result.append('(');
            for (int i = 0; i < subterms.size(); i++) {
                result.append(subterms.get(i).toString());
                if (i < subterms.size()-1)
                    result.append(",");
            }
            result.append(')');     
        }
        return result.toString();
    }

    /** ***************************************************************
     */
    public int compareTo(Object o) {

        //System.out.println("Term.compareTo(): " + o.getClass().getName());
        if (!o.getClass().getName().equals("atp.Term"))
            throw new ClassCastException();
        Term ot = (Term) o;
        if (t != ot.t) {
            return t.compareTo(ot.t);
        }
        else {
            for (int i = 0; i < subterms.size(); i++)
                if (!subterms.get(i).equals(ot.subterms.get(i)))
                    return subterms.get(i).compareTo(ot.subterms.get(i));
        }
        return 0;
    }

    /** ***************************************************************
     */
    public String toKIFString() {

        //System.out.println("Term.toKIFString(): " + this);
        //System.out.println("Term.toKIFString(): t: " + t);
        //System.out.println("Term.toKIFString(): subterms: " + subterms);
        StringBuffer result = new StringBuffer();
        if (subterms.size() > 0) 
            result.append('(');
        if (this.isVar())
            result.append("?" + t);
        else {
            if (KIF.opMap.containsKey(t))
                result.append(KIF.opMap.get(t));
            else
                result.append(t);
        }
        if (subterms.size() > 0) {
            result.append(" ");
            for (int i = 0; i < subterms.size(); i++) {
                Term subt = subterms.get(i);
                if (subt.isVar())
                    result.append("?" + subt);
                else
                    result.append(subt.toKIFString());
                if (i < subterms.size()-1)
                    result.append(" ");
            }
            result.append(')');
        }
        //System.out.println("Term.toKIFString(): result: " + result);
        return result.toString();
    }
    
    /** ***************************************************************
     */
    public Term parseTermList(Lexer lex) {
               
        try {
            //System.out.println("in Term.parseTermList(): " + lex.literal);
            Term newT = new Term();            
            subterms.add(newT.parse(lex));
            lex.next();
            while (lex.literal.equals(",")) {
                newT = new Term();
                subterms.add(newT.parse(lex));
                lex.next();
                //System.out.println("in Term.parseTermList(): next token: " + lex.literal);
            }
            return this;
        }
        catch (Exception ex) {
            System.out.println("Error in Term.parseTermList(): " + ex.getMessage());
            System.out.println("Error in Term.parseTermList(): token:" + lex.literal);            
            ex.printStackTrace();
        }
        return null;
    }

    /** ***************************************************************
     * This routine expects the tokenizer to be set before the starting token.
     * A term is either a variable or a function, where a function can have
     * 0 arguments, and therefore be just a constant.
     */
    public Term parse(Lexer lex) {
               
        try {   
            //System.out.println("INFO in Term.parse(): before next token: " + lex.literal);
            lex.next(); 
            //if (!lex.type.equals(Lexer.IdentLower) && !lex.type.equals(Lexer.IdentUpper))
                //lex.next();
            //System.out.println("INFO in Term.parse(): after next token: " + lex.literal);
            if (!lex.type.equals(Lexer.IdentLower) && !lex.type.equals(Lexer.IdentUpper) &&
                !lex.type.equals(Lexer.DefFunctor) && !lex.type.equals(Lexer.SQString) &&
                !lex.type.equals(Lexer.DQString) && !lex.type.equals(Lexer.Number))
                throw new ParseException("Error in Term.parse(): Expected a word. Found " + 
                        lex.literal + " type: " + lex.type, lex.input.getLineNumber());
            if (lex.type.equals(Lexer.IdentUpper)) {
                t = lex.literal;
                return this;
            }
            else {
                if (lex.type.equals(Lexer.IdentLower) || lex.type.equals(Lexer.SQString) ||  lex.type.equals(Lexer.DQString) || lex.type.equals(Lexer.DefFunctor)) {
                    //System.out.println("lower case term: " + lex.literal);
                    t = lex.literal;
                    if (lex.look().equals("(")) {
                        lex.next();
                        parseTermList(lex); 
                        if (!lex.literal.equals(")"))
                            throw new ParseException("Error in Term.parse(): Close paren expected. Found " + 
                                    lex.literal + " " + lex.type,lex.input.getLineNumber()); 
                        //System.out.println("INFO in Term.parse(): got close paren: " + lex.literal);
                        return this;
                    }
                    else {
                        return this;
                    }
                }
                else {
                    // if (lex.literal.equals("$false"))
                    if (lex.type.equals(Lexer.DefFunctor) || lex.type.equals(Lexer.SQString) ||  lex.type.equals(Lexer.DQString) ||
                            lex.type.equals(Lexer.Number))
                        t = lex.literal;
                    else
                        if (lex.type == Lexer.EOFToken)
                            return this;
                        else
                            throw new ParseException("Error in Term.parse(): Identifier " + lex.literal + " with type " + lex.type + 
                                    " doesn't start with upper or lower case letter.",lex.input.getLineNumber()); 
                }                  
            }                
        }
        catch (ParseException ex) {
            if (lex.literal == lex.EOFToken)
                return this;
            System.out.println("Error in Term.parse(): " + ex.getMessage());
            System.out.println("Error in Term.parse(): word token:" + lex.literal); 
            System.out.println("encountered at line: " + ex.getErrorOffset());
            System.out.println("in file: " + lex.filename);
            ex.printStackTrace();
        }
        //System.out.println("Term.parse(): returning: " + this);
        return this;
    }

    /** ***************************************************************
     * This routine expects the tokenizer to be set before the starting token.
     * A term is either a variable or a function, where a function can have
     * 0 arguments, and therefore be just a constant.  But in this first
     * pass, we treat functional terms as Literals, and disambiguate them
     * on a second pass through the resulting tree of pseudo-Literals and atomic Terms.
     * parseKIF() will not result in any subterms
     */
    public Term parseKIF(KIFLexer lex) {

        try {
            lex.next();
            if (lex.type == KIFLexer.IdentUpper || lex.type == KIFLexer.IdentLower ||
                    lex.type == KIFLexer.RegularVar || lex.type == KIFLexer.RowVar ||
                    lex.type == KIFLexer.DQString || lex.type == KIFLexer.Number)
                return this;
            lex.look();
        }
        catch (ParseException ex) {
            if (lex.literal == lex.EOFToken)
                return this;
            System.out.println("Error in Term.parseKIF(): " + ex.getMessage());
            System.out.println("Error in Term.parseKIF(): word token:" + lex.literal);
            System.out.println("encountered at line: " + ex.getErrorOffset());
            System.out.println("in file: " + lex.filename);
            ex.printStackTrace();
        }
        //System.out.println("Term.parseKIF(): returning: " + this);
        return this;
    }

    /** ***************************************************************
     */
    public static Term string2Term(String s) {
        
        Term t = new Term();
        Lexer lex = new Lexer(s);
        return t.parse(lex);
    }

    /** ***************************************************************
     */
    public static Term kifString2Term(String s) {

        Term t = new Term();
        KIFLexer lex = new KIFLexer(s);
        return t.parseKIF(lex);
    }

    /** ***************************************************************
     * Check if the term is a variable. This assumes that t is a
     * well-formed term.
     */
    public boolean isVar() {
       
        return Character.isUpperCase(t.charAt(0));
    }
    
    /** ***************************************************************
     * Check if the term is a compound term. This assumes that t is a
     * well-formed term.
     */
    public boolean isCompound() {

        return !isVar();
    }
    
    /** ***************************************************************
     * Return True if term has no variables, False otherwise
     */
    public boolean isGround() { 
        
        if (!Term.emptyString(t) && Character.isUpperCase(t.charAt(0)))
            return false;
        for (int i = 0; i < subterms.size(); i++)
            if (!subterms.get(i).isGround())
                return false;
        return true;
    }
    
    /** ***************************************************************
     */
    public LinkedHashSet<Term> collectVars() {

        LinkedHashSet<Term> result = new LinkedHashSet<Term>();
        if (isVar())
            result.add(this);
        for (int i = 0; i < subterms.size(); i++) {
            LinkedHashSet<Term> newvars = subterms.get(i).collectVars();
        	for (Term newv : newvars) {
        		if (!result.contains(newv))
        			result.add(newv);
        	}
        }
        return result;
    }
    
    /** ***************************************************************
     * Return all function symbols
     */
    public ArrayList<String> collectFuns() {

    	ArrayList<String> res = new ArrayList<String>();
        if (isCompound()) {
            res.add(t);
            for (Term s : subterms) {
            	ArrayList<String> newfuns = s.collectFuns();
            	for (String news : newfuns) {
            		if (!res.contains(news))
                        res.add(news);
            	}
            }
        }
        return res;
    }
    
    /** ***************************************************************
     */
    public ArrayList<String> getConstantStrings() {
    
        ArrayList<String> result = new ArrayList<String>();
        if (Character.isLowerCase(t.charAt(0)))
            result.add(t);
        for (int i = 0; i < subterms.size(); i++) {
            ArrayList<String> temp = subterms.get(i).getConstantStrings();
            if (temp != null)
                result.addAll(temp);
        }
        return result;
    }
    
    /** ***************************************************************
     */
    public String getFunc() {
               
        return t;
    }
    
    /** ***************************************************************
     */
    public ArrayList<Term> getArgs() {
               
        return subterms;
    }

    /** ***************************************************************
     *  Insert all function symbols and their associated arities in t into
     *  the signature       
     */
    public Signature collectSig(Signature sig) {

        if (isCompound()) {
            sig.addFun(getFunc(), subterms.size());
            for (Term s:getArgs())
                sig = s.collectSig(sig);
        }
        return sig;
    }
    
    /** ***************************************************************
     * Return the weight of the term,  counting fweight for each function symbol
     * occurrence, vweight for each variable occurrence. Examples: 
     *                  termWeight(f(a,b), 1, 1) = 3
     *                  termWeight(f(a,b), 2, 1) = 6
     *                  termWeight(f(X,Y), 2, 1) = 4
     *                  termWeight(X, 2, 1)      = 1
     *                  termWeight(g(a), 3, 1)   = 6
     *
     * Note however that we actually implement equal weighting for variables and function symbols
     */
    public int weight(int fweight, int vweight) {
        
        int total = 0;
        if (Character.isUpperCase(t.charAt(0)))
            total = vweight;
        else
            total = fweight;
        for (int i = 0; i < subterms.size(); i++)
            total = total + subterms.get(i).weight(fweight,vweight);
        return total;
    }
    
    /** ***************************************************************
     * Return the subterm of t at position pos (or None if pos is not a 
     * position in term). pos is a list of integers denoting branches, e.g.
     *                 subterm(f(a,b), [])        = f(a,b)
     *                 subterm(f(a,g(b)), [0])    = a
     *                 subterm(f(a,g(b)), [1])    = g(b)
     *                 subterm(f(a,g(b)), [1,0])  = b
     *                 subterm(f(a,g(b)), [3,0])  = None
     * Note that pos will be destroyed.
     */
    public Term subterm(ArrayList<Integer> pos) {

        System.out.println("subterm(): pos: " + pos);
        System.out.println("subterms: " + this.subterms);
        ArrayList<Integer> newpos = new ArrayList<>(pos);
        if (pos.size() == 0)
            return this;
        int index = newpos.remove(0);
        System.out.println("index: " + index);
        if (index > subterms.size())
            return null;
        if (newpos.size() == 0) {
            if (index == 0)
                return (new Term()).parse(new Lexer(t));
            return subterms.get(index-1);
        }
        else {
            if (index == 0)
                return (new Term()).parse(new Lexer(t));
            return subterms.get(index-1).subterm(newpos);
        }
    }
    
    /** ***************************************************************
     */
    @Override public boolean equals(Object other_obj) {
        
    	//System.out.println("Term.equals(): term: " + this + " other: " + other_obj);
        //if (other_obj == null) {
        //	System.out.println("Term.equals() argument is null");
        //	return false;
        //}
       // if (!other_obj.getClass().getName().equals("Term")) {
       // 	System.out.println("Term.equals() passed object not of type Term");
       // 	return false;
       // }
        Term t2 = (Term) other_obj;
        //System.out.println("INFO in Term.equals(): term:" + this + " other: " + other_obj);
        if (!t2.t.equals(t))
            return false;
        if (t2.subterms.size() != subterms.size())
            return false;
        for (int i = 0; i < subterms.size(); i++)
            if (!subterms.get(i).equals(t2.subterms.get(i)))
                return false;
        return true;
    }

    /** ***************************************************************
     */
    public static boolean termListEqual(ArrayList<Term> l1, ArrayList<Term> l2) {

        if (l1.size() != l2.size())
            return false;
        if (l1.size() == 0) // l1 is empty, and so, by the previous test, is l2
            return true;
        for (int i = 0; i< l1.size(); i++) {
            if (!l1.get(i).equals(l2.get(i)))
                return false;
        }
        return true;
    }

    /** ***************************************************************
     */
    @Override public int hashCode() {
    
        int total = 0;
 //       if (negated) 
 //           total = 1;
        if (subterms.size() < 1)
            return total + t.hashCode() * 2;
        else {
            for (int i = 0; i < subterms.size(); i++)
                total = total + subterms.get(i).hashCode();
            return total;
        }
    }
    
    /** ***************************************************************
     */
    public Term deepCopy() {
        
        Term result = new Term();
        result.t = t;
        for (int i = 0; i < subterms.size(); i++)
            result.subterms.add(subterms.get(i).deepCopy());
        return result;
    }
    

}
