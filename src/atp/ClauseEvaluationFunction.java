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
    

}
