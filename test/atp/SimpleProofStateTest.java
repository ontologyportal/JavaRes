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

public class SimpleProofStateTest {

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
     * Test that saturation works.
     */
    @Test
    public void testSaturation() {

        evalSatResult(spec1, true);
        evalSatResult(spec2, true);
        evalSatResult(spec3, false);
    }
}
