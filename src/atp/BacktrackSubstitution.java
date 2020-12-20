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
            assert false : "BacktrackSubstitutions$Binding.hashCode not designed";
            return 0;
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
        assert false : "BacktrackSubstitutions.hashCode not designed";
        return 0;
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
    
    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    private static Term s1 = null;
    private static Term s2 = null;
    private static Term s3 = null;
    private static Term s4 = null;
    private static Term s5 = null;
    private static Term s6 = null;
    private static Term s7 = null;
    private static Term t1 = null;
    private static Term t2 = null;
    private static Term t3 = null;
    private static Term t4 = null;
    private static Term t5 = null;
    private static Term t6 = null;
    private static Term t7 = null;
    
    /** ***************************************************************
     */ 
    public static void setUp() {
        
        s1 = Term.string2Term("X");
        t1 = Term.string2Term("a");

        s2 = Term.string2Term("X");
        t2 = Term.string2Term("f(X)");

        s3 = Term.string2Term("X");
        t3 = Term.string2Term("f(Y)");

        s4 = Term.string2Term("f(X, a)");
        t4 = Term.string2Term("f(b, Y)");

        s5 = Term.string2Term("f(X, g(a))");
        t5 = Term.string2Term("f(X, Y))");

        s6 = Term.string2Term("f(X, g(a))");
        t6 = Term.string2Term("f(X, X))");

        s7 = Term.string2Term("g(X)");
        t7 = Term.string2Term("g(f(g(X),b))");
    }
    
    /** ***************************************************************
     * Test if s can be matched onto t. If yes, report the
     * result. Compare to the expected result.
     */ 
    public static void match_test(boolean noRec, Term s, Term t, boolean success_expected) {

       System.out.println("INFO in BacktrackSubstitution.match_test(): Trying to match " + s + " onto " + t);
       BacktrackSubstitution sigma = new BacktrackSubstitution();
       boolean res = false;
       if (noRec)
           res = sigma.match_norec(s,t);
       if (success_expected) {
          if (!res)
              System.out.println("failure");
          if (!sigma.apply(s).equals(t))
              System.out.println("failure");              
          System.out.println(sigma.apply(s) + " " + t + " " + sigma);
       }
       else {
           System.out.println("Failure");
           if (res)
               System.out.println("Failure");
       }
    }

    /** ***************************************************************
     * Test Matching.
     */ 
    public static void testMatch() {

        match_test(false, s1, t1, true);
        match_test(false, s2, t2, true);
        match_test(false, s3, t3, true);
        match_test(false, s4, t4, false);
        match_test(false, s5, t5, false);
        match_test(false, s6, t6, false);
        match_test(false, s7, t7, true);

        match_test(false, t1, s1, false);
        match_test(false, t2, s2, false);
        match_test(false, t3, s3, false);
        match_test(false, t4, s4, false);
        match_test(false, t5, s5, true);
        match_test(false, t6, s6, false);
        match_test(false, t7, s7, false);
    }
    
    /** ***************************************************************
     * Test Matching.
     */ 
    public static void testMatchNoRec() {

        match_test(true, s1, t1, true);
        match_test(true, s2, t2, true);
        match_test(true, s3, t3, true);
        match_test(true, s4, t4, false);
        match_test(true, s5, t5, false);
        match_test(true, s6, t6, false);
        match_test(true, s7, t7, true);

        match_test(true, t1, s1, false);
        match_test(true, t2, s2, false);
        match_test(true, t3, s3, false);
        match_test(true, t4, s4, false);
        match_test(true, t5, s5, true);
        match_test(true, t6, s6, false);
        match_test(true, t7, s7, false);
    }
    
    /** ***************************************************************
     * Test backtrackable substitutions.
     */ 
    public static void testBacktrack() {
            
        System.out.println("---------------------");
        System.out.println("INFO in testBacktrack()");
        BacktrackSubstitution sigma = new BacktrackSubstitution();
        int state = sigma.getState();
        sigma.addBinding(Term.string2Term("X"), Term.string2Term("f(Y)"));
        int res = sigma.backtrackToState(state);
        if (res == 1)
            System.out.println("INFO in testBacktrack(): correct number of states");
        else
            System.out.println("INFO in testBacktrack(): error correct number of states (expected 1): " + res);
        boolean success = sigma.backtrack();
        if (!success)
            System.out.println("INFO in testBacktrack(): correct empty stack");
        else
            System.out.println("INFO in testBacktrack(): failure, non-empty stack");           
    }
        
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
        
        testBacktrack();
    }
}
