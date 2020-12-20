/*
 * This module implements the rules of the simple resolution calculus,
    namely binary resolution and factoring.
    inference rule:

    Binary resolution:

    c1|a1     c2|~a2
    ---------------- where sigma=mgu(a1,a2)
     sigma(c1|c2)

    Note that c1 and c2 are arbitrary disjunctions of literals (each of
    which may be positive or negative). Both c1 and c2 may be empty.  Both
    a1 and a2 are atoms (so a1 and ~a2 are a positive and a negative
    literal, respectively).  Also, since | is AC (or, alternatively, the
    clauses are unordered multisets), the order of literals is irrelevant.

    Clauses are interpreted as implicitly universally quantified
    disjunctions of literals. This implies that the scope of the variables
    is a single clause. In other words, from a theoretical point of view,
    variables in different clauses are different. In practice, we have to
    enforce this explicitly by making sure that all clauses used as
    premises in an inference are indeed variable disjoint.

    Factoring:

       c|a|b
    ----------  where sigma = mgu(a,b)
    sigma(c|a)

    Again, c is an arbitrary disjunction.
    
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
import java.lang.Math.*;

public class Resolution {

    /** ***************************************************************
     * Implementation of the Resolution rule. lit1 and lit2 are indices
     * of literals in clause1 and clause2, respectively, so clause1|lit1
     * and clause2|lit2 are literals.
     * Try to resolve clause1|lit1 against clause2|lit2. If this is
     * possible, return the resolvent. Otherwise, return None.
     */
    public static Clause resolution(Clause clause1, int lit1, Clause clause2, int lit2) {

        //System.out.println("INFO in Resolution.resolution(): resolving (clause1, lit1, clause2, lit2): " + 
        //        clause1 + " " + lit1 + " " + clause2 + " " + lit2);
        Literal l1 = clause1.getLiteral(lit1);
        assert l1 != null;
        Literal l2 = clause2.getLiteral(lit2);
        assert l2 != null;
        if (l1 == null || l2 == null)
            System.out.println("Error in Resolution.resolution(): literals are null " + l1 + " " + l2);
        if (l1.isNegative() == l2.isNegative())
            return null;
        //System.out.println("INFO in Resolution.resolution():l1 is negative: " + l1.isNegative());
        //System.out.println("INFO in Resolution.resolution():l2 is negative: " + l2.isNegative());
        Substitutions sigma = null;
       	sigma = Unification.mgu(l1.atom, l2.atom);
        //System.out.println("INFO in Resolution.resolution(): sigma " + sigma);
        if (sigma == null)
            return null;
        ArrayList<Literal> lits1 = new ArrayList<Literal> ();
       
        //System.out.println("INFO in Resolution.resolution(): clause1 size " + clause1.literals.size());
        //System.out.println("INFO in Resolution.resolution(): literal 11 " + l1);
        for (int i = 0; i < clause1.literals.size(); i++) {
            Literal l = clause1.literals.get(i); 
            //System.out.println("INFO in Resolution.resolution(): literal " + l);
            if (!l.equals(l1))
                lits1.add(l.substitute(sigma));
            //System.out.println("INFO in Resolution.resolution(): literals " + lits1);
        }
        
        //System.out.println("INFO in Resolution.resolution(): clause2 size " + clause2.literals.size());
        //System.out.println("INFO in Resolution.resolution(): literal 12 " + l2);
        ArrayList<Literal> lits2 = new ArrayList<Literal> ();
        for (int i = 0; i < clause2.literals.size(); i++) {
            Literal l = clause2.literals.get(i); 
            //System.out.println("INFO in Resolution.resolution(): literal " + l);
            if (!l.equals(l2))
                lits2.add(l.substitute(sigma));
            //System.out.println("INFO in Resolution.resolution(): literals " + lits1);
        }
        //System.out.println("INFO in Resolution.resolution(): uncombined literals " + lits1 + " " + lits2);
        lits1.addAll(lits2);
        //System.out.println("INFO in Resolution.resolution(): combined literals " + lits1);
        Clause res = new Clause();
        res.createName();
        res.addAll(lits1);
        res.removeDupLits();
        res.rationale = "resolution";
        res.support.add(clause1.name);
        res.support.add(clause2.name);
        clause1.supportsClauses.add(res.name);  // Keep track of clauses used to supported others
        clause2.supportsClauses.add(res.name);  // so they can't be subsumed away.
        res.depth = Math.max(clause1.depth,clause2.depth) + 1; 
        res.subst.addAll(sigma);
        //System.out.println("INFO in Resolution.resolution(): result " + res.toStringJustify());
        return res;
    }

    /** ***************************************************************
     * Check if it is possible to form a factor between lit1 and lit2. If
     * yes, return it, otherwise return None.
     */
    public static Clause factor(Clause clause, int lit1, int lit2) {

        //System.out.println("INFO in Resolution.factor(): " + clause + " " + lit1 + " " + lit2);
        //System.out.println("INFO in Resolution.factor(): " + clause.getLiteral(lit1) + " " + clause.getLiteral(lit2));
        Literal l1 = clause.getLiteral(lit1);
        Literal l2 = clause.getLiteral(lit2);
        if (l1.isNegative() != l2.isNegative())
            return null;
        Substitutions sigma = null;
       	sigma = Unification.mgu(l1.atom, l2.atom);
        if (sigma == null)
            return null;
        ArrayList<Literal> lits = new ArrayList<Literal>();
        for (int i = 0; i < clause.literals.size(); i++) {
            Literal l = clause.literals.get(i); 
            //if (!l.equals(l2))
            lits.add(l.substitute(sigma));
        }
        Clause res = new Clause();
        res.createName();
        res.addAll(lits);
        res.removeDupLits();
        res.rationale = "factoring";
        res.support.add(clause.name);
        return res;
    }

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */    
    public static Clause c1 = new Clause();
    public static Clause c2 = new Clause();
    public static Clause c3 = new Clause();
    public static Clause c4 = new Clause();
    public static Clause c5 = new Clause();
    public static Clause c6 = new Clause();
    public static Clause c7 = new Clause();
    public static Clause c8 = new Clause();
    public static Clause c9 = new Clause();
    public static Clause c10 = new Clause();
    public static Clause c11 = new Clause();
    public static Clause c12 = new Clause();
    public static Clause c13 = new Clause();
    
    /** ***************************************************************
     * Setup function for resolution testing
     */
    public static void setup() {

       String spec = "cnf(c1,axiom,p(a, X)|p(X,a)).\n" +
           "cnf(c2,axiom,~p(a,b)|p(f(Y),a)).\n" +
           "cnf(c3,axiom,p(Z,X)|~p(f(Z),X0)).\n" +
           "cnf(c4,axiom,p(X,X)|p(a,f(Y))).\n" +
           "cnf(ftest,axiom,p(X)|~q|p(a)|~q|p(Y))."; 
       Lexer lex = new Lexer(spec);
       c1.parse(lex);
       c2.parse(lex);
       c3.parse(lex);
       c4.parse(lex);
       c5.parse(lex);
       System.out.println("Resolution.setup(): expected clauses:");
       System.out.println(spec);
       System.out.println("actual:");
       System.out.println(c1);
       System.out.println(c2);
       System.out.println(c3);
       System.out.println(c4);
       System.out.println(c5); 
       
       String spec2 = "cnf(not_p,axiom,~p(a)).\n" + 
       "cnf(taut,axiom,p(X4)|~p(X4)).\n";
       lex = new Lexer(spec2);
       c6.parse(lex);
       c7.parse(lex);
       
       String spec3 = "cnf(00019,plain,disjoint(X212, null_class))." +
       "cnf(00020,plain,~disjoint(X271, X271)|~member(X270, X271))." +
       "cnf(c00025,axiom,( product(X1,X1,X1) ))." +
       "cnf(c00030,plain, ( ~ product(X354,X355,e_1) | ~ product(X354,X355,e_2) ))." +
       "cnf(c00001,axiom,~killed(X12, X13)|hates(X12, X13))." +
       "cnf(c00003,axiom,~killed(X3, X4)|~richer(X3, X4)).";
       lex = new Lexer(spec3);
       c8.parse(lex);
       c9.parse(lex);
       c10.parse(lex);
       c11.parse(lex);
       c12.parse(lex);
       c13.parse(lex);
    }
    
    /** ***************************************************************
     * Test resolution
     */
    public static void testResolution() {
        
        System.out.println("Resolution.testResolution()");
        
        Clause res1 = resolution(c1, 0, c2,0);
        assert res1 != null;
        System.out.println("expected result: cnf(c1,plain,p(b,a)|p(f(Y),a)). result: " + res1);

        Clause res2 = resolution(c1, 0, c3,0);
        assert res2 == null;
        System.out.println("Resolution.testResolution(): successful (null) result: " + res2);

        Clause res3 = resolution(c2, 0, c3,0);
        assert res3 != null;
        System.out.println("Expected result: cnf(c2,plain,p(f(Y),a)|~p(f(a),X0)). result: " + res3);

        Clause res4 = resolution(c1, 0, c3,1);
        assert res4 == null;
        System.out.println("Resolution.testResolution(): successful (null) result: " + res4);        
        
        Clause res5 = resolution(c6, 0, c7,0);
        assert res5 != null;
        System.out.println("Resolution.testResolution(): cnf(~p(a)) successful result: " + res5);
        
        Clause res6 = resolution(c8, 0, c9,0);
        assert res6 != null;
        System.out.println("Resolution.testResolution(): ~member(X270, null_class) successful result: " + res6);
        
        Clause res7 = resolution(c10, 0, c11,0);
        assert res7 != null;
        System.out.println("Resolution.testResolution(): ~product(e_1,e_1,e_2) successful result: " + res7);
        
        Clause res8 = resolution(c12, 0, c13,0);
        assert res8 != null;
        System.out.println("Resolution.testResolution(): successful null result: " + res8);
    }
    
    /** ***************************************************************
     * Test the factoring inference.
     */
    public static void testFactoring() {
  
        System.out.println("Resolution.testFactoring()");
        Clause f1 = factor(c1,0,1);
        assert f1 != null;
        assert f1.length()==1;
        System.out.println("Expected result: cnf(c0,plain,p(a,a)). Factor:" + f1);
        
        Clause f2 = factor(c2,0,1);
        assert f2 == null;
        System.out.println("Resolution.testFactoring(): successful (null) result: Factor:" + f2);

        Clause f4 = factor(c4,0,1);
        assert f4 == null;
        System.out.println("Resolution.testFactoring(): successful (null) result: Factor:" + f4);
        
        Clause f5 = factor(c5,1,3);
        assert f5 == null;
        System.out.println("Resolution.testFactoring(): Expected result: cnf(c2,plain,p(X)|~q|p(a)|p(Y)). Factor:" + f5);
    }
    
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
        
        setup();
        testResolution();
        testFactoring();
    }
}
