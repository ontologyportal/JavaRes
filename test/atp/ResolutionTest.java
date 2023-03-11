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

public class ResolutionTest {
    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    public static Clause c1 = new Clause();
    public static Clause c2 = new Clause();
    public static Clause c3 = new Clause();
    public static Clause c4 = new Clause();
    public static Clause c5 = new Clause();
    public static Clause c6 = new Clause();
    public static Clause c7 = new Clause();
    public static Clause c8 = new Clause();
    public static Clause c9 = new Clause();
    public static Clause c10 = new Clause();
    public static Clause c11 = new Clause();
    public static Clause c12 = new Clause();
    public static Clause c13 = new Clause();

    /** ***************************************************************
     * Setup function for resolution testing
     */
    @BeforeClass
    public static void setup() {

        String spec = "cnf(c1,axiom,p(a, X)|p(X,a)).\n" +
                "cnf(c2,axiom,~p(a,b)|p(f(Y),a)).\n" +
                "cnf(c3,axiom,p(Z,X)|~p(f(Z),X0)).\n" +
                "cnf(c4,axiom,p(X,X)|p(a,f(Y))).\n" +
                "cnf(ftest,axiom,p(X)|~q|p(a)|~q|p(Y)).";
        Lexer lex = new Lexer(spec);
        c1 = Clause.parse(lex);
        c2 = Clause.parse(lex);
        c3 = Clause.parse(lex);
        c4 = Clause.parse(lex);
        c5 = Clause.parse(lex);
        System.out.println("Resolution.setup(): expected clauses:");
        System.out.println(spec);
        System.out.println("actual:");
        System.out.println(c1);
        System.out.println(c2);
        System.out.println(c3);
        System.out.println(c4);
        System.out.println(c5);

        String spec2 = "cnf(not_p,axiom,~p(a)).\n" +
                "cnf(taut,axiom,p(X4)|~p(X4)).\n";
        lex = new Lexer(spec2);
        c6 = Clause.parse(lex);
        c7 = Clause.parse(lex);

        String spec3 = "cnf(00019,plain,disjoint(X212, null_class))." +
                "cnf(00020,plain,~disjoint(X271, X271)|~member(X270, X271))." +
                "cnf(c00025,axiom,( product(X1,X1,X1) ))." +
                "cnf(c00030,plain, ( ~ product(X354,X355,e_1) | ~ product(X354,X355,e_2) ))." +
                "cnf(c00001,axiom,~killed(X12, X13)|hates(X12, X13))." +
                "cnf(c00003,axiom,~killed(X3, X4)|~richer(X3, X4)).";
        lex = new Lexer(spec3);
        c8 = Clause.parse(lex);
        c9 = Clause.parse(lex);
        c10 = Clause.parse(lex);
        c11 = Clause.parse(lex);
        c12 = Clause.parse(lex);
        c13 = Clause.parse(lex);
    }

    /** ***************************************************************
     * Test resolution
     */
    @Test
    public void testResolution() {

        System.out.println("Resolution.testResolution()");

        Clause res1 = Resolution.resolution(c1, 0, c2,0);
        assert res1 != null;
        System.out.println("expected result: cnf(c1,plain,p(b,a)|p(f(Y),a)). result: " + res1);

        Clause res2 = Resolution.resolution(c1, 0, c3,0);
        assert res2 == null;
        System.out.println("Resolution.testResolution(): successful (null) result: " + res2);

        Clause res3 = Resolution.resolution(c2, 0, c3,0);
        assert res3 != null;
        System.out.println("Expected result: cnf(c2,plain,p(f(Y),a)|~p(f(a),X0)). result: " + res3);

        Clause res4 = Resolution.resolution(c1, 0, c3,1);
        assert res4 == null;
        System.out.println("Resolution.testResolution(): successful (null) result: " + res4);

        Clause res5 = Resolution.resolution(c6, 0, c7,0);
        assert res5 != null;
        System.out.println("Resolution.testResolution(): cnf(~p(a)) successful result: " + res5);

        Clause res6 = Resolution.resolution(c8, 0, c9,0);
        assert res6 != null;
        System.out.println("Resolution.testResolution(): ~member(X270, null_class) successful result: " + res6);

        Clause res7 = Resolution.resolution(c10, 0, c11,0);
        assert res7 != null;
        System.out.println("Resolution.testResolution(): ~product(e_1,e_1,e_2) successful result: " + res7);

        Clause res8 = Resolution.resolution(c12, 0, c13,0);
        assert res8 == null;
        System.out.println("Resolution.testResolution(): successful null result: " + res8);
    }

    /** ***************************************************************
     * Test the factoring inference.
     */
    @Test
    public void testFactoring() {

        System.out.println("Resolution.testFactoring()");
        Clause f1 = Resolution.factor(c1,0,1);
        assert f1 != null;
        assert f1.length()==1;
        System.out.println("Expected result: cnf(c0,plain,p(a,a)). Factor:" + f1);

        Clause f2 = Resolution.factor(c2,0,1);
        assert f2 == null;
        System.out.println("Resolution.testFactoring(): successful (null) result: Factor:" + f2);

        Clause f4 = Resolution.factor(c4,0,1);
        assert f4 == null;
        System.out.println("Resolution.testFactoring(): successful (null) result: Factor:" + f4);

        Clause f5 = Resolution.factor(c5,1,3);
        assert f5 != null;
        System.out.println("Resolution.testFactoring(): Expected result: cnf(c2,plain,p(X)|~q|p(a)|p(Y)). Factor:" + f5);
    }
}
