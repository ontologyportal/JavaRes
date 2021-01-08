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

public class EqAxiomsTest {

    /** ***************************************************************
     * Unit Tests
     ****************************************************************/

    /** ***************************************************************
     * Test that the equivalence axioms are generated (or at least
     * provide coverage).
     */
    @Test
    public void testEquivAxioms() {

        System.out.println("INFO in EqAxioms.testEquivAxioms(): all should be true");
        ArrayList<Clause> ax = EqAxioms.generateEquivAxioms();
        System.out.println(ax);
        System.out.println(ax.size() == 3);
        assertEquals(3,ax.size());
    }

    /** ***************************************************************
     * Test variable and premise generation.
     */
    @Test
    public void testVarStuff() {

        System.out.println("INFO in EqAxioms.testVarStuff(): all should be true");
        String vars = EqAxioms.generateVarList("X", 4);
        System.out.println(vars.indexOf("X1") >= 0);
        assertTrue(vars.indexOf("X1") >= 0);
        System.out.println(vars.indexOf("X4") >= 0);
        assertTrue(vars.indexOf("X4") >= 0);
        System.out.println(vars.indexOf("X5") < 0);
        assertTrue(vars.indexOf("X5") < 0);
        System.out.println(vars.indexOf("Y1") < 0);
        assertTrue(vars.indexOf("Y1") < 0);
        System.out.println(vars.length() == 11);
        assertEquals(11,vars.length());
        System.out.println(vars);

        ArrayList<Literal> lits = EqAxioms.generateEqPremise(3);
        System.out.println(lits.size() == 3);
        assertEquals(3,lits.size());
        System.out.println(lits);
    }

    /** ***************************************************************
     * Test that compatibility axioms are generated as expected.
     */
    @Test
    public void testCompatibility() {

        System.out.println("INFO in EqAxioms.testCompatibility(): all should be true");
        Clause ax = EqAxioms.generateFunCompatAx("f", 3);
        System.out.println(ax.literals.size() == 4);
        assertEquals(4,ax.literals.size());
        System.out.println(ax);

        ax = EqAxioms.generatePredCompatAx("p", 5);
        System.out.println(ax.literals.size() == 7);
        assertEquals(7,ax.literals.size());
        System.out.println(ax);

        Signature sig = new Signature();
        sig.addFun("f", 2);
        sig.addPred("p", 3);
        sig.addFun("a", 0);

        ArrayList<Clause> tmp = EqAxioms.generateCompatAxioms(sig);
        // Note: No axiom for a
        System.out.println(tmp.size() == 2);
        assertEquals(2,tmp.size());
    }
}
