package atp;

import java.util.*;

/** ***************************************************************
 *  This class implements a simple index that can return resolution
 *  candidates (a set of clause and literal index pairs) for a given
 *  query literal. The returned literal occurrences have the opposite
 *  polarity of the query literal and the same top symbol (i.e. we
 *  implement a simple version of top symbol hashing).
 */
public class ResolutionIndex extends Index {

    public HashMap<String,HashSet<Literal>> posIdx = new HashMap();
    public HashMap<String,HashSet<Literal>> negIdx = new HashMap();

    /** ***************************************************************
     * We use separate Maps for mapping predicate symbols to
     * positive literal occurrences and negative literal occurrences.
     */
    public ResolutionIndex() {

    }

    /** ***************************************************************
     */
    public void insertData(HashMap<String,HashSet<Literal>> idx,
                           String topsymbol, Literal payload) {

        if (!idx.containsKey(topsymbol))
            idx.put(topsymbol, new HashSet<>());
        HashSet<Literal> existing = idx.get(topsymbol);
        existing.add(payload);
    }

    /** ***************************************************************
     */
    public void removeData(HashMap<String,HashSet<Literal>> idx,
                           String topsymbol, Literal payload) {

        if (idx.containsKey(topsymbol)) {
            HashSet<Literal> existing = idx.get(topsymbol);
            existing.remove(payload);
        }
    }

    /** ***************************************************************
     */
    public void insertClause(Clause clause) {

        for (Literal lit : clause.literals) {
            if (lit.isInferenceLit()) {
                if (lit.isPositive())
                    insertData(posIdx, lit.atom.getFunc(), lit);
                else
                   insertData(negIdx, lit.atom.getFunc(), lit);
            }
        }
    }

    /** ***************************************************************
     */
    public void removeClause(Clause clause) {

        for (Literal lit : clause.literals) {
            if (lit.isInferenceLit()) {
                if (lit.isPositive())
                    removeData(posIdx, lit.atom.getFunc(), lit);
                else
                    removeData(negIdx, lit.atom.getFunc(), lit);
            }
        }
    }

    /** ***************************************************************
     */
    public HashSet<Literal> getResolutionLiterals(Literal lit) {

        HashMap<String, HashSet<Literal>> idx = null;
        if (lit.isPositive())
            idx = negIdx;
        else
            idx = posIdx;
        if (idx.containsKey(lit.atom.getFunc()))
            return new HashSet<Literal>(idx.get(lit.atom.getFunc()));
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
