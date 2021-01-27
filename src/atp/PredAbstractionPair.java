package atp;

// An abstraction of a literal that supports indexing

import java.util.ArrayList;
import java.util.TreeSet;

/* The predicate abstraction of a clause is an ordered tuple of
   the predicate abstractions of its literals. As an example, the
   predicate abstraction of p(x)|~q(Y)|q(a) would be
   ((False, q), (True, p), (True, q)) (assuming True > False and
   q > p).  A PredAbstractionPair is just the predicate and whether
   its negated.  A predicate abstraction is represented as a sorted
   list of PredAbstractionPairs
 */
public class PredAbstractionPair implements Comparable {
    public boolean negated = false;
    public String pred = "";

    public PredAbstractionPair() {}

    /** ***************************************************************
     */
    public PredAbstractionPair(boolean n, String p) {
        negated = n;
        pred = p;
    }

    /** ***************************************************************
     */
    public int compareTo(Object paobj) {

        if (!paobj.getClass().getName().equals("atp.PredAbstractionPair"))
            throw new ClassCastException();
        PredAbstractionPair pa = (PredAbstractionPair) paobj;
        if (negated == pa.negated) {
            return pred.compareTo(pa.pred);
        }
        else
            return (new Boolean(negated)).compareTo(new Boolean(pa.negated));
    }

    /** ***************************************************************
     */
    @Override
    public boolean equals(Object o) {

        //System.out.println("PredAbstractionPair.equals(): " + o.getClass().getName());
        if (!o.getClass().getName().equals("atp.PredAbstractionPair"))
            throw new ClassCastException();
        PredAbstractionPair pap = (PredAbstractionPair) o;
        if (pred.equals(pap.pred) && negated == pap.negated)
            return true;
        else
            return false;
    }

    /** ***************************************************************
     */
    @Override
    public int hashCode() {
        return pred.hashCode() + (negated ? 0 : 1);
    }

    /** ***************************************************************
     */
    public String toString() {
        return "(" + negated + "," + pred + ")";
    }

    /** ***************************************************************
     * Check if candidate is a subsequence of superseq. That is a
     * necessary condition for the clause that produced candidate to
     * subsume the clause that produced superseq.
     */
    public static boolean predAbstractionIsSubSequence(ArrayList<PredAbstractionPair> candidate,
                                                       ArrayList<PredAbstractionPair> superseq) {

        int i = 0;
        try {
            for (PredAbstractionPair la : candidate) {
                while (!superseq.get(i).equals(la))
                    i = i + 1;
                i = i + 1;
            }
        }
        catch (IndexOutOfBoundsException iobe) {
            return false;
        }
        return true;
    }
}
