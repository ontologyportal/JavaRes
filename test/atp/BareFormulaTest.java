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
}
