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

public class TermTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     * Set up test content.
     */
    public static String example1 = "X";
    public static String example2 = "a";
    public static String example3 = "g(a,b)";
    public static String example4 = "g(X,f(Y))";
    public static String example5 = "g(X, f(Y))";
    public static String example6 = "f(X,g(a,b))";
    public static String example7 = "g(X)";
    public static String example8 = "g(b,b)";
    public static String example9 = "'g'(b,b)";

    public static String kif1 = "?X";
    public static String kif2 = "a";
    public static String kif3 = "(g a b)";
    public static String kif4 = "(g ?X (f ?Y))";
    // 5
    public static String kif6 = "(f ?X (g a b))";
    public static String kif7 = "(g ?X)";
    public static String kif8 = "(g b b)";
    // 9
    
    public static Term t1 = null;
    public static Term t2 = null;
    public static Term t3 = null;
    public static Term t4 = null;
    public static Term t5 = null;
    public static Term t6 = null;
    public static Term t7 = null;
    public static Term t8 = null;
    public static Term t9 = null;

    /** ***************************************************************
     * Set up test content.
     */
    @BeforeClass
    public static void setupTests() {

        KIF.init();
        t1 = Term.string2Term(example1);
        t2 = Term.string2Term(example2);
        t3 = Term.string2Term(example3);
        t4 = Term.string2Term(example4);
        t5 = Term.string2Term(example5);
        t6 = Term.string2Term(example6);
        t7 = Term.string2Term(example7);
        t8 = Term.string2Term(example8);
        t9 = Term.string2Term(example9);
    }

    /** ***************************************************************
     * Test that parse() is working properly
     */
    @Test
    public void parseTest() {

        System.out.println("---------------------");
        System.out.println("INFO in parseTest()");
        System.out.println(t1 + " = " + example1);
        assertEquals(example1,t1.toString());
        System.out.println(t2 + " = " + example2);
        assertEquals(example2,t2.toString());
        System.out.println(t3 + " = " + example3);
        assertEquals(example3,t3.toString());
        System.out.println(t4 + " = " + example4);
        assertEquals(example4,t4.toString());
        System.out.println(t5 + " = " + example5);
        assertEquals(example4,t5.toString());  // e4 is e5 without extract embedded space
        System.out.println(t6 + " = " + example6);
        assertEquals(example6,t6.toString());
        System.out.println(t7 + " = " + example7);
        assertEquals(example7,t7.toString());
        System.out.println(t8 + " = " + example8);
        assertEquals(example8,t8.toString());
        System.out.println(t9 + " = " + example9);
        assertEquals(example9,t9.toString());
    }

    /** ***************************************************************
     * Test that parse() and toString() are dual. Start with terms,
     * so that we are sure to get the canonical string representation.
     */
    @Test
    public void testToString() {

        System.out.println("---------------------");
        System.out.println("INFO in Term.testToString(): all should be true");
        Term t = new Term();
        t = Term.string2Term(t1.toString());
        System.out.println(t1.toString().equals(t.toString()));
        assertEquals(t1.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t2.toString());
        System.out.println(t2.toString().equals(t.toString()));
        assertEquals(t2.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t3.toString());
        System.out.println(t3.toString().equals(t.toString()));
        assertEquals(t3.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t4.toString());
        System.out.println(t4.toString().equals(t.toString()));
        assertEquals(t4.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t5.toString());
        System.out.println(t5.toString().equals(t.toString()));
        assertEquals(t5.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t6.toString());
        System.out.println(t6.toString().equals(t.toString()));
        assertEquals(t6.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t7.toString());
        System.out.println(t7.toString().equals(t.toString()));
        assertEquals(t7.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t8.toString());
        System.out.println(t8.toString().equals(t.toString()));
        assertEquals(t8.toString(),t.toString());
        t = new Term();
        t = Term.string2Term(t9.toString());
        System.out.println(t9.toString().equals(t.toString()));
        assertEquals(t9.toString(),t.toString());
    }

    /** ***************************************************************
     * Test that parse() and toString() are dual. Start with terms,
     * so that we are sure to get the canonical string representation.
     */
    @Test
    public void testToKIF() {

        System.out.println("---------------------");
        System.out.println("INFO in Term.testToKIF(): all should be true");
        System.out.println("input: " + t1);
        String actual = t1.toKIFString().toString();               // X
        String expected = kif1;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t1.toKIFString().equals(kif1));
        assertEquals(kif1,t1.toKIFString());

        System.out.println("input: " + t2);
        actual = t2.toKIFString().toString();                      // a
        expected = kif2;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t2.toKIFString().equals(kif2));
        assertEquals(kif2,t2.toKIFString());

        System.out.println("input: " + t3);
        actual = t3.toKIFString().toString();                      // g(a,b)
        expected = kif3;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t3.toKIFString().equals(kif3));
        assertEquals(kif3,t3.toKIFString());

        System.out.println("input: " + t4);
        actual = t4.toKIFString().toString();                      // g(X,f(Y))
        expected = kif4;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t4.toKIFString().equals(kif4));
        assertEquals(kif4,t4.toKIFString());

        // 5

        System.out.println("input: " + t6);
        actual = t6.toKIFString().toString();                      // f(X,g(a,b))
        expected = kif6;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t6.toKIFString().equals(kif6));
        assertEquals(kif6,t6.toKIFString());

        System.out.println("input: " + t7);
        actual = t7.toKIFString().toString();                      // g(X)
        expected = kif7;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t7.toKIFString().equals(kif7));
        assertEquals(kif7,t7.toKIFString());

        System.out.println("input: " + t8);
        actual = t8.toKIFString().toString();                      // g(b,b)
        expected = kif8;
        System.out.println("actual: " + actual);
        System.out.println("expected: " + expected);
        System.out.println(t8.toKIFString().equals(kif8));
        assertEquals(kif8,t8.toKIFString());

        // 9
    }
    
    /** ***************************************************************
     * Test if the classification function works as expected.
     */
    @Test
    public void testIsVar() {

        System.out.println("---------------------");
        System.out.println("INFO in testIsVar(): first true, rest false");
        System.out.println(t1.isVar());
        assertTrue(t1.isVar());
        System.out.println(t2.isVar());
        assertFalse(t2.isVar());
        System.out.println(t3.isVar());
        assertFalse(t3.isVar());
        System.out.println(t4.isVar());
        assertFalse(t4.isVar());
        System.out.println(t5.isVar());
        assertFalse(t5.isVar());
        System.out.println(t6.isVar());
        assertFalse(t6.isVar());
        System.out.println(t7.isVar());
        assertFalse(t7.isVar());
        System.out.println(t8.isVar());
        assertFalse(t8.isVar());
        System.out.println(t9.isVar());
        assertFalse(t9.isVar());
    }

    /** ***************************************************************
     * Test if the classification function works as expected.
     */
    @Test
    public void testIsCompound() {

        System.out.println("---------------------");
        System.out.println("INFO in testIsCompound(): first false, rest true");
        System.out.println(t1.isCompound());
        assertFalse(t1.isCompound());
        System.out.println(t2.isCompound());
        assertTrue(t2.isCompound());
        System.out.println(t3.isCompound());
        assertTrue(t3.isCompound());
        System.out.println(t4.isCompound());
        assertTrue(t4.isCompound());
        System.out.println(t5.isCompound());
        assertTrue(t5.isCompound());
        System.out.println(t6.isCompound());
        assertTrue(t6.isCompound());
        System.out.println(t7.isCompound());
        assertTrue(t7.isCompound());
        System.out.println(t8.isCompound());
        assertTrue(t8.isCompound());
        System.out.println(t9.isCompound());
        assertTrue(t9.isCompound());
    }

    /** ***************************************************************
     * Test if term equality works as expected.
     */
    @Test
    public void testEquality() {

        System.out.println("---------------------");
        System.out.println("INFO in testEquality()");
        System.out.println(t1.equals(t1));
        assertEquals(t1,t1);
        System.out.println(t2.equals(t2));
        assertEquals(t2,t2);
        System.out.println(t3.equals(t3));
        assertEquals(t3,t3);
        System.out.println(t4.equals(t4));
        assertEquals(t4,t4);
        System.out.println(t5.equals(t5));
        assertEquals(t5,t5);
        System.out.println(t4.equals(t5));
        assertEquals(t4,t5);
        System.out.println(t5.equals(t4));
        assertEquals(t5,t4);
        System.out.println(t6.equals(t6));
        assertEquals(t6,t6);
        System.out.println(t7.equals(t7));
        assertEquals(t7,t7);
        System.out.println(t8.equals(t8));
        assertEquals(t8,t8);
        System.out.println(t9.equals(t9));
        assertEquals(t9,t9);

        System.out.println(t1.equals(t4));
        assertNotEquals(t1,t4);
        System.out.println(t3.equals(t4));
        assertNotEquals(t3,t4);
        System.out.println(t3.equals(t6));
        assertNotEquals(t3,t6);

        ArrayList<Term> tlist1 = new ArrayList<>();
        ArrayList<Term> tlist2 = new ArrayList<>();
        tlist2.add(t1);
        assertTrue(!Term.termListEqual(tlist1,tlist2));
    }

    /** ***************************************************************
     * Test if term copying works.
     */
    @Test
    public void testCopy() {

        System.out.println("---------------------");
        System.out.println("INFO in testCopy(): all true");
        Term t = new Term();
        t = t1.deepCopy();
        System.out.println(t.equals(t1));
        assertEquals(t,t1);
        t = t2.deepCopy();
        System.out.println(t.equals(t2));
        assertEquals(t,t2);
        t = t3.deepCopy();
        System.out.println(t.equals(t3));
        assertEquals(t,t3);
        t = t4.deepCopy();
        System.out.println(t.equals(t4));
        assertEquals(t,t4);
        t = t5.deepCopy();
        System.out.println(t.equals(t5));
        assertEquals(t,t5);
        t = t6.deepCopy();
        System.out.println(t.equals(t6));
        assertEquals(t,t6);
        t = t7.deepCopy();
        System.out.println(t.equals(t7));
        assertEquals(t,t7);
        t = t8.deepCopy();
        System.out.println(t.equals(t8));
        assertEquals(t,t8);
        t = t9.deepCopy();
        System.out.println(t.equals(t9));
        assertEquals(t,t9);
    }

    /** ***************************************************************
     * Test if isGround() works as expected.
     */
    @Test
    public void testIsGround() {

        System.out.println("---------------------");
        System.out.println("INFO in testIsGround(): all true");

        System.out.println(!t1.isGround());
        assertTrue(!t1.isGround());

        System.out.println(t2.isGround());
        assertTrue(t2.isGround());

        System.out.println(t3.isGround());
        assertTrue(t3.isGround());

        System.out.println(!t4.isGround());
        assertTrue(!t4.isGround());

        System.out.println(!t5.isGround());
        assertTrue(!t5.isGround());

        System.out.println(!t6.isGround());
        assertTrue(!t6.isGround());

        System.out.println(!t7.isGround());
        assertTrue(!t7.isGround());

        System.out.println(t8.isGround());
        assertTrue(t8.isGround());

        System.out.println(t9.isGround());
        assertTrue(t9.isGround());
    }

    /** ***************************************************************
     * Test the variable collection.
     */
    @Test
    public void testCollectVars() {

        System.out.println("---------------------");
        System.out.println("INFO in testCollectVars(): all true");
        ArrayList<Term> vars = t1.collectVars();
        System.out.println(vars.size() == 1);
        assertEquals(vars.size(), 1);

        vars = t2.collectVars();
        System.out.println(vars.size() == 0);
        assertEquals(vars.size(), 0);

        vars = t3.collectVars();
        System.out.println(vars.size() == 0);
        assertEquals(vars.size(), 0);

        vars = t4.collectVars();
        System.out.println(vars.size() == 2);
        assertEquals(vars.size(), 2);

        vars = t5.collectVars();
        System.out.println(vars.size() == 2);
        assertEquals(vars.size(), 2);

        System.out.println(vars.contains(Term.string2Term("X")));
        System.out.println(vars.contains(Term.string2Term("Y")));

        vars = t6.collectVars();
        System.out.println(vars.size() == 1);
        assertEquals(vars.size(), 1);

        vars = t7.collectVars();
        System.out.println(vars.size() == 1);
        assertEquals(vars.size(), 1);

        vars = t8.collectVars();
        System.out.println(vars.size() == 0);
        assertEquals(vars.size(), 0);

        vars = t9.collectVars();
        System.out.println(vars.size() == 0);
        assertEquals(vars.size(), 0);
    }

    /** ***************************************************************
     * Test the function symbol collection.
     */
    @Test
    public void testCollectFuns() {

        System.out.println("---------------------");
        System.out.println("INFO in testCollectFuns(): all true");
        ArrayList<String> funs = t1.collectFuns();
        System.out.println("t1: " + funs);
        System.out.println(funs.size() == 0);
        assertEquals(0,funs.size());

        funs = t2.collectFuns();
        System.out.println("t2: " + funs);
        System.out.println(funs.size() == 1 && funs.contains("a"));
        assertEquals(1,funs.size());

        funs = t3.collectFuns();
        System.out.println("t3: " + funs);
        System.out.println(funs.size() == 3 && funs.contains("g") && funs.contains("a") && funs.contains("b"));
        assertEquals(3,funs.size());

        funs = t4.collectFuns();
        System.out.println("t4: " + funs);
        System.out.println(funs.size() == 2 && funs.contains("g") && funs.contains("f"));
        assertEquals(2,funs.size());

        funs = t5.collectFuns();
        System.out.println("t5: " + funs);
        System.out.println(funs.size() == 2 && funs.contains("g") && funs.contains("f"));
        assertEquals(2,funs.size());

        funs = t6.collectFuns();
        System.out.println("t6: " + funs);
        System.out.println(funs.size() == 4 && funs.contains("f") && funs.contains("g") && funs.contains("a") && funs.contains("b"));
        assertEquals(4,funs.size());

        funs = t7.collectFuns();
        System.out.println("t7: " + funs);
        System.out.println(funs.size() == 1 && funs.contains("g"));
        assertEquals(1,funs.size());

        funs = t8.collectFuns();
        System.out.println("t8: " + funs);
        System.out.println(funs.size() == 2 && funs.contains("g") && funs.contains("b"));
        assertEquals(2,funs.size());

        funs = t9.collectFuns();
        System.out.println("t9: " + funs);
        System.out.println("t9 operator: " + t9.t);
        System.out.println("t9 subterms: " + t9.subterms);
        System.out.println(funs.size() == 2 && funs.contains("'g'") && funs.contains("b"));
        assertEquals(2,funs.size());
    }

    /** ***************************************************************
     * Test signature collection.
     */
    @Test
    public void testCollectSig() {

        System.out.println("---------------------");
        System.out.println("INFO in testCollectSig(): all should be true");
        Signature sig = new Signature();
        sig = t1.collectSig(sig);
        System.out.println(sig);
        assertTrue(sig.funs.size() == 0 && sig.preds.size() == 0);

        sig = t2.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a]",sig.funs.toString());

        sig = t3.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b]",sig.funs.toString());

        sig = t4.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b, f]",sig.funs.toString()); // sig is additive so we get a and b previous term

        sig = t5.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b, f]",sig.funs.toString());

        sig = t6.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b, f]",sig.funs.toString());

        sig = t7.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b, f]",sig.funs.toString());

        sig = t8.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b, f]",sig.funs.toString());

        sig = t9.collectSig(sig);
        System.out.println(sig);
        assertEquals("[a, g, b, f, 'g']",sig.funs.toString());

        System.out.println(sig.getArity("f") == 1);
        assertEquals(sig.getArity("f"), 1);
        System.out.println(sig.getArity("g") == 2);
        assertEquals(sig.getArity("g"), 2);
        System.out.println(sig.getArity("a") == 0);
        assertEquals(sig.getArity("a"), 0);
        System.out.println(sig.getArity("b") == 0);
        assertEquals(sig.getArity("b"), 0);
    }

    /** ***************************************************************
     * Test term weight function
     */
    @Test
    public void testTermWeight() {

        System.out.println("---------------------");
        System.out.println("INFO in testTermWeight()");
        System.out.println("Expected: 3 actual: " + t3.weight(1,1));
        assertEquals(t3.weight(1,1),3);
        System.out.println("Expected: 6 actual: " + t3.weight(2,1));
        assertEquals(t3.weight(2,1),6);
        System.out.println("Expected: 1 actual: " + t1.weight(2,1));
        assertEquals(t1.weight(2,1),1);

        assertEquals(t1.weight(1,2),2);
        assertEquals(t2.weight(1,2),1);
        assertEquals(t3.weight(1,2),3);
        assertEquals(t4.weight(1,2),6);
        assertEquals(t5.weight(2,1),6);
    }

    /** ***************************************************************
     * Test subterm function
     */
    @Test
    public void testSubTerm() {

        // t6 = "f(X,g(a,b))";
        System.out.println("---------------------");
        System.out.println("INFO in testSubTerm()");
        ArrayList<Integer> al = new ArrayList<Integer>();
        System.out.println("Expected: f(X,g(a,b)) actual: " + t6.subterm(al));
        assertEquals("f(X,g(a,b))",t6.subterm(al).toString());

        al.add(new Integer(0));
        System.out.println("Expected: f actual: " + t6.subterm(al).toString());
        assertEquals("f",t6.subterm(al).toString());

        al = new ArrayList<Integer>();
        al.add(new Integer(1));
        System.out.println("Expected: X actual: " + t6.subterm(al).toString());
        assertEquals("X",t6.subterm(al).toString());

        al = new ArrayList<Integer>();
        al.add(new Integer(2));
        System.out.println("Expected: g(a,b) actual: " + t6.subterm(al));
        assertEquals("g(a,b)",t6.subterm(al).toString());

        al = new ArrayList<Integer>();
        al.add(new Integer(2));
        al.add(new Integer(1));
        System.out.println("Expected: a actual: " + t6.subterm(al));
        assertEquals("a",t6.subterm(al).toString());

        al = new ArrayList<Integer>();
        al.add(new Integer(3));
        al.add(new Integer(0));
        System.out.println("Expected: null actual: " + t6.subterm(al));
        assertEquals(null,t6.subterm(al));

        // t5 = g(X, f(Y))
        assertEquals("g(X,f(Y))",t5.subterm(new ArrayList<>()).toString());
        assertEquals("g",t5.subterm(new ArrayList<>(Arrays.asList(0))).toString());
        assertEquals("X",t5.subterm(new ArrayList<>(Arrays.asList(1))).toString());
        assertEquals("f",t5.subterm(new ArrayList<>(Arrays.asList(2,0))).toString());
        assertEquals(null,t5.subterm(new ArrayList<>(Arrays.asList(5,0))));
    }
}
