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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClauseTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static String str1 = "";
    public static String str2 = "";
    public static String str3 = "";
    public static String str4 = "";
    public static String str5 = "";
    public static String str6 = "";
    public static String str7 = "";
    public static String str8 = "";

    public static Clause c1 = null;
    public static Clause c2 = null;
    public static Clause c3 = null;
    public static Clause c4 = null;
    public static Clause c5 = null;
    public static Clause c6 = null;
    public static Clause c7 = null;
    public static Clause c8 = null;

    /** ***************************************************************
     *  Setup function for clause/literal unit tests. Initialize
     *  variables needed throughout the tests.
     */
    @BeforeClass
    public static void setup() {

        str1 = "cnf(test,axiom,p(a)|p(f(X))).";
        str2 = "cnf(test,axiom,(p(a)|p(f(X)))).";
        str3 = "cnf(test3,lemma,(p(a)|~p(f(X)))).";
        str4 = "cnf(taut,axiom,p(a)|q(a)|~p(a)).";
        str5 = "cnf(dup,axiom,p(a)|q(a)|p(a)).";
        str6 = "cnf(c6,axiom,f(f(X1,X2),f(X3,g(X4,X5)))!=f(f(g(X4,X5),X3),f(X2,X1))|k(X1,X1)!=k(a,b)).";
        str7 = "cnf(c7,axiom,f(f(X10,X2),f(X30,g(X4,X5)))!=f(f(g(X4,X5),X30),f(X2,X10))|k(X10,X10)!=k(a,b)).";
        str8 = "cnf(c8,plain,$false).";
    }

    /** ***************************************************************
     *  Test that basic literal parsing works correctly.  Make sure,
     *  via the lexical ordering of the test method name, that this
     *  executes first.
     */
    @Test
    public void testAAAClauses() {

        System.out.println("INFO in ClauseTest.testClauses(): expected results: \n" + str1);
        System.out.println("results:");
        Lexer lex = new Lexer(str1);

        c1 = Clause.parse(lex);
        if (c1.toString().equals("cnf(test,axiom,p(a)|p(f(X)))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c1.toString() + " not equal to cnf(test,axiom,p(a)|p(f(X))).");
        System.out.println("c1: " + c1);
        assertEquals("cnf(test,axiom,p(a)|p(f(X))).",c1.toString());

        lex = new Lexer(str2);
        c2 = Clause.parse(lex);
        if (c2.toString().equals(c1.toString()))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c2.toString() + " not equal to " + c1.toString());
        System.out.println("c2: " + c2);
        assertEquals(c2.toString(),c1.toString());

        lex = new Lexer(str3);
        c3 = Clause.parse(lex);
        if (c3.toString().equals("cnf(test3,lemma,p(a)|~p(f(X)))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c3.toString() + " not equal to cnf(test3,lemma,p(a)|~p(f(X))).");
        System.out.println("c3: " + c3);
        assertEquals("cnf(test3,lemma,p(a)|~p(f(X))).",c3.toString());

        lex = new Lexer(str4);
        c4 = Clause.parse(lex);
        if (c4.toString().equals("cnf(taut,axiom,p(a)|q(a)|~p(a))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c4.toString() + " not equal to cnf(taut,p(a)|q(a)|~p(a)).");
        System.out.println("c4: " + c4);
        assertEquals("cnf(taut,axiom,p(a)|q(a)|~p(a)).",c4.toString());

        lex = new Lexer(str5);
        c5 = Clause.parse(lex);
        if (c5.toString().equals("cnf(dup,axiom,p(a)|q(a)|p(a))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c5.toString() + " not equal to cnf(dup,p(a)|q(a)|p(a)).");
        System.out.println("c5: " + c5);
        assertEquals("cnf(dup,axiom,p(a)|q(a)|p(a)).",c5.toString());

        lex = new Lexer(str6);
        c6 = Clause.parse(lex);
        if (c6.toString().equals("cnf(c6,axiom,p(a)|q(a)|p(a))."))
            System.out.println("Success");
        else
            System.out.println("Failure. " + c5.toString() + " not equal to cnf(c6,axiom,(f(f(X1,X2),f(X3,g(X4,X5)))!=f(f(g(X4,X5),X3),f(X2,X1))|k(X1,X1)!=k(a,b))).");
        System.out.println("c6: " + c6);
        assertEquals("cnf(c6,axiom,~f(f(X1,X2),f(X3,g(X4,X5)))=f(f(g(X4,X5),X3),f(X2,X1))|~k(X1,X1)=k(a,b)).",c6.toString());

        lex = new Lexer(str7);
        c7 = Clause.parse(lex);
        System.out.println("c6: " + c6);
        System.out.println("c6 normalized: " + c6.normalizeVarCopy());
        System.out.println("c7: " + c7);
        System.out.println("c7 normalized: " + c7.normalizeVarCopy());
        if (c7.normalizeVarCopy().equals(c6.normalizeVarCopy()))
            System.out.println("Success");
        else
            System.out.println("Failure.");
        assertEquals(c7.normalizeVarCopy(),c6.normalizeVarCopy());

        lex = new Lexer(str8);
        c8 = Clause.parse(lex);
        System.out.println("c8: " + c8);
        if (c8.toString().equals(str8))
            System.out.println("Success");
        else
            System.out.println("Failure.");
        assertEquals(str8,c8.toString());
    }

    /** ***************************************************************
     */
    @Test
    public void testWeights() {

        System.out.println("INFO in ClauseTest.testWeights(): ");
        Clause cf = c1.freshVarCopy();
        assertEquals(cf.weight(2,1), c1.weight(2,1));
        assertEquals(cf.weight(1,1), c1.weight(1,1));
    }

    /** ***************************************************************
     */
    @Test
    public void testGetLiteral() {

        Clause cnew = new Clause(c4.literals);
        assertEquals(cnew.getLiteral(0),c4.getLiteral(0));
    }

    /** ***************************************************************
     */
    @Test
    public void testClassifiers() {

        Clause empty = new Clause(new ArrayList<>());
        assertTrue(empty.isEmpty());
        assertTrue(!empty.isUnit());
        assertTrue(empty.isHorn());

        ArrayList<Literal> literals = new ArrayList<Literal>();
        literals.add(c5.getLiteral(0));
        Clause unit = new Clause(literals);
        assertTrue(!unit.isEmpty());
        assertTrue(unit.isUnit());
        assertTrue(unit.isHorn());

        assertTrue(!c1.isHorn());
        assertTrue(c3.isHorn());

        assertTrue(c4.isTautology());
        assertTrue(!c5.isTautology());
    }

    /** ***************************************************************
     */
    @Test
    public void testSignatures() {

        Signature sig = c1.collectSig();
        c2.collectSig(sig);
        c3.collectSig(sig);
        c4.collectSig(sig);
        c5.collectSig(sig);
        System.out.println("testSignatures(): " + sig);
        assertEquals("[a, f]",sig.funs.toString());
        assertEquals("[p, q]",sig.preds.toString());
    }

    /** ***************************************************************
     */
    @Test
    public void testNegLits() {

        ArrayList<Literal> negs = c1.getNegativeLits();
        assertEquals(0, negs.size());
        negs = c2.getNegativeLits();
        assertEquals(0, negs.size());
        negs = c3.getNegativeLits();
        assertEquals(1, negs.size());
        negs = c4.getNegativeLits();
        assertEquals(1, negs.size());
        negs = c5.getNegativeLits();
        assertEquals(0, negs.size());
    }

    /** ***************************************************************
     */
    @Test
    public void testInfLits() {

        c2.selectInferenceLits(LitSelection.LitSelectors.FIRST);
        for (Literal l : c2.literals)
            assertTrue(l.isInferenceLit());

        c3.selectInferenceLits(LitSelection.LitSelectors.FIRST);
        for (Literal l : c3.literals)
            assertEquals(l.isNegative(), l.isInferenceLit());

        c2.selectInferenceLits(LitSelection.LitSelectors.LEASTVARS);
        for (Literal l : c2.literals)
            assertTrue(l.isInferenceLit());

        c3.selectInferenceLits(LitSelection.LitSelectors.LEASTVARS);
        for (Literal l : c3.literals)
            assertEquals(l.isNegative(), l.isInferenceLit());
    }
}