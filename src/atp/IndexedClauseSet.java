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
import java.io.*;
import java.util.*;
import java.text.*;

public class IndexedClauseSet extends ClauseSet {

    // This is a normal clause set, augmented by indices that speeds up
    // the finding of resolution and subsumption partners.

    public static ResolutionIndex resIndex = new ResolutionIndex();
    public static SubsumptionIndex subIndex = new SubsumptionIndex();

    /**************************************************************
     * Create the two indices and call the superclass initializer.
     */
    public IndexedClauseSet(ClauseSet clauses) {

        resIndex = new ResolutionIndex();
        subIndex = new SubsumptionIndex();
        new ClauseSet(clauses.clauses);
    }

    /*************************************************************
     * Add the clause to the indices, then use the superclass
     * function to add it to the actual set.
     */
    public void addClause(Clause clause) {

        resIndex.insertClause(clause);
        subIndex.insertClause(clause);
        super.add(clause);
    }

    /**************************************************************
     * Remove the clause from the indices, then use the  superclass
     * function to remove it to the actual set.
     */
    public Clause extractClause(Clause clause) {

        resIndex.removeClause(clause);
        subIndex.removeClause(clause);
        return super.extractClause(clause);
    }

    /**************************************************************
     * Overwrite the original function with one based on indexing.
     */
    public HashSet<Literal> getResolutionLiterals(Literal lit) {

        return resIndex.getResolutionLiterals(lit);
    }

    /***************************************************************
     * Overwrite the original function with one based on indexing.
     */
    public ArrayList<Clause> getSubsumingCandidates(Clause queryclause) {

        return subIndex.getSubsumingCandidates(queryclause);
    }

    /***************************************************************
     * Overwrite the original function with one based on indexing.
     */
    public ArrayList<Clause> getSubsumedCandidates(Clause queryclause) {

        return subIndex.getSubsumedCandidates(queryclause);
    }
}
