/*
 *     Minimal implementation of the given-clause algorithm for
    saturation of clause sets under the rules of the resolution calculus.
    
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

/** ***************************************************************
 * Top-level data structure for the prover. The complete knowledge
 * base is split into two sets, processed clauses and unprocessed
 * clauses. These are represented here as individual clause sets. The
 * main algorithm "processes" clauses and moves them from the
 * unprocessed into the processed set. Processing typically generates
 * several new clauses, which are direct consequences of the given
 * clause and the processed clauses. These new clauses are added to
 * the set of unprocessed clauses.
 */
public class SimpleProofState {

    ClauseSet unprocessed = new ClauseSet();                                    
    ClauseSet processed = new ClauseSet();

    /** ***************************************************************
     * Initialize the proof state with a set of clauses.  
     */
    public SimpleProofState(ClauseSet clauses) {

        unprocessed.addAll(clauses);
    }
            
    /** ***************************************************************
     * Pick a clause from unprocessed and process it. If the empty
     * clause is found, return it. Otherwise return None.  
     */
    public Clause processClause() {

        Clause given_clause = unprocessed.extractFirst();
        given_clause = given_clause.freshVarCopy();
        System.out.println("#" + given_clause.toStringJustify());
        if (given_clause.isEmpty())    // We have found an explicit contradiction
            return given_clause;
        
        ClauseSet newClauses = new ClauseSet();
        ClauseSet factors = ResControl.computeAllFactors(given_clause);      
        //System.out.println("INFO in SimpleProofState.processClause(): factors: " + factors);

        newClauses.addAll(factors);
        ClauseSet resolvents = ResControl.computeAllResolvents(given_clause, processed);
        //System.out.println("INFO in SimpleProofState.processClause(): resolvents: " + resolvents);

        newClauses.addAll(resolvents);

        processed.add(given_clause);
        //System.out.println("INFO in SimpleProofState.processClause(): processed clauses: " + processed);

        for (int i = 0; i < newClauses.length(); i++) {
            Clause c = newClauses.get(i);
            unprocessed.add(c);
        }
        return null;
    }
    
    /** ***************************************************************
     * Main proof procedure. If the clause set is found
     * unsatisfiable, return the empty clause as a witness. Otherwise
     * return None.  
     */
    public Clause saturate() {

        while (unprocessed.length() != 0) {
            //System.out.println("INFO in SimpleProofState.saturate(): unprocessed clauses: " + unprocessed);
            Clause res = processClause();
            if (res != null)
                return res;
            //else
                //return null;
        }
        return null;
    }
    
    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static String spec1 = "cnf(axiom, a_is_true, a).\n" +
        "cnf(negated_conjecture, is_a_true, ~a).";

    public static String spec2 = "cnf(axiom1, humans_are_mortal, mortal(X)|~human(X)).\n" + 
        "cnf(axiom2, socrates_is_human, human(socrates)).\n" +
        "cnf(negated_conjecture, is_socrates_mortal, ~mortal(socrates)).\n";

    public static String spec3 = "cnf(p_or_q, axiom, p(a)).\n" +
        "cnf(taut, axiom, q(a)).\n" +
        "cnf(not_p, axiom, p(b)).\n";   

    /** ***************************************************************
     * Evaluate the result of a saturation compared to the expected result.
     */
    public static void evalSatResult(String spec, boolean provable) {

        Lexer lex = new Lexer(spec);        
        ClauseSet problem = new ClauseSet();
        problem.parse(lex);

        //System.out.println("SimpleProofState.evalSatResult(): problem: " + problem);
        SimpleProofState prover = new SimpleProofState(problem);
        Clause res = prover.saturate();

        if (provable) {
            if (res == null)
                System.out.println("# Failure: Should have found a proof!");
            else
                System.out.println("# Success: Proof found");
        }
        else {
            if (res != null)
                System.out.println("# Failure: Should not have found a proof!");
            else
                System.out.println("# Success: No proof found");
        }
    }
            
    /** ***************************************************************
     * Test that saturation works.
     */
    public static void testSaturation() {

        evalSatResult(spec1, true);
        evalSatResult(spec2, true);
        evalSatResult(spec3, false);
    }
    
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
        
        testSaturation();
    }
}
