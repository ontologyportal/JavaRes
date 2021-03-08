package atp;

/** ***************************************************************
 * A simple container for different parameter settings for the proof
 *     search.
 */
public class SearchParams {

    public EvalStructure heuristics;
    public boolean delete_tautologies;
    public boolean forward_subsumption;
    public boolean backward_subsumption;
    public boolean indexing;
    public LitSelection.LitSelectors literal_selection;

    public String filename; // convenient to have this here to store all command line options

    /** ***************************************************************
     */
    public SearchParams() {

        //     This defines the clause selection heuristic, i.e. the order in
        //    which uprocessed clauses are selected for processing.
        heuristics = ClauseEvaluationFunction.PickGiven5;

        // This determines if tautologies will be deleted. Tautologies in
        //    plain first-order logic (without equality) are clauses which
        //    contain two literals with the same atom, but opposite signs.
        delete_tautologies   = true;

        // Forward-subsumption checks the given clause against already
        //    processed clauses, and discards it if it is subsumed.
        forward_subsumption  = true;

        // Backwards subsumption checks the processed clauses against the
        //    given clause, and discards all processed clauses that are
        //    subsumed.
        backward_subsumption = true;

        // Either None, or a function that selects a subset of negative
        //    literals from a set of negative literals (both represented as
        //            lists, not Python sets) as the inference literal.
        literal_selection    = LitSelection.LitSelectors.LARGEST;

        indexing = true;
    }

    /** ***************************************************************
     */
    public SearchParams(EvalStructure heuristics,
                        boolean delete_tautologies,
                        boolean forward_subsumption,
                        boolean backward_subsumption,
                        boolean indexing,
                        LitSelection.LitSelectors literal_selection) {

        this.heuristics = heuristics;
        this.delete_tautologies = delete_tautologies;
        this.forward_subsumption = forward_subsumption;
        this.backward_subsumption = backward_subsumption;
        this.indexing = indexing;
        this.literal_selection = literal_selection;
    }

    /** ***************************************************************
     */
    public String toString() {
        return "Heuristics: " + heuristics.toString() + " litSelect: " + literal_selection + " indexing: " + indexing +
                " delTaut: " + delete_tautologies + " forSub: " + forward_subsumption + " backSub: " + backward_subsumption;
    }
}
