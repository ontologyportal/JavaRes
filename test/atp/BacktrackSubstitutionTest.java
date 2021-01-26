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
import org.junit.runners.MethodSorters;

import java.util.*;
import static org.junit.Assert.*;

public class BacktrackSubstitutionTest {
    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    private static Term s1 = null;
    private static Term s2 = null;
    private static Term s3 = null;
    private static Term s4 = null;
    private static Term s5 = null;
    private static Term s6 = null;
    private static Term s7 = null;
    private static Term t1 = null;
    private static Term t2 = null;
    private static Term t3 = null;
    private static Term t4 = null;
    private static Term t5 = null;
    private static Term t6 = null;
    private static Term t7 = null;

    /** ***************************************************************
     */
    @BeforeClass
    public static void setUp() {

        s1 = Term.string2Term("X");
        t1 = Term.string2Term("a");

        s2 = Term.string2Term("X");
        t2 = Term.string2Term("f(X)");

        s3 = Term.string2Term("X");
        t3 = Term.string2Term("f(Y)");

        s4 = Term.string2Term("f(X, a)");
        t4 = Term.string2Term("f(b, Y)");

        s5 = Term.string2Term("f(X, g(a))");
        t5 = Term.string2Term("f(X, Y))");

        s6 = Term.string2Term("f(X, g(a))");
        t6 = Term.string2Term("f(X, X))");

        s7 = Term.string2Term("g(X)");
        t7 = Term.string2Term("g(f(g(X),b))");
    }

    /** ***************************************************************
     * Test if s can be matched onto t. If yes, report the
     * result. Compare to the expected result.
     */
    public static void match_test(boolean noRec, Term s, Term t, boolean success_expected) {

        System.out.println("INFO in BacktrackSubstitution.match_test(): Trying to match " + s + " onto " + t);
        System.out.println("INFO in BacktrackSubstitution.match_test(): noRec " + noRec + " success expected " + success_expected);
        BacktrackSubstitution sigma = new BacktrackSubstitution();
        boolean res = false;
        if (noRec)
            res = sigma.match_norec(s,t);
        else
            res = sigma.match(s,t);
        if (success_expected) {
            if (!res) {
                System.out.println("failure");
                assertTrue(res);
            }
            if (!sigma.apply(s).equals(t)) {
                System.out.println("failure");
                assertEquals(t,sigma.apply(s));
            }
            System.out.println("match_test(): success: " + sigma.apply(s) + " " + t + " " + sigma);
        }
        else {
            if (res) {
                System.out.println("Failure: found a false match: " + res);
                assertTrue(false);
            }
            else
                System.out.println("match_test(): success - no match found, as expected");
        }
    }

    /** ***************************************************************
     * Test Matching.
     */
    @Test
    public void testMatch() {

        match_test(false, s1, t1, true);
        match_test(false, s2, t2, true);
        match_test(false, s3, t3, true);
        match_test(false, s4, t4, false);
        match_test(false, s5, t5, false);
        match_test(false, s6, t6, false);
        match_test(false, s7, t7, true);

        match_test(false, t1, s1, false);
        match_test(false, t2, s2, false);
        match_test(false, t3, s3, false);
        match_test(false, t4, s4, false);
        match_test(false, t5, s5, true);
        match_test(false, t6, s6, false);
        match_test(false, t7, s7, false);
    }

    /** ***************************************************************
     * Test Matching.
     */
    @Test
    public void testMatchNoRec() {

        match_test(true, s1, t1, true);
        match_test(true, s2, t2, true);
        match_test(true, s3, t3, true);
        match_test(true, s4, t4, false);
        match_test(true, s5, t5, false);
        match_test(true, s6, t6, false);
        match_test(true, s7, t7, true);

        match_test(true, t1, s1, false);
        match_test(true, t2, s2, false);
        match_test(true, t3, s3, false);
        match_test(true, t4, s4, false);
        match_test(true, t5, s5, true);
        match_test(true, t6, s6, false);
        match_test(true, t7, s7, false);
    }

    /** ***************************************************************
     * Test backtrackable substitutions.
     */
    @Test
    public void testBacktrack() {

        System.out.println("---------------------");
        System.out.println("INFO in testBacktrack()");
        BacktrackSubstitution sigma = new BacktrackSubstitution();
        int state = sigma.getState();
        sigma.addBinding(Term.string2Term("X"), Term.string2Term("f(Y)"));
        int res = sigma.backtrackToState(state);
        if (res == 1)
            System.out.println("INFO in testBacktrack(): correct number of states");
        else
            System.out.println("INFO in testBacktrack(): error correct number of states (expected 1): " + res);
        assertEquals(1,res);
        boolean success = sigma.backtrack();
        if (!success)
            System.out.println("INFO in testBacktrack(): correct empty stack");
        else
            System.out.println("INFO in testBacktrack(): failure, non-empty stack");
        assertFalse(success);
    }


}
