package atp;

import java.util.*;

/*
This class implements a simple index to speed up subsumption. This
is based on the predicate abstraction of a clause. The index
organises clauses by their predicate abstraction. Since we know
that a clause C can only subsume a clause c if C's predicate
abstraction is a subset of c's predicate abstraction, we can
exclude whole sets of clauses at once.
*/
public class SubsumptionIndex extends Index {

    /** ***************************************************************
     * We store predicate abstractions (with associated clauses) in a
     * dictionary for for fast access by abstraction. We also store
     * them in an array sorted by length, so that we only need to
     * consider stored clauses that are short enough to have a chance
     * to subsume.  The predAbstrMap is a map of predicate abstractions
     * pointing to the Clauses that have that abstraction.  We also have
     * the predicate abstraction in each clause, so it's essentially a
     * bidirectional map
     */
    public HashMap<ArrayList<PredAbstractionPair>,HashSet<Clause>> predAbstrMap = new HashMap<>();
    public ArrayList<ArrayList<PredAbstractionPair>> predAbstrArr = new ArrayList<>();

    public static class LengthComparator implements Comparator<ArrayList<PredAbstractionPair>> {
        @Override
        public int compare(ArrayList<PredAbstractionPair> a1, ArrayList<PredAbstractionPair> a2) {
            return a1.size() < a2.size() ? -1 : a1.size() == a2.size() ? 0 : 1;
        }
    }

    public static class PAComparator implements Comparator<PredAbstractionPair> {
        @Override
        public int compare(PredAbstractionPair pap1, PredAbstractionPair pap2) {
            return pap1.compareTo(pap2);
        }
    }

    /** ***************************************************************
     * Insert a clause into the index. If the predicate abstraction
     * already is stored, just add the clause to the associated set
     * of clauses. Otherwise, create a new entry for the pa and add
     * the clause.
     *
     * For example, the
     *    predicate abstraction of p(x)|~q(Y)|q(a) would be
     *    ((False, q), (True, p), (True, q))
     */
    public void insertClause(Clause clause) {

        ArrayList<PredAbstractionPair> pa = clause.predicateAbstraction();
        pa.sort(new PAComparator());

        HashSet<Clause> entry = null;
        if (predAbstrMap.containsKey(pa))
            entry = predAbstrMap.get(pa);
        else {  // array and map are kept in sync so this means that array has this entry
            entry = new HashSet<>();
            predAbstrMap.put(pa, entry);
            predAbstrArr.add(pa);
        }
        entry.add(clause);
        predAbstrArr.sort(new LengthComparator());
    }

    /** ***************************************************************
     * Remove a clause. This is easy, since we never remove the entry
     * for the predicate abstraction, only the clause from its
     * set. In general, successful backward subsumption is rare, so
     * deletion of a processed clause will be rare, too.
     */
    public void removeClause(Clause clause) {

        ArrayList<PredAbstractionPair> pa = clause.predicateAbstraction();
        HashSet<Clause> clauses = predAbstrMap.get(pa);
        clauses.remove(clause);
    }

    /** ***************************************************************
     * Return True if a clause is in the index. At the moment, this
     *         is only used for unit tests.
     */
    public boolean isIndexed(Clause clause) {

        //System.out.println("isIndexed(): " + clause);
        //System.out.println("isIndexed(): index: " + predAbstrMap);
        ArrayList<PredAbstractionPair> pa = clause.predicateAbstraction();
        //System.out.println("isIndexed(): pa: " + pa);
        if (predAbstrMap.containsKey(pa))
            return predAbstrMap.get(pa).contains(clause);
        return false;
    }

    /** ***************************************************************
     * Return a list of all clauses that can potentially subsume the
     * query. This goes through the relevant part of the list of
     * predicate abstractions and collects the clauses stored with
     * predicate abstractions compatible with subsumption.
     */
    public ArrayList<Clause> getSubsumingCandidates(Clause queryclause) {

        ArrayList<PredAbstractionPair> pa = queryclause.predicateAbstraction();
        int pa_len = pa.size();
        ArrayList<Clause> res = new ArrayList<>();
        for (ArrayList<PredAbstractionPair> cpa : predAbstrArr) {
            if (cpa.size() > pa_len)
                break;
            if (PredAbstractionPair.predAbstractionIsSubSequence(cpa, pa)) {
                res.addAll(predAbstrMap.get(cpa));
            }
        }
        return res;
    }

    /** ***************************************************************
     * Return a list of all clauses that can potentially be subsumed
     *         by query. See previous function
     */
    public ArrayList<Clause> getSubsumedCandidates(Clause queryclause) {

        ArrayList<PredAbstractionPair> pa = queryclause.predicateAbstraction();
        int pa_len = pa.size();
        ArrayList<Clause> res = new ArrayList<>();
        for (ArrayList<PredAbstractionPair> cpa : predAbstrArr) {
            if (cpa.size() < pa_len)
                continue;
            if (PredAbstractionPair.predAbstractionIsSubSequence(pa, cpa))
                res.addAll(predAbstrMap.get(cpa));

        }
        return res;
    }
}
