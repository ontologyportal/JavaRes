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
import static org.junit.Assert.*;

public class BareFormulaTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static String nformulas = "![X]:(a(x) | ~a=b)" +
            "(![X]:a(X)|b(X)|?[X,Y]:(p(X,f(Y))))<=>q(g(a),X)" +
            "((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))<=>q(g(a),X))";

    /** ***************************************************************
     */
    @BeforeClass
    public static void init() {
        KIF.init();
    }

    /** ***************************************************************
     * Test that basic parsing and functionality works.
     * The structure of the example should be
     *
     *         Formula
     *     ![X]:(a(X) ~| ~a=b)
     *         |                 |
     * op    literal           Formula
     * !       X            (a(X) ~| ~a=b)
     *                        |           |
     *                    op Literal     Literal
     *                    ~|  a(X)        ~a=b
     *                         |           |
     *                       Atom        negated Atom
     *                        a(X)        a=b
     *                        |            |
     *                       Term        Term
     *                      |   |        |    |
     *                     op   args    op    args
     *                     a     X      =      a,b
     */
    @Test
    public void testParse() {

        String formstr = "![X]:(a(x) | ~a=b)";
        try {
            System.out.println("-----------------------------------------");
            System.out.println("INFO in BareFormula.testParse()");
            Lexer lex = new Lexer(formstr);
            System.out.println("Parsing formula: " + formstr);
            BareFormula f1 = BareFormula.parse(lex);
            f1 = f1.promoteChildren();
            System.out.println("INFO in BareFormula.testParse(): f1: " + f1.toStructuredString());
            System.out.println("op should be !: " + f1.op);
            assertTrue(f1.op.equals("!"));
            System.out.println("f1.lit1 != null should be true: " + f1.lit1);
            assertTrue(f1.lit1 != null);
            System.out.println("f1.child2 != null should be true: " + f1.child2);
            assertTrue(f1.child2 != null);

            System.out.println("op should be |: " + f1.child2.op); // a~|b becomes ~a|b
            assertTrue(f1.child2.op.equals("|"));
            System.out.println("f1.child2.lit1 != null should be true: " + f1.child2.lit1);
            assertTrue(f1.child2.lit1 != null);
            System.out.println("f1.child2.lit2 != null should be true: " + f1.child2.lit2);
            assertTrue(f1.child2.lit2 != null);
        }
        catch (Exception e) {
            System.out.println("Error in BareFormula.testParse()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Test that basic parsing and functionality works.
     */
    @Test
    public void testNakedFormula() {

        try {
            System.out.println("INFO in BareFormula.testNakedFormula()");
            Lexer lex = new Lexer(nformulas);
            System.out.println("Parsing formula: " + nformulas);
            BareFormula f1 = BareFormula.parse(lex);
            System.out.println("INFO in BareFormula.testNakedFormula(): f1: " + f1);
            System.out.println();

            //st.pushBack();
            BareFormula f2 = BareFormula.parse(lex);
            System.out.println("INFO in BareFormula.testNakedFormula(): f2: " + f2);
            System.out.println();
            //st.pushBack();
            BareFormula f3 = BareFormula.parse(lex);
            System.out.println("INFO in BareFormula.testNakedFormula(): f3: " + f3);
            System.out.println();
            System.out.println("all should be true:");
            System.out.println(f2 + " should be equal " + f3 + ":" + f2.equals(f3));
            assertEquals(f2.toString(),f3.toString());
            System.out.println(f3 + " should be equal " + f2 + ":" + f3.equals(f2));
            assertEquals(f3.toString(),f2.toString());
            System.out.println(f1 + " should not be equal " + f2 + ":" + !f1.equals(f2));
            assertNotEquals(f1.toString(),f2.toString());
            System.out.println(f2 + " should not be equal " + f1 + ":" + !f2.equals(f1));
            assertNotEquals(f2.toString(),f1.toString());
        }
        catch (Exception e) {
            System.out.println("Error in BareFormula.testNakedFormula()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testToKIF() {

        String input = "(![Fluent]:(![Time]:(((holdsAt(Fluent,Time)&(~releasedAt(Fluent,plus(Time,n1))))&" +
                "(~(?[Event]:(happens(Event,Time)&terminates(Event,Fluent,Time)))))=>" +
                "holdsAt(Fluent,plus(Time,n1)))))";
        String expected = "(forall (?Fluent)" +
                " (forall (?Time)" +
                  " (=>" +
                    " (and" +
                      " (and" +
                        " (holdsAt ?Fluent ?Time)" +
                        " (not" +
                          " (releasedAt ?Fluent (plus ?Time n1))))" +
                      " (not" +
                        " (exists (?Event)" +
                          " (and" +
                            " (happens ?Event ?Time)" +
                            " (terminates ?Event ?Fluent ?Time)))))" +
                    " (holdsAt ?Fluent (plus ?Time n1)))))";
        System.out.println("testToKIF()");
        System.out.println("expected: " + expected);
        Lexer lex = new Lexer(input);
        try {
            BareFormula f1 = BareFormula.parse(lex);
            String actual = f1.toKIFString();
            System.out.println("actual: " + actual);
            assertEquals(expected,actual);
        }
        catch (Exception e) {
            System.out.println("Error in BareFormula.testNakedFormula()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
