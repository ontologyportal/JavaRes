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

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DerivationTest {

    /***************************************************************
     */
    @BeforeClass
    public static void setUp() {
        System.out.println();
    }

    /** ***************************************************************
     * Test basic properties of derivations.
     */
    @Test
    public void testDerivable() {

        System.out.println("---------------");
        System.out.println("testDerivable()");
        Derivable o1 = new Derivable("o1",null);
        Derivable o2 = new Derivable("o2",null);
        Derivable o3 = new Derivable("o3",null);

        ArrayList<Derivable> ar = new ArrayList<>();
        ar.add(o1);
        System.out.println("o1: " + o1);
        ar.add(o2);
        System.out.println("o2: " + o2);
        o3.setDerivation(Derivation.flatDerivation("resolution", ar, ""));
        System.out.println("o3: " + o3);
        assertEquals(new ArrayList<>(),o1.getParents());
        assertEquals(new ArrayList<>(),o2.getParents());
        System.out.println("o3 parents (expect 2): " + o3.getParents().size());
        assertEquals(o3.getParents().size(), 2);

        System.out.println("o3: " + o3);
        System.out.println("o3 derivation: " + o3.derivation);
        ar = new ArrayList<>();
        ar.add(o1);
        o3.setDerivation(Derivation.flatDerivation("factor", ar,""));
        System.out.println(o3.derivation);
        System.out.println("o3 parents (expect 1): " + o3.getParents().size());
        assertEquals(o3.getParents().size(), 1);
        System.out.println("testDerivable(): Success");
    }

    /** ***************************************************************
     *   Test basic proof extraction.
     */
    @Test
    public void testProofExtraction() {

        System.out.println("---------------");
        System.out.println("testProofExtraction()");
        Derivable o1 = new Derivable("o1",null);
        Derivable o2 = new Derivable("o2",null);
        Derivable o3 = new Derivable("o3",null);
        Derivable o4 = new Derivable("o4",null);
        Derivable o5 = new Derivable("o5",null);
        Derivable o6 = new Derivable("o6",null);
        Derivable o7 = new Derivable("o7",null);
        o1.setDerivation(new Derivation("eq_axiom",null,""));
        System.out.println("o1.derivation: (expect eq_axiom): " + o1.derivation);
        o2.setDerivation(new Derivation("input",null,""));
        ArrayList<Derivable> ar = new ArrayList<>();
        ar.add(o1);
        o3.setDerivation(Derivation.flatDerivation("factor", ar,""));
        ar = new ArrayList<>();
        ar.add(o3);
        o4.setDerivation(Derivation.flatDerivation("factor", ar,""));
        ar = new ArrayList<>();
        ar.add(o1);
        ar.add(o2);
        o5.setDerivation(Derivation.flatDerivation("resolution", ar,""));
        ar = new ArrayList<>();
        ar.add(o5);
        o6.setDerivation(new Derivation("reference", ar,""));
        ar = new ArrayList<>();
        ar.add(o5);
        ar.add(o1);
        o7.setDerivation(Derivation.flatDerivation("resolution", ar,""));
        System.out.println("o7: " + o7);
        ArrayList<Derivable> proof = o7.orderedDerivation();
        System.out.println(proof);
        assertEquals(proof.size(),4);
        assertTrue(proof.contains(o1));
        assertTrue(proof.contains(o2));
        assertTrue(proof.contains(o5));
        assertTrue(proof.contains(o7));
        System.out.println("testProofExtraction(): Success");
    }

    /** ***************************************************************
     * Test derivation output functions.
     */
    @Test
    public void testOutput() {

        System.out.println("---------------");
        System.out.println("testOutput()");
        Derivable o1 = new Derivable();
        Derivable o2 = new Derivable();
        Derivable o3 = new Derivable();
        Derivable o4 = new Derivable();
        o1.setDerivation(new Derivation("eq_axiom",null,""));
        o2.setDerivation(new Derivation("input",null,""));
        ArrayList<Derivable> ar = new ArrayList<>();
        ar.add(o1);
        ar.add(o2);
        o3.setDerivation(Derivation.flatDerivation("resolution",ar,""));
        Derivable.enableDerivationOutput();
        assertTrue(o2.strDerivation() != "");
        assertTrue(o3.strDerivation() != "");
        assertTrue(o4.strDerivation() == "");
        Derivable.disableDerivationOutput();
        assertTrue(o3.strDerivation() == "");
        assertTrue(o4.strDerivation() == "");
        Derivable.toggleDerivationOutput();
        assertTrue(o3.strDerivation() != "");
        assertTrue(o4.strDerivation() == "");
        System.out.println("testOutput(): Success");
    }

}
