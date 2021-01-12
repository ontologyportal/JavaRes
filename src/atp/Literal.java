/*
A simple implementation of first-order terms. 
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

/** ***************************************************************
 * atom - predicate symbol with term arguments
 * literal - atom or negated atom
  */
public class Literal {

    public Term atom = null;
    public boolean negated = false;
    public boolean inferenceLit = true;

    /**************************************************************
     */
    public Literal() {

    }

    /**************************************************************
     */
    public Literal(Term t) {

        atom = new Term(t);
    }

    /**************************************************************
     */
    public Literal(Literal l) {

        this.atom = new Term(l.atom);
        this.negated = l.negated;
    }

    /***************************************************************
     */
    public Literal(Term atom, boolean negative) {

        if (atom.getFunc().equals("!=")) {
            this.negated = !negative;
            this.atom = new Term();
            this.atom.t = "=";
            if (atom.subterms != null && atom.subterms.size() > 0)
                this.atom.subterms.addAll(atom.getArgs());
        }
        else {
            this.negated = negative;
            this.atom = new Term();
            this.atom.t = atom.t;
            if (atom.subterms != null && atom.subterms.size() > 0)
                this.atom.subterms.addAll(atom.getArgs());
        }
        this.setInferenceLit(true);
    }

    /**************************************************************
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        if (negated)
            result.append("~");
        if (atom != null && !Term.emptyString(atom.getFunc())) {
            if (atom.getFunc().equals("=") || atom.getFunc().equals("!="))
                result.append(atom.getArgs().get(0) + atom.getFunc() + atom.getArgs().get(1));
            else
                result.append(atom);
        } else
            result.append(atom);
        return result.toString();
    }

    /***************************************************************
     */
    public String toKIFString() {

        //System.out.println("Literal.toKIFString(): " + this);
        //System.out.println("Literal.toKIFString(): atom: " + atom);
        //System.out.println("Literal.toKIFString(): func: " + atom.getFunc());
        //System.out.println("Literal.toKIFString(): args: " + atom.getArgs());
        StringBuffer result = new StringBuffer();
        if (negated)
            result.append("(not ");
        result.append(atom.toKIFString());
        if (negated)
            result.append(")");
        //System.out.println("Literal.toKIFString(): result: " + result);
        return result.toString();
    }

    /***************************************************************
     */
    public boolean equals(Object l_obj) {

        if (!l_obj.getClass().getName().equals("atp.Literal")) {
            System.out.println("# Error: Literal.equals() passed object not of type Literal:" + l_obj.getClass());
            Exception e = new Exception("DEBUG");
            e.printStackTrace();
        }
        Literal l = (Literal) l_obj;
        if (negated != l.negated)
            return false;
        if (!atom.equals(l.atom))
            return false;
        return true;
    }

    /***************************************************************
     * should never be called so throw an error.
     */
    public int hashCode() {

        assert false : "Literal.hashCode not designed";
        return 0;
    }

    /***************************************************************
     */
    public Literal deepCopy() {

        Literal result = new Literal();
        result.atom = atom.deepCopy();
        result.negated = negated;
        return result;
    }

    /***************************************************************
     * Return true if the atoms of self and other are structurally
     * identical to each other, but the sign is the opposite.
     */
    public boolean isOpposite(Literal other) {

        return this.isNegative() != other.isNegative() &&
                atom.equals(other.atom);
    }

    /**************************************************************
     */
    public Literal negate() {

        if (isPropTrue())
            return new Literal(new Term("$false", null, null));
        else if (isPropFalse())
            return new Literal(new Term("$true", null, null));
        negated = !negated;
        return this.deepCopy();
    }

    /***************************************************************
     */
    public boolean isNegative() {

        return !isPositive();
    }

    /**************************************************************
     */
    public boolean isPositive() {

        return !negated;
    }

    /**************************************************************
     */
    public boolean isEquational() {

        return atom.getFunc().equals("=");
    }

    /**************************************************************
     * Return True iff the literal is of the form X=Y
     */
    public boolean isPureVarLit() {

        if (isEquational())
            return (atom.getArgs().get(0).isVar()) &&
                    (atom.getArgs().get(1).isVar());
        return false;
    }

    /***************************************************************
     */
    public boolean isInferenceLit() {

        return inferenceLit;
    }

    /**************************************************************
     */
    public void setInferenceLit(boolean litP) {

        inferenceLit = litP;
    }

    /***************************************************************
     * Return True if the atom is $true.
     */
    public boolean atomIsConstTrue() {

        return atom.getFunc().equals("$true");
    }

    /***************************************************************
     * Return True if the atom is $false.
     */
    public boolean atomIsConstFalse() {

        return atom.getFunc().equals("$false");
    }

    /***************************************************************
     * Return True if the literal is of the form $true or ~$false.
     */
    public boolean isPropTrue() {

        return ((isNegative() && atomIsConstFalse())
                ||
                (isPositive() && atomIsConstTrue()));
    }

    /***************************************************************
     * Return True if the literal is of the form $false or ~$true.
     */
    public boolean isPropFalse() {

        return ((isNegative() && atomIsConstTrue())
                ||
                (isPositive() && atomIsConstFalse()));
    }

    /***************************************************************
     */
    public ArrayList<String> getConstantStrings() {

        return atom.getConstantStrings();
    }

    /****************************************************************
     */
    public ArrayList<Term> collectVars() {

        return atom.collectVars();
    }

    /**************************************************************
     * Get all functions
     */
    public ArrayList<String> collectFuns() {

        return atom.collectFuns();
    }

    /****************************************************************
     * Collect function- and predicate symbols into the signature.
     * Return the signature
     */
    public Signature collectSig(Signature sig) {

        sig.addPred(atom.getFunc(), atom.subterms.size());
        for (int i = 0; i < atom.subterms.size(); i++)
            atom.subterms.get(i).collectSig(sig);
        return sig;
    }

    /****************************************************************
     * Return a copy of self, instantiated with the given substitution.
     */
    public Literal instantiate(Substitutions subst) {

        //System.out.println("INFO in Literal.instantiate(): "  + this + " " + subst);
        Literal newLit = deepCopy();
        newLit.atom = subst.apply(atom);
        return newLit;
    }

    /****************************************************************
     * Return a copy of self, instantiated with the given substitution.

    public Literal substitute(Substitutions subst) {

        //System.out.println("INFO in Literal.substitute(): "  + this + " " + subst);
        Literal newLit = deepCopy();
        newLit.atom = subst.apply(atom);
        return newLit;
    }
*/
    /**************************************************************
     * Return the weight of the term,  counting fweight for each function symbol
     * occurrence, vweight for each variable occurrence. Examples:
     * termWeight(f(a,b), 1, 1) = 3
     * termWeight(f(a,b), 2, 1) = 6
     * termWeight(f(X,Y), 2, 1) = 4
     * termWeight(X, 2, 1)      = 1
     * termWeight(g(a), 3, 1)   = 6
     */
    public int weight(int fweight, int vweight) {

        return atom.weight(fweight, vweight);
    }

    /**************************************************************
     * An atom is either a conventional atom, in which case it's
     * syntactically identical to a term, or it is an equational literal,
     * of the form 't1=t2' or 't1!=t2', where t1 and t2 are terms.
     * In either case, we represent the atom as a first-order
     * term. Equational literals are represented at terms with faux
     * function symbols "=" and "!=".
     * The parser must be pointing to the token before the atom.
     */
    private static Term parseAtom(Lexer lex) {

        //System.out.println("INFO in Literal.parseAtom(): " + lex.literal);
        try {
            Term atom = new Term();
            atom.parse(lex);
            ArrayList<String> tokens = new ArrayList<String>();
            tokens.add(Lexer.EqualSign);
            tokens.add(Lexer.NotEqualSign);
            if (lex.testTok(tokens)) {
                // The literal is equational. We get the actual operator, '=' or '!=', followed by the
                // other side of the (in)equation
                String op = lex.next();
                Term lhs = atom;
                Term rhs = new Term();
                rhs = rhs.parse(lex);
                atom = new Term(op, lhs, rhs);
            }
            return new Term(atom);
        }
        catch (Exception ex) {
            System.out.println("Error in Literal.parseAtom(): " + ex.getMessage());
            System.out.println("Error in Literal.parseAtom(): token:" + lex.type + " " + lex.literal);
            ex.printStackTrace();
        }
        return null;
    }

