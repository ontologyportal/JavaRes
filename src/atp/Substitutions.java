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

    A simple implementation of substitutions.

    Definition: A substitution sigma is a function sigma:V->Terms(F,V)
    with the property that sigma(X)=X for all but finitely many variables
    X from V.

    A substitution is continued to terms recursively:
    sigma(f(t1, ..., tn)) = f(sigma(t1), ..., sigma(t2))

    Substitutions are customarily represented by the Greek letter sigma.

    Footnote:
    If more than one substitution is needed, the second one is usually
    called tau, and further ones are denoted with primes or subscripts. 
    
    Substitutions map variables to terms. Substitutions as used here
    are always fully expanded, i.e. each variable is bound directly to
    the term it maps too.
*/

package atp;
import java.util.*;

public class Substitutions {

    public HashMap<Term,Term> subst = new HashMap<Term,Term>();
    private static int freshVarCounter = 0;
    
    /** ***************************************************************
     */    
    public Substitutions (Term var, Term val) {
    	subst.put(var,val);
    }
    
    /** ***************************************************************
     */    
    public Substitutions () {
    	subst = new HashMap<Term,Term>();
    }
    
    /** ***************************************************************
     */    
    @Override public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("{");
        Iterator<Term> it = subst.keySet().iterator();
        while (it.hasNext()) {
            Term key = it.next();
            Term value = subst.get(key);
            result.append(key + "<-" + value);
            if (it.hasNext())
                result.append(",");
        }
        result.append("}");
        return result.toString();
    }

    /** ***************************************************************
     * Return a copy of the substitution.
     */    
    public Substitutions deepCopy() {

        Substitutions result = new Substitutions();
        Iterator<Term> it = subst.keySet().iterator();
        while (it.hasNext()) {
            Term key = it.next();
            Term value = subst.get(key);
            result.subst.put(key.deepCopy(),value.deepCopy());
        }
        return result;
    }

    /** ***************************************************************
     * Return the value of a variable (i.e. the term it is bound to,
     * or the variable itself if it is not bound).
     */    
    public Term value(Term var) {

        if (subst.keySet().contains(var))
            return subst.get(var);
        else
            return var;
    }

    /** ***************************************************************
     * Return True if var is bound in self, false otherwise.
     */    
    public boolean isBound(Term var) {

        return subst.keySet().contains(var);
    }
    
    /** ***************************************************************
     */ 
    public void addSubst(Term t1, Term t2) {
        subst.put(t1,t2);
    }

    /** ***************************************************************
     */ 
    public void addAll(Substitutions s) {
        
        Iterator<Term> it = s.subst.keySet().iterator();
        while (it.hasNext()) {
            Term t1 = it.next();
            Term t2 = s.subst.get(t1);
            subst.put(t1,t2);
        }
    }
    
    /** ***************************************************************
     * Return whether two substitutions are equal.
     */    
    @Override public boolean equals(Object s2_obj) {

        assert s2_obj != null : "Substitutions.equals() argument is null";
        assert !s2_obj.getClass().getName().equals("Substitutions") : "Substitutions.equals() passed object not of type Substitutions"; 
        Substitutions s2 = (Substitutions) s2_obj;
        if (subst.keySet().size() != s2.subst.keySet().size())
            return false;
        //System.out.println("INFO in Substitutions.equals(): keySet size : " + subst.keySet().size());        
        //System.out.println("INFO in Substitutions.equals(): this : " + this + " other: " + s2);
        //System.out.println("INFO in Substitutions.equals(): this keys: " + this.subst.keySet() + 
        //        " other keys: " + s2.subst.keySet()); 
        Iterator<Term> it = subst.keySet().iterator();
        while (it.hasNext()) {
            Term key = it.next();
            //System.out.println("INFO in Substitutions.equals(): key " + key + 
            //        " value: " + subst.get(key) + " other value: " + s2.subst.get(key));
            if (!s2.subst.containsKey(key))
                return false;
            if (!subst.get(key).equals(s2.subst.get(key)))
                return false;
        }
        return true;
    }

    /** ***************************************************************
     * should never be called so throw an error.
     */   
    public int hashCode() {
        assert false : "Substitutions.hashCode not designed";
        return 0;
    }
    
    /** ***************************************************************
     * Apply the substitution to a term. Return the result.
     */    
    public Term apply(Term term) {
                
        //System.out.println("INFO in Substitutions.apply(): " + this + " " + term);
        Term res = new Term();
        if (term.isVar()) {
            //System.out.println("term is var");
            if (subst.containsKey(term)) {
                //System.out.println("contains key");
                res = subst.get(term);
            }
            else
                res.t = term.t;
        }
        else {
            res.t = term.t;
            for (int i = 0; i < term.subterms.size(); i++)
                res.subterms.add(apply(term.subterms.get(i)));
            return res;
        }       
        //System.out.println("INFO in Substitutions.apply(): returning: " + res);
        return res;
    }
    
    /** ***************************************************************
     * Apply the substitution to a list. Return the result.
     */    
    public ArrayList<Term> applyList(ArrayList<Term> l) {
                
        ArrayList<Term> res = new ArrayList<Term>();
        for (int i = 0; i < l.size(); i++)
            res.add(apply(l.get(i)));
        return res;
    }
    
    /** ***************************************************************
     * Modify the substitution by adding a new binding (var,term). 
     * If the term is null, remove any binding for var. If it
     * is not, add the binding. In either case, return the previous
     * binding of the variable, or null if it was unbound.
     */    
    public Term modifyBinding(Term var, Term binding) {

    	Term res = null;
        if (isBound(var))
            res = value(var);
        else
            res = null;

        if (binding == null) {        	
            if (isBound(var))
                subst.remove(var);
        }
        else
            subst.put(var,binding);
        return res;
    }
    
    /** ***************************************************************
     * Apply a new binding to an existing substitution.
     */    
    public void composeBinding(Term var, Term term) {

    	//System.out.println("INFO in Substitutions.composeBinding(): var,term: " + var + " " + term);
    	//System.out.println("INFO in Substitutions.composeBinding(): initial subst: " + subst);
    	Substitutions tempsubst = new Substitutions(var,term);
        Iterator<Term> it = subst.keySet().iterator();
        while (it.hasNext()) {
            Term key = it.next();
            Term bound = subst.get(key);
        	//System.out.println("INFO in Substitutions.composeBinding(): key,bound: " + key + " " + bound);
            subst.put(key,tempsubst.apply(bound));
        }
        if (!subst.containsKey(var))
            subst.put(var,term);   
    	//System.out.println("INFO in Substitutions.composeBinding(): subst: " + subst);
    }
    
    /** ***************************************************************
     * Return a fresh variable. Note that this is not guaranteed to be
     * different from input variables. However, it is guaranteed that
     * freshVar() will never return the same variable more than once.
     */    
    public static Term freshVar() {

        Substitutions.freshVarCounter = Substitutions.freshVarCounter + 1;
        return Term.string2Term("X" + Integer.toString(Substitutions.freshVarCounter));
    }
    
    /** ***************************************************************
     * Create a substitution that maps all variables in var to fresh
     * variables. Note that there is no guarantee that the fresh
     * variables have never been used. However, there is a a guarantee
     * that the fresh variables have never been produced by a uniqSubst
     * substitution.
     */    
    public static Substitutions freshVarSubst(ArrayList<Term> vars) {

        Substitutions s = new Substitutions();
        for (int i = 0; i < vars.size(); i++) {
            Term newVar = freshVar();
            s.subst.put(vars.get(i),newVar);
        }
        return s;
    }
    

}
