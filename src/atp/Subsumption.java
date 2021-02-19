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

This module implements first-order subsumption, as defined by the
simplification rule below:

Subsumption:

 C|R    D
=========== if sigma(D)=C for some substitution sigma
     D

Note that C, D, R (and hence C|R) are clauses, i.e. they are
multi-sets of literals interpreted as disjunctions. The multi-set
aspect is important for this particular calculus, otherwise
p(X)|p(Y) would be able to subsume p(X), i.e. a clause would subsume
its own factors. This would destroy completeness.

*/
package atp;

import java.io.StringReader;
import java.util.*;
import java.text.*;

public class Subsumption {
    
    /** ***************************************************************
     * Try to extend subst so that subst(subsumer) is a multi-subset of
     * subsumed. Recursively call this routine, checking the first literal
     * of subsumer for a match with subsumed, then calling again with 
     * the first literal removed.
     */ 
    private static boolean subsumeLitLists(Clause subsumer, 
            Clause subsumed, BacktrackSubstitution subst) {

        if (subsumer == null || subsumer.literals.size() < 1)
            return true;
        for (Literal lit : subsumed.literals) {
            int btstate = subst.getState();
            if (subsumer.literals.get(0).match(lit, subst)) {
                Clause rest = new Clause();
                for (Literal l : subsumed.literals)
                    if (!l.equals(lit))
                        rest.literals.add(l);
                if (subsumeLitLists(subsumer.deepCopy(1), rest, subst))
                    return true;
            }
            subst.backtrackToState(btstate);
        }
        return false;
    }
    
    /** ***************************************************************
     * Return True if subsumer subsumes subsumed, False otherwise.
     */ 
    public static boolean subsumes(Clause subsumer, Clause subsumed) {

        //System.out.println("subsumes(): subsumer " + subsumer + " subsumed " + subsumed);
        if (subsumer.literals.size() > subsumed.literals.size())
            return false;
        BacktrackSubstitution subst = new BacktrackSubstitution();
        return subsumeLitLists(subsumer, subsumed, subst);
    }

    /** ***************************************************************
     * Return True if any clause from set subsumes clause, False otherwise.
     */ 
    public static boolean forwardSubsumption(ClauseSet cs, Clause clause) {

        ArrayList<Clause> candidates = cs.getSubsumingCandidates(clause);
        System.out.println("forwardSubsumption(): check " + cs + " against\n" + clause);
        for (Clause c : candidates) {
            if (subsumes(c, clause)) {
                System.out.println("forwardSubsumption(): " + c + " subsumes\n" + clause);
                return true;
            }
        }
        return false;
    }

    /** ***************************************************************
     * Remove all clauses that are subsumed by clause from set.
     */ 
    public static int backwardSubsumption(Clause clause, ClauseSet cs) {

        ArrayList<Clause> subsumed_set = new ArrayList<Clause>();
        ArrayList<Clause> candidates = cs.getSubsumedCandidates(clause);
        for (Clause c : candidates) {
            if (subsumes(clause, c))
                subsumed_set.add(c);        
        }
        int res = subsumed_set.size();
        for (Clause c : subsumed_set) {
            if (c.supportsClauses.size() == 0) // make sure that clauses that support others are not removed.
            	cs.extractClause(c);
        }
        return res;
    }


}
