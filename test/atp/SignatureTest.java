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

public class SignatureTest {

    /** ***************************************************************
     * Test signature object.
     */
    @Test
    public  void test() {

        System.out.println("-----------------------------------------");
        System.out.println("Signature.test():");
        String s = "cnf(qg1_2,negated_conjecture,~product(X1, Y1, Z1)|~product(X2, Y2, Z1)|~product(Z2, Y1, X1)|~product(Z2, Y2, X2)|equalish(Y1, Y2)).";
        Clause c = Clause.string2Clause(s);
        Signature sig = new Signature();
        c.collectSig(sig);
        System.out.println("result: " + sig);
        sig = new Signature();

        sig.addFun("mult",2);
        sig.addFun("a", 0);
        sig.addPred("weird", 4);

        System.out.println(sig + " should be preds[wierd] and funs[mult,a]");
        System.out.println("all should be true:");
        System.out.println(sig.isPred("weird"));
        System.out.println(!sig.isPred("unknown"));
        System.out.println(!sig.isPred("a"));
        System.out.println(sig.isFun("a"));
        System.out.println(sig.isConstant("a"));
        System.out.println(!sig.isFun("unknown"));
        System.out.println(!sig.isFun("weird"));

        assertTrue(sig.getArity("a")==0);
        assertTrue(sig.getArity("weird")==4);

        assertTrue(sig.isPred("weird"));
        assertTrue(!sig.isPred("unknown"));
        assertTrue(!sig.isPred("a"));
        assertTrue(sig.isFun("a"));
        assertTrue(sig.isConstant("a"));
        assertTrue(!sig.isFun("unknown"));
        assertTrue(!sig.isFun("weird"));

        assertTrue(sig.getArity("a")==0);
        assertTrue(sig.getArity("weird")==4);


    }
}
