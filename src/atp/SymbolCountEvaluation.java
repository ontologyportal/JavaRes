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

/** ***************************************************************
 * Implement a standard symbol counting heuristic.
 */
public class SymbolCountEvaluation extends ClauseEvaluationFunction {

    public int fweight = 2;
    public int vweight = 1;
    public String name = "SymbolCountEval" + fweight + vweight;

    /** ***************************************************************
     */
    public SymbolCountEvaluation(int f, int v) {
        
        fweight = f;
        vweight = v;
    }
    
    /** ***************************************************************
     * Actual evaluation function.
     */
    public int hEval(Clause clause) {

        return clause.weight(fweight, vweight);
    }
}

