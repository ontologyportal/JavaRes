/*
 * Functions wrapping basic inference rules for convenience.
    
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

import java.io.*;
import java.util.*;
import java.text.*;

public class ResControl {
 
    /** ***************************************************************
     * Compute all binary resolvents between a given clause and all
     * clauses in clauseset. This is used when integrating a new clause 
     * into the processed part of the proof state, where all possible
     * resolvents between the new clause and the already processed
     * clauses are computed. [Note: Explain  better]  
     */
    public static ClauseSet computeAllResolvents(Clause clause, ClauseSet clauseset) {

        //System.out.println("computeAllResolvents(): clause: " + clause);
        //System.out.println("computeAllResolvents(): clauseset: " + clauseset);
        ClauseSet res = new ClauseSet();
        for (int lit = 0; lit < clause.length(); lit++) {
            ArrayList<Clause> clauseres = new ArrayList<Clause>();
            ArrayList<Integer> indices = new ArrayList<Integer>();
            clauseset.getResolutionLiterals(clause.getLiteral(lit),clauseres,indices);
            assert clauseres.size() == indices.size();
            //System.out.println("computeAllResolvents(): clauseres: " + clauseres);
            for (int i = 0; i < clauseres.size(); i++) {               
                Clause resolvent = Resolution.resolution(clause, lit, clauseres.get(i), indices.get(i).intValue());
                if (resolvent != null)
                    res.add(resolvent);
            }
        }
        return res;
    }

    /** ***************************************************************
     * Compute all (direct) factors of clause. This operation is O(n^2)
     * if n is the number of literals. However, factoring is nearly never
     * a critical operation. Single-clause operations are nearly always
     * much cheaper than clause/clause-set operations.  
     */
    public static ClauseSet computeAllFactors(Clause clause) {

        ClauseSet res = new ClauseSet();
        for (int i = 0; i < clause.length(); i++) {
            for (int j = i+1; j < clause.length(); j++) {
                Clause fact = Resolution.factor(clause, i, j);
                //System.out.println("INFO in ResControl.computeAllFactors(): adding factor: " + fact);
                if (fact != null)
                    res.add(fact);
            }
        }
        return res;
    }
    

}
