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
import org.junit.runners.MethodSorters;

import java.util.*;
import static org.junit.Assert.*;

public class ClauseEvaluationFunctionTest {

    public static String spec = null;
    public static Clause c1 = new Clause();
    public static Clause c2 = new Clause();
    public static Clause c3 = new Clause();
    public static Clause c4 = new Clause();
    public static Clause c5 = new Clause();
    public static Clause c6 = new Clause();
    public static Clause c7 = new Clause();
    public static Clause c8 = new Clause();

    /** ***************************************************************
     * Test that FIFO evaluation works as expected.
     */
    @BeforeClass
    public static void setup() {

        ClauseEvaluationFunction.setupEvaluationFunctions();
        spec = "cnf(c1,axiom,(f(X1,X2)=f(X2,X1))).\n" +
                "cnf(c2,axiom,(f(X1,f(X2,X3))=f(f(X1,X2),X3))).\n" +
                "cnf(c3,axiom,(g(X1,X2)=g(X2,X1))).\n" +
                "cnf(c4,axiom,(f(f(X1,X2),f(X3,g(X4,X5)))!=f(f(g(X4,X5),X3),f(X2,X1))|k(X1,X1)!=k(a,b))).\n" +
                "cnf(c5,axiom,(b=c|X1!=X2|X3!=X4|c!=d)).\n" +
                "cnf(c6,axiom,(a=b|a=c)).\n" +
                "cnf(c7,axiom,(i(X1)=i(X2))).\n" +
                "cnf(c8,axiom,(c=d|h(i(a))!=h(i(e)))).\n";

        Lexer lex = new Lexer(spec);
        c1.parse(lex);
        c2.parse(lex);
        c3.parse(lex);
        c4.parse(lex);
        c5.parse(lex);
        c6.parse(lex);
        c7.parse(lex);
        c8.parse(lex);
    }

    /** ***************************************************************
     * Test that FIFO evaluation works as expected.
     */
    @Test
    public void testFIFO() {

        FIFOEvaluation eval = new FIFOEvaluation();
        int e1 = eval.hEval(c1);
        int e2 = eval.hEval(c2);
        int e3 = eval.hEval(c3);
        int e4 = eval.hEval(c4);
        int e5 = eval.hEval(c5);
        int e6 = eval.hEval(c6);
        int e7 = eval.hEval(c7);
        int e8 = eval.hEval(c8);
        System.out.println("ClauseEvaluationFunction.testFIFO(): Expecting " + e1 + " < " + e2);
        System.out.println(e2 + " < " + e3);
        System.out.println(e3 + " < " + e4);
        System.out.println(e4 + " < " + e5);
        System.out.println(e5 + " < " + e6);
        System.out.println(e6 + " < " + e7);
        System.out.println(e7 + " < " + e8);

        assert e1 < e2;
        assert e2 < e3;
        assert e3 < e4;
        assert e4 < e5;
        assert e5 < e6;
        assert e6 < e7;
        assert e7 < e8;
        System.out.println("INFO in ClauseEvaluationFunction.testFIFO() success");
    }

    /** ***************************************************************
     * Test that symbol counting works as expected.
     */
    @Test
    public void testSymbolCount() {

        SymbolCountEvaluation eval = new SymbolCountEvaluation(2,1);
        int e1 = eval.hEval(c1);
        int e2 = eval.hEval(c2);
        int e3 = eval.hEval(c3);
        int e4 = eval.hEval(c4);
        int e5 = eval.hEval(c5);
        int e6 = eval.hEval(c6);
        int e7 = eval.hEval(c7);
        int e8 = eval.hEval(c8);

        assert e1 == c1.weight(2,1);
        assert e2 == c2.weight(2,1);
        assert e3 == c3.weight(2,1);
        assert e4 == c4.weight(2,1);
        assert e5 == c5.weight(2,1);
        assert e6 == c6.weight(2,1);
        assert e7 == c7.weight(2,1);
        assert e8 == c8.weight(2,1);
        System.out.println("INFO in ClauseEvaluationFunction.testSymbolCount(): success");
    }

    /** ***************************************************************
     * Test composite evaluations.
     */
    @Test
    public void testEvalStructure() {

        ClauseEvaluationFunction.evals = new ArrayList<ClauseEvaluationFunction>();
        ClauseEvaluationFunction.evals.add(new SymbolCountEvaluation(2,1));
        ClauseEvaluationFunction.evals.add(new FIFOEvaluation());
        ClauseEvaluationFunction.ratings = new ArrayList<Integer>();
        ClauseEvaluationFunction.ratings.add(2);
        ClauseEvaluationFunction.ratings.add(1);
        EvalStructure eval_funs = new EvalStructure(ClauseEvaluationFunction.evals,ClauseEvaluationFunction.ratings);
        ArrayList<Integer> evalRatings = eval_funs.evaluate(c1);
        assert evalRatings.size() == 2;
        assert eval_funs.nextEval() == 0;
        assert eval_funs.nextEval() == 0;
        assert eval_funs.nextEval() == 1;
        assert eval_funs.nextEval() == 0;
        assert eval_funs.nextEval() == 0;
        assert eval_funs.nextEval() == 1;
        System.out.println("INFO in ClauseEvaluationFunction.testEvalStructure() success");
    }
}
