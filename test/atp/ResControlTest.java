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

public class ResControlTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static ClauseSet cset = new ClauseSet();
    public static Clause conj = new Clause();
    public static Clause fclause = new Clause();

    /** ***************************************************************
     * Setup function for clause/literal unit tests. Initialize
     * variables needed throughout the tests.
     */
    @BeforeClass
    public static void setup() {

        Clausifier.counterReset();
        Clause.resetCounter();
        System.out.println();
        String spec = "cnf(g1, negated_conjecture, ~c).\n" +
                "cnf(c1, axiom, a|b|c).\n" +
                "cnf(c2, axiom, b|c).\n" +
                "cnf(c3, axiom, c).";

        Lexer lex = new Lexer(spec);
        conj = Clause.parse(lex);
        System.out.println("ResControlTest.setup(): conj: " + conj);
        cset.parse(lex);
        System.out.println("ResControlTest.setup(): cset: " + cset);
        String cstr = "cnf(ftest, axiom, p(X)|~q|p(a)|~q|p(Y)).";
        lex = new Lexer(cstr);
        fclause = Clause.parse(lex);
        System.out.println("ResControlTest.setup(): fclause: " + fclause);
    }

    /** ***************************************************************
     * Test that forming resolvents between a clause and a clause set
     * works.
     */
    @Test
    public void testSetResolution() {

        System.out.println("ResControl.testSetResolution()");
        ClauseSet res = ResControl.computeAllResolvents(conj,cset);
        String result = res.toString();
        String expected = "cnf(c4,plain,a|b).\ncnf(c5,plain,b).\ncnf(c6,plain,$false).\n";
        System.out.println("Should see: " + expected);
        System.out.println("Result: " + res);
        assertEquals(expected,result);
    }

    /** ***************************************************************
     * Test full factoring of a clause.
     */
    @Test
    public void testFactoring() {

        System.out.println("testFactoring()");
        ClauseSet res = ResControl.computeAllFactors(fclause);
        String expected = "cnf(c0,plain,p(a)|~q|p(Y)).\ncnf(c1,plain,p(Y)|~q|p(a)).\ncnf(c2,plain,p(X)|~q|p(a)|p(Y)).\ncnf(c3,plain,p(X)|~q|p(a)).\n";
        System.out.println("should see: " + expected);
        String result = res.toString();

        System.out.println("Result: " + res);
        assertEquals(expected,result);
    }
}
