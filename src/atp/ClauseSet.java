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

/** ***************************************************************
 * A class representing a clause set (or, more precisely,
 * a multi-set of clauses). 
 */    
public class ClauseSet {

    public ArrayList<Clause> clauses = new ArrayList<Clause>();
    public String SZSexpected = ""; // set from a structured comment when reading a TPTP problem file
    public String SZSresult = "";

    /**
     * **************************************************************
     */
    public ClauseSet() {

    }

    /**
     * **************************************************************
     */
    public ClauseSet(ArrayList<Clause> clauses) {

        for (Clause c : clauses)
            this.addClause(new Clause(c));
    }

    /**
     * **************************************************************
     * Return a string representation of the clause set.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < clauses.size(); i++)
            sb.append(clauses.get(i) + "\n");
        return sb.toString();
    }

    /**
     * **************************************************************
     */
    public boolean equals(Object o) {

        assert !o.getClass().getName().equals("atp.ClauseSet") : "ClauseSetequals() passed object not of type ClauseSet";
        ClauseSet oc = (ClauseSet) o;
        if (clauses.size() != oc.clauses.size())
            return false;
        for (Clause clause : oc.clauses)
            if (!clauses.contains(clause))
                return false;
        for (Clause clause : clauses)
            if (!oc.clauses.contains(clause))
                return false;
        return true;
    }

    /**
     * **************************************************************
     */
    public void sort() {

        for (Clause c : clauses)
            c.sortLiterals();
        Collections.sort(clauses);
    }

    /**
     * **************************************************************
     */
    public boolean containsEquality() {

        for (Clause c : clauses)
            if (c.containsEquality())
                return true;
        return false;
    }

    /**
     * **************************************************************
     * Return number of clauses in set.
     */
    public int length() {

        return clauses.size();
    }

    /**
     * **************************************************************
     * get a clause
     */
    public Clause get(int i) {

        assert i < clauses.size() : "index out of bounds: " + Integer.toString(i) +
                " with clause list length: " + Integer.toString(clauses.size());
        return clauses.get(i);
    }

    /**
     * **************************************************************
     * Add a clause to the clause set.
     */
    public void addClause(Clause clause) {

        clauses.add(clause);
    }

    /**
     * **************************************************************
     * Add a clause to the clause set.
     */
    public void addAll(ClauseSet clauseSet) {

        for (Clause c : clauseSet.clauses)
            addClause(c);
    }

    /**
     * **************************************************************
     * Add a clause to the clause set.
     */
    public void addAll(ArrayList<Clause> clauseSet) {

        for (Clause c : clauseSet)
            addClause(c);
    }

    /**
     * **************************************************************
     * Add a clause to the clause set.
     */
    public void addAll(HashSet<Clause> clauseSet) {

        for (Clause c : clauseSet)
            addClause(c);
    }

    /**
     * **************************************************************
     * Remove a clause from the clause set and return it.
     */
    public Clause extractClause(Clause clause) {

        clauses.remove(clause);
        return clause;
    }

    /**
     * **************************************************************
     * Collect function- and predicate symbols into the signature.
     */
    public Signature collectSig(Signature sig) {

        for (Clause c : clauses)
            sig = c.collectSig(sig);
        return sig;
    }

    /**
     * **************************************************************
     * Collect function- and predicate symbols into the signature.
     */
    public Signature collectSig() {

        Signature sig = new Signature();
        for (Clause c : clauses)
            sig = c.collectSig(sig);
        return sig;
    }

    /**
     * **************************************************************
     * Add equality axioms (if necessary).
     *
     * @return new clauses if equality is present, unmodified otherwise.
     */
    public ClauseSet addEqAxioms() {

        System.out.println("INFO in ClauseSet.addEqAxioms(): adding axioms");
        Signature sig = new Signature();
        sig = collectSig(sig);

        //System.out.println("INFO in ClauseSet.addEqAxioms(): signature: " + sig);
        if (sig.isPred("=")) {
            ArrayList<Clause> res = EqAxioms.generateEquivAxioms();
            res.addAll(EqAxioms.generateCompatAxioms(sig));
            this.addAll(res);
        }
        return this;
    }

    /**
     * **************************************************************
     * Extract and return the first clause.
     */
    public Clause extractFirst() {

        if (clauses.size() > 0)
            return clauses.remove(0);
        else
            return null;
    }

    /**
     * **************************************************************
     * Return the negatedConjecture, if it exists.
     */
    public Clause getConjecture() {

        for (int i = 0; i < clauses.size(); i++) {
            if (clauses.get(i).type.startsWith("negatedConjecture") || clauses.get(i).type.startsWith("conjecture"))
                return clauses.get(i);
        }
        return null;
    }

