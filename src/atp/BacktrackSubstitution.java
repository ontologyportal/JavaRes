package atp;
/*
Copyright 2010-2011 Adam Pease, apease@articulatesoftware.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

 A substitution that does not allow composition of new bindings, but
 in exchange offers backtrackability. Bindings are recorded in two
 data structures:
    subst (defined in the parent class Substitutions) is a dictionary that maps variables to terms
    bindings is an ordered list of bindings.
*/

import java.io.StringReader;
import java.util.*;

public class BacktrackSubstitution extends Substitutions {

    public ArrayDeque<Binding> bindings = new ArrayDeque<Binding>();
    
    public class Binding {
        Term key = null;
        Term value = null;
        
        /** ***************************************************************
         * Return whether two bindings are equal.
         */    
        @Override public boolean equals(Object s2_obj) {

            Binding s2 = (Binding) s2_obj;
            if (!key.equals(s2.key))
                return false;
            if (!value.equals(s2.value))
                return false;
            return true;
        }

        /** ***************************************************************
         * should never be called so throw an error.
         */   
        public int hashCode() {

            return key.hashCode() + value.hashCode();
        }        
    }
    
    /** ***************************************************************
     * Return a state to which this substitution can be backtracked
     * later. We encode the state of the binding list, but also the
     * object itself, to allow for some basic sanity checking.
     */ 
    public int getState() {

       return bindings.size();
    }
    
    /** ***************************************************************
     * Return whether two substitutions are equal.
     */    
    @Override public boolean equals(Object s2_obj) {

        BacktrackSubstitution s2 = (BacktrackSubstitution) s2_obj;
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
        if (bindings.size() != s2.bindings.size())
            return false;
        for (int i = 0; i < bindings.size(); i++)
            if (!bindings.pop().equals(s2.bindings.pop()))
                return false;
        return true;
    }

    /** ***************************************************************
     * should never be called so throw an error.
     */   
    public int hashCode() {

        int result = 0;
        for (Binding b : bindings)
            result += b.hashCode();
        return result;
    }
    
    /** ***************************************************************
     * Backtrack a single binding (if there is one). Return success or
     * failure.
     */ 
    public boolean backtrack() {

       if (bindings != null && bindings.size() > 0) {
          Binding tmp = bindings.pop();
          subst.remove(tmp.key);
          return true;
       }
       else
          return false;
    }
    
    /** ***************************************************************
     * Backtrack to the given state. Note that we only perform very
     * basic sanity checking. Return number of binding retracted.
     */ 
    public int backtrackToState(int bt_state) {

       int subst = bt_state;
       int state = bt_state;
       // assert subst == self;
       int res = 0;
       
       while (bindings.size() > state) {
           backtrack();
           res = res + 1;
       }
       return res;
    }
    
    /** ***************************************************************
     * Add a single binding to the substitution.
     */ 
    public void addBinding(Term var, Term term) {

       subst.put(var,term);
       Binding b = new Binding();
       b.key = var;
       b.value = term;
       bindings.add(b);
    }
    
    /** ***************************************************************
     * Match t1 onto t2. If this succeeds, return true and modify subst
     * accordingly. Otherwise, return false and leave subst unchanged
     * (i.e. backtrack subst to the old state). Providing a partial
     * substitution allows us to use the function in places where we need
     * to find a common match for several terms.
     */    
    public boolean match(Term matcher, Term target) {

    	//System.out.println("BacktrackSubstitution.match(): matcher: " + matcher + " target: " + target);
        int bt_state = getState();
        boolean result = true;

        if (matcher.isVar()) {
           if (isBound(matcher)) {
              if (!value(matcher).equals(target))
                 result = false;
                // No else case - variable is already bound correctly
           }
           else
              addBinding(matcher, target);
        }
        else {
           if (target.isVar() || !matcher.getFunc().equals(target.getFunc()))
              result = false;
           else {
              int index = matcher.getArgs().size();
              if (target.getArgs().size() < index)
                  index = target.getArgs().size();
              for (int i = 0; i < index; i++) {
                 Term s = matcher.getArgs().get(i);
                 Term t = target.getArgs().get(i);
                 result = match(s, t);
                 if (!result)
                    break;
              }
           }
        }
        if (result)
           return true;
        backtrackToState(bt_state);
        return false;
    }
                 
    /** ***************************************************************
     * Match t1 onto t2. If this succeeds, return true and modify subst
     * accordingly. Otherwise, return false and leave subst unchanged
     * (i.e. backtrack subst to the old state). Providing a partial
     * substitution allows us to use the function in places where we need
     * to find a common match for several terms. This is an alternative
     * implementation using explicit work lists instead of recursion.
     */    
    public boolean match_norec(Term t1, Term t2) {

        int bt_state = getState();
        boolean result = true;
        ArrayDeque<Term> mlist = new ArrayDeque<Term>();
        mlist.add(t1);
        ArrayDeque<Term> tlist = new ArrayDeque<Term>();
        tlist.add(t2);
        while (mlist != null && mlist.size() > 0) {
            Term matcher  = mlist.pop();
            Term target   = tlist.pop();

            if (matcher.isVar()) {
                if (isBound(matcher)) {
                    if (!value(matcher).equals(target)) {
                        result = false;
                        break;                    
                    // No else case - variable is already bound correctly
                    }
                }
                else
                    addBinding(matcher, target);
            }
            else {
                if (target.isVar() || !matcher.getFunc().equals(target.getFunc())) {
                    result = false;
                    break;
                }
                else {
                    // We now know that matcher is of the form f(s1, ..., sn)
                    // and target is of the form f(t1, ..., tn). So now we
                    // need to find a common substitution for s1 onto t1,
                    // ..., sn onto tn. To do this, we add the argument lists
                    // to the work lists and let them be processed in the same
                    // loop. 
                    mlist.addAll(matcher.getArgs());
                    tlist.addAll(target.getArgs());               
                }
            }
        }
        if (result)
           return true;
        backtrackToState(bt_state);
        return false;
    }
        
    /** ***************************************************************
     * Overloaded to catch usage errors!
     */ 
    public void composeBinding(Term var, Term term) {

       System.out.println("You cannot compose backtrackable substitutions.");
    }
}
