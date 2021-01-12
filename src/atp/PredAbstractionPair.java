package atp;

// An abstraction of a literal that supports indexing

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

        if (!paobj.getClass().getName().equals("PredAbstractionPair"))
            throw new ClassCastException();
        PredAbstractionPair pa = (PredAbstractionPair) paobj;
        if (negated == pa.negated) {
            return pred.compareTo(pa.pred);
        }
        else
            return (new Boolean(negated)).compareTo(new Boolean(pa.negated));
    }
}
