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
        //System.out.println("Derivation(): par " + par);
        operator = op;
        if (par != null)
            parents.addAll(par);
        //System.out.println("Derivation(): parents " + parents);
        status = stat;
    }

    /** ***************************************************************
     */
    public Derivation deepCopy() {

        Derivation result = new Derivation();
        result.operator = this.operator;
        result.status = this.status;
        result.parents = new ArrayList<Derivable>();
        if (parents != null) {
            for (Derivable d : parents)
                result.parents.add(d.deepCopy());
        }
        if (derivation != null)
            result.derivation = this.derivation.deepCopy();
        result.refCount = this.refCount;
        result.name = this.name;
        return result;
    }

    /** ***************************************************************
     * Return a string for the derivation in TPTP-3 format.
     */
    public String toString() {

        //System.out.println("Derivation.toString(): op: " + operator);
        //System.out.println("Derivation.toString(): parents: " + parents);
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
     * references all parents.
     */
    public static Derivation flatDerivation(String operator, ArrayList<Derivable> plist, String status) {

        //System.out.println("flatDerivation(): plist " + plist);
        if (Term.emptyString(status))
            status = "status(thm)";
        ArrayList<Derivable> parentlist = new ArrayList<>();
        for (Derivable d : plist) {
            ArrayList<Derivable> dlist = new ArrayList<>();
            dlist.add(d);
            //System.out.println("flatDerivation(): added to dlist " + dlist);
            Derivation der = new Derivation("reference", dlist, "");
            //System.out.println("flatDerivation(): der " + der);
            //System.out.println("flatDerivation(): der.operator " + der.operator);
            //System.out.println("flatDerivation(): der.parents " + der.parents);
            //if (der.parents.size() == 1) {
            //    Derivable parent = der.parents.get(0);
                //System.out.println("flatDerivation(): der.parents.get(0) " + parent);
                //System.out.println("flatDerivation(): der.parents.get(0).name " + parent.name);
                //System.out.println("flatDerivation(): der.parents.get(0).class " + parent.getClass());
                //if (parent.getClass().getName().equals("atp.Formula"))
                //    System.out.println("as formula: " + ((Formula) parent).name);
            //}
            parentlist.add(der);
            //System.out.println("flatDerivation(1): parentlist " + parentlist);
        }
        //System.out.println("flatDerivation(2): parentlist " + parentlist);
        return new Derivation(operator, parentlist, status);
    }
}
