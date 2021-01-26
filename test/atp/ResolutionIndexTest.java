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

public class ResolutionIndexTest {

    // Unit test class for clauses. Test clause and literal functionality.
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
     *  Test inserting and removal of clauses into the resolution index.
     */
    @Test
    public void testResolutionInsertRemove() {

        System.out.println("-------------------------------");
        System.out.println("testResolutionInsertRemove()");
        ResolutionIndex index = new ResolutionIndex();
        index.insertClause(c1);
        index.insertClause(c2);

        System.out.println("posIdx: " + index.posIdx);
        assertEquals(index.posIdx.size(), 1);
        assertEquals(index.posIdx.get("p").size(), 3);
        System.out.println("negIdx: " + index.negIdx);
        assertEquals(index.negIdx.size(), 1);
        assertEquals(index.negIdx.get("p").size(), 1);

        index.insertClause(c3);
        System.out.println("Insert " + c3);
        System.out.println("posIdx: " + index.posIdx);
        assertEquals(index.posIdx.size(), 2);
        assertEquals(index.posIdx.get("p").size(), 3);
        assertEquals(index.posIdx.get("q").size(), 1);

        System.out.println("negIdx: " + index.negIdx);
        assertEquals(index.negIdx.size(), 2);
        assertEquals(index.negIdx.get("p").size(), 1);
        assertEquals(index.negIdx.get("q").size(), 1);

        index.removeClause(c3);
        System.out.println("Removed " + c3);
        System.out.println("posIdx: " + index.posIdx);
        System.out.println("negIdx: " + index.negIdx);
        assertEquals(index.posIdx.size(), 2);
        assertEquals(index.posIdx.get("p").size(), 3);
        assertEquals(index.posIdx.get("q").size(), 0);

        System.out.println("negIdx: " + index.negIdx);
        assertEquals(index.negIdx.size(), 2);
        assertEquals(index.negIdx.get("p").size(), 1);
        assertEquals(index.negIdx.get("q").size(), 0);
    }

    /** ***************************************************************
     * Test actual retrieval of literal occurrences from the index.
     */
    @Test
    public void testResolutionRetrieval() {

        System.out.println("-------------------------------");
        System.out.println("testResolutionRetrieval()");

        ResolutionIndex index = new ResolutionIndex();
        index.insertClause(c1);
        index.insertClause(c2);
        index.insertClause(c3);
        index.insertClause(c4);
        index.insertClause(c5);

        Literal lit = c6.getLiteral(0);
        HashSet<KVPair> cands = index.getResolutionLiterals(lit);
        System.out.println("literal: " + lit);
        System.out.println("index: " + index);
        System.out.println("resolution literals: " + cands);
        System.out.println("expected size: " + 8);
        assertEquals(cands.size(), 8);
        for (KVPair kvp : cands) {
            Literal l = kvp.c.getLiteral(kvp.value);
            assertEquals(l.isNegative(), !lit.isNegative());
            assertEquals(l.atom.getFunc(), lit.atom.getFunc());
        }
        lit = c7.getLiteral(0);
        cands = index.getResolutionLiterals(lit);
        System.out.println(cands);
        assertEquals(cands.size(), 3);
        for (KVPair kvp : cands) {
            Literal l = kvp.c.getLiteral(kvp.value);
            assertEquals(l.isNegative(), !lit.isNegative());
            assertEquals(l.atom.getFunc(), lit.atom.getFunc());
        }
        lit = c8.getLiteral(0);
        cands = index.getResolutionLiterals(lit);
        System.out.println(cands);
        assertEquals(cands.size(),0);


    }
}
