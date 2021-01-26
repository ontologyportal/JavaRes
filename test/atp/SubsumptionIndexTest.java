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

public class SubsumptionIndexTest {

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

        System.out.println();
        String spec = "cnf(c1,axiom,p(a, X)|p(X,a))." +
                "cnf(c2,axiom,~p(a,b)|p(f(Y),a))." +
                "cnf(c3,axiom,q(Z,X)|~q(f(Z),X0))." +
                "cnf(c4,axiom,p(X,X)|p(a,f(Y)))." +
                "cnf(c5,axiom,p(X,Y)|~q(b,a)|p(a,b)|~q(a,b)|p(Y,a))." +
                "cnf(c6,axiom,~p(a,X))." +
                "cnf(c7,axiom, q(f(a),a))." +
                "cnf(c8,axiom, r(f(a)))." +
                "cnf(c9,axiom, p(X,Y)).";
            Lexer lex = new Lexer(spec);
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
    public void testSubsumptionIndex() {

        SubsumptionIndex index = new SubsumptionIndex();

        assertFalse(index.isIndexed(c1));
        assertFalse(index.isIndexed(c6));
        index.insertClause(c1);
        index.insertClause(c2);
        index.insertClause(c3);
        index.insertClause(c4);
        index.insertClause(c5);
        index.insertClause(c6);
        System.out.println(index.predAbstrArr);
        assertTrue(index.isIndexed(c1));
        assertTrue(index.isIndexed(c2));
        assertTrue(index.isIndexed(c3));
        assertTrue(index.isIndexed(c4));
        assertTrue(index.isIndexed(c5));
        assertTrue(index.isIndexed(c6));

        index.removeClause(c1);
        index.removeClause(c5);
        index.removeClause(c3);
        System.out.println(index.predAbstrArr);
        assertFalse(index.isIndexed(c1));
        assertTrue(index.isIndexed(c2));
        assertFalse(index.isIndexed(c3));
        assertTrue(index.isIndexed(c4));
        assertFalse(index.isIndexed(c5));
        assertTrue(index.isIndexed(c6));

        index.insertClause(c3);
        index.insertClause(c1);
        index.insertClause(c5);
        index.insertClause(c9);
        System.out.println(index.predAbstrArr);
        assertTrue(index.isIndexed(c1));
        assertTrue(index.isIndexed(c2));
        assertTrue(index.isIndexed(c3));
        assertTrue(index.isIndexed(c4));
        assertTrue(index.isIndexed(c5));
        assertTrue(index.isIndexed(c6));
        assertTrue(index.isIndexed(c9));

        ArrayList<Clause> cands = index.getSubsumingCandidates(c1);
        System.out.println(cands);
        assertEquals(cands.size(), 3);
        cands = index.getSubsumingCandidates(c9);
        System.out.println(cands);
        assertEquals(cands.size(), 1);

        cands = index.getSubsumedCandidates(c9);
        System.out.println(cands);
        assertEquals(cands.size(), 5);

        cands = index.getSubsumedCandidates(c8);
        System.out.println(cands);
        assertEquals(cands.size(), 0);

        cands = index.getSubsumedCandidates(c5);
        System.out.println(cands);
        assertEquals(cands.size(), 1);
    }
}
