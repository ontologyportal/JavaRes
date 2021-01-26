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

public class PredicateAbstractionTest {

    public static Clause c1 = null;
    public static Clause c2 = null;
    public static Clause c3 = null;
    public static Clause c4 = null;
    public static Clause c5 = null;
    public static Clause c6 = null;
    public static Clause c7 = null;
    public static Clause c8 = null;
    public static Clause c9 = null;


    /** ***************************************************************
     */
    @BeforeClass
    public static void setup() {

        String input = "cnf(c1, axiom, p(a, X) | p(X, a))." +
                "cnf(c2, axiom, ~p(a, b) | p(f(Y), a))." +
                "cnf(c3, axiom, q(Z, X) | ~q(f(Z), X0))." +
                "cnf(c4, axiom, p(X, X) | p(a, f(Y)))." +
                "cnf(c5, axiom, p(X, Y) | ~q(b, a) | p(a, b) | ~q(a, b) | p(Y, a))." +
                "cnf(c6, axiom, ~p(a, X))." +
                "cnf(c7, axiom, q(f(a), a))." +
                "cnf(c8, axiom, r(f(a)))." +
                "cnf(c9, axiom, p(X, Y)).";

        Lexer lex = new Lexer(input);
        c1 = Clause.parse(lex);
        c2 = Clause.parse(lex);
        c3 = Clause.parse(lex);
        c4 = Clause.parse(lex);
        c5 = Clause.parse(lex);
        c6 = Clause.parse(lex);
        c7 = Clause.parse(lex);
        c8 = Clause.parse(lex);
        c9 = Clause.parse(lex);
    }

    /** ***************************************************************
     */
    @Test
    public void testPredAbstraction() {
        ArrayList<PredAbstractionPair> p1 = new ArrayList<PredAbstractionPair>();
        ArrayList<PredAbstractionPair> p2 = new ArrayList<PredAbstractionPair>();
        ArrayList<PredAbstractionPair> p3 = new ArrayList<PredAbstractionPair>();
        ArrayList<PredAbstractionPair> p4 = new ArrayList<PredAbstractionPair>();

        p2.add(new PredAbstractionPair(true, "p"));

        p3.add(new PredAbstractionPair(true, "p"));
        p3.add(new PredAbstractionPair(true, "p"));
        p3.add(new PredAbstractionPair(true, "q"));

        p4.add(new PredAbstractionPair(false, "p"));
        p4.add(new PredAbstractionPair(true, "p"));

        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p1, p1));
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p2, p2));
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p3, p3));
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p4, p4));

        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p1, p2));
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p1, p3));
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p1, p4));

        System.out.println("p2: "  + p2);
        System.out.println("p3: "  + p3);
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p2, p3));
        assertTrue(PredAbstractionPair.predAbstractionIsSubSequence(p2, p4));

        System.out.println("p2: "  + p2);
        System.out.println("p1: "  + p1);
        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p2, p1));
        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p3, p1));
        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p4, p1));

        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p3, p2));
        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p4, p2));

        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p3, p4));
        assertFalse(PredAbstractionPair.predAbstractionIsSubSequence(p4, p3));
    }
}
