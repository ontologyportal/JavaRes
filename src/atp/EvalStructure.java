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
    public int current = 0;
    public int current_count = 0;
    public String name = "";
    
    /** ***************************************************************
     * Initialize this structure. The argument is a list of pairs,
     * where each pair consists of a function and its relative weight
     * count.
     */
    public EvalStructure(ArrayList<ClauseEvaluationFunction> descriptor, ArrayList<Integer> rating) {

        if (descriptor != null && rating != null && descriptor.size() > 0 && rating.size() > 0) {
            eval_funs = descriptor;
            eval_vec  = rating;
            current = 0;
            current_count = eval_vec.get(0).intValue();
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

        current++;
        if (current >= eval_vec.size())
            current = 0;
        current_count = eval_vec.get(current);            
        return current;
    }
}

