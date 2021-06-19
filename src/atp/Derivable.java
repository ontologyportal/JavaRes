package atp;

import java.util.ArrayList;
import java.util.Collections;

/* This class represents "derivable" objects. Derivable objects have
    a name and a justification. Names can be generated
    automatically. They are not strictly required to be different for
    different objects, but will usually be (this makes live easier for
    users). Derivable objects will typically be logical formulas,
    either full FOF formulas, or clauses.
 */
public class Derivable {

    // Counter for generating new clause names.
    public static int derivedIdCounter = 0;

    // Indicate if derivations should be printed as part of Derivable
    // objects. It's up to the concrete classes to support this.
    public static boolean printDerivation = false;

    public Derivation derivation = null;

    public int refCount = 0;

    public String name = "";

    /** ***************************************************************
     */
    public Derivable() {
    }

    /** ***************************************************************
     */
    public Derivable(String n, Derivation d) {

        setName(n);
        derivation = d;
    }

    /** ***************************************************************
     */
    public Derivable deepCopy() {

        Derivable result = new Derivable();
        result.derivation = this.derivation.deepCopy();
        result.refCount = this.refCount;
        result.name = this.name;
        return result;
    }

    /** ***************************************************************
     */
    public static void enableDerivationOutput() {

        Derivable.printDerivation = true;
    }

    /** ***************************************************************
     */
    public static void disableDerivationOutput() {

        Derivable.printDerivation = false;
    }

    /** ***************************************************************
     */
    public static void toggleDerivationOutput() {

        Derivable.printDerivation = !Derivable.printDerivation;
    }

    /** ***************************************************************
     */
    public String toString() {
        return name;
    }

    /** ***************************************************************
     * If no name is given, generate a default name.
     */
    public void setName(String n) {

        if (!Term.emptyString(n))
            name = n;
        else {
            name = "c" + Derivable.derivedIdCounter;
            Derivable.derivedIdCounter++;
        }
    }

    /** ***************************************************************
     */
    public void setDerivation(Derivation d) {
        derivation = d;
    }

    /** ***************************************************************
     * Return a list of all ancestors of this node in the derivation
     *         graph.
     */
    public ArrayList<Derivable> getParents() {
        if (derivation == null)
            return new ArrayList<Derivable>();
        return derivation.getParents();
    }

    /** ***************************************************************
     * Increase reference counter (counts virtual edges in the
     *         derivation graph coming from the children).
     */
    public void incRefCount() {
        refCount++;
    }

    /** ***************************************************************
     * Decrease reference counter (counts virtual edges in the
     *         derivation graph coming from the children).
     */
    public void decRefCount() {
        refCount--;
    }

    /** ***************************************************************
     * If printing of derivations is enabled, return a string
     *         representation suitable as part of TPTP-3 output. Otherwise
     *         return the empty string.
     */
    public String strDerivation() {

        if (derivation == null)
            return "";
        if (Derivable.printDerivation)
            return "," + derivation.toString();
        return "";
    }

    /** ***************************************************************
     * Compute and set the number of virtual edges in all descendents
     *         of self. The root node has one "virtual" edge.
     */
    public void annotateDerivationGraph() {

        //System.out.println("Derivable.annotateDerivationGraph(): " + this);
        ArrayList<Derivable> parents = null;
        if (refCount == 0) {
            parents = getParents();
            for (Derivable p : parents)
                p.annotateDerivationGraph();
        }
        incRefCount();
    }

    /** ***************************************************************
     * Return linearized derivation.
     */
    public ArrayList<Derivable> linearizeDerivation(ArrayList<Derivable> res) {

        //System.out.println("Derivable.linearizeDerivation(): " + this);
        if (res == null)
            res = new ArrayList<Derivable>();
        decRefCount();
        if (refCount == 0) {
            res.add(this);
            ArrayList<Derivable> parents = getParents();
            for (Derivable p : parents)
                p.linearizeDerivation(res);
        }
        return res;
    }

    /** ***************************************************************
     * Return linearized derivation.
     */
    public ArrayList<Derivable> orderedDerivation() {

        annotateDerivationGraph();
        ArrayList<Derivable> res = linearizeDerivation(null);
        Collections.reverse(res);
        return res;
    }

}