    /**
     * **************************************************************
     * Return the negatedConjecture, if it exists.
     */
    public HashSet<String> getConjectureSymbols() {

        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < clauses.size(); i++) {
            if (clauses.get(i).type.startsWith("negatedConjecture") || clauses.get(i).type.startsWith("conjecture")) {
                Signature sig = new Signature();
                clauses.get(i).collectSig(sig);
                result.addAll(sig.funs);
                result.addAll(sig.preds);
            }
        }
        return result;
    }

    /***************************************************************
     * Return a subset (as a list) of the set containing at least all
     * clauses potentially subsuming queryclause. For a plain
     * ClauseSet, we just return all clauses in the set.
     */
    public ArrayList<Clause> getSubsumingCandidates(Clause queryclause) {

        return clauses;
    }

    /***************************************************************
     * Return a subset (as a list) of the set containing at least the
     * clauses  potentially subsumed by queryclause). For a plain
     * ClauseSet, we just return all clauses in the set.
     */
    public ArrayList<Clause> getSubsumedCandidates(Clause queryclause) {

        return clauses;
    }

   /** ***************************************************************
    * Return a copy of the instance.
    */                            
   public ClauseSet deepCopy() {
       
       ClauseSet newcs = new ClauseSet();
       for (int i = 0; i < clauses.size(); i++)
           newcs.addClause(clauses.get(i).deepCopy());
      return newcs;
   }
   
    /** ***************************************************************
     * Return a list of tuples (clause, literal-index) such that the
     * set includes at least all literals that can potentially be
     * resolved against lit. In the naive and obviously correct first
     * implementation, this simply returns a list of all
     * literal-indices for all clauses.
     * @return a side effect on @param clauseres and @param indices
     */ 
    public HashSet<KVPair> getResolutionLiterals(Literal lit) {

        HashSet<KVPair> result = new HashSet<>();
        for (int i = 0; i < clauses.size(); i++) {
            Clause c = clauses.get(i);
            for (int j = 0; j < c.length(); j++) {
                if (c.getLiteral(j).isInferenceLit())
                    result.add(new KVPair(clauses.get(i), j));
            }
        }
        return result;
    }
        
    /** ***************************************************************
     * Parse a sequence of clauses from st and add them to the
     * set. Return number of clauses parsed.
     */ 
    public int parse(Lexer lex) {

        int count = 0;
        while (lex.type != Lexer.EOFToken) {
            Clause clause = new Clause();
            try {
                clause = Clause.parse(lex);
                if (clause == null)
                    return 0;
            }
            catch (Exception e) {
                System.out.println("Error in ClauseSet.parse():");
                System.out.println(e.getMessage());
                e.printStackTrace();
                return 0;
            }
            if (lex.type != Lexer.EOFToken) {
                addClause(clause);
            }
        }
        return count;
    }
    
    /** ***************************************************************
     * Parse a clause set from a file.
     */ 
    public static ClauseSet parseFromFile(String filename) {
            
        if (filename != null && filename != "") {
        	File fin = null;
            try {
                fin  = new File(filename);
                if (fin != null) {
                    Lexer lex = new Lexer(fin);               
                    ClauseSet cs = new ClauseSet();
                    cs.parse(lex);
                    return cs;
                }
                else {
                    System.out.println("Error in ClauseSet.parseFromFile(): File error reading " + filename);
                }
            }
            catch (Exception e) {
                System.out.println("Error in ClauseSet.parseFromFile(): File error reading " + filename + ": " + e.getMessage());
                return null;
            }           
        }   
        return null;
    }

    /** ***************************************************************
     * side effect of normalizing variables in the clauses
     */
    public void normalizeVars() {

        ArrayList<Clause> newClauses = new ArrayList();
        Substitutions s = new Substitutions();
        for (Clause c : clauses) {
            //System.out.println("INFO in Clause.normalizeVars(): clause: " + c);
            LinkedHashSet<Term> vars = c.collectVars();
            int varCounter = 0;
            for (Term oldTerm : vars) {
                Term newTerm = new Term();
                newTerm.t = "VAR" + Integer.toString(varCounter++);
                s.addSubst(oldTerm, newTerm);
            }
        }
        //System.out.println("INFO in Clause.normalizeVars(): subst: " + s);
        for (Clause c : clauses) {
            Clause newc = c.deepCopy();
            c.subst.addAll(s);
            newClauses.add(c.substitute(s));
        }
        clauses = newClauses;
    }
}
