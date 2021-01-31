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

public class ClausifierTest {

    /** ***************************************************************
     * *************** Unit Tests ******************
     */

    /** ***************************************************************
     */
    @Test
    public void testRemoveImpEq() {

        System.out.println();
        System.out.println("================== testRemoveImpEq ======================");
        BareFormula form = BareFormula.string2form("a=>b");
        System.out.println("input: " + form);
        form = Clausifier.removeImpEq(form);
        System.out.println(form);
        String expected = "((~a)|b)";
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("a<=>b");
        System.out.println("input: " + form);
        form = Clausifier.removeImpEq(form);
        System.out.println(form);
        expected = "(((~a)|b)&((~b)|a))";
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))<=>q(g(a),X))");
        System.out.println("input: " + form);
        form = Clausifier.removeImpEq(form);
        System.out.println(form);
        expected = "(((~(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y))))))|q(g(a),X))&((~q(g(a),X))|(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))))";
        assertEquals(expected,form.toString());
        System.out.println();
    }

    /** ***************************************************************
     */
    @Test
    public void testMoveQuantifiersLeft() {

        System.out.println();
        System.out.println("================== testMoveQuantifiersLeft ======================");
        BareFormula form = BareFormula.string2form("p|![X]:q(X)");

        System.out.println("input: " + form);
        form = Clausifier.moveQuantifiersLeft(form);
        System.out.println("actual: " + form);
        String expected = "(![X]:(p|q(X)))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~((![X]:a(X)) | b(X))");
        System.out.println("input: " + form);
        form = Clausifier.moveQuantifiersLeft(form);
        System.out.println("result: " + form);
        expected = "(![X]:(~(a(X)|b(X))))";
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~(((![X]:(a(X)|b(X))))|(?[Y]:(?[Z]:p(Y,f(Z)))))");
        System.out.println("input: " + form);
        form = Clausifier.moveQuantifiersLeft(form);
        System.out.println("result: " + form);
        expected = "(![X]:(?[Y]:(?[Z]:(~((a(X)|b(X))|p(Y,f(Z)))))))";
        assertEquals(expected,form.toString());
        System.out.println();
    }

    /** ***************************************************************
     */
    @Test
    public void testMoveNegationIn() {

        System.out.println();
        System.out.println("================== testMoveNegationIn ======================");
        BareFormula form = BareFormula.string2form("~(p | q)");

        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        String expected = "(~p&~q)";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~(p & q)");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        expected = "(~p|~q)";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~![X]:p");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        expected = "(?[X]:~p)";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~?[X]:p");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        expected = "(![X]:~p)";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~~p");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        expected = "p";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("(~(?[Y]:p(X, f(Y))))");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        expected = "(![Y]:~p(X,f(Y)))";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("(~(?[X]:(?[Y]:p(X, f(Y)))))");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        System.out.println("expected result: (![X]:(![Y]:~p(X,f(Y))))");
        expected = "(![X]:(![Y]:~p(X,f(Y))))";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("~(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X, f(Y)))))");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        System.out.println("result should be ( ((?[X]:~a(X))&~b(X)) & (![X]:(![Y]:~p(X, f(Y)))) ): ");
        expected = "(((?[X]:~a(X))&~b(X))&(![X]:(![Y]:~p(X,f(Y)))))";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();
