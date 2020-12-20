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

public class ClauseTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static String str1 = "";

    /** ***************************************************************
     *  Setup function for clause/literal unit tests. Initialize
     *  variables needed throughout the tests.
     */
    @BeforeClass
    public static void setup() {

        str1 = "cnf(test1,axiom,p(a)|p(f(X))).\n" +
                "cnf(test2,axiom,(p(a)|p(f(X)))).\n" +
                "cnf(test3,lemma,(p(a)|~p(f(X)))).\n" +
                "cnf(taut,axiom,p(a)|q(a)|~p(a)).\n" +
                "cnf(dup,axiom,p(a)|q(a)|p(a)).\n" +
                "cnf(c6,axiom,f(f(X1,X2),f(X3,g(X4,X5)))!=f(f(g(X4,X5),X3),f(X2,X1))|k(X1,X1)!=k(a,b)).\n" +
                "cnf(c7,axiom,f(f(X10,X2),f(X30,g(X4,X5)))!=f(f(g(X4,X5),X30),f(X2,X10))|k(X10,X10)!=k(a,b)).\n";
    }

    /** ***************************************************************
     *  Test that basic literal parsing works correctly.
     */
    @Test
    public void testClauses() {

        System.out.println("INFO in Clause.testClauses(): expected results: \n" + str1);
        System.out.println("results:");
        Lexer lex = new Lexer(str1);

        Clause c1 = Clause.parse(lex);
        if (c1.toString().equals("cnf(test1,axiom,p(a)|p(f(X)))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c1.toString() + " not equal to cnf(test1,axiom,p(a)|p(f(X))).");
        System.out.println("c1: " + c1);
        assertEquals(c1.toString(),"cnf(test1,axiom,p(a)|p(f(X))).");

        Clause c2 = Clause.parse(lex);
        if (c2.toString().equals("cnf(test2,axiom,(p(a)|p(f(X))))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c2.toString() + " not equal to cnf(test2,(p(a)|p(f(X)))).");
        System.out.println("c2: " + c2);
        assertEquals(c2.toString(),"cnf(test2,axiom,(p(a)|p(f(X)))).");

        Clause c3 = Clause.parse(lex);
        if (c3.toString().equals("cnf(test3,axiom,(p(a)|~p(f(X))))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c3.toString() + " not equal to cnf(test3,(p(a)|~p(f(X)))).");
        System.out.println("c3: " + c3);
        assertEquals(c3.toString(),"cnf(test3,axiom,(p(a)|~p(f(X)))).");

        Clause c4 = Clause.parse(lex);
        if (c4.toString().equals("cnf(taut,axiom,p(a)|q(a)|~p(a))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c4.toString() + " not equal to cnf(taut,p(a)|q(a)|~p(a)).");
        System.out.println("c4: " + c4);
        assertEquals(c4.toString(),"cnf(taut,axiom,p(a)|q(a)|~p(a))).");

        Clause c5 = Clause.parse(lex);
        if (c5.toString().equals("cnf(dup,axiom,p(a)|q(a)|p(a))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c5.toString() + " not equal to cnf(dup,p(a)|q(a)|p(a)).");
        System.out.println("c5: " + c5);
        assertEquals(c5.toString(),"cnf(dup,axiom,p(a)|q(a)|p(a))).");

        Clause c6 = Clause.parse(lex);
        if (c6.toString().equals("cnf(c6,axiom,p(a)|q(a)|p(a))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c5.toString() + " not equal to cnf(c6,axiom,(f(f(X1,X2),f(X3,g(X4,X5)))!=f(f(g(X4,X5),X3),f(X2,X1))|k(X1,X1)!=k(a,b))).");
        System.out.println("c6: " + c6);
        assertEquals(c6.toString(),"cnf(c6,axiom,(f(f(X1,X2),f(X3,g(X4,X5)))!=f(f(g(X4,X5),X3),f(X2,X1))|k(X1,X1)!=k(a,b))).");

        Clause c7 = Clause.parse(lex);
        assert c7.normalizeVarCopy().equals(c6.normalizeVarCopy());
        System.out.println("c6: " + c6);
        System.out.println("c6 normalized: " + c6.normalizeVarCopy());
        System.out.println("c7: " + c7);
        System.out.println("c7 normalized: " + c7.normalizeVarCopy());
    }

}