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
        Clause res = prover.saturate(1);

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
        System.out.println(prover.generateStatisticsString());
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

        System.out.println("INFO in ProofStateTest.testSaturation()");
        evalSatResult(spec1, true);
        evalSatResult(ClauseSet.parseFromFile("/home/apease/EProver/fod_pi/PYTHON/EXAMPLES/PUZ001-1.p"), true);
        evalSatResult(spec3, false);
    }
}
