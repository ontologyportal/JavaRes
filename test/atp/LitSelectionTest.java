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

public class LitSelectionTest {

    String str1 = "~p(a)|~p(f(X,g(a)))|X!=Y|~q(a,g(a))";
    String str2 = "~p(a)|~p(f(X,g(a)))|~q(a,g(a))";

    /** ***************************************************************
     */
    @Test
    public void testLitSelect() {

        System.out.println("testLitSelect(): from " + str1);
        Lexer lex = new Lexer(str1);
        ArrayList<Literal> ll1 = Literal.parseLiteralList(lex);
        Literal l1 = ll1.get(0);
        Literal l2 = ll1.get(1);
        Literal l3 = ll1.get(2);
        Literal l4 = ll1.get(3);

        ArrayList<Literal> ll = LitSelection.firstLit(ll1);
        assertEquals(1,ll.size());
        Literal l = ll.get(0);
        assertEquals(l1, l);

        LitSelection ls = new LitSelection();
        ll = ls.smallestLit(ll1);
        assertEquals(1,ll.size());
        l = ll.get(0);
        assertEquals(l1, l);

        ll = ls.largestLit(ll1);
        assertEquals(1,ll.size());
        l = ll.get(0);
        assertEquals(l2, l);

        ll = ls.varSizeLit(ll1);
        assertEquals(1,ll.size());
        l = ll.get(0);
        assertEquals(l4, l);

        ll = ls.eqResVarSizeLit(ll1);
        assertEquals(1,ll.size());
        l = ll.get(0);
        System.out.println("Should be equal: " + l3 + " and " + l);
        //assertEquals(l3, l);

        System.out.println("testLitSelect(): from " + str2);
        lex = new Lexer(str2);
        ll1 = Literal.parseLiteralList(lex);
        l1 = ll1.get(0);
        l2 = ll1.get(1);
        l3 = ll1.get(2);
        ll = ls.eqResVarSizeLit(ll1);
        assertEquals(1,ll.size());
        l = ll.get(0);
        assertEquals(l3, l);
    }

}
