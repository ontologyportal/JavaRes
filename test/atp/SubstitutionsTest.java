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

public class SubstitutionsTest {
    /** ***************************************************************
     * ************ UNIT TESTS *****************
     * Set up test content.
     */
    static String example1 = "f(X, g(Y))";
    static String example2 = "a";
    static String example3 = "b";
    static String example4 = "f(a, g(a))";
    static String example5 = "f(a, g(b))";
    static String example6 = "X";
    static String example7 = "Y";
    static String example8 = "Z";
    static String example9 = "f(M)";

    static Term t1 = null;
    static Term t2 = null;
    static Term t3 = null;
    static Term t4 = null;
    static Term t5 = null;
    static Term t6 = null;
    static Term t7 = null;
    static Term t8 = null;
    static Term t9 = null;

    static Substitutions s1 = new Substitutions();
    static Substitutions s2 = new Substitutions();
    static Substitutions s3 = new Substitutions();

    /** ***************************************************************
     * Set up test content.
     */
    @BeforeClass
    public static void setupTests() {

        t1 = Term.string2Term(example1);
        t2 = Term.string2Term(example2);
        t3 = Term.string2Term(example3);
        t4 = Term.string2Term(example4);
        t5 = Term.string2Term(example5);
        t6 = Term.string2Term(example6);
        t7 = Term.string2Term(example7);
        t8 = Term.string2Term(example8);
        t9 = Term.string2Term(example9);
        s1.subst.put(t6,t2);   // X->a
        s1.subst.put(t7,t2);   // Y->a
        s2.subst.put(t6,t2);   // X->a
        s2.subst.put(t7,t3);   // Y->b
        s3.subst.put(t8,t9);   // Z->f(M)
    }

    /** ***************************************************************
     * Test basic stuff.
     */
    @Test
    public void testSubstBasic() {

        System.out.println("---------------------");
        System.out.println("INFO in testSubstBasic()");
        Substitutions tau = s1.deepCopy();
        System.out.println("should be true: " + s1 + " equals " + tau + " " + s1.equals(tau));
        assertEquals(s1,tau);
        System.out.println("should be true.  Value: " + tau.apply(t6).equals(s1.apply(t6)));
        assertEquals(tau.apply(t6),s1.apply(t6));
        System.out.println("should be true.  Value: " + tau.apply(t7).equals(s1.apply(t7)));
        assertEquals(tau.apply(t7),s1.apply(t7));
        System.out.println("should be true.  Value: " + tau.apply(t8).equals(s1.apply(t8)));
        assertEquals(tau.apply(t8),s1.apply(t8));
        System.out.println("should be true.  " + s3 + " -> " + t8 + " = " + t9 + " = " + s3.apply(t8) + " Value: " + t9.equals(s3.apply(t8)));
        assertEquals(t9,s3.apply(t8));
    }

    /** ***************************************************************
     * Check application of substitutions.
     */
    @Test
    public void testSubstApply() {

        System.out.println("---------------------");
        System.out.println("INFO in testSubstApply()");
        System.out.println(s1 + " -> " + t1 + " = " + s1.apply(t1));
        System.out.println(t4);
        System.out.println("should be true: " + s1.apply(t1).equals(t4));
        assertEquals(t4,s1.apply(t1));
        System.out.println(s2 + " -> " + t1 + " = " + s2.apply(t1));
        System.out.println(t5);
        System.out.println("should be true: " + s2.apply(t1).equals(t5));
        assertEquals(t5,s2.apply(t1));
    }

    /** ***************************************************************
     */
    @Test
    public void testFreshVarSubst() {

        System.out.println("---------------------");
        System.out.println("INFO in testSubstApply()");
        Term var1 = Substitutions.freshVar();
        Term var2 = Substitutions.freshVar();
        if (!var1.equals(var2))
            System.out.println("Correct, " + var1 + " != " + var2);
        else
            System.out.println("Failure, " + var1 + " == " + var2);
        assertNotEquals(var1,var2);
        LinkedHashSet<Term> vars = t1.collectVars();
        Substitutions sigma = Substitutions.freshVarSubst(vars);
        LinkedHashSet<Term> vars2 = sigma.apply(t1).collectVars();
        boolean shared = vars.removeAll(vars2);   // if false, intersection is empty set
        if (!shared)
            System.out.println("Correct, " + var1 + " & " + var2 + " don't share variables.");
        else
            System.out.println("Failure, " + var1 + " & " + var2 + " do share variables.");
        assertFalse(shared);
    }

}
