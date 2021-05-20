package atp;

import java.util.*;

/* A derivation object. A derivation is either trivial ("input"), a
    reference to an existing Derivable object ("reference"), or an
    inference with a list of premises.
 */
public class Derivation extends Derivable {

    public String operator = "";
    public String status = "";
    public ArrayList<Derivable> parents = new ArrayList<>();

    /** ***************************************************************
     */
    public Derivation() {
    }

    /** ***************************************************************
     */
    public Derivation(String op, ArrayList<Derivable> par, String stat) {

        super("",null);
        operator = op;
        parents =  par;
        status = stat;
    }

    /** ***************************************************************
     */
    public Derivation deepCopy() {

        Derivation result = new Derivation();
        result.operator = this.operator;
        result.status = this.status;
        result.parents = new ArrayList<Derivable>();
        for (Derivable d : parents)
            result.parents.add(d.deepCopy());
        result.derivation = this.derivation.deepCopy();
        result.refCount = this.refCount;
        return result;
    }

    /** ***************************************************************
     * Return a string for the derivation in TPTP-3 format.
     */
    public String toString() {

        if (operator.equals("input"))
            return "input";
        else if (operator.equals("eq_axiom"))
            return "eq_axiom";
        else if (operator.equals("reference")) {
            if (parents.size() == 1)
                return parents.get(0).name;
            else {
                System.out.println("Error in Derivation.toString(): more than one parent: " + parents);
            }
        }
        else
            return "inference(" + operator + ", " + status + ", " + parents.toString() + ")";
        return "";
    }

    /** ***************************************************************
     * Return a list of all derived objects that are used in this
     * derivation.
     */
    public ArrayList<Derivable> getParents() {

        if (operator.equals("input"))
            return new ArrayList<>();
        else if (operator.equals("eq_axiom"))
            return new ArrayList<>();
        else if (operator.equals("reference")) {
            assert (parents.size() == 1);
            return parents;
        }
        else {
            ArrayList<Derivable> res = new ArrayList<>();
            for (Derivable p : parents)
                res.addAll(p.getParents());
            return res;
        }
    }

    /** ***************************************************************
     * Simple convenience function: Create a derivation which directly
     *     references all parents.
     */
    public static Derivation flatDerivation(String operator, ArrayList<Derivable> plist, String status) {

        if (Term.emptyString(status))
            status = "status(thm)";
        ArrayList<Derivable> parentlist = new ArrayList<>();
        for (Derivable d : plist) {
            ArrayList<Derivable> dlist = new ArrayList<Derivable>();
            dlist.add(d);
            parentlist.add(new Derivation("reference", dlist, ""));
        }
        return new Derivation(operator, parentlist, status);
    }
}
