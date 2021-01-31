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
    */
package atp;

import java.util.*;

public class Unification {
  
    /** ***************************************************************
     * Perform an occurs-check, i.e. determine if the variable x occurs in
     * the term t. If that is the case (and t != x), the two can never be
     * unified.   
     */
    private static boolean occursCheck(Term x, Term t) {

       if (t.isCompound()) {
            for (int i = 0; i < t.subterms.size(); i++)
                if (occursCheck(x, t.subterms.get(i)))
                    return true;
            return false;
       }
       else
           return x.equals(t);
    }

    /** ***************************************************************
     * Unify all terms in term1 with the corresponding terms in term2 with a
     * common substitution variable "subst".   
     */
    private static Substitutions mguTermList(ArrayList<Term> l1, ArrayList<Term> l2) {

        //System.out.println("INFO in Unification.mguTermList(): attempting to unify " + term1 + " and " + term2);
        Substitutions subst = new Substitutions();
        
        if (l1.size() != l2.size()) {
            if (subst.subst.keySet().size() > 0)
                return subst;
            else
                return null;
        }
        while (l1.size() != 0) {           
            Term t1 = l1.remove(0); // Pop the first term pair to unify off the lists            
            Term t2 = l2.remove(0); // (removes and returns the denoted elements).
            //System.out.println("INFO in Unification.mguTermList(): attempting to unify " + t1 + " and " + t2); 
            if (t1.isVar()) {
                if (t1.equals(t2))
                    // We could always test this upfront, but that would
                    // require an expensive check every time. 
                    // We descend recursively anyway, so we only check this on
                    // the terminal case.  
                    continue;
                if (occursCheck(t1,t2))
                    return null;
                // We now create a new substitution that binds t2 to t1, and
                // apply it to the remaining unification problem. We know
                // that every variable will only ever be bound once, because
                // we eliminate all occurrences of it in this step - remember
                // that by the failed occurs-check, t2 cannot contain t1.
                Substitutions newBinding = new Substitutions();
                newBinding.addSubst(t1,t2);                
                l1 = newBinding.applyList(l1);
                l2 = newBinding.applyList(l2);
                subst.composeBinding(t1, t2);
            }
            else if (t2.isVar()) {
                // Symmetric case
                // We know that t1!=t2, so we can drop this check
                if (occursCheck(t2, t1))
                    return null;
                Substitutions newBinding = new Substitutions();
                newBinding.addSubst(t2, t1);          
                l1 = newBinding.applyList(l1);
                l2 = newBinding.applyList(l2);
                subst.composeBinding(t2, t1);
            }
            else {
                if (!t1.isCompound() || !t2.isCompound())
                    return null;
                // For f(s1, ..., sn) = g(t1, ..., tn), first f and g have to
                // be equal...
                if (!t1.t.equals(t2.t))
                    return null;
                // ...and then we need to ensure that for all i si=ti get
                // added to the list of equations to be solved.
                l1.addAll(t1.getArgs());
                l2.addAll(t2.getArgs());
            }
        }
        //System.out.println("INFO in Unification.mguTermList(): subst on exit: " + subst);
        return subst;
    }

    /** ***************************************************************
     * Try to unify t1 and t2, return substitution on success, or None 
     * on failure.   
     */
    public static Substitutions mgu(Term t1, Term t2) {

    	ArrayList<Term> l1 = new ArrayList<Term>();
    	l1.add(t1);
    	ArrayList<Term> l2 = new ArrayList<Term>();
    	l2.add(t2);
        return mguTermList(l1, l2);
    }
}
