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
package atp;

import java.io.StringReader;
import java.util.*;
import java.text.*;

public class Subsumption {
    
    /** ***************************************************************
     * Try to extend subst so that subst(subsumer) is a multi-subset of
     * subsumed. Recursively call this routine, checking the first literal
     * of subsumer for a match with subsumed, then calling again with 
     * the first literal removed.
     */ 
    private static boolean subsumeLitLists(Clause subsumer, 
            Clause subsumed, BacktrackSubstitution subst) {

        if (subsumer == null || subsumer.literals.size() < 1)
            return true;
        for (int i = 0; i < subsumed.literals.size(); i++) {
            int btstate = subst.getState();
            if (subsumer.literals.get(0).match(subsumed.literals.get(i), subst)) {
                Clause rest = new Clause();
                for (int j = 0; j < subsumer.literals.size(); j++)
                    if (subsumer.literals.get(j) != subsumed.literals.get(i))
                        rest.literals.add(subsumer.literals.get(j));
                if (subsumeLitLists(subsumer.deepCopy(1), rest, subst))
                    return true;
            }
            subst.backtrackToState(btstate);
        }
        return false;
    }
    
    /** ***************************************************************
     * Return True if subsumer subsumes subsumed, False otherwise.
     */ 
    private static boolean subsumes(Clause subsumer, Clause subsumed) {

        if (subsumer.literals.size() > subsumed.literals.size())
            return false;
        BacktrackSubstitution subst = new BacktrackSubstitution();
        return subsumeLitLists(subsumer, subsumed, subst);
    }

    /** ***************************************************************
     * Return True if any clause from set subsumes clause, False otherwise.
     */ 
    public static boolean forwardSubsumption(ClauseSet cs, Clause clause) {

        for (int i = 0; i < cs.length(); i++) {
            Clause c = cs.get(i);
            if (subsumes(c, clause))
                return true;
        }
        return false;
    }

    /** ***************************************************************
     * Remove all clauses that are subsumed by clause from set.
     */ 
    public static int backwardSubsumption(Clause clause, ClauseSet cs) {

        ArrayList<Clause> subsumed_set = new ArrayList<Clause>();
        for (int i = 0; i < cs.length(); i++) {
            Clause c = cs.get(i);
            if (subsumes(clause, c))
                subsumed_set.add(c);        
        }
        int res = subsumed_set.size();
        for (int i = 0; i < subsumed_set.size(); i++) {
            Clause c = subsumed_set.get(i);
            if (c.supportsClauses.size() == 0) // make sure that clauses that support others are not removed.
            	cs.extractClause(c);
        }
        return res;
    }

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static Clause c1 = null;
    public static Clause c2 = null;
    public static Clause c3 = null;
    public static Clause c4 = null;
    public static Clause c5 = null;
    
    /** ***************************************************************
     * Setup function for resolution testing
     */ 
    public static void setup() {

        String spec = "cnf(axiom, c1, $false).\n" +
                    "cnf(axiom, c2, p(a)).\n" +
                    "cnf(axiom, c3, p(X)).\n" +
                    "cnf(axiom, c4, p(a)|q(f(X))).\n" +
                    "cnf(axiom, c5, p(a)|q(f(b))|p(X)).\n";

        Lexer lex = new Lexer(spec);
            c1 = Clause.parse(lex);
            System.out.println(c1);
            c2 = Clause.parse(lex);
            System.out.println(c2);
            c3 = Clause.parse(lex);
            System.out.println(c3);
            c4 = Clause.parse(lex);
            System.out.println(c4);
            c5 = Clause.parse(lex); 
            System.out.println(c5);
    }
    
    /** ***************************************************************
     * Test subsumption.
     */ 
    public static void testSubsumption() {

        System.out.println("---------------------");
        System.out.println("INFO in Subsumption.testSubsumption(): all should be true");
        System.out.println(c1 + " subsumes " + c1 + " = " + subsumes(c1,c1));
        System.out.println(c2 + " subsumes " + c2 + " = " + subsumes(c2,c2));
        System.out.println(c3 + " subsumes " + c3 + " = " + subsumes(c3,c3));
        System.out.println(c4 + " subsumes " + c4 + " = " + subsumes(c4,c4));
        System.out.println(c1 + " subsumes " + c2 + " = " + subsumes(c1,c2));
        System.out.println(c2 + " does not subsume " + c1 + " = " + !subsumes(c2,c1));
        System.out.println(c2 + " does not subsume " + c3 + " = " + !subsumes(c2,c3));
        System.out.println(c3 + " subsumes " + c2 + " = " + subsumes(c3,c2));
        System.out.println(c4 + " subsumes " + c5 + " = " + subsumes(c4,c5));        
        System.out.println(c5 + " does not subsume " + c4 + " = " + !subsumes(c5,c4));
    }
    
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
        
        setup();
        testSubsumption();
    }
}
