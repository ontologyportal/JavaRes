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

public class ClauseSetTest {

    /** ***************************************************************
     */
    @Test
    public void testParse1() {

        System.out.println("---------------------");
        System.out.println("INFO in testParse1()");
        String spec2 = "cnf(humans_are_mortal,axiom,mortal(X)|~human(X)).\n" +
                "cnf(socrates_is_human,axiom,human(socrates)).\n" +
                "cnf(is_socrates_mortal,negated_conjecture,~mortal(socrates)).\n";
        Lexer lex = new Lexer(spec2);
        ClauseSet problem = new ClauseSet();
        problem.parse(lex);
        System.out.println("ClauseSet test.  Expected: ");
        System.out.println(spec2);
        System.out.println("Actual: ");
        System.out.println(problem);
        assertEquals(spec2.toString(),problem.toString());
        String expectedStr = "cnf(c4,plain,a|b).\ncnf(c5,plain,b).\ncnf(c6,plain,$false).\n";
    }

    /** ***************************************************************
     */
    @Test
    public void testParse2() {

        System.out.println("---------------------");
        System.out.println("INFO in testParse2()");
        String expectedStr = "cnf(c4,plain,a|b).\ncnf(c5,plain,b).\ncnf(c6,plain,$false).\n";
        Lexer lex = new Lexer(expectedStr);
        ClauseSet problem = new ClauseSet();
        problem.parse(lex);
        System.out.println("ClauseSet test.  Expected: ");
        System.out.println(expectedStr);
        System.out.println("Actual: ");
        System.out.println(problem);
        assertEquals(expectedStr,problem.toString());

    }

    /** ***************************************************************
     * Test that clause set initialization and parsing work.
     */
    @Test
    public void testClauseSetChanges() {

        System.out.println("---------------------");
        System.out.println("INFO in testClauseSetChanges()");
        ClauseSet clauses = ClauseSet.parseFromFile("/home/apease/ontology/TPTP-v7.3.0/Problems/PUZ/PUZ001-1.p");
        System.out.println("Clauses: " + clauses);
        int oldlen = clauses.clauses.size();
        Clause c = clauses.clauses.get(0);
        clauses.extractClause(c);
        System.out.println("Should be true: ");
        System.out.println(clauses.clauses.size() == oldlen-1);
        assertTrue(clauses.clauses.size() == oldlen-1);
        Signature sig = new Signature();
        clauses.collectSig(sig);
        System.out.println(sig);
    }
}
