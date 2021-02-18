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
        ArrayList<Literal> example = Literal.parseLiteralList(lex);
        Literal l1 = example.get(0);  // first and smallest and fewest vars (tie with l4)
        Literal l2 = example.get(1);  // largest
        Literal l3 = example.get(2);  // most vars
        Literal l4 = example.get(3);  // fewest vars: 0, in tie with l1
        System.out.println("testLitSelect(): test literals: " + l1 + "  " + l2 + "  " + l3 + "  " + l4 + "  ");

        ArrayList<Literal> actual = LitSelection.firstLit(example);
        assertEquals(1,actual.size());  // always check to ensure there's just one result for this example
        System.out.println("testLitSelect(): first literal: " + actual);
        Literal expected = l1;
        if (actual.get(0).equals(expected))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,actual.get(0));

        LitSelection ls = new LitSelection();
        actual = ls.smallestLit(example);
        assertEquals(1,actual.size());
        System.out.println("testLitSelect(): smallest literal: " + actual);
        expected = l1;
        if (actual.get(0).equals(expected))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,actual.get(0));

        actual = ls.largestLit(example);
        assertEquals(1,actual.size());
        System.out.println("testLitSelect(): largest literal: " + actual);
        expected = l2;
        if (actual.get(0).equals(expected))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,actual.get(0));

        actual = ls.varSizeLit(example);
        assertEquals(1,actual.size());
        System.out.println("testLitSelect(): fewest variables: " + actual);
        expected = l4;
        if (actual.get(0).equals(expected))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,actual.get(0));

        actual = ls.eqResVarSizeLit(example);
        assertEquals(1,actual.size());
        System.out.println("testLitSelect(): smallest equational literal: " + actual);
        expected = l3;
        if (actual.get(0).equals(expected))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,actual.get(0));

        System.out.println("\ntestLitSelect(): from " + str2);
        lex = new Lexer(str2);
        example = Literal.parseLiteralList(lex);
        l1 = example.get(0);
        l2 = example.get(1);
        l3 = example.get(2);
        actual = ls.eqResVarSizeLit(example);
        assertEquals(1,actual.size());
        System.out.println("testLitSelect(): second example: smallest equational literal (or fewest vars): " + actual);
        expected = l3;
        if (actual.get(0).equals(expected))
            System.out.println("success");
        else
            System.out.println("fail");
        assertEquals(expected,actual.get(0));
    }

}
