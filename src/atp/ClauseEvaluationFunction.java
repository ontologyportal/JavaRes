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

    This module implements heuristic evaluation functions for clauses. 
    The purpose of heuristic evaluation is selection of clauses during the
    resolution process.

    A heuristic evaluation function is a function h:Clauses(F,P,X)->R
    (where R denotes the set of real numbers, or, in the actual
    implementation, the set of floating point numbers).

    A lower value of h(C) for some clause C implies that C is assumed to
    be better (or more useful) in a given proof search, and should be
    processed before a clause C' with larger value h(C').
*/
package atp;
import java.io.*;
import java.util.*;
import java.text.*;

/** ***************************************************************
 *  A class representing a clause evaluation function. This is a pure
 *  virtual class, and it really is just a wrapper around the given
 *  clause evaluation function. However, some heuristics may need
 *  to be able to store information, either from initialization, or
 *  from previous calls.
 */  
public abstract class ClauseEvaluationFunction {
                
    public static String name = "Virtual Base";
                
    /** ***************************************************************
     * Return a string representation of the clause.
     */
    public String toString() {

       return "ClauseEvalFun" + name;
    }
           
    /** ***************************************************************
     * This needs to be overloaded...
     */
    public int hEval(Clause clause) {

        assert false : "Virtual base class is not callable";
        return 0;
    }       
        
    /** ***************************************************************
     * ************ Define Strategies *****************
     */
    public static ArrayList<ClauseEvaluationFunction> evals = new ArrayList<ClauseEvaluationFunction>();
    public static ArrayList<Integer> ratings = new ArrayList<Integer>();
    public static String spec = null;
    public static Clause c1 = new Clause();
    public static Clause c2 = new Clause();
    public static Clause c3 = new Clause();
    public static Clause c4 = new Clause();
    public static Clause c5 = new Clause();
    public static Clause c6 = new Clause();
    public static Clause c7 = new Clause();
    public static Clause c8 = new Clause();
    public static EvalStructure FIFOEval = null;
    public static EvalStructure SymbolCountEval = null;
    public static EvalStructure PickGiven5 = null;
    public static EvalStructure PickGiven2 = null;
    
    public static void setupEvaluationFunctions() {
        
        // Strict first-in/first-out evaluation. This is obviously fair
        // (i.e. every clause will be picked eventually), but not a good search
        // strategy.        
       evals.add(new FIFOEvaluation());                
       ratings.add(new Integer(1));
       FIFOEval = new EvalStructure(evals,ratings);
       FIFOEval.name = "FIFOEval";

        // Strict symbol counting (a smaller clause is always better than a
        // larger clause). This is only fair if subsumption or a similar
        // mechanism is employed, otherwise there can e.g. be an infinite set of 
        // clauses p(X1), p(X2), p(X3),.... that are all smaller than q(f(X)), so
        // that the latter is never selected.
       evals = new ArrayList<ClauseEvaluationFunction>();
       evals.add(new SymbolCountEvaluation(2,1));
       ratings = new ArrayList<Integer>();
       ratings.add(new Integer(1));
       SymbolCountEval = new EvalStructure(evals,ratings);
       SymbolCountEval.name = "SymbolCountEval";
       
        // Experience has shown that picking always the smallest clause (by
        // symbol count) isn't optimal, but that it pays off to interleave smallest
        // and oldest clause. The ratio between the two schemes is sometimes
        // called the "pick-given ratio", and, according to folklore, Larry Wos
        // has stated that "the optimal pick-given ratio is five." Since he is a
        // very smart person we use this value here.
       evals = new ArrayList<ClauseEvaluationFunction>();
       evals.add(new SymbolCountEvaluation(2,1));
       evals.add(new FIFOEvaluation());
       ratings = new ArrayList<Integer>();
       ratings.add(new Integer(5));
       ratings.add(new Integer(1));
       PickGiven5 = new EvalStructure(evals,ratings);
       PickGiven5.name = "PickGiven5";

        // See above, but now with a pick-given ration of 2 for easier testing
       evals = new ArrayList<ClauseEvaluationFunction>();
       evals.add(new SymbolCountEvaluation(2,1));
       evals.add(new FIFOEvaluation());
       ratings = new ArrayList<Integer>();
       ratings.add(new Integer(5));
       ratings.add(new Integer(1));
       PickGiven2 = new EvalStructure(evals,ratings);  
       PickGiven2.name = "PickGiven2";
    }
    
    /** ***************************************************************
     * Test that FIFO evaluation works as expected.
     */
    public static void setup() {
                    
        setupEvaluationFunctions();
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
    public static void testFIFO() {

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
    public static void testSymbolCount() {

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
    public static void testEvalStructure() {

        evals = new ArrayList<ClauseEvaluationFunction>();
        evals.add(new SymbolCountEvaluation(2,1));
        evals.add(new FIFOEvaluation());
        ratings = new ArrayList<Integer>();
        ratings.add(2);
        ratings.add(1);
        EvalStructure eval_funs = new EvalStructure(evals,ratings);            
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
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
        
        setup();
        testFIFO();
        testSymbolCount();
        testEvalStructure();
    }        
}
