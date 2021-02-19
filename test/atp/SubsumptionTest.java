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

public class SubsumptionTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static Clause c1 = null;
    public static Clause c2 = null;
    public static Clause c3 = null;
    public static Clause c4 = null;
    public static Clause c5 = null;
    public static Clause c10 = null;
    public static Clause c11 = null;
    /** ***************************************************************
     * Setup function for resolution testing
     */
    @BeforeClass
    public static void setup() {

        String spec = "cnf(axiom, c1, $false).\n" +
                "cnf(axiom, c2, p(a)).\n" +
                "cnf(axiom, c3, p(X)).\n" +
                "cnf(axiom, c4, p(a)|q(f(X))).\n" +
                "cnf(axiom, c5, p(a)|q(f(b))|p(X)).\n" +
                "cnf(clause9,negated_conjecture,ssSkC0|chevy(skc12)).\n" +
                "cnf(clause25,negated_conjecture,ssSkC0|in(skc14,skc15)).\n";

        Lexer lex = new Lexer(spec);
        c1 = Clause.parse(lex);
        System.out.println(c1);
        c2 = Clause.parse(lex);
        System.out.println(c2);
        c3 = Clause.parse(lex);
        System.out.println(c3);
        c4 = Clause.parse(lex);
        System.out.println(c4);
        c5 = Clause.parse(lex);
        System.out.println(c5);

        c10 = Clause.parse(lex);
        c11 = Clause.parse(lex);
    }

    /** ***************************************************************
     * Test subsumption.
     */
    @Test
    public void testSubsumption() {

        System.out.println("---------------------");
        System.out.println("INFO in Subsumption.testSubsumption(): all should be true");
        System.out.println(c1 + " subsumes " + c1 + " = " + Subsumption.subsumes(c1,c1));
        assertTrue(Subsumption.subsumes(c1,c1));
        System.out.println(c2 + " subsumes " + c2 + " = " + Subsumption.subsumes(c2,c2));
        assertTrue(Subsumption.subsumes(c2,c2));
        System.out.println(c3 + " subsumes " + c3 + " = " + Subsumption.subsumes(c3,c3));
        assertTrue(Subsumption.subsumes(c3,c3));
        System.out.println(c4 + " subsumes " + c4 + " = " + Subsumption.subsumes(c4,c4));
        assertTrue(Subsumption.subsumes(c4,c4));
        System.out.println(c1 + " subsumes " + c2 + " = " + Subsumption.subsumes(c1,c2));
        assertTrue(Subsumption.subsumes(c1,c2));
        System.out.println(c2 + " does not subsume " + c1 + " = " + !Subsumption.subsumes(c2,c1));
        assertTrue(!Subsumption.subsumes(c2,c1));
        System.out.println(c2 + " does not subsume " + c3 + " = " + !Subsumption.subsumes(c2,c3));
        assertTrue(!Subsumption.subsumes(c2,c3));
        System.out.println(c3 + " subsumes " + c2 + " = " + Subsumption.subsumes(c3,c2));
        assertTrue(Subsumption.subsumes(c3,c2));
        System.out.println(c4 + " subsumes " + c5 + " = " + Subsumption.subsumes(c4,c5));
        assertTrue(Subsumption.subsumes(c4,c5));
        System.out.println(c5 + " does not subsume " + c4 + " = " + !Subsumption.subsumes(c5,c4));
        assertTrue(!Subsumption.subsumes(c5,c4));
    }


    /** ***************************************************************
     */
    @Test
    public void testSubsumption2() {

        System.out.println("--------------------");
        System.out.println("testSubsumption2");
        System.out.println(c10);
        System.out.println(c11);
        System.out.println("Subsumes: " + Subsumption.subsumes(c10,c11));
        assertTrue(Subsumption.subsumes(c10,c11));
    }
}