/*
        form = BareFormula.string2form("(((~(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X, f(Y))))))|q(g(a), X))&((~q(g(a), X))|(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X, f(Y)))))))");
        System.out.println("input: " + form);
        form = Clausifier.moveNegationIn(form);
        System.out.println("expected: ( ( ((( (?[X]:~a(X)) & ~b(X)) & (![X]:(![Y]:~p(X, f(Y)))) )) | q(g(a), X)) & " +
                "((~q(g(a), X)) | (((![X]:a(X))|b(X)) | (?[X]:(?[Y]:p(X, f(Y)))) )))");
        expected = "((((((?[X]:~a(X))&~b(X))&(![X]:(![Y]:~p(X,f(Y))))))|q(g(a),X))&" +
                                "((~q(g(a),X))|(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))))";
        System.out.println("expected : " + expected);
        System.out.println("result : " + form);
        assertEquals(expected,form.toString());
        System.out.println();
        */
    }

    /** ***************************************************************
     */
    @Test
    public void testStandardizeVariables() {

        System.out.println();
        System.out.println("================== testStandardizeVariables ======================");
        BareFormula form = BareFormula.string2form("~((![X]:(a(X) | b(X))))");
        System.out.println("input: " + form);
        Clausifier.counterReset();
        form = Clausifier.standardizeVariables(form);
        String expected = "(~(![VAR0]:(a(VAR0)|b(VAR0))))";
        System.out.println("result should be : " + expected);
        System.out.println("actual: " + form);
        assertEquals(expected,form.toString());
        System.out.println();
/*
        form = BareFormula.string2form("(((~(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X, f(Y))))))|q(g(a), X))&((~q(g(a), X))|(((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X, f(Y)))))))");
        System.out.println("input: " + form);
        Clausifier.counterReset();
        form = Clausifier.standardizeVariables(form);
        expected = "(((~(((![VAR1]:a(VAR1))|b(VAR1))|(?[VAR2]:(?[VAR3]:p(VAR2, f(VAR3))))))|q(g(a), VAR2))&((~q(g(a), VAR2))|(((![VAR4]:a(VAR4))|b(VAR4))|(?[VAR5]:(?[VAR6]:p(VAR5, f(VAR6)))))))";
        System.out.println("result should be : " + expected);
        System.out.println("actual: "+ form);
        assertEquals(expected,form.toString());
        System.out.println();
 */
    }

    /** ***************************************************************
     */
    @Test
    public void testSkolemization() {

        Clausifier.varCounter = 0;
        System.out.println();
        System.out.println("================== testSkolemization ======================");
        BareFormula form = BareFormula.string2form("(?[VAR0]:(![VAR3]:(![VAR2]:(?[VAR5]:(![VAR1]:(?[VAR4]:((((~a(VAR0)&~b(X))&~p(VAR2, f(VAR1)))|q(g(a), X))&(~q(g(a), X)|((a(VAR3)|b(X))|p(VAR5, f(VAR4)))))))))))");
        System.out.println("input: " + form);
        form = Clausifier.skolemization(form);
        System.out.println("actual: " + form);
        String expected = "(![VAR3]:(![VAR2]:(![VAR1]:(((((~a(skf2))&(~b(X)))&(~p(VAR2,f(VAR1))))|q(g(a),X))&((~q(g(a),X))|((a(VAR3)|b(X))|p(skf1(VAR2,VAR3),f(skf0(VAR1,VAR2,VAR3)))))))))";
        System.out.println("expected: " + expected);
        if (expected.equals(form.toString()))
            System.out.println("success");
        else
            System.out.println("fail");
        assertTrue(expected.equals(form.toString()));
        System.out.println();
    }

    /** ***************************************************************
     */
    @Test
    public void testDistribute() {

        System.out.println();
        System.out.println("================== testDistribute ======================");
        BareFormula form = BareFormula.string2form("(a & b) | c");

        System.out.println("input: " + form);
        form = Clausifier.distributeAndOverOr(form);
        String expected = "((a|c)&(b|c))";
        System.out.println("expected : " + expected);
        System.out.println("actual: " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("(a & b) | (c & d)");
        System.out.println("input: " + form);
        form = Clausifier.distributeAndOverOr(form);
        System.out.println("result should be : ");
        expected = "(((c|a)&(d|a))&((c|b)&(d|b)))";
        System.out.println("expected : " + expected);
        System.out.println("actual: " + form);
        assertEquals(expected,form.toString());
        System.out.println();

        form = BareFormula.string2form("(((~holdsAt(VAR2, VAR1)|releasedAt(VAR2, plus(VAR1, n1)))|" +
                "(happens(skf3, VAR1)&terminates(skf3, VAR2, VAR1)))|holdsAt(VAR2, plus(VAR1, n1)))");
        System.out.println("input: " + form);
        form = Clausifier.distributeAndOverOr(form);
        String result = form.toKIFString();
        expected = "(and (or (or (happens skf3 ?VAR1) (or (not (holdsAt ?VAR2 ?VAR1)) (releasedAt ?VAR2 (plus ?VAR1 n1)))) (holdsAt ?VAR2 (plus ?VAR1 n1))) " +
                "(or (or (terminates skf3 ?VAR2 ?VAR1) (or (not (holdsAt ?VAR2 ?VAR1)) (releasedAt ?VAR2 (plus ?VAR1 n1)))) (holdsAt ?VAR2 (plus ?VAR1 n1))))";
        System.out.println("expected : " + expected);
        System.out.println("actual: " + result);
        assertEquals(expected,result);
        System.out.println();
    }

    /** ***************************************************************
     */
    public void testClausificationSteps(String s) {

        KIF.init();
        Clausifier.varCounter = 0;
        System.out.println();
        System.out.println("================== testClausification ======================");
        System.out.println("input: " + s);
        BareFormula form = BareFormula.string2form(s);
        System.out.println("input as BareFormula: " + form);
        String expected = "(![Fluent]:(![Time]:(((holdsAt(Fluent,Time)&(~releasedAt(Fluent,plus(Time,n1))))&" +
                "(~(?[Event]:(happens(Event,Time)&terminates(Event,Fluent,Time)))))=>" +
                "holdsAt(Fluent,plus(Time,n1)))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form =  Clausifier.removeImpEq(form);
        System.out.println("after Remove Implications and Equivalence: " + form);
        expected = "(![Fluent]:(![Time]:((~((holdsAt(Fluent,Time)&(~releasedAt(Fluent,plus(Time,n1))))&" +
                "(~(?[Event]:(happens(Event,Time)&terminates(Event,Fluent,Time))))))|" +
                "holdsAt(Fluent,plus(Time,n1)))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = Clausifier.moveNegationIn(form);
        System.out.println("after Move Negation In: " + form);
        expected = "(![Fluent]:(![Time]:(((~holdsAt(Fluent,Time)|releasedAt(Fluent,plus(Time,n1)))|" +
                "(?[Event]:(happens(Event,Time)&terminates(Event,Fluent,Time))))|holdsAt(Fluent,plus(Time,n1)))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = Clausifier.standardizeVariables(form);
        System.out.println("after Standardize Variables: " + form);
        expected = "(![VAR2]:(![VAR1]:(((~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1)))|(?[VAR0]:(happens(VAR0,VAR1)&" +
                "terminates(VAR0,VAR2,VAR1))))|holdsAt(VAR2,plus(VAR1,n1)))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = Clausifier.moveQuantifiersLeft(form);
        System.out.println("after Move Quantifiers: " + form);
        expected = "(![VAR2]:(![VAR1]:(?[VAR0]:(((~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1)))|(happens(VAR0,VAR1)&" +
                "terminates(VAR0,VAR2,VAR1)))|holdsAt(VAR2,plus(VAR1,n1))))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = Clausifier.skolemization(form);
        System.out.println("after Skolemization: " + form);
        expected = "(![VAR2]:(![VAR1]:(((~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1)))|(happens(skf3(VAR1,VAR2),VAR1)&" +
                "terminates(skf3(VAR1,VAR2),VAR2,VAR1)))|holdsAt(VAR2,plus(VAR1,n1)))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = Clausifier.removeUQuant(form);
        System.out.println("after remove universal quantifiers: " + form);
        expected = "(((~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1)))|(happens(skf3(VAR1,VAR2),VAR1)&" +
                "terminates(skf3(VAR1,VAR2),VAR2,VAR1)))|holdsAt(VAR2,plus(VAR1,n1)))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        form = Clausifier.distributeAndOverOr(form);
        System.out.println("after Distribution: " + form);
        expected = "(((happens(skf3(VAR1,VAR2),VAR1)|(~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1))))|holdsAt(VAR2,plus(VAR1,n1)))&" +
                "((terminates(skf3(VAR1,VAR2),VAR2,VAR1)|(~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1))))|holdsAt(VAR2,plus(VAR1,n1))))";
        System.out.println("expected: " + expected);
        assertEquals(expected,form.toString());
        System.out.println();

        ArrayList<BareFormula> forms = Clausifier.separateConjunctions(form);
        System.out.println("after separation: " + forms);
        expected = "[((happens(skf3(VAR1,VAR2),VAR1)|(~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1))))|holdsAt(VAR2,plus(VAR1,n1))), " +
                "((terminates(skf3(VAR1,VAR2),VAR2,VAR1)|(~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1))))|holdsAt(VAR2,plus(VAR1,n1)))]";
        System.out.println("expected: " + expected);
        assertEquals(expected,forms.toString());
        System.out.println();

        ArrayList<Clause> clauses = Clausifier.flattenAll(forms);
        System.out.println("after flattening: " + clauses);
        expected = "[cnf(cnf0,axiom,happens(skf3(VAR1,VAR2),VAR1)|~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1))|holdsAt(VAR2,plus(VAR1,n1)))., " +
                "cnf(cnf1,axiom,terminates(skf3(VAR1,VAR2),VAR2,VAR1)|~holdsAt(VAR2,VAR1)|releasedAt(VAR2,plus(VAR1,n1))|holdsAt(VAR2,plus(VAR1,n1))).]";
        System.out.println("expected: " + expected);
        assertEquals(expected,clauses.toString());
        System.out.println();
    }

    /** ***************************************************************
     */
    @Test
    public void testClausification() {

        Clausifier.varCounter = 0;
        testClausificationSteps("(![Fluent]:(![Time]:(((holdsAt(Fluent, Time)&(~releasedAt(Fluent, plus(Time, n1))))&" +
                "(~(?[Event]:(happens(Event, Time)&terminates(Event, Fluent, Time)))))=>holdsAt(Fluent, plus(Time, n1)))))).");
    }

    /** ***************************************************************
     */
    @Test
    public void testClausificationSimple() {

        Clausifier.varCounter = 0;
        System.out.println();
        System.out.println("================== testClausificationSimple ======================");
        BareFormula form = BareFormula.string2form("(((![X]:(a(X)|b(X)))|(?[X]:(?[Y]:p(X,f(Y)))))<=>(?[X]:(q(g(a),X))))");
        System.out.println("input: " + form);
        System.out.println();
        ArrayList<Clause> result = Clausifier.clausify(form);
        assertEquals(4,result.size());
        Clause res = result.get(0);
        System.out.println("result: " + res);
        String r1 = "cnf(cnf0,axiom,~a(skf11)|q(g(a),skf10(VAR4))).";
        System.out.println("expected: " + r1);
        assertEquals(r1,res.toString());

        res = result.get(1);
        System.out.println("result: " + res);
        String r2 = "cnf(cnf1,axiom,~b(skf11)|q(g(a),skf10(VAR4))).";
        System.out.println("expected: " + r2);
        assertEquals(r2,res.toString());

        res = result.get(2);
        System.out.println("result: " + res);
        String r3 = "cnf(cnf2,axiom,~p(VAR2,f(VAR1))|q(g(a),skf10(VAR4))).";
        System.out.println("expected: " + r3);
        assertEquals(r3,res.toString());

        res = result.get(3);
        System.out.println("result: " + res);
        String r4 = "cnf(cnf3,axiom,~q(g(a),VAR4)|a(VAR5)|b(VAR5)|p(skf9(VAR2,VAR4,VAR5),f(skf8(VAR1,VAR2,VAR4,VAR5)))).";
        System.out.println("expected: " + r4);
        assertEquals(r4,res.toString());
    }

    /** ***************************************************************
     */
    public static void testFileClause(String filename) {

        System.out.println();
        System.out.println("================== testClaus ======================");
        ClauseSet cs = Formula.file2clauses(filename);
        for (int i = 0; i < cs.clauses.size(); i++)
            System.out.println(cs.clauses.get(i));
    }

}
