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

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;
import static org.junit.Assert.*;
public class IndexedClauseSetTest {

    /** ***************************************************************
     */
    public static String spec = null;

    /** ***************************************************************
     */
    @Test
    public void testIndexedClauseSetChanges() {

        //Test that clause set initialization and parsing work.

        System.out.println("---------------------");
        String tptpHome = System.getenv("TPTP");
        String clausePath = Paths.get(tptpHome, "Problems", "PUZ", "PUZ001-1.p").toString();
        ClauseSet clauses = ClauseSet.parseFromFile(clausePath);
        System.out.println("testIndexedClauseSetChanges() clauses: \n" + clauses);
        IndexedClauseSet iclauses = new IndexedClauseSet();
        for (Clause clau : clauses.clauses)
            iclauses.addClause(clau);
        System.out.println("Indexed clauses: " + iclauses);
        int oldlen = iclauses.clauses.size();
        Clause c = iclauses.clauses.get(0);
        iclauses.extractClause(c);
        assertEquals(iclauses.clauses.size(), oldlen-1);
        Signature sig = iclauses.collectSig();
        System.out.println("Signature: " + sig);
    }

    /** ***************************************************************
     * Test the function returning all possible literal positions
     * of possible resolution partner vie indexing works. The indexed
     * version should return the clause/position pairs of all
     * literals with opposite polarity and the same top symbol as the
     * query literal.
     */
    @Test
    public void testResIndexedPositions() {

        System.out.println("---------------------");
        String tptpHome = System.getenv("TPTP");
        String clausePath = Paths.get(tptpHome, "Problems", "PUZ", "PUZ001-1.p").toString();
        ClauseSet clauses = ClauseSet.parseFromFile(clausePath);
        System.out.println("testResIndexedPositions() clauses: \n" + clauses);
        IndexedClauseSet iclauses = new IndexedClauseSet();
        for (Clause clau : clauses.clauses)
            iclauses.addClause(clau);
        Lexer lexer = new Lexer("hates(X,agatha)");
        Literal lit = Literal.parseLiteral(lexer);
        ArrayList<Clause> clauseres = new ArrayList<Clause>();
        ArrayList<Integer> indices = new ArrayList<>();
        System.out.println("IndexedClauseSetTest try getResolutionLiterals(): lit: " + lit);
        HashSet<KVPair> res = iclauses.getResolutionLiterals(lit);
        System.out.println("Six clauses expected: " + res);
        if (res.size() == 6)
            System.out.println("Success");
        else
            System.out.println("fail");
        assertEquals(res.size(), 6);
    }
}
