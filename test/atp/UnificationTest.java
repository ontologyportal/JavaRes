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

public class UnificationTest {

    /** ***************************************************************
     *      * ************ UNIT TESTS *****************
     * Test basic substitution functions.
     */
    public static Term s1 = null;
    public static Term t1 = null;

    public static Term s2 = null;
    public static Term t2 = null;

    public static Term s3 = null;
    public static Term t3 = null;

    public static Term s4 = null;
    public static Term t4 = null;

    public static Term s5 = null;
    public static Term t5 = null;

    public static Term s6 = null;
    public static Term t6 = null;

    public static Term s7 = null;
    public static Term t7 = null;

    public static Term s8 = null;
    public static Term t8 = null;

    public static Term s9 = null;
    public static Term t9 = null;

    public static Term s10 = null;
    public static Term t10 = null;

    public static Term s11 = null;
    public static Term t11 = null;

    @BeforeClass
    public static void setup() {

        s1 = Term.string2Term("X");
        System.out.println("INFO in Unification.setup(): " + s1);
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

        s8 = Term.string2Term("p(X,X,X)");
        t8 = Term.string2Term("p(Y,Y,e)");

        s9 = Term.string2Term("f(f(g(X),a),X)");
        t9 = Term.string2Term("f(Y,g(Y))");

        s10 = Term.string2Term("f(f(g(X),a),g(X))");
        t10 = Term.string2Term("f(Y,g(Z))");

        s11 = Term.string2Term("p(X,g(a), f(a, f(a)))");
        t11 = Term.string2Term("p(f(a), g(Y), f(Y, Z))");
    }

    /** ***************************************************************
     * Test if s and t can be unified. If yes, report the
     * result. Compare to the expected result..
     */
    public void unifTest(Term s, Term t, boolean successExpected) {

        System.out.println("INFO in Unification.unifTest() Trying to unify " + s + " and " + t);
        Substitutions sigma = Unification.mgu(s,t);
        if (successExpected) {
            if (sigma != null) {
                if (sigma.apply(s).equals(sigma.apply(t)))
                    System.out.println("Success " + sigma.apply(s) + " " + sigma.apply(t) + " " + sigma);
                else
                    System.out.println("Failure, " + sigma + "doesn't unify " + s + " and " + t +
                            ". " + sigma.apply(s) + "!=" + sigma.apply(t));
            }
            else {
                System.out.println("Failure, sigma is null");
                assertFalse(sigma != null);
            }
        }
        else {
            if (sigma == null)
                System.out.println("Success, sigma is null ");
            else {
                System.out.println("Failure, sigma is not null " + sigma);
                assertFalse(sigma == null);
            }
        }
        System.out.println();
    }

    /** ***************************************************************
     * Test basic stuff.
     */
    @Test
    public  void testMGU() {

        System.out.println("-----------------------------------");
        System.out.println("INFO in Unification.testMGU() ");
        System.out.println(s1);
        System.out.println(t1);

        unifTest(s1, t1, true);
        unifTest(s2, t2, false);
        unifTest(s3, t3, true);
        unifTest(s4, t4, true);
        unifTest(s5, t5, true);
        unifTest(s6, t6, true);
        unifTest(s7, t7, false);
        unifTest(s8, t8, true);
        unifTest(s9, t9, false);
        unifTest(s10, t10, true);
        unifTest(s11, t11, true);

        // Unification should be symmetrical
        unifTest(t1, s1, true);
        unifTest(t2, s2, false);
        unifTest(t3, s3, true);
        unifTest(t4, s4, true);
        unifTest(t5, s5, true);
        unifTest(t6, s6, true);
        unifTest(t7, s7, false);
        unifTest(t8, s8, true);
        unifTest(t9, s9, false);
        unifTest(t10, s10, true);
        unifTest(t11, s11, true);
    }
}
