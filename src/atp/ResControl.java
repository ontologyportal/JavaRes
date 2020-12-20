/*
 * Functions wrapping basic inference rules for convenience.
    
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

public class ResControl {
 
    /** ***************************************************************
     * Compute all binary resolvents between a given clause and all
     * clauses in clauseset. This is used when integrating a new clause 
     * into the processed part of the proof state, where all possible
     * resolvents between the new clause and the already processed
     * clauses are computed. [Note: Explain  better]  
     */
    public static ClauseSet computeAllResolvents(Clause clause, ClauseSet clauseset) {

        ClauseSet res = new ClauseSet();
        for (int lit = 0; lit < clause.length(); lit++) {
            ArrayList<Clause> clauseres = new ArrayList<Clause>();
            ArrayList<Integer> indices = new ArrayList<Integer>();
            clauseset.getResolutionLiterals(clause.getLiteral(lit),clauseres,indices);
            assert clauseres.size() == indices.size();
            for (int i = 0; i < clauseres.size(); i++) {               
                Clause resolvent = Resolution.resolution(clause, lit, clauseres.get(i), indices.get(i).intValue());
                if (resolvent != null)
                    res.add(resolvent);
            }
        }
        return res;
    }

    /** ***************************************************************
     * Compute all (direct) factors of clause. This operation is O(n^2)
     * if n is the number of literals. However, factoring is nearly never
     * a critical operation. Single-clause operations are nearly always
     * much cheaper than clause/clause-set operations.  
     */
    public static ClauseSet computeAllFactors(Clause clause) {

        ClauseSet res = new ClauseSet();
        for (int i = 0; i < clause.length(); i++) {
            for (int j = i+1; j < clause.length(); j++) {
                Clause fact = Resolution.factor(clause, i, j);
                //System.out.println("INFO in ResControl.computeAllFactors(): adding factor: " + fact);
                if (fact != null)
                    res.add(fact);
            }
        }
        return res;
    }
    
    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static ClauseSet cset = new ClauseSet();
    public static Clause conj = new Clause();
    public static Clause fclause = new Clause();
    
    /** ***************************************************************
     * Setup function for clause/literal unit tests. Initialize
     * variables needed throughout the tests.
     */
    public static void setup() {

        System.out.println();
        String spec = "cnf(g1, negated_conjecture, ~c).\n" +
            "cnf(c1, axiom, a|b|c).\n" +
            "cnf(c2, axiom, b|c).\n" +
            "cnf(c3, axiom, c).";
   
        Lexer lex = new Lexer(spec);
        conj.parse(lex);
        cset.parse(lex);

        String cstr = "cnf(ftest, axiom, p(X)|~q|p(a)|~q|p(Y)).";
        lex = new Lexer(cstr);
        fclause.parse(lex);
    }
           
    /** ***************************************************************
     * Test that forming resolvents between a clause and a clause set
     * works.
     */
    public static void testSetResolution() {

        System.out.println("ResControl.testSetResolution()");
        ClauseSet res = computeAllResolvents(conj,cset);
        System.out.println("Should see [cnf(c4,plain,a|b)., cnf(c5,plain,b)., cnf(c6,plain,$false).]");         
        System.out.println(res);
    }
         
    /** ***************************************************************
     * Test full factoring of a clause.
     */
    public static void testFactoring() {
        
        System.out.println("testFactoring()");
        ClauseSet res = computeAllFactors(fclause);
        System.out.println("should see [cnf(c0,plain,p(a)|~q|p(Y))., cnf(c1,plain,p(Y)|~q|p(a))., cnf(c2,plain,p(X)|~q|p(a)|p(Y))., cnf(c3,plain,p(X)|~q|p(a)).]");
        System.out.println(res);
    }
    
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
        
        setup();
        testSetResolution();
        testFactoring();
    }
}
