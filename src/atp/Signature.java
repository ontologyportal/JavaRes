package atp;
import java.io.*;
import java.util.*;

public class Signature {
	/*
	First-order signatures describe which names (for functions, including
	constants, and predicates) are available in a given first-order
	language. Very often, signatures are given implicitly. In other words,
	the symbols used in terms and formulas implictly make up the
	formula. For implementations of standard untyped predicate logic, we
	can always extract the necessary information directly from the
	formulae. 

	However, for certain operations it is much easier to have an explicit
	data object providing signature information. 

	A signature is a triple (F,P,ar), with the following properties:

	- F is a finite set of function symbols (including constants).
	- P is a finite set of predicate symbols.
	- F and P are disjunct, i.e. they don't share any symbols.
	- ar:F \cup P ->N_0 is the  arity function that associates a natural
	  number (the "arity") with each function symbol and predicate
	  symbols.
*/
	public ArrayList<String> funs  = new ArrayList<String>();
	public ArrayList<String> preds  = new ArrayList<String>();
	public HashMap<String,Integer> arity  = new HashMap<String,Integer>();

    /** ***************************************************************
     * Return a printable representation of the signature.
     */
	public String toString() {

		StringBuffer res = new StringBuffer();
	    res.append("Predicates:\n");
	    res.append(preds.toString() + "\n");
	    res.append("Functions:\n");
	    res.append(funs.toString() + "\n");
	    return res.toString();
	}

    /** ***************************************************************
     * Add a new function
     */
	public void addFun(String f, int a) {
		
		if (!funs.contains(f)) {
			funs.add(f);
			arity.put(f,new Integer(a));
		}
	}
    /** ***************************************************************
     * Add a new predicate
     */
	public void addPred(String p, int a) {
		
		if (!preds.contains(p)) {
			preds.add(p);
			arity.put(p,new Integer(a));
		}
	}

    /** ***************************************************************
     * Return True if p is a known predicate symbol.
     */
	public boolean isPred(String p) {
	    return preds.contains(p);
	}

    /** ***************************************************************
     * Return True if f is a known function symbol.
     */
	public boolean isFun(String f) {
	    return funs.contains(f);
	}
	
    /** ***************************************************************
     * Return True if f is a constant function symbol.
     */
	public boolean isConstant(String f) {

	    return isFun(f) && arity.get(f).intValue() == 0;
	}

    /** ***************************************************************
     */
	public int getArity(String f) {

		if (arity.containsKey(f))
			return arity.get(f).intValue();
		else
			return 0;
	}
	
    /** ***************************************************************
     * Test signature object.
     */	    
	public static void test() {

		String s = "cnf(qg1_2,negated_conjecture,~product(X1, Y1, Z1)|~product(X2, Y2, Z1)|~product(Z2, Y1, X1)|~product(Z2, Y2, X2)|equalish(Y1, Y2)).";
		Clause c = Clause.string2Clause(s);
		Signature sig = new Signature();
		c.collectSig(sig); 
		System.out.println("result: " + sig);
		sig = new Signature();

	    sig.addFun("mult",2);
	    sig.addFun("a", 0);
	    sig.addPred("weird", 4);
	        
	    System.out.println(sig + " should be preds[wierd] and funs[mult,a]");
	    System.out.println("all should be true:");
	    System.out.println(sig.isPred("weird"));
	    System.out.println(!sig.isPred("unknown"));
	    System.out.println(!sig.isPred("a"));
	    System.out.println(sig.isFun("a"));
	    System.out.println(sig.isConstant("a"));
	    System.out.println(!sig.isFun("unknown"));
	    System.out.println(!sig.isFun("weird"));

	    System.out.println(sig.getArity("a")==0);
	    System.out.println(sig.getArity("weird")==4);
	    
	    
	}
	
    /** ***************************************************************
     */	    
	public static void main(String[] args) {
		test();
	}

}
