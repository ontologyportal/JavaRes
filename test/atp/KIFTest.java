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

public class KIFTest {

    @Test
    public void test() {

        System.out.println("KIFTest.test -------------------------");
        KIF.init();
        String ex = "![X]:(a(x) | ~a=b)";
        BareFormula bf = BareFormula.string2form(ex);
        String result = KIF.format(bf.toKIFString());
        System.out.println("Result: " + result);
        String expected = "(forall (?X)\n" +
                "  (or\n" +
                "    (a x)\n" +
                "    (not\n" +
                "      (equals a b))))";
        assertEquals(expected,result);
    }
}
