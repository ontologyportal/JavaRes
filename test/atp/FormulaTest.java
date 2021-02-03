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

public class FormulaTest {

    /** ***************************************************************
     * Setup function for clause/literal unit tests. Initialize
     * variables needed throughout the tests.
     */
    public static String wformulas = "fof(small, axiom, ![X]:(a(x) | ~a=b))." +
            "fof(complex, conjecture, (![X]:a(X)|b(X)|?[X,Y]:(p(X,f(Y))))<=>q(g(a),X))." +
            "fof(clean, conjecture, ((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))<=>q(g(a),X)))." +
            "fof(queens_p,axiom,(queens_p => ![I,J]:((le(s(n0),I)& le(I,n) & le(s(I),J) & le(J,n) )=> ( p(I) != p(J) & plus(p(I),I) != plus(p(J),J) & minus(p(I),I) != minus(p(J),J) ) ) ))." +
            "fof(weird, weird, ![X]:(p(X) | ~q(X))).";

    /** ***************************************************************
     */
    @Test
    public void testWrappedFormula() {

        System.out.println("---------------------");
        System.out.println("INFO in testWrappedFormula()");
        try {
            Lexer lex = new Lexer(wformulas);
            Formula f1 = Formula.parse(lex);
            System.out.println("Result 1: " + f1);
            String expected = "fof(small,axiom,(![X]:(a(x)|(~a=b)))).";
            System.out.println("expected: " + expected);
            if (expected.equals(f1.toString()))
                System.out.println("success");
            else
                System.out.println("fail");
            assertEquals(expected,f1.toString());
            System.out.println();

            Formula f2 = Formula.parse(lex);
            System.out.println("Result 2: " + f2);
            expected = "fof(complex,conjecture,((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))<=>q(g(a),X))).";
            System.out.println("expected: " + expected);
            if (expected.equals(f2.toString()))
                System.out.println("success");
            else
                System.out.println("fail");
            assertEquals(expected,f2.toString());
            System.out.println();

            Formula f3 = Formula.parse(lex);
            System.out.println("Result 3: " + f3);
            expected = "fof(clean,conjecture,((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))<=>q(g(a),X))).";
            System.out.println("expected: " + expected);
            if (expected.equals(f3.toString()))
                System.out.println("success");
            else
                System.out.println("fail");
            assertEquals(expected,f3.toString());
            System.out.println();

            Formula f4 = Formula.parse(lex);
            System.out.println("Result 4: " + f4);
            expected = "fof(queens_p,axiom,(queens_p=>(![I]:(![J]:((((le(s(n0),I)&le(I,n))&le(s(I),J))&le(J,n))=>((~p(I)=p(J)&~plus(p(I),I)=plus(p(J),J))&~minus(p(I),I)=minus(p(J),J))))))).";
            System.out.println("expected: " + expected);
            if (expected.equals(f4.toString()))
                System.out.println("success");
            else
                System.out.println("fail");
            assertEquals(expected,f4.toString());
            System.out.println();

            Signature sig = f1.form.collectSig();
        }
        catch (Exception e) {
            System.out.println("Error in Formula.testWrappedFormula()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testEqAxioms() {

        EqAxioms.resetCounter();
        System.out.println("---------------------");
        System.out.println("INFO in testEqAxioms()");
        try {
            String testeq = "fof(eqab, axiom, a=b)." +
                    "fof(pa, axiom, p(a))." +
                    "fof(fb, axiom, ![X]:f(X)=b)." +
                    "fof(pa, conjecture, ?[X]:p(f(X))).";
            Lexer lex = new Lexer(testeq);
            Clausifier.counterReset();
            ClauseSet cs = Formula.lexer2clauses(lex);
            String expected1 = "cnf(cnf0,axiom,a=b).cnf(cnf1,axiom,p(a)).cnf(cnf2,axiom,f(VAR0)=b).cnf(cnf3,conjecture,p(f(skf2))).";
            System.out.println("INFO in testEqAxioms() first result: " + cs.toString().replaceAll("\n",""));
            System.out.println("INFO in testEqAxioms() expected: " + expected1);
            if (expected1.equals(cs.toString().replaceAll("\n","")))
                System.out.println("success");
            else
                System.out.println("fail");
            assertEquals(expected1,cs.toString().replaceAll("\n",""));
            cs = cs.addEqAxioms();
            System.out.println("testEqAxioms(): Second Result: " + cs);
            String expected = "cnf(cnf0,axiom,a=b).\n" +
                    "cnf(cnf1,axiom,p(a)).\n" +
                    "cnf(cnf2,axiom,f(VAR0)=b).\n" +
                    "cnf(cnf3,conjecture,p(f(skf2))).\n" +
                    "cnf(reflexivity,axiom,X=X).\n" +
                    "cnf(symmetry,axiom,~X=Y|Y=X).\n" +
                    "cnf(transitivity,axiom,~X=Y|~Y=Z|X=Z).\n" +
                    "cnf(funcompat0,plain,~X1=Y1|f(X1)=f(Y1)).\n" +
                    "cnf(predcompat1,plain,~X1=Y1|~p(X1)|p(Y1)).\n";
            System.out.println("INFO in testEqAxioms() expected: " + expected);
            if (expected.equals(cs.toString()))
                System.out.println("success");
            else
                System.out.println("fail");
            assertEquals(expected,cs.toString());
        }
        catch (Exception e) {
            System.out.println("Error in Formula.testEqAxioms()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Ignore
    @Test
    public void testEqAxiomProving() {

        System.out.println("---------------------");
        System.out.println("INFO in testEqAxiomProving()");
        try {
            String testeq = "fof(eqab, axiom, a=b)." +
                    "fof(pa, axiom, p(a))." +
                    "fof(fb, axiom, ![X]:f(X)=b)." +
                    "fof(pa, conjecture, ?[X]:p(f(X))).";
            Lexer lex = new Lexer(testeq);
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            cs = cs.addEqAxioms();
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            SearchParams sp = new SearchParams();
            sp.heuristics = ClauseEvaluationFunction.PickGiven2;
            ProofState state = new ProofState(cs,sp);
            state.evalFunctionName = ClauseEvaluationFunction.PickGiven2.name;
            state.delete_tautologies = true;
            state.forward_subsumption = true;
            state.backward_subsumption = true;
            state.verbose = true;
            state.res = state.saturate(10);
            if (state.res != null)
                System.out.println(state);
            else
                System.out.println("# SZS GaveUp");
            System.out.println("INFO in testEqAxiomProving(): done processing");
        }
        catch (Exception e) {
            System.out.println("Error in testEqAxiomProving()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testEqAxiomProving2() {

        System.out.println("---------------------");
        System.out.println("INFO in testEqAxiomProving2()");
        try {
            String testeq = "cnf(c00004,plain,~X79=X77|~X77=X80|X79=X80). " +
                    "cnf(c00005,input,apply(apply(apply(n1, X4), X5), X6)=apply(apply(apply(X4, X5), X5), X6)).";
            // "cnf(00076,plain,~X159=apply(apply(apply(n1, X160), X161), X162)|X159=apply(apply(apply(X160, X161), X161), X162)).";
            Lexer lex = new Lexer(testeq);
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            System.out.println("Result: " + ResControl.computeAllResolvents(cs.get(0), cs));
            System.out.println("done");
        }
        catch (Exception e) {
            System.out.println("Error in testEqAxiomProving2()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testEqAxiomProving3() {

        System.out.println("---------------------");
        System.out.println("INFO in testEqAxiomProving3()");
        try {
            String testeq = "cnf(c00004,plain,X=X). " +
                    "cnf(c00005,input,Y!=m | Y=n).";
            // "cnf(00076,plain,~X159=apply(apply(apply(n1, X160), X161), X162)|X159=apply(apply(apply(X160, X161), X161), X162)).";
            Lexer lex = new Lexer(testeq);
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            ClauseSet result = ResControl.computeAllResolvents(cs.get(0), cs);
            System.out.println(result);
            if (result.toString().trim().endsWith("plain,m=n)."))
                System.out.println("Success!");
            else
                System.out.println("Fail");
            assertTrue(result.toString().trim().endsWith("plain,m=n)."));
        }
        catch (Exception e) {
            System.out.println("Error in testEqAxiomProving3()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testProving1() {

        System.out.println("---------------------");
        System.out.println("INFO in in FormulaTest.testProving1()");
        try {
            String testeq = "fof(axiom1,input,(p(X)=>q(X))). " +
                    "fof(axiom2,input,p(a))." +
                    "fof(conj,conjecture,?[X]:q(x)).";
            Lexer lex = new Lexer(testeq);
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            ClauseSet result = ResControl.computeAllResolvents(cs.get(0), cs);
            System.out.println(result.toString());
            System.out.println("done");
            if (result.toString().trim().endsWith("plain,q(a))."))
                System.out.println("Success!");
            else
                System.out.println("Fail");
            assertTrue(result.toString().trim().endsWith("plain,q(a))."));
        }
        catch (Exception e) {
            System.out.println("Error in in FormulaTest.testProving1()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testProving2() {

        System.out.println("---------------------");
        System.out.println("INFO in FormulaTest.testProving2()");
        try {
            String testeq = "fof(axiom1,input,(p(X)=>q(X))). " +
                    "fof(axiom2,input,p(a))." +
                    "fof(conj,conjecture,?[X]:q(x)).";
            Lexer lex = new Lexer(testeq);
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            cs = cs.addEqAxioms();
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            SearchParams sp = new SearchParams();
            sp.heuristics = ClauseEvaluationFunction.FIFOEval;
            ProofState state = new ProofState(cs,sp);
            state.evalFunctionName = ClauseEvaluationFunction.FIFOEval.name;
            state.delete_tautologies = true;
            state.forward_subsumption = true;
            state.backward_subsumption = true;
            state.verbose = true;
            state.res = state.saturate(10);
            if (state.res != null)
                System.out.println(state);
            else
                System.out.println("# SZS GaveUp");
            System.out.println("INFO in in FormulaTest.testProving2(): done processing");
        }
        catch (Exception e) {
            System.out.println("Error in in FormulaTest.testProving2()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    @Test
    public void testParse1() {

        System.out.println("---------------------");
        System.out.println("INFO in FormulaTest.testParse1()");

        String input = "";
        String expected = "";
        ClauseSet cs = null;
        Lexer lex = null;

        Clausifier.counterReset();
        input = "fof(single_quoted,axiom,( 'A proposition' )).";
        lex = new Lexer(input);
        cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        expected = "cnf(cnf0,axiom,'A proposition').";
        assertEquals(expected,cs.toString().trim());

        input = "fof(single_quoted,axiom,( 'A predicate'(a) )).";
        lex = new Lexer(input);
        cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        expected = "cnf(cnf1,axiom,'A predicate'(a)).";
        assertEquals(expected,cs.toString().trim());

        input = "fof(single_quoted,axiom,( p('A constant') )).";
        lex = new Lexer(input);
        cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        expected = "cnf(cnf2,axiom,p('A constant')).";
        assertEquals(expected,cs.toString().trim());

        input = "fof(single_quoted,axiom,( p('A function'(a)) )).";
        lex = new Lexer(input);
        cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        expected = "cnf(cnf3,axiom,p('A function'(a))).";
        assertEquals(expected,cs.toString().trim());

        input = "fof(single_quoted,axiom,( 'A proposition' | 'A predicate'(a) | p('A constant') | p('A function'(a)) )).";
        lex = new Lexer(input);
        cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        expected = "cnf(cnf4,axiom,'A proposition'|'A predicate'(a)|p('A constant')|p('A function'(a))).";
        assertEquals(expected,cs.toString().trim());
    }

    /** ***************************************************************
     */
    @Test
    public void testParse2() {

        System.out.println("---------------------");
        System.out.println("INFO in FormulaTest.testParse2()");
        Clausifier.counterReset();
        String input = "fof(single_quoted,axiom,( p('A \\'quoted \\\\ escape\\'') )).";
        Lexer lex = new Lexer(input);
        ClauseSet cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        String expected = "cnf(cnf0,axiom,p('A \\'quoted \\\\ escape\\'')).";
        assertEquals(expected,cs.toString().trim());
    }

    /** ***************************************************************
     */
    @Test
    public void testParse3() {

        System.out.println("---------------------");
        System.out.println("INFO in FormulaTest.testParse3()");
        Clausifier.counterReset();
        String input = "fof(useful_connectives,axiom,(\n" +
                "    ! [X] :\n" +
                "      ( ( p(X) <~> ~ s(f) )) )).";
        Lexer lex = new Lexer(input);
        ClauseSet cs = Formula.lexer2clauses(lex);
        System.out.println(cs);
        String expected = "cnf(cnf0,axiom,~p(X)|s(f)).\n" +
                "cnf(cnf1,axiom,p(X)|~s(f)).";
        assertEquals(expected,cs.toString().trim());
    }
}
