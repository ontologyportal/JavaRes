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

public class LiteralTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static Literal a1 = null;
    public static Literal a2 = null;
    public static Literal a3 = null;
    public static Literal a4 = null;
    public static Literal a5 = null;
    public static Literal a6 = null;
    public static Literal a7 = null;
    public static Literal a8 = null;

    public static String input1 = "p(X)  ~q(f(X,a), b)  ~a=b  a!=b  ~a!=f(X,b)  p(X)  ~p(X) p(a)";
    public static String input2 = "p(X)|~q(f(X,a), b)|~a=b|a!=b|~a!=f(X,b)";
    public static String input3 = "$false";
    public static String input4 = "$false|~q(f(X,a), b)|$false";
    public static String input5 = "p(a)|p(f(X))";
    public static String input6 = "foo(bar,vaz)|f(X1,X2)!=g(X4,X5)|k(X1,X1)!=k(a,b)";

    public static String kif1a = "(p ?X)";
    public static String kif1b = "(not (q (f ?X a) b))";
    public static String kif1c = "(not (equals a b))";
    public static String kif1d = "(not (equals a b))";
    public static String kif1e = "(equals a (f ?X b))";
    public static String kif1f = "(p ?X)";
    public static String kif1g = "(not (p ?X))";
    public static String kif1h = "(p a)";

    public static String kif2 = "(or (p ?X) (or (not (q (f ?X a) b)) (or (equals (not a) b) (or (equals (not a) b) (equals a (f ?X b))))))";
    public static String kif3 = "false";
    public static String kif4 = "(or false (or (not (q (f ?X))) false)";
    public static String kif5= "(or (p a) (p (f ?X)))";
    public static String kif6 = "(or (foo bar baz) (or (equals (not (f ?X1 ?X2)) (g ?X4 ?X5)) (equals (not (k ?X1 ?X1)) (k a b))))";

    /** ***************************************************************
     * Setup function for clause/literal unit tests. Initialize
     * variables needed throughout the tests.
     */
    @BeforeClass
    public static void setup() {

        KIF.init();
        Lexer lex = new Lexer(input1);

        System.out.println("INFO in Literal.setup(): input: " + input1);
        a1 = new Literal();
        a1 = a1.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a1: " + a1);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a2 = new Literal();
        a2 = a2.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a2: " + a2);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a3 = new Literal();
        a3 = a3.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a3: " + a3);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a4 = new Literal();
        a4 = a4.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a4: " + a4);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a5 = new Literal();
        a5 = a5.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a5: " + a5);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a6 = new Literal();
        a6 = a6.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a6: " + a6);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a7 = new Literal();
        a7 = a7.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a7: " + a7);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);

        a8 = new Literal();
        a8 = a8.parseLiteral(lex);
        System.out.println("INFO in Literal.setup(): finished parsing a8: " + a8);
        System.out.println("INFO in Literal.setup(): pointing at token: " + lex.literal);
    }

    /** ***************************************************************
     *  Test that basic literal literal functions work correctly.
     */
    @Test
    public void testLiterals() {

        System.out.println("---------------------");
        System.out.println("INFO in testLiterals(): all true");
        System.out.println("a1: " + a1);
        System.out.println("is positive:" + a1.isPositive());
        assertTrue(a1.isPositive());
        System.out.println("is not equational: " + !a1.isEquational());
        assertTrue(!a1.isEquational());
        ArrayList vars = a1.collectVars();
        System.out.println("Number of variables. Should be 1 :" + vars.size());
        assertEquals(vars.size(), 1);

        System.out.println();
        System.out.println("a2: " + a2);
        System.out.println("is negative:" + a2.isNegative());
        assertTrue(a2.isNegative());
        System.out.println("is not equational: " + !a2.isEquational());
        assertTrue(!a2.isEquational());
        vars = a2.collectVars();
        System.out.println("Number of variables. Should be 1 :" + vars.size());
        assertEquals(vars.size(), 1);

        System.out.println();
        System.out.println("a3: " + a3);
        System.out.println("is positive:" + a3.isNegative());
        assertTrue(a3.isNegative());
        System.out.println("is equational: " + a3.isEquational());
        assertTrue(a3.isEquational());
        System.out.println(a3 + " equals " + a4 + " :" + a3.equals(a4));
        assertEquals(a3,a4);
        vars = a3.collectVars();
        System.out.println("Number of variables. Should be 0 :" + vars.size());
        assertEquals(vars.size(), 0);

        System.out.println();
        System.out.println("a4: " + a4);
        System.out.println("is negative:" + a4.isNegative());
        assertTrue(a4.isNegative());
        System.out.println("is equational: " + a4.isEquational());
        assertTrue(a4.isEquational());
        System.out.println(a4 + " equals " + a3 + " :" + a4.equals(a3));
        vars = a4.collectVars();
        System.out.println("Number of variables. Should be 0 :" + vars.size());
        assertEquals(vars.size(), 0);

        System.out.println();
        System.out.println("a5: " + a5);
        System.out.println("is positive:" + !a5.isNegative());
        assertTrue(!a5.isNegative());
        System.out.println("is equational: " + a5.isEquational());
        assertTrue(a5.isEquational());
        vars = a5.collectVars();
        System.out.println("Number of variables. Should be 1 :" + vars.size());
        assertEquals(vars.size(), 1);

        System.out.println();
        System.out.println("a6: " + a6);
        System.out.println("is positive:" + !a6.isNegative());
        assertTrue(!a6.isNegative());
        System.out.println("is not equational: " + !a6.isEquational());
        assertTrue(!a6.isEquational());
        vars = a6.collectVars();
        System.out.println("Number of variables. Should be 1 :" + vars.size());
        assertEquals(vars.size(), 1);

        System.out.println();
        System.out.println("a7: " + a7);
        System.out.println("is negative:" + a7.isNegative());
        assertTrue(a7.isNegative());
        System.out.println("is not equational: " + !a7.isEquational());
        assertTrue(!a7.isEquational());
        vars = a7.collectVars();
        System.out.println("Number of variables. Should be 1 :" + vars.size());
        assertEquals(vars.size(), 1);

        System.out.println();
        System.out.println("a8: " + a8);
        System.out.println("is positive:" + !a8.isNegative());
        assertTrue(!a8.isNegative());
        System.out.println("is not equational: " + !a8.isEquational());
        assertTrue(!a8.isEquational());
        vars = a8.collectVars();
        System.out.println("Number of variables. Should be 0 :" + vars.size());
        assertEquals(vars.size(), 0);
    }

    /** ***************************************************************
     * Test the weight function.
     */
    @Test
    public void testLitWeight() {

        System.out.println("---------------------");
        System.out.println("INFO in testLitWeight(): all true");
        System.out.println(a1.weight(2,1) == 3);
        assertEquals(a1.weight(2,1), 3);
        System.out.println(a2.weight(2,1) == 9);
        assertEquals(a2.weight(2,1), 9);
        System.out.println(a3.weight(2,1) == 6);
        assertEquals(a3.weight(2,1), 6);
        System.out.println(a4.weight(2,1) == 6);
        assertEquals(a4.weight(2,1), 6);
        System.out.println(a5.weight(2,1) == 9);
        assertEquals(a5.weight(2,1), 9);
        System.out.println(a6.weight(2,1) == 3);
        assertEquals(a6.weight(2,1), 3);
        System.out.println(a7.weight(2,1) == 3);
        assertEquals(a7.weight(2,1), 3);
        System.out.println(a8.weight(2,1) == 4);
        assertEquals(a8.weight(2,1), 4);
    }

    /** ***************************************************************
     * Test literal list parsing and printing.
     */
    @Test
    public void testLitList() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in testLitList(): all true");

        System.out.println("input2: " + input2);
        Lexer lex = new Lexer(input2);
        ArrayList<Literal> l2 = Literal.parseLiteralList(lex);
        System.out.println("result: " + l2);
        System.out.println(l2.size() == 5);
        assertEquals(l2.size(), 5);
        assertEquals("p(X)",l2.get(0).toString());
        System.out.println();

        System.out.println("input3: " + input3);
        lex = new Lexer(input3);
        ArrayList<Literal> l3 = Literal.parseLiteralList(lex);
        System.out.println(l3);
        System.out.println(l3.size() == 0);
        assertEquals(l3.size(), 0);
        System.out.println();

        System.out.println("input4: " + input4);
        lex = new Lexer(input4);
        ArrayList<Literal> l4 = Literal.parseLiteralList(lex);
        System.out.println(l4);
        System.out.println(l4.size() == 1);
        assertEquals(l4.size(), 1);
        System.out.println();

        System.out.println("input5: " + input5);
        lex = new Lexer(input5);
        ArrayList<Literal> l5 = Literal.parseLiteralList(lex);
        System.out.println(l5);
        System.out.println(l5.size() == 2);
        assertEquals(l5.size(), 2);
        System.out.println();

        System.out.println("input6: " + input6);
        lex = new Lexer(input6);
        ArrayList<Literal> l6 = Literal.parseLiteralList(lex);
        System.out.println(l6);
        System.out.println(l6.size() == 3);
        assertEquals(l6.size(), 3);
    }

    /** ***************************************************************
     */
    @Test
    public void testTokens() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in testTokens():");
        Lexer lex = new Lexer("0 1 2 3");
        try {
            lex.next();
            System.out.println(lex.literal);
            assertEquals(lex.literal,"0");
            lex.next();
            System.out.println(lex.literal);
            assertEquals(lex.literal,"1");
            lex.look();
            lex.next();
            System.out.println(lex.literal);
            assertEquals(lex.literal,"2");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** ***************************************************************
     * Test signature collection.
     */
    @Test
    public void testSig() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in testSig(): all true");
        Signature sig = new Signature();
        sig = a1.collectSig(sig);
        System.out.println(sig);
        assertEquals("[p]",sig.preds.toString());

        sig = a2.collectSig(sig);
        System.out.println(sig);
        assertEquals("[p, q]",sig.preds.toString());
        assertEquals("[f, a, b]",sig.funs.toString());

        sig = a3.collectSig(sig);
        System.out.println(sig);

        sig = a4.collectSig(sig);
        System.out.println(sig);

        sig = a5.collectSig(sig);
        System.out.println(sig);

        sig = a6.collectSig(sig);
        System.out.println(sig);

        sig = a7.collectSig(sig);
        System.out.println(sig);

        sig = a8.collectSig(sig);
        System.out.println(sig);
        assertEquals("[p, q, =]",sig.preds.toString());
        assertEquals("[f, a, b]",sig.funs.toString());

        sig.addFun("mult", 2);

        System.out.println(sig);
        System.out.println(sig.isPred("q"));
        assertTrue(sig.isPred("q"));
        System.out.println(!sig.isPred("unknown"));
        assertTrue(!sig.isPred("unknown"));
        System.out.println(!sig.isPred("a"));
        assertTrue(!sig.isPred("a"));
        System.out.println(sig.isFun("a"));
        assertTrue(sig.isFun("a"));
        System.out.println(!sig.isFun("unknown"));
        assertTrue(!sig.isPred("unknown"));
        System.out.println(!sig.isFun("q"));
        assertTrue(!sig.isFun("q"));

        System.out.println(sig.getArity("b") == 0);
        assertEquals(sig.getArity("b"), 0);
        System.out.println(sig.getArity("p") == 1);
        assertEquals(sig.getArity("p"), 1);
    }

    /** ***************************************************************
     */
    @Test
    public void testPureVarLit() {

        String input7 = "X!=Y";
        String input8 = "~q(a,g(a)";
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in testPureVarLit(): ");
        Lexer lex = new Lexer(input7);
        Literal l = new Literal();
        l = l.parseLiteral(lex);
        assertTrue(l.isPureVarLit());

        lex = new Lexer(input8);
        l = new Literal();
        l = l.parseLiteral(lex);
        assertFalse(l.isPureVarLit());
    }

    /** ***************************************************************
     */
    @Test
    public void testToKIF() {
            /*
                public static String kif1a = "(p ?X)";
    public static String kif1b = "(not (q (f ?X a) b))";
    public static String kif1c = "(equals (not a) b)";
    public static String kif1d = "(equals (not a) b)";
    public static String kif1e = "(equals a (f ?X b))";
    public static String kif1f = "(p ?X)";
    public static String kif1g = "(not (p ?X))";
    public static String kif1h = "(p a)";

    public static String kif2 = "(or (p ?X) (or (not (q (f ?X a) b)) (or (equals (not a) b) (or (equals (not a) b) (equals a (f ?X b))))))";
    public static String kif3 = "false";
    public static String kif4 = "(or false (or (not (q (f ?X))) false)";
    public static String kif5= "(or (p a) (p (f ?X)))";
    public static String kif6 = "(or (foo bar baz) (or (equals (not (f ?X1 ?X2)) (g ?X4 ?X5)) (equals (not (k ?X1 ?X1)) (k a b))))";
             */
        System.out.println("-------------------------------------------------");
        System.out.println("testToKIF()");
        System.out.println("input: " + a1);
        System.out.println("expected: " + kif1a);
        String actual = a1.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1a,actual);

        System.out.println("input: " + a2);
        System.out.println("expected: " + kif1b);
        actual = a2.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1b,actual);

        System.out.println("input: " + a3);
        System.out.println("expected: " + kif1c);
        actual = a3.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1c,actual);

        System.out.println("input: " + a4);
        System.out.println("expected: " + kif1d);
        actual = a4.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1d,actual);

        System.out.println("input: " + a5);
        System.out.println("expected: " + kif1e);
        actual = a5.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1e,actual);

        System.out.println("input: " + a6);
        System.out.println("expected: " + kif1f);
        actual = a6.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1f,actual);

        System.out.println("input: " + a7);
        System.out.println("expected: " + kif1g);
        actual = a7.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1g,actual);

        System.out.println("input: " + a8);
        System.out.println("expected: " + kif1h);
        actual = a8.toKIFString();
        System.out.println("actual: " + actual);
        assertEquals(kif1h,actual);
    }
}