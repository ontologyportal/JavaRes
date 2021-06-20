package atp;
import java.io.*;
import java.util.*;
import java.text.*;

public class EqAxioms {
	/*
	Equality is a central relation in automated deduction. The simplest
	way of handling equality is to add axioms describing its
	properties. In this way, any calculus that is complete for first-order
	logic can be applied to proof problems with equality. 

	Equality is a congruence relation, i.e. it is an equivalence relation
	that is compatible with the term structure. As an equivalence
	relation, it has to conform the the axioms of reflexivity, symmetry,
	and transitivity. These can be written as follows:

	Reflexivity:  ![X]:X=X
	Symmetry:     ![X,Y]:(X=Y -> Y=X)
	Transitivity: ![X,Y,Z]:((X=Y & Y=Z) -> X=Z)

	The compatibility property requires that we can replace "equals with
	equals". The need to be stated for each function symbol and each
	predicate symbols in the problem:

	Assume f|n in F, i.e. f is s function symbol of arity n. Then
	![X1,...,Xn,Y1,...,Yn]:((X1=Y1 & ... & Xn=Yn)->f(X1,...,Xn)=f(Y1,...,Yn))
	describes the compatibility of the equality relation (=) with f.

	Assume p|n in P. Then
	![X1,...,Xn,Y1,...,Yn]:((X1=Y1 & ... & Xn=Yn)->(p(X1,...Xn)->p(Y1,...Yn)))
	describes the compatibility of the equality relation with p. Note that
	we do not need to specify the symmetric case p(X1,...Xn)<-p(Y1,...Yn)
	because it follows from the contrapositive (~p(Y1,...Yn)->~p(X1,...Xn)
	and the symmetry of equality.
	[* Make easier *]

	The axioms can be directly converted into clausal logic, yielding:

	X=X
	X!=Y | Y=X
	X!=Y | Y!=Z | X=Z

	X1!=Y1|...|Xn!=Yn|f(X1,...,Xn)=f(Y1,...Yn) for all f|n in F.
	X1!=Y1|...|Xn!=Yn|~p(X1,...Xn)|p(Y1,...,Yn) for all p|n in P.

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

	public static int axCount = 0;

	/** ***************************************************************
	 */
	public static void resetCounter() {
		axCount = 0;
	}

    /** ***************************************************************
     * Return a list with the three axioms describing an equivalence
     * relation. We are lazy here...
     */
	public static ArrayList<Clause> generateEquivAxioms() {

	    Lexer lex = new Lexer("cnf(reflexivity, axiom, X=X)." +
	    		"cnf(symmetry, axiom, X!=Y|Y=X)." +
	    		"cnf(transitivity, axiom, X!=Y|Y!=Z|X=Z).");
	    ArrayList<Clause> res = new ArrayList<Clause>(); 
	    try {
		    while (!lex.testTok(Lexer.EOFToken)) {
		        Clause c = new Clause();
		        c = c.parse(lex);
		        c.rationale = "eq_axiom";
		        c.setDerivation(new Derivation("eq_axioms",null,""));
		        res.add(c);
		    }
	    }
	    catch (ParseException pe) {
	    	System.out.println("Error in EqAxioms.generateEquivAxioms(): parse error");
	    	System.out.println(pe.getMessage());
	    	pe.printStackTrace();
	    }
	    return res;
	}

    /** ***************************************************************
     * Generate a list of variables of the form x1,...,xn, where x is any
     * string, and n is >= 0.
     */
	public static String generateVarList(String x, int n) {
	    
		StringBuffer res = new StringBuffer();
		for (int i = 1; i <= n; i++) {
			if (i != 1)
				res.append(",");
			res.append(x + Integer.toString(i));
		}
	    return res.toString();
	}

    /** ***************************************************************
     * Generate a list of literals of the form X1!=Y1|...|Xn!=Yn.
     */
	public static ArrayList<Literal> generateEqPremise(int arity) {

		ArrayList<Literal> res = new ArrayList<Literal>();
		for (int i = 1; i <= arity; i++) 
			res.add(Literal.string2lit("X" + Integer.toString(i) + "!=" + "Y" + Integer.toString(i)));		
	    return res;
	}

    /** ***************************************************************
     * Generate axioms for the form X1!=Y1|...|Xn!=Yn|f(X1,...,Xn)=f(Y1,...Yn)
     * for f with the given arity.
     */
	public static Clause generateFunCompatAx(String f, int arity) {

		ArrayList<Literal> res = generateEqPremise(arity);
	    String lterm = f + "(" + generateVarList("X",arity) + ")";
	    String rterm = f + "(" + generateVarList("Y",arity) + ")";
	    Literal concl = Literal.string2lit(lterm + "=" + rterm);
	    Clause c = new Clause();
	    c.name = "funcompat" + Integer.toString(axCount++);
	    c.literals.addAll(res);
	    c.literals.add(concl);

		c.setDerivation(new Derivation("eq_axiom",null,""));
	    return c;
	}
	    
    /** ***************************************************************
     * Generate axioms for the form X1!=Y1|...|Xn!=Yn|~p(X1,...,Xn)|p(Y1,...Yn)    
     * for f with the given arity.
     */
	public static Clause generatePredCompatAx(String p, int arity) {

		//System.out.println("# INFO in EqAxioms.generatePredCompatAx(): pred: " + p);
		ArrayList<Literal> res = generateEqPremise(arity);
	    String lterm = "~" + p + "(" + generateVarList("X",arity) + ")";
	    String rterm = p + "(" + generateVarList("Y",arity) + ")";
	    Literal neg = Literal.string2lit(lterm);
	    Literal pos = Literal.string2lit(rterm);
	    Clause c = new Clause();
	    c.name = "predcompat" + Integer.toString(axCount++);
	    c.literals.addAll(res);
	    c.literals.add(neg);
	    c.literals.add(pos);
		c.setDerivation(new Derivation("eq_axiom",null,""));
	    return c;
	}

    /** ***************************************************************
     * Given a signature, generate and return all the compatibility axioms.
     */
	public static ArrayList<Clause> generateCompatAxioms(Signature sig) {

	    //System.out.println("# INFO in EqAxioms.generateCompatAxioms(): signature: " + sig);
		ArrayList<Clause> res = new ArrayList<Clause>();
	    for (String f:sig.funs) {
	        int arity = sig.getArity(f);
	        if (arity>0) {
	            Clause c = generateFunCompatAx(f, arity);
	            res.add(c);
	        }
	    }
	    for (String p:sig.preds) {
	        int arity = sig.getArity(p);
	        if (arity > 0 && !p.equals("=") && !p.equals("!=")) {
	            Clause c = generatePredCompatAx(p, arity);
	            res.add(c);
	        }
	    }
	    //System.out.println("# INFO in EqAxioms.generateCompatAxioms(): result: " + res);
	    return res;
	}

}
