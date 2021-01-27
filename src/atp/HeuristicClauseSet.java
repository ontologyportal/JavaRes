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

 A class representing a clause set (or, more precisely, a multi-set
 of clauses) with heuristic evaluations.  

 All clauses inserted into the set are evaluated
 according to all criteria. The clause set support extraction of
 the "best" clause according to any of the configured heuristics.
*/

package atp;

import java.io.*;
import java.util.*;

public class HeuristicClauseSet extends ClauseSet {

    // public ArrayList<Clause> clauses = null; // defined in ClauseSet
    public EvalStructure eval_functions = null;

    /** ***************************************************************
     * Initialize the clause. 
     */    
    public HeuristicClauseSet(ClauseSet cs, EvalStructure efunctions) {

        eval_functions = efunctions;
        for (int i = 0; i < cs.length(); i++)
            addClause(cs.get(i));                
    }   

    /** ***************************************************************
     * Add a clause to the clause set. If the clause set supports
     * heuristic evaluations, add the relevant evaluations to the clause. 
     */    
    public void addClause(Clause clause) {

    	if (eval_functions == null) {
    		System.out.println("Error in HeuristicsClauseSet.addClause(): null eval_functions");
    		return;
    	}
    	else {
	        ArrayList<Integer> evals = eval_functions.evaluate(clause);
	        clause.addEval(evals);
	        super.addClause(clause);
    	}
    }
    
    /** ***************************************************************
     * Extract and return the clause with the lowest weight according
     * to the selected heuristic. If the set is empty, return None. 
     */    
    public Clause extractBestByEval(int heuristic_index) {

        if (clauses.size() > 0) {
            int best = 0;
            Clause c = clauses.get(0);
            int besteval = c.evaluation.get(heuristic_index);
            for (int i = 1; i < clauses.size(); i++) {
                c = clauses.get(i);
                if (c.evaluation == null)
                    System.out.println("Error in HeuristicClauseSet.extractBestByEval(): no eval for clause: " + c);
                if (c.evaluation.get(heuristic_index) < besteval) {
                    //System.out.println("INFO in HeuristicClauseSet.extractBestByEval(): heuristic: " + i);
                    besteval = clauses.get(i).evaluation.get(heuristic_index);
                    best = i;
                }
            }
            return clauses.remove(best);
        }
        else
            return null;
    }
    
    /** ***************************************************************
     * Extract and return the next "best" clause according to the 
     * evaluation scheme. 
     */    
    public Clause extractBest() {

        //System.out.println("INFO in HeuristicClauseSet.extractBest(): clauses.size(): " + clauses.size());
        return extractBestByEval(eval_functions.nextEval());
    }

    /** ***************************************************************
     * Return the clause with the lowest weight according
     * to the selected heuristic. If the set is empty, return None. 
     */    
    public Clause selectBestByEval(int heuristic_index) {

        if (clauses.size() > 0) {
            int best = 0;
            Clause c = clauses.get(0);
            int besteval = c.evaluation.get(heuristic_index);
            for (int i = 1; i < clauses.size(); i++) {
                c = clauses.get(i);
                if (c.evaluation == null)
                    System.out.println("Error in HeuristicClauseSet.extractBestByEval(): no eval for clause: " + c);
                if (c.evaluation.get(heuristic_index) < besteval) {
                    //System.out.println("INFO in HeuristicClauseSet.extractBestByEval(): heuristic: " + i);
                    besteval = clauses.get(i).evaluation.get(heuristic_index);
                    best = i;
                }
            }
            return clauses.get(best);
        }
        else
            return null;
    }
    
    /** ***************************************************************
     * Return the next "best" clause according to the 
     * evaluation scheme.  Don't increment to the next eval function.
     */    
    public Clause selectBest() {

        Clause c = selectBestByEval(eval_functions.nextEval());
        eval_functions.current--;
        if (eval_functions.current < 0)
            eval_functions.current = eval_functions.eval_vec.size() - 1;
        return c;
    }
    

}
