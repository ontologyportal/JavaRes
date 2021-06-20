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

This is a Java rewrite of PyRes - https://github.com/eprover/PyRes
*/
package atp;

import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;

public class ProofStateTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static String spec1 = "cnf(axiom, a_is_true, a).\n" +
            "cnf(negated_conjecture, is_a_true, ~a).\n";
    public static String spec3 = "cnf(p_or_q, axiom, p(X)|q(a)).\n" +
            "cnf(taut, axiom, p(X)|~p(X)).\n" +
            "cnf(not_p, axiom, ~p(a)).";
    public static SearchParams params = null;

    /** ***************************************************************
     * Evaluate the result of a saturation compared to the expected result.
     */
    @BeforeClass
    public static void setup() {

        Derivable.disableDerivationOutput();
        ClauseEvaluationFunction.setupEvaluationFunctions();
        params = new SearchParams();
        params.delete_tautologies = true;
    }

    /** ***************************************************************
     * Evaluate the result of a saturation compared to the expected result.
     */
    public static void evalSatResult(ClauseSet cs, boolean provable) {

        System.out.println("ProofStateTest.evalSatResult(): problem: " + cs);
        ProofState prover = new ProofState(cs,params);
        Clause res = prover.saturate(2);
        System.out.println("ProofStateTest.evalSatResult(): result: " + res);
        System.out.println("ProofStateTest.evalSatResult(): expected proof: " + provable);

        System.out.println(prover.generateStatisticsString());

        if (provable) {
            if (res == null)
                System.out.println("# Failure: Should have found a proof!");
            else
                System.out.println("# Success: Proof found");
            assertFalse(res == null);
        }
        else {
            if (res != null)
                System.out.println("# Failure: Should not have found a proof!");
            else
                System.out.println("# Success: No proof found");
            assertFalse(res != null);
        }

    }

    /** ***************************************************************
     * Evaluate the result of a saturation compared to the expected result.
     */
    public static void evalSatResult(String spec, boolean provable) {

        System.out.println("INFO in ProofStateTest.evalSatResult()");
        Lexer lex = new Lexer(spec);
        ClauseSet problem = new ClauseSet();
        problem.parse(lex);
        evalSatResult(problem,provable);
    }

    /** ***************************************************************
     * Test that saturation works.
     */
    @Test
    public void testSaturation() {

        System.out.println("-------------------------------------");
        System.out.println("INFO in ProofStateTest.testSaturation()");
        evalSatResult(spec1, true);
        String tptpDir = System.getenv("TPTP");
        System.out.println("-------");
        evalSatResult(ClauseSet.parseFromFile(tptpDir + "/Problems/PUZ/PUZ001-1.p"), true);
        System.out.println("-------");
        evalSatResult(spec3, false);
        System.out.println();
    }

    /** ***************************************************************
     * Test that the main clause processing method works
     */
    @Test
    public void testProcessClause() {

        String input = "cnf(clause29,negated_conjecture,~street(U)|~way(U)|~lonely(U)|~old(V)|~dirty(V)|~white(V)|~car(V)|~chevy(V)|~event(W)|~barrel(W,V)|~down(W,U)|~in(W,X)|~city(X)|~hollywood(X)|ssSkC0)." +
                "cnf(clause25,negated_conjecture,ssSkC0|in(skc14,skc15)).\n";
        System.out.println("---------------------");
        System.out.println("INFO in ProofStateTest.testProcessClause()");
        try {
            Clause.resetCounter();
            Lexer lex = new Lexer(input);
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println("input: " + cs);
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            SearchParams sp = new SearchParams();
            ProofState state = new ProofState(cs,sp);
            state.verbose = true;
            state.processClause();  // first clause has nothing in 'processed' to work with
            state.processClause();  // the one remaining clause from 'unprocessed' is the given clause and resolves with the one clause in 'processed'
            if (state.unprocessed != null)
                System.out.println("success " + state);
            else {
                System.out.println("fail : # SZS GaveUp");
                System.out.println("processed: " + state.processed);
                System.out.println("unprocessed: " + state.unprocessed);
            }
            assertTrue(state.unprocessed != null);
            System.out.println("------");
            System.out.println("INFO in in ProofStateTest.testProcessClause(): done processing");
            String actual = state.unprocessed.get(0).normalizeVarCopy().toString();
            System.out.println("actual: " + actual);
            String expected = "cnf(c0,plain,~street(VAR0)|~way(VAR0)|~lonely(VAR0)|~old(VAR1)|~dirty(VAR1)|~white(VAR1)|~car(VAR1)|~chevy(VAR1)|~event(skc14)|~barrel(skc14,VAR1)|~down(skc14,VAR0)|~city(skc15)|~hollywood(skc15)|ssSkC0).";
            System.out.println("expected: " + expected);
            if (expected.equals(actual))
                System.out.println("success ");
            else
                System.out.println("fail");
            state.verbose = false;
            assertEquals(expected,actual);
        }
        catch (Exception e) {
            System.out.println("Error in in ProofStateTest.testProcessClause()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
