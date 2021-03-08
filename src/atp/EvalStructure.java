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
*/
package atp;
import java.util.ArrayList;
import java.util.HashMap;

/** ***************************************************************
 * Represent a heuristic clause processing schema. The scheme
 * contains several different evaluation functions, and a way to
 * alternate between them. Concretely, each evaluation function is
 * paired with a counter, and clauses are picked according to each
 * function in a weighted round-robin scheme.
 */
public class EvalStructure {

    public ArrayList<ClauseEvaluationFunction> eval_funs = null;
    public ArrayList<Integer> eval_vec = null;
    public int current = 0; // index of the current evaluation function in eval_funs
    public int current_count = 0; // count of how many times more to use the current evaluation function
    public String name = "";

    public static HashMap<String,EvalStructure> GivenClauseHeuristics;
    static {
        GivenClauseHeuristics = new HashMap<>();
        GivenClauseHeuristics.put("FIFO",ClauseEvaluationFunction.FIFOEval);
        GivenClauseHeuristics.put("SymbolCount",ClauseEvaluationFunction.SymbolCountEval);
        GivenClauseHeuristics.put("PickGiven5",ClauseEvaluationFunction.PickGiven5);
        GivenClauseHeuristics.put("PickGiven2",ClauseEvaluationFunction.PickGiven2);
    };

    /** ***************************************************************
     * Initialize this structure.
     * @param descriptor is an array of evaluation functions.
     * @param rating is an array of frequencies that corresponds to the
     *               descriptor array.  If descriptor's first element is
     *
     */
    public EvalStructure(ArrayList<ClauseEvaluationFunction> descriptor, ArrayList<Integer> rating) {

        if (descriptor != null && rating != null && descriptor.size() > 0 && rating.size() > 0) {
            eval_funs = descriptor;
            eval_vec  = rating;
            current = 0;
            current_count = eval_vec.get(0); // set the frequency counter with the number for the 0th evaluation function
        }
    }
    
    /** ***************************************************************
     * Convenience construction that takes just one evaluation 
     * function.
     */
    public EvalStructure(ClauseEvaluationFunction cef, int rating) {
        
        ArrayList<ClauseEvaluationFunction> evals = new ArrayList<ClauseEvaluationFunction>();
        ArrayList<Integer> ratings = new ArrayList<Integer>();
        evals.add(cef);
        ratings.add(new Integer(rating));
        new EvalStructure(evals,ratings);
    }

    /** ***************************************************************
     */
    public String toString() {
        return name + " : " + eval_funs;
    }

    /** ***************************************************************
     * Return an evaluation of the clause for each evaluation function
     * available.
     */
    public ArrayList<Integer> evaluate(Clause clause) {

        ArrayList<Integer> evals = new ArrayList<Integer>();
        for (ClauseEvaluationFunction f:eval_funs)
            evals.add(new Integer(f.hEval(clause)));
        return evals;
    }

    /** ***************************************************************
     * Return the index of the next evaluation function of the scheme.
     */
    public int nextEval() {

        //System.out.println("nextEval(): current_count: " + current_count);
        //System.out.println("nextEval(): current eval index: " + current);
        //System.out.println("nextEval(): eval_vec: " + eval_vec);
        if (current_count == 0) {
            current = (current + 1) % eval_vec.size();
            //System.out.println("nextEval(): new current: " + current);
            current_count = eval_vec.get(current);
            //System.out.println("nextEval(): new current_count: " + current_count);
        }
        current_count--;
        return current;
        /*
        current++;
        if (current >= eval_vec.size())
            current = 0;
        current_count = eval_vec.get(current);            
        return current;

         */
    }
}

