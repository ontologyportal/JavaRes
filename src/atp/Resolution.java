/*
 * This module implements the rules of the simple resolution calculus,
    namely binary resolution and factoring.
    inference rule:

    Binary resolution:

    c1|a1     c2|~a2
    ---------------- where sigma=mgu(a1,a2)
     sigma(c1|c2)

    Note that c1 and c2 are arbitrary disjunctions of literals (each of
    which may be positive or negative). Both c1 and c2 may be empty.  Both
    a1 and a2 are atoms (so a1 and ~a2 are a positive and a negative
    literal, respectively).  Also, since | is AC (or, alternatively, the
    clauses are unordered multisets), the order of literals is irrelevant.

    Clauses are interpreted as implicitly universally quantified
    disjunctions of literals. This implies that the scope of the variables
    is a single clause. In other words, from a theoretical point of view,
    variables in different clauses are different. In practice, we have to
    enforce this explicitly by making sure that all clauses used as
    premises in an inference are indeed variable disjoint.

    Factoring:

       c|a|b
    ----------  where sigma = mgu(a,b)
    sigma(c|a)

    Again, c is an arbitrary disjunction.
    
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
import java.lang.Math.*;

import static atp.ProofState.verbose;

public class Resolution {

    /** ***************************************************************
     * Implementation of the Resolution rule. lit1 and lit2 are indices
     * of literals in clause1 and clause2, respectively, so clause1|lit1
     * and clause2|lit2 are literals.
     * Try to resolve clause1|lit1 against clause2|lit2. If this is
     * possible, return the resolvent. Otherwise, return None.
     */
    public static Clause resolution(Clause clause1, int lit1, Clause clause2, int lit2) {

        //System.out.println("INFO in Resolution.resolution(): resolving (clause1, lit1, clause2, lit2): " +
        //        clause1 + " " + lit1 + " " + clause2 + " " + lit2);
        Literal l1 = clause1.getLiteral(lit1);
        Literal l2 = clause2.getLiteral(lit2);
        //System.out.println("Resolution.resolution(): literals: " + l1 + " " + l2);
        if (l1 == null || l2 == null) {
            System.out.println("Error in Resolution.resolution(): literals are null " + l1 + " " + l2);
            return null;
        }
        if (l1.isNegative() == l2.isNegative())
            return null;
        //System.out.println("INFO in Resolution.resolution():l1 is negative: " + l1.isNegative());
        //System.out.println("INFO in Resolution.resolution():l2 is negative: " + l2.isNegative());
        Substitutions sigma = null;
       	sigma = Unification.mgu(l1.atom, l2.atom);
        //System.out.println("INFO in Resolution.resolution(): sigma " + sigma);
        if (sigma == null)
            return null;
        ArrayList<Literal> lits1 = new ArrayList<Literal> ();
       
        //System.out.println("INFO in Resolution.resolution(): clause1 size " + clause1.literals.size());
        //System.out.println("INFO in Resolution.resolution(): literal 11 " + l1);
        if (clause1.literals.size() > 1000) {
            System.out.println("Error in Resolution.resolution(): clause too big: " + clause1);
            assert clause1.literals.size() < 1000;
        }
        for (int i = 0; i < clause1.literals.size(); i++) {
            Literal l = clause1.literals.get(i); 
            //System.out.println("INFO in Resolution.resolution(): literal " + l);
            if (!l.equals(l1))
                lits1.add(l.instantiate(sigma));
            //System.out.println("INFO in Resolution.resolution(): literals " + lits1);
        }
        
        //System.out.println("INFO in Resolution.resolution(): clause2 size " + clause2.literals.size());
        //System.out.println("INFO in Resolution.resolution(): literal 12 " + l2);
        ArrayList<Literal> lits2 = new ArrayList<Literal> ();
        if (clause2.literals.size() > 1000) {
            System.out.println("Error in Resolution.resolution(): clause too big: " + clause1);
            assert clause1.literals.size() < 1000;
        }
        for (int i = 0; i < clause2.literals.size(); i++) {
            Literal l = clause2.literals.get(i); 
            //System.out.println("INFO in Resolution.resolution(): literal " + l);
            if (!l.equals(l2))
                lits2.add(l.instantiate(sigma));
            //System.out.println("INFO in Resolution.resolution(): literals " + lits1);
        }
        //System.out.println("INFO in Resolution.resolution(): uncombined literals " + lits1 + " " + lits2);
        lits1.addAll(lits2);
        //System.out.println("INFO in Resolution.resolution(): combined literals " + lits1);
        Clause res = new Clause();
        res.createName();
        res.addAll(lits1);
        res.removeDupLits();
        res.rationale = "resolution";
        res.support.add(clause1.name);
        res.support.add(clause2.name);
        clause1.supportsClauses.add(res.name);  // Keep track of clauses used to supported others
        clause2.supportsClauses.add(res.name);  // so they can't be subsumed away.
        res.depth = Math.max(clause1.depth,clause2.depth) + 1; 
        res.subst.addAll(sigma);
        //System.out.println("INFO in Resolution.resolution(): result " + res.toStringJustify());
        return res;
    }

    /** ***************************************************************
     * Check if it is possible to form a factor between lit1 and lit2. If
     * yes, return it, otherwise return None.
     */
    public static Clause factor(Clause clause, int lit1, int lit2) {

        //System.out.println("INFO in Resolution.factor(): " + clause + " " + lit1 + " " + lit2);
        //System.out.println("INFO in Resolution.factor(): " + clause.getLiteral(lit1) + " " + clause.getLiteral(lit2));
        Literal l1 = clause.getLiteral(lit1);
        Literal l2 = clause.getLiteral(lit2);
        if (l1.isNegative() != l2.isNegative())
            return null;
        Substitutions sigma = null;
       	sigma = Unification.mgu(l1.atom, l2.atom);
        if (sigma == null)
            return null;
        ArrayList<Literal> lits = new ArrayList<Literal>();
        for (int i = 0; i < clause.literals.size(); i++) {
            if (i != lit2) {
                Literal l = clause.literals.get(i);
                //if (!l.equals(l2))
                lits.add(l.instantiate(sigma));
            }
        }
        Clause res = new Clause();
        res.createName();
        res.addAll(lits);
        res.removeDupLits();
        res.rationale = "factoring";
        res.support.add(clause.name);
        if (verbose) {
            System.out.println("INFO in Resolution.factor(): result Clause: " + res.printHighlight(lits));
            System.out.println("INFO in Resolution.factor(): input Clause: " + clause);
        }
        return res;
    }
}
