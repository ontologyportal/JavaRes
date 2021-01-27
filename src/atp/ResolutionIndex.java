package atp;

import java.util.*;

/** ***************************************************************
 *  This class implements a simple index that can return resolution
 *  candidates (a set of clause and literal index pairs) for a given
 *  query literal. The returned literal occurrences have the opposite
 *  polarity of the query literal and the same top symbol (i.e. we
 *  implement a simple version of top symbol hashing - "top symbol" is
 *  the predicate name).
 */
public class ResolutionIndex extends Index {

    // the key is the relation symbol
    // the value is the set of
    public HashMap<String,HashSet<KVPair>> posIdx = new HashMap();
    public HashMap<String,HashSet<KVPair>> negIdx = new HashMap();

    /** ***************************************************************
     * We use separate Maps for mapping predicate symbols to
     * positive literal occurrences and negative literal occurrences.
     */
    public ResolutionIndex() {

    }

    /** ***************************************************************
     */
    public String toString() {

        return "posIdx: " + posIdx + "\nnegIdx: " + negIdx + "\n";
    }

    /** ***************************************************************
     * Insert the payload into the provided index, associating it
     * with the given top symbol (i.e. the predicate symbol of the
     * indexed literal). The payload here is a tuple (clause, pos),
     * where pos is the position of the indexed literal in the clause
     * (counting from 0).
     */
    public void insertData(HashMap<String,HashSet<KVPair>> idx,
                           String topsymbol, KVPair payload) {

        if (!idx.containsKey(topsymbol))
            idx.put(topsymbol, new HashSet<>());
        HashSet<KVPair> existing = idx.get(topsymbol);
        existing.add(payload);
    }

    /** ***************************************************************
     * Remove a payload indexed at topsymbol from the provided
     * index
     */
    public void removeData(HashMap<String,HashSet<KVPair>> idx,
                           String topsymbol, KVPair payload) {

        System.out.println("removeData(): removing " + payload + " with " + topsymbol + " from " + idx);
        if (idx.containsKey(topsymbol)) {
            HashSet<KVPair> existing = idx.get(topsymbol);
            System.out.println("removeData(): removing " + payload + " from " + existing);
            existing.remove(payload);
            System.out.println("removeData(): after remove " + existing);
        }
        System.out.println("removeData(): removed " + payload + " from " + idx);
    }

    /** ***************************************************************
     * Insert all inference literals of clause into the appropriate
     * index (positive or negative, depending on the sign of the
     * literal).
     */
    public void insertClause(Clause clause) {

        for (int i = 0; i < clause.literals.size(); i++) {
            Literal lit = clause.literals.get(i);
            if (lit.isInferenceLit()) {
                if (lit.isPositive())
                    insertData(posIdx, lit.atom.getFunc(), new KVPair(clause,i));
                else
                    insertData(negIdx, lit.atom.getFunc(), new KVPair(clause,i));
            }
        }
    }

    /** ***************************************************************
     * Remove all inference literals of the clause from the index.
     */
    public void removeClause(Clause clause) {

        System.out.println("removeClause(): clause: " + clause);
        for (int i = 0; i < clause.literals.size(); i++) {
            Literal lit = clause.literals.get(i);
            System.out.println("removeClause(): lit: " + lit + " topsymbol: " + lit.atom.getFunc());
            if (lit.isInferenceLit()) {
                if (lit.isPositive())
                    removeData(posIdx, lit.atom.getFunc(), new KVPair(clause,i));
                else
                    removeData(negIdx, lit.atom.getFunc(), new KVPair(clause,i));
            }
        }
    }

    /** ***************************************************************
     * Return a list of resolution candidates for lit. Every
     * candidate is a pair (clause, pos), where pos is the position
     * of the literal that potentially unifies with lit (and has the
     * opposite sign).
     */
    public HashSet<KVPair> getResolutionLiterals(Literal lit) {

        System.out.println("ResolutionIndex.getResolutionLiterals(): lit: " + lit);
        HashMap<String, HashSet<KVPair>> idx = null;
        if (lit.isPositive())
            idx = negIdx;
        else
            idx = posIdx;
        if (idx.containsKey(lit.atom.getFunc())) {
            HashSet<KVPair> result = new HashSet<KVPair>(idx.get(lit.atom.getFunc()));
            System.out.println("ResolutionIndex.getResolutionLiterals(): result: " + result);
            return result;
        }
        else
            return new HashSet<>();
    }

    /** ***************************************************************
     * Check if candidate is a subsequence of superseq. That is a
     * necessary condition for the clause that produced candidate to
     * subsume the clause that produced superseq.
     */
    public boolean predAbstractionIsSubSequence(ArrayList<PredAbstractionPair> candidate,
                                                ArrayList<PredAbstractionPair> superseq) {

        int i = 0;
        int end = superseq.size();
        for (PredAbstractionPair la : candidate) {
            while (superseq.get(i) != la) {
                i = i + 1;
                if (i > superseq.size() - 1)
                    return false;
            }
            i = i + 1;
        }
        return true;
    }
}