    /***************************************************************
     * Parse a literal. A literal is an optional negation sign '~',
     * followed by an atom.
     *
     * @return the Literal.  Note that there is a side effect on this Literal.
     */
    public static Literal parseLiteral(Lexer lex) {

        //System.out.println("INFO in Literal.parseLiteral(): " + lex.literal);
        try {
            String s = lex.look();
            if (s == Lexer.Or) {
                lex.next();
                lex.next();
            }
            boolean negative = false;
            if (lex.type == Lexer.Negation) {
                negative = true;
                lex.next();   // pointer will be left on the negation
            }
            Term atom = parseAtom(lex);
            //System.out.println("INFO in Literal.parseLiteral(): exiting with pointer at: " + lex.literal);
            return new Literal(atom, negative);
        }
        catch (Exception ex) {
            System.out.println("Error in Literal.parseLiteral(): " + ex.getMessage());
            System.out.println("Error in Literal.parseLiteral(): token:" + lex.type + " " + lex.literal);
            ex.printStackTrace();
        }
        return null;
    }

    /***************************************************************
     * Parse a list of literals separated by "|" (logical or). As per
     * TPTP 3 syntax, the single word "$false" is interpreted as the
     * false literal, and ignored.
     */
    public static ArrayList<Literal> parseLiteralList(Lexer lex) {

        //System.out.println("INFO in Literal.parseLiteralList(): " + lex.literal);
        ArrayList<Literal> res = new ArrayList<Literal>();
        try {
            Literal l = new Literal();
            if (lex.look().equals("$false"))
                lex.next();
            else {
                Literal newl = l.parseLiteral(lex);
                res.add(newl);
            }
            //if (!l.toString().equals("$false"))
            //    res.add(l);
            while (lex.look().equals(Lexer.Or)) {
                lex.next();
                l = new Literal();
                if (lex.look().equals("$false"))
                    lex.next();
                else {
                    Literal newl = l.parseLiteral(lex);
                    res.add(newl);
                }
            }
            return res;
        }
        catch (Exception ex) {
            System.out.println("Error in parseLiteralList(): " + ex.getMessage());
            System.out.println("Error in parseLiteralList(): token:" + lex.type + " " + lex.literal);
            ex.printStackTrace();
        }
        return null;
    }

    /***************************************************************
     * Convert a literal list to a textual representation that can be
     * parsed back.
     */
    public static String literalList2String(ArrayList<Literal> l) {

        StringBuffer result = new StringBuffer();
        if (l == null || l.size() < 1)
            return "$false";
        result.append(l.get(0).toString());
        for (int i = 1; i < l.size(); i++)
            result.append("|" + l.get(i).toString());
        return result.toString();
    }

    /****************************************************************
     * Return true if (a literal equal to) lit is in litlist, false
     * otherwise.
     */
    public static boolean litInLitList(Literal lit, ArrayList<Literal> litList) {

        for (int i = 0; i < litList.size(); i++)
            if (lit.equals(litList.get(i)))
                return true;
        return false;
    }

    /***************************************************************
     * Return true if (a literal equal to) lit is in litlist, false
     * otherwise.
     */
    public static boolean oppositeInLitList(Literal lit, ArrayList<Literal> litList) {

        for (int i = 0; i < litList.size(); i++)
            if (lit.isOpposite(litList.get(i)))
                return true;
        return false;
    }

    /***************************************************************
     * Try to extend subst a match from self to other. Return True on
     * success, False otherwise. In the False case, subst is unchanged.
     */
    public boolean match(Literal other, BacktrackSubstitution subst) {

        //System.out.println("Literal.match(): this: " + this + " other: " + other + " op: " + op);
        if (this.isNegative() != other.isNegative())
            return false;
        else
            return subst.match(atom, other.atom);
    }

    /***************************************************************
     * The predicate abstraction of a literal is a pair (pol, pred),
     * where pol is an encoding of the polarity (abritrarily True for
     * positive, False for negative), and pred is the predicate
     * symbol of the atom of the literal. Predicate abstractions can
     * be used to quickly reject the possibility that two literals
     * can be unified with or matched to each other.
     */
    public PredAbstractionPair predicateAbstraction() {

        return(new PredAbstractionPair(isPositive(),atom.getFunc()));
    }

     /** ***************************************************************
      */
     public static Literal string2lit(String s) {
         
    	 //System.out.println("Literal.string2lit(): s: " + s);
         Lexer lex = new Lexer(s);
         Literal l = new Literal();
         return l.parseLiteral(lex);
     }
}
