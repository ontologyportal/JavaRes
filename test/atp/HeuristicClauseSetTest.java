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
import java.util.*;
import static org.junit.Assert.*;
public class HeuristicClauseSetTest {

    /** ***************************************************************
     * ************ Define Strategies *****************
     */
    public static String spec = null;

    /** ***************************************************************
     * Test that clause set initialization and parsing work.
     */
    @Test
    public void testClauseSetChanges() {

        System.out.println("---------------------");
        ClauseSet clauses = ClauseSet.parseFromFile("/home/apease/EProver/fod_pi/PYTHON/EXAMPLES/PUZ001-1.p");
        System.out.println("testClauseSetChanges() clauses: \n" + clauses);
        int oldlen = clauses.length();
        Clause c = clauses.get(0);
        clauses.extractClause(c);
        System.out.println();
        System.out.println("should be equal: " + clauses.length() + " and " + (oldlen-1));
        if (clauses.length() == oldlen - 1)
            System.out.println("Success in testClauseSetChanges()");
        else
            System.out.println("fail");
        assertEquals(clauses.length(),oldlen - 1);
        System.out.println();
    }

    /** ***************************************************************
     * Test the evaluation and heuristic methods.
     */
    @Test
    public void testClauseSetHeuristics() {

        System.out.println("---------------------");
        System.out.println("testClauseSetHeuristics");
        ClauseSet input = ClauseSet.parseFromFile("/home/apease/EProver/fod_pi/PYTHON/EXAMPLES/PUZ001-1.p");
        System.out.println("file input: " + input);
        System.out.println();
        ClauseEvaluationFunction.setupEvaluationFunctions();
        HeuristicClauseSet cs = new HeuristicClauseSet(ClauseEvaluationFunction.PickGiven2);
        for (Clause c : input.clauses)
            cs.addClause(c);
        int parsed = cs.length();
        if (parsed == 12)
            System.out.println("Successful test, parsed clauses size = 12");
        else
            System.out.println("Error in expected length of clause, should be 12 but was: " + Integer.toString(parsed));
        assertEquals(12,parsed);
        System.out.println();

        Clause c1 = cs.extractBestByEval(1);
        if (c1.name.equals("agatha"))
            System.out.println("Successful selected clause: " + c1);
        else
            System.out.println("Error wrong selected clause: " + c1);
        assertEquals("agatha",c1.name);

        Clause c2 = cs.extractBestByEval(1);
        if (c2.name.equals("butler"))
            System.out.println("Successful selected clause: " + c2);
        else
            System.out.println("Error wrong selected clause: " + c2);
        assertEquals("butler",c2.name);

        Clause c3 = cs.extractFirst();
        if (c3.name.equals("charles"))
            System.out.println("Successful selected clause: " + c3);
        else
            System.out.println("Error wrong selected clause: " + c3);
        assertEquals("charles",c3.name);

        Clause c = cs.extractBestByEval(0);
        while (c != null)
            c = cs.extractBestByEval(0);
        System.out.println();

        System.out.println("================part2==========================");
        cs = new HeuristicClauseSet(ClauseEvaluationFunction.PickGiven2);
        for (Clause ccc : input.clauses)
            cs.addClause(ccc);
        c = cs.extractBest();
        while (c != null) {
            System.out.println(c);
            c = cs.extractBest();
        }
        c = cs.extractFirst();
        assert c == null;
        System.out.println("Successful testClauseSetHeuristics()");
        System.out.println();
    }

    /** ***************************************************************
     * Test the the resolution position function works.
     */
    @Test
    public void testResPositions() {

        System.out.println("---------------------");
        ClauseSet input = ClauseSet.parseFromFile("/home/apease/EProver/fod_pi/PYTHON/EXAMPLES/PUZ001-1.p");
        HeuristicClauseSet cs = new HeuristicClauseSet(ClauseEvaluationFunction.PickGiven2);
        for (Clause c : input.clauses)
            cs.addClause(c);
        ArrayList<Clause> clauseres = new ArrayList<Clause>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        Literal lit = new Literal();
        StreamTokenizer_s st = new StreamTokenizer_s(new StringReader("p"));
        // !!! lit.parseLiteral(st);
        HashSet<KVPair> res = cs.getResolutionLiterals(lit);
        System.out.println("res: " + res);
        System.out.println();
    }
}
