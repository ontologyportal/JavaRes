/*
 A simple implementation of Russel&Norvig's clausification algorithm.
  
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

public class Clausifier {

    public static int varCounter = 0;
    public static int skolemCounter = 0;
    public static int axiomCounter = 0;
    public static String typePrefix = "axiom";
    public static long startTime = 0;

    /** ***************************************************************
     */
    public static void counterReset() {
        varCounter = 0;
        skolemCounter = 0;
        axiomCounter = 0;
    }

    /** ***************************************************************
     * a->b is the same as -a|b
     */
    private static BareFormula removeImp(BareFormula form) {
    
        BareFormula result = new BareFormula();
        result.op = Lexer.Or;
        BareFormula newLHS = new BareFormula();
        newLHS.op = Lexer.Negation;
        if (form.child2 != null)
            result.child2 = removeImpEq(form.child2);
        if (form.child1 != null)
            newLHS.child1 = removeImpEq(form.child1);
        if (form.lit1 != null)
            newLHS.lit1 = form.lit1;
        if (form.lit2 != null)
            result.lit2 = form.lit2;
        result.child1 = newLHS;
        return result;
    }
    /** ***************************************************************
     * a<-b is the same as a|~b
     */
    private static BareFormula removeBImp(BareFormula form) {
    
        BareFormula result = new BareFormula();
        result.op = Lexer.Or;
        BareFormula newRHS = new BareFormula();
        newRHS.op = Lexer.Negation;
        if (form.child1 != null)
            result.child1 = removeImpEq(form.child1);
        if (form.child2 != null)
            newRHS.child1 = removeImpEq(form.child2);
        if (form.lit2 != null)
            newRHS.lit1 = form.lit2;
        if (form.lit1 != null)
            result.lit1 = form.lit1;
        result.child2 = newRHS;
        return result;
    }

    /** ***************************************************************
     * a<=>b is the same as
     * (a->b)&(b->a)
     */
    private static BareFormula removeEq(BareFormula form) {

        BareFormula result = new BareFormula();
        BareFormula lhs = new BareFormula();
        if (form.child1 != null)
            lhs.child1 = form.child1;
        if (form.child2 != null)
            lhs.child2 = form.child2;
        if (form.lit1 != null)
            lhs.lit1 = form.lit1;
        if (form.lit2 != null)
            lhs.lit2 = form.lit2;
        lhs.op = Lexer.Implies;
        BareFormula rhs = new BareFormula();
        if (form.child1 != null)
            rhs.child2 = form.child1;
        if (form.child2 != null)
            rhs.child1 = form.child2;
        if (form.lit1 != null)
            rhs.lit2 = form.lit1;
        if (form.lit2 != null)
            rhs.lit1 = form.lit2;
        rhs.op = Lexer.Implies;
        result.op = Lexer.And;
        result.child1 = lhs;
        result.child2 = rhs;
        return removeImpEq(result);       
    }
    
    /** ***************************************************************
     * a<=>b is the same as
     * (a->b)&(b->a)
     * a->b is the same as -a|b
     */
    public static BareFormula removeImpEq(BareFormula form) {

        if (form.op.equals(Lexer.Implies)) {         
            return removeImp(form);
        }
        else if (form.op.equals(Lexer.Equiv)) {
            return removeEq(form);
        }
        else if (form.op.equals(Lexer.BImplies)) {
            return removeBImp(form);
        }
        else {
            BareFormula result = form.deepCopy();
            if (result.child1 != null)
                result.child1 = removeImpEq(result.child1);
            if (result.child2 != null)
                result.child2 = removeImpEq(result.child2);
            return result;
        }
    }

    /** ***************************************************************
     * -(p | q) becomes -p & -q
     * -(p & q) becomes -p | -q
     * -![X]:p becomes ?[X]:-p
     * -?[X]:p becomes ![X]:-p
     * --p becomes p
     * @param flip when true indicates to change the operator of the given
     * literal
     */
    public static Literal moveNegationIn(Literal lit, boolean flip) {

        if (!flip)
            return lit;
        Literal result = lit.deepCopy();
        if (result.atom.getFunc().equals(Lexer.EqualSign) && flip) 
            result.atom.t = Lexer.NotEqualSign;        
        else if (result.atom.getFunc().equals(Lexer.NotEqualSign) && flip) 
            result.atom.t = Lexer.EqualSign;     
        else 
            result.negated = !result.negated;       
        return result;
    }

    /** ***************************************************************
     */
    public static BareFormula moveNegationIn(BareFormula form) {
        return moveNegationIn(form,false);
    }
    
    /** ***************************************************************
     */
    private static String flipOperator(String s) {
        
        if (s.equals("|"))
            return "&";
        else if (s.equals("&"))
            return "|";
        else if (s.equals("~|"))
            return "|";
        else if (s.equals("~&"))
            return "&";
        else if (s.equals("!"))
            return "?";
        else if (s.equals("?"))
            return "!"; 
        return s;
    }
    
    /** ***************************************************************
     * -(p | q) becomes -p & -q
     * -(p & q) becomes -p | -q
     * -![X]:p becomes ?[X]:-p
     * -?[X]:p becomes ![X]:-p
     * --p becomes p
     */
    public static BareFormula moveNegationIn(BareFormula form, boolean flip) {
            
        //System.out.println("INFO in Clausifier.moveNegationIn(): " + form + " " + flip);
        BareFormula result = form.deepCopy();
        if (result.op.equals("~")) {  // there is no child2 or lit2 in this case
            //System.out.println("INFO in Clausifier.moveNegationIn(): has a negation: " + result);
            if (flip) {
                //System.out.println("INFO in Clausifier.moveNegationIn(): negations cancel: " + result);
                if (result.child1 != null)
                    result = moveNegationIn(result.child1,false);
                if (result.lit1 != null)
                    result.lit1 = moveNegationIn(result.lit1,false);
            }
            else {      
                //System.out.println("INFO in Clausifier.moveNegationIn(): negation with no flip: " + result);
                if (result.child1 != null) {
                    result = moveNegationIn(result.child1,true);
                }
                else if (result.lit1 != null) {  // should handle a no-op parent
                    result.lit1 = moveNegationIn(result.lit1,true);
                    result.op = "";
                }
            }
        }
        else {
            if (flip) {
                //System.out.println("INFO in Clausifier.moveNegationIn(): flipping: " + result);
                result.op = flipOperator(result.op);
                if (result.child2 != null)
                    result.child2 = moveNegationIn(form.child2,true);
                if (result.child1 != null)
                    result.child1 = moveNegationIn(form.child1,true);
                if (result.lit1 != null && !BareFormula.isQuantifier(result.op)) 
                    result.lit1 = moveNegationIn(form.lit1,true);                
                if (result.lit2 != null)
                    result.lit2 = moveNegationIn(form.lit2,true);
            }
            else {
                //System.out.println("INFO in Clausifier.moveNegationIn(): 5: " + result);
                if (result.child2 != null)
                    result.child2 = moveNegationIn(form.child2,false);
                if (result.child1 != null)
                    result.child1 = moveNegationIn(form.child1,false);
                if (result.lit1 != null && !BareFormula.isQuantifier(result.op)) 
                    result.lit1 = moveNegationIn(form.lit1,false);                
                if (result.lit2 != null)
                    result.lit2 = moveNegationIn(form.lit2,false);
            }
        }
        //System.out.println("INFO in Clausifier.moveNegationIn(): 6 returning: " + result);
        return result;
    }

    /** ***************************************************************
     */
    private static Term generateNewVar() {
        
        return Term.string2Term("VAR" + Integer.toString(varCounter++));
    }
    
    /** ***************************************************************
     * The scope of variables in quantifiers is local to the quantifier, 
     * but to avoid confusion, variables of the same name but different scope 
     * in a formula are renamed.
     * 
     * ![X]:p(X) | ?[X]:q(X) becomes
     * ![X]:p(X) | ?[Y]:q(Y)
     */
    public static BareFormula standardizeVariables(BareFormula form) {
    
        BareFormula result = form.deepCopy();
        if (form.child1 != null)
            result.child1 = standardizeVariables(form.child1);
        if (form.child2 != null)
            result.child2 = standardizeVariables(form.child2);
        if (BareFormula.isQuantifier(form.op)) {
            Substitutions subst = new Substitutions();
            Term oldVar = form.lit1.atom;
            Term newVar = generateNewVar();
            subst.addSubst(oldVar,newVar);
            return result.substitute(subst);
        }
        return result;
    }

    /** ***************************************************************
     */
    private static BareFormula moveQuantLeftChild(BareFormula form) {
        
        //System.out.println("Child1 has quantifier.  Current result: " + form);
        BareFormula newParent = new BareFormula();
        newParent.op = form.child1.op;
        newParent.lit1 = form.child1.lit1;  // child1 has quantifier list
        BareFormula newChild = new BareFormula();
        newParent.child2 = newChild;
        if (form.child2 != null)
            newChild.child2 = form.child2;
        if (form.lit2 != null)
            newChild.lit2 = form.lit2;
        if (form.child1.child2 != null)
            newChild.child1 = form.child1.child2;
        if (form.child1.lit2 != null)
            newChild.lit1 = form.child1.lit2;
        newChild.op = form.op;
        //System.out.println("Child1 has quantifier.  New result: " + newParent);   
        return newParent;
    }

    /** ***************************************************************
     */
    private static BareFormula moveQuantRightChild(BareFormula form) {
        
        //System.out.println("Child2 is not null: " + form.child2);
        //System.out.println("Child2 has quantifier.  Current result: " + form);
        BareFormula newParent = new BareFormula();
        newParent.op = form.child2.op;
        newParent.lit1 = form.child2.lit1;  // child2 has a quantifier list
        BareFormula newChild = new BareFormula();
        newParent.child2 = newChild;
        if (form.child1 != null)
            newChild.child1 = form.child1;
        if (form.lit1 != null)
            newChild.lit1 = form.lit1;
        if (form.child2.child2 != null)
            newChild.child2 = form.child2.child2;
        if (form.child2.lit2 != null)
            newChild.lit2 = form.child2.lit2;
        newChild.op = form.op;
        //System.out.println("Child2 has quantifier.  New result: " + newParent);
        return newParent;
    }
    
    /** ***************************************************************
     * [op1   child1            child2]
     *    [op2 lit1 child3] [op3 lit2 child4]
     * becomes
     * [op2 lit1 new1]
     *      [op3 lit2 new2]
     *          [op1 child3 child4]  
     */
    private static BareFormula moveQuantBothChildren(BareFormula form) {
    
        BareFormula newParent = new BareFormula();
        BareFormula newMidChild = new BareFormula();
        BareFormula newBottomChild = new BareFormula();
        newParent.op = form.child1.op;
        newParent.lit1 = form.child1.lit1;
        newParent.child2 = newMidChild;
        newMidChild.op = form.child2.op;
        newMidChild.lit1 = form.child2.lit1;
        newMidChild.child2 = newBottomChild;
        newBottomChild.op = form.op;
        if (form.child1.child2 != null)
            newBottomChild.child1 = form.child1.child2;
        if (form.child1.lit2 != null)
            newBottomChild.lit1 = form.child1.lit2;
        if (form.child2.child2 != null)
            newBottomChild.child2 = form.child2.child2;
        if (form.child2.lit2 != null)
            newBottomChild.lit2 = form.child2.lit2;
        return newParent;
    }
    
    /** ***************************************************************
     * p|![X]q(X)
     * becomes
     * ![X]p | q(X)
     */
    private static BareFormula moveQuantifiersLeftIterate(BareFormula form) {
    
        //System.out.println("INFO in Clausifier.moveQuantifiersLeft(): " + form);
        //System.out.println("op: " + form.op);
        if (form == null)
            return null;
        BareFormula result = form.deepCopy(); 
        if (BareFormula.isQuantifier(form.op)) {
            //System.out.println("Formula has quantifier(1): " + form);
            result.op = form.op;
            result.lit1 = form.lit1;   // child1 not needed since it's null when there's a quantifier
            result.child2 = moveQuantifiersLeftIterate(form.child2);
            result.lit2 = form.lit2;
            //System.out.println("Formula has quantifier: " + result);
            return result;
        }
        if (form.child2 != null)
            result.child2 = moveQuantifiersLeftIterate(form.child2);
        if (form.child1 != null) 
            result.child1 = moveQuantifiersLeftIterate(form.child1);
        if (result.child2 != null && BareFormula.isQuantifier(result.child2.op) &&
            result.child1 != null && BareFormula.isQuantifier(result.child1.op)) {
            changed = true;
            return moveQuantBothChildren(result);        
        }
        if (result.child2 != null && BareFormula.isQuantifier(result.child2.op)) {
            changed = true;
            return moveQuantRightChild(result);      
        }
        if (result.child1 != null && BareFormula.isQuantifier(result.child1.op)) {
            changed = true;
            return moveQuantLeftChild(result);             
        }
        return result;
    }    

    private static boolean changed = true;
    
    /** ***************************************************************
     */
    public static BareFormula moveQuantifiersLeft(BareFormula form) {

        changed = true;
        BareFormula result = form.deepCopy();
        while (changed) {
            changed = false;
            result = moveQuantifiersLeftIterate(result);
        }
        return result;
    }
    
    /** ***************************************************************
     */
    private static Term generateNewSkolem(HashSet<Term> args) {
        
        StringBuffer argList = new StringBuffer();
        Iterator<Term> it = args.iterator();
        while (it.hasNext()) {
            Term t = it.next();
            argList.append(t.toString());
            if (it.hasNext())
                argList.append(",");
        }
        if (argList.length() > 0)
            return Term.string2Term("skf" + Integer.toString(varCounter++) + "(" + argList + ")");
        else
            return Term.string2Term("skf" + Integer.toString(varCounter++));
    }
    
    /** ***************************************************************
     */
    private static BareFormula skolemizationRecurse(BareFormula form, 
            HashSet<Term> uList) {
    
        //System.out.println("INFO in Clausifier.skolemizationRecurse(): " + form);
        //System.out.println(uList);
        BareFormula result = form.deepCopy();
        if (form.child1 != null)
            result.child1 = skolemizationRecurse(form.child1,uList);
        if (form.child2 != null)
            result.child2 = skolemizationRecurse(form.child2,uList);
        if (form.op.equals("?")) { // existential
            Term var = form.lit1.atom;
            Term skolem = generateNewSkolem(uList);
            Substitutions subst = new Substitutions();
            subst.addSubst(var,skolem);
            //System.out.println("calling substitution with: " + subst);
            result = result.substitute(subst);
            result.op = ""; // not sure if this is ok
            if (result.child2 != null)
                result = result.child2;
            //System.out.println("returning result: " + result);
            return result; 
        }
        if (form.op.equals("!")) // universal
            uList.add(form.lit1.atom);
        return result;
    }   
    
    /** ***************************************************************
     * Create a unique function in place of every existentially quantified variable.  
     * Include every universally quantified variable that is in scope, inside the function.
     * ![X]:p(X) => (?[Y]:h(Y) & a(X,Y))
     *     becomes                
     * ![X]:p(X) => (h(skf(X)) & a(X,skf(X)))
     */
    public static BareFormula skolemization(BareFormula form) {
    
        return skolemizationRecurse(form,new HashSet<Term>());
    }   

    /** ***************************************************************
     * Remove universal quantifiers
     */
    public static BareFormula removeUQuant(BareFormula form) {
    
        BareFormula result = form.deepCopy();
        if (form.child1 != null)
            result.child1 = removeUQuant(form.child1);
        if (form.child2 != null)
            result.child2 = removeUQuant(form.child2);
        if (form.op.equals("!")) {
            return result.child2;
        }
        else
            return result;
    }
    
    /** ***************************************************************
     * (a & b) | c becomes (a | c) & (b | c)
     */
    private static BareFormula distributeAndOverOrRecurse(BareFormula form) {

        if (((System.currentTimeMillis() - startTime) / 1000.0) > 30)
            return null;
        //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): " + KIF.format(form.toKIFString()) + " " + changed);
        BareFormula result = form.deepCopy();
        if (form.child1 != null)
            result.child1 = distributeAndOverOr(form.child1);
        if (form.child2 != null)
            result.child2 = distributeAndOverOr(form.child2);
        //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): (2): " + KIF.format(form.toKIFString()));
        if (form.op.equals("|")) {
            //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): top level or: " + KIF.format(form.toKIFString()));
            if (result.child1 != null && result.child1.op.equals("&")) {
                //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): child1 and: " + KIF.format(form.toKIFString()));
                BareFormula newParent = new BareFormula();
                newParent.op = "&";
                BareFormula newChild1 = new BareFormula();
                newChild1.op = "|";
                if (result.child1.child1 != null)
                    newChild1.child1 = result.child1.child1;
                if (result.child1.lit1 != null)
                    newChild1.lit1 = result.child1.lit1;
                if (result.child2 != null)
                    newChild1.child2 = result.child2;
                if (result.lit2 != null)
                    newChild1.lit2 = result.lit2;
                BareFormula newChild2 = new BareFormula();
                newChild2.op = "|";
                if (result.child1.child2 != null)
                    newChild2.child1 = result.child1.child2;
                if (result.child1.lit2 != null)
                    newChild2.lit1 = result.child1.lit2;
                if (result.child2 != null)
                    newChild2.child2 = result.child2;
                if (result.lit2 != null)
                    newChild2.lit2 = result.lit2;
                newParent.child1 = newChild1;
                newParent.child2 = newChild2;
                changed = true;
                //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): result: " + KIF.format(newParent.toKIFString()));
                return newParent;
            }
            else if (result.child2 != null && result.child2.op.equals("&")) {
                //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): child2 and: " + KIF.format(form.toKIFString()));
                BareFormula newParent = new BareFormula();
                newParent.op = "&";
                BareFormula newChild1 = new BareFormula();
                newChild1.op = "|";
                if (result.child2.child1 != null)
                    newChild1.child1 = result.child2.child1;
                if (result.child2.lit1 != null)
                    newChild1.lit1 = result.child2.lit1;
                if (result.child1 != null)
                    newChild1.child2 = result.child1;
                if (result.lit1 != null)
                    newChild1.lit2 = result.lit1;
                BareFormula newChild2 = new BareFormula();
                newChild2.op = "|";
                if (result.child2.child2 != null)
                    newChild2.child1 = result.child2.child2;
                if (result.child2.lit2 != null)
                    newChild2.lit1 = result.child2.lit2;
                if (result.child1 != null)
                    newChild2.child2 = result.child1;
                if (result.lit1 != null)
                    newChild2.lit2 = result.lit1;
                newParent.child1 = newChild1;
                newParent.child2 = newChild2;
                changed = true;
                //System.out.println("INFO in Clausifier.distributeAndOverOrRecurse(): result: " + KIF.format(newParent.toKIFString()));
                return newParent;
            }   
        }
        return result;
    } 

    /** ***************************************************************
     */
    public static BareFormula distributeAndOverOr(BareFormula form) {
        
        BareFormula result = form.deepCopy();
        changed = true;
        while (changed) {
            changed = false;
            result = distributeAndOverOrRecurse(result);
        }
        return result;
    }
 
    /** ***************************************************************
     */
    public static ArrayList<BareFormula> separateConjunctions(BareFormula form) {
        
        ArrayList<BareFormula> result = new ArrayList<BareFormula>();
        if (form.op.equals("&")) {
            if (form.child1 != null)
                result.addAll(separateConjunctions(form.child1));
            if (form.child2 != null)
                result.addAll(separateConjunctions(form.child2));
        }
        else
            result.add(form);
        return result;
    }

    /** ***************************************************************
     * (a | b) | c becomes a | b | c
     */
    public static Clause flatten(BareFormula form) {
    
        Clause result = new Clause();
        if (!Term.emptyString(form.op) && !form.op.equals("|")) {
            System.out.println("Error in Clausifier.flatten(): operator '" + form.op + "' is not a disjunction");
            return result;
        }
        if (form.lit1 != null)
            result.add(form.lit1);
        if (form.lit2 != null)
            result.add(form.lit2);
        if (form.child1 != null)
            result.addAll(flatten(form.child1).literals);
        if (form.child2 != null)
            result.addAll(flatten(form.child2).literals);
        return result;
    }

    /** ***************************************************************
     */
    public static ArrayList<Clause> flattenAll(ArrayList<BareFormula> forms) {
        
        ArrayList<Clause> result = new ArrayList<Clause>();
        for (int i = 0; i < forms.size(); i++) {
            BareFormula form = forms.get(i);
            Clause c = flatten(form);
            c.name = "cnf" + Integer.toString(axiomCounter++);
            c.type = typePrefix;
            result.add(c);
        }
        return result;
    }
    
    /** ***************************************************************
     */
    public static ArrayList<Clause> clausify(BareFormula bf) {

        startTime = System.currentTimeMillis();
        BareFormula result = bf.deepCopy();
        //System.out.println("Clausifier.clausify(): formulaOpSimplify with " + result);
        BareFormula newresult = SmallCNFization.formulaOpSimplify(result);
        if (newresult != null)
        	result = newresult;
        //System.out.println("Clausifier.clausify(): removeImpEq with " + result);
        result = removeImpEq(result);
        //System.out.println("Clausifier.clausify(): moveNegationIn with " + result);
        result = moveNegationIn(result);
        //System.out.println("Clausifier.clausify(): standardizeVariables with " + result);
        result = standardizeVariables(result);
        //System.out.println("Clausifier.clausify(): moveQuantifiersLeft with " + result);
        result = moveQuantifiersLeft(result);
        //System.out.println("Clausifier.clausify(): skolemization with " + result);
        result = skolemization(result);
        //System.out.println("Clausifier.clausify(): removeUQuant with " + result);
        result = removeUQuant(result);
        //System.out.println("Clausifier.clausify(): distributeAndOverOr with " + result);
        result = distributeAndOverOr(result);
        //System.out.println("Clausifier.clausify(): separateConjunctions with " + result);
        ArrayList<BareFormula> forms = separateConjunctions(result);
        //System.out.println("Clausifier.clausify(): flattenAll with " + forms);
        ArrayList<Clause> clauses = flattenAll(forms);
        return clauses;
    }
    
    /** ***************************************************************
     */
    public static ArrayList<Clause> clausify(Formula f) {
    
        typePrefix = f.type;        
        return clausify(f.form);
    }
}
