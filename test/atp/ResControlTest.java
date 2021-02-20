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

        System.out.println("---------------------");
        System.out.println("ResControl.testSetResolution()");
        ClauseSet res = ResControl.computeAllResolvents(conj,cset);
        //String result = res.toString();
        String expectedStr = "cnf(c4,plain,a|b).\ncnf(c5,plain,b).\ncnf(c6,plain,$false).\n";
        Lexer lex = new Lexer(expectedStr);
        ClauseSet expected = new ClauseSet();
        expected.parse(lex);
        System.out.println("Should see: " + expected);
        System.out.println("Result: " + res);
        if (expected.equals(res))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,res);
    }

    /** ***************************************************************
     * Test that forming resolvents between a clause and a clause set
     * works.
     */
    @Test
    public void testSetResolution2() {

        System.out.println("---------------------");
        System.out.println("ResControl.testSetResolution2()");
        String givenClauseStr = "cnf(clause29,negated_conjecture,~street(U)| ~way(U)| ~lonely(U)| ~old(V)|" +
                " ~dirty(V)| ~white(V)| ~car(V)| ~chevy(V)| ~event(W)| ~barrel(W,V)| ~down(W,U)| ~in(W,X)| " +
                "~city(X)| ~hollywood(X)| ssSkC0).";
        Lexer lex = new Lexer(givenClauseStr);
        Clause givenClause = Clause.parse(lex);
        String processedStr = "cnf(clause2,negated_conjecture,event(skc14)).\n" +
                "cnf(clause3,negated_conjecture,street(skc13)).\n" +
                "cnf(clause4,negated_conjecture,old(skc12)).\n" +
                "cnf(clause23,negated_conjecture,ssSkC0| barrel(skc14,skc12)).\n" +
                "cnf(clause24,negated_conjecture,ssSkC0| down(skc14,skc13)).\n" +
                "cnf(clause25,negated_conjecture,ssSkC0| in(skc14,skc15)).\n" +
                "cnf(clause26,negated_conjecture,~ssSkC0|barrel(skc10,skc9)).\n";
        lex = new Lexer(processedStr);
        ClauseSet processed = new ClauseSet();
        processed.parse(lex);
        System.out.println("processed: " + processed);
        ClauseSet res = ResControl.computeAllResolvents(givenClause,processed);
        //String result = res.toString();
        String expectedStr = "\n";
        lex = new Lexer(expectedStr);
        ClauseSet expected = new ClauseSet();
        expected.parse(lex);
        System.out.println("Should see: " + expected);
        System.out.println("Result: " + res);
        if (res.clauses.size() == 8)
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(7,res.clauses.size());
    }

    /** ***************************************************************
     * Test full factoring of a clause.
     */
    @Test
    public void testFactoring() {

        System.out.println("---------------------");
        System.out.println("testFactoring()");
        ClauseSet res = ResControl.computeAllFactors(fclause);
        String expected = "cnf(c0,plain,p(a)|~q|p(Y)).\ncnf(c1,plain,p(Y)|~q|p(a)).\ncnf(c2,plain,p(X)|~q|p(a)|p(Y)).\ncnf(c3,plain,p(X)|~q|p(a)).\n";
        System.out.println("should see: " + expected);
        String result = res.toString();

        System.out.println("Result: " + res);
        if (expected.equals(result))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,result);
    }

    /** ***************************************************************
     * Test full factoring of a clause.
     */
    @Test
    public void testFactoring2() {

        Clause.resetCounter();
        System.out.println("---------------------");
        System.out.println("testFactoring2()");
        String input = "cnf(associativity_addition,axiom,equalish(add(X4,add(X3,X5)),add(add(X4,X3),X5))|~defined(X4)|~defined(X3)|~defined(X5)). ";
        String expected =
                "cnf(c0,plain,equalish(add(X5,add(X3,X5)),add(add(X5,X3),X5))|~defined(X5)|~defined(X3)).\n" +
                "cnf(c1,plain,equalish(add(X4,add(X5,X5)),add(add(X4,X5),X5))|~defined(X4)|~defined(X5)).\n";
        Lexer lex = new Lexer(input);
        Clause inclause = Clause.parse(lex);
        SearchParams sp = new SearchParams();
        inclause.selectInferenceLits(sp.literal_selection);
        ClauseSet res = ResControl.computeAllFactors(inclause);
        System.out.println("should see: " + expected);
        String result = res.toString();

        System.out.println("Result: " + res);
        if (expected.equals(result))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,result);
    }
}
