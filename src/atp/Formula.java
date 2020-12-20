package atp;

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

import java.io.*;
import java.text.*;
import java.util.*;

/** ***************************************************************
 * Datatype for the complete first-order formula, including 
 * meta-information like type and name.
 */
public class Formula {

    // Counter for generating new clause names.
    public static int formulaIdCounter = 0;
    public BareFormula form = null;
    public String type = "plain";
    public String name = "";
    
    // TPTP file include paths
    public static String includePath = null;  
    // public static String defaultPath = "/home/apease/Programs/TPTP-v5.3.0";
    public static String defaultPath = "";
    
    public ArrayList<String> support = new ArrayList<String>();  // Clauses or Formulas from which this clause is derived.
    public String rationale = "input";                           // If not input, reason for derivation.
    public String status = "";

    /** ***************************************************************
     */
    public Formula(BareFormula f, String t) {
        
        form = f;
        type = t;
    }
    
    /** ***************************************************************
     * Return a string representation of the formula.
     */
    public String toString() {

        return "fof(" + name + "," + type + "," + form + ").";
    }
    
    /** ***************************************************************
     * Set the name. If no name is given, generate a default name.
     */
    public void setName(String n) {

        if (!Term.emptyString(n))
            name = n;
        else {
            name = "f" + Integer.toString(formulaIdCounter);
            formulaIdCounter++;        
        }
    }

    /** ***************************************************************
     * Parse a formula in (slightly simplified) TPTP-3 syntax. It is
     *  written 
     *      fof(<name>, <type>, <lformula>).
     *  where <name> is a lower-case ident, type is a lower-case ident
     *  from a specific list, and <lformula> is a Formula.
     *  
     *  For us, all clause types are essentially the same, so we only
     *  distinguish "axiom", "conjecture", and "negated_conjecture", and
     *  map everything else to "plain".
     */
    public static Formula parse(Lexer lex) throws IOException, ParseException {

        lex.acceptLit("fof");
        lex.acceptTok(Lexer.OpenPar);
        //String name = lex.lookLit();  
        //lex.acceptTok(Lexer.IdentLower);    used to require alphanumeric ID but E generates number ID
        String name = lex.next();   // accept any ID
        lex.acceptTok(Lexer.Comma);
        String type = lex.lookLit();
        if (!type.equals("axiom") && !type.equals("conjecture") && !type.equals("negated_conjecture"))
            type = "plain";
        lex.acceptTok(Lexer.IdentLower);
        lex.acceptTok(Lexer.Comma);

        BareFormula bform = BareFormula.parse(lex);
        
        lex.next();
        if (!lex.type.equals(Lexer.ClosePar)) {
            //System.out.println("Warning in Formula.parse(): Clause close paren expected. Instead found '" + lex.literal + "' with clause so far " + bform);
            //System.out.println("Discarding remainder of line.");
            while (lex.type != Lexer.FullStop && lex.type != Lexer.EOFToken)
            	lex.next();
            Formula f = new Formula(bform,type);
            f.name = name;
            return f;
        }
        lex.acceptTok(Lexer.FullStop);

        Formula f = new Formula(bform,type);
        f.name = name;
        return f;
    }

    /** ***************************************************************
     * timeout if the total time to process exceeds a certain
     * amount.  Typically, this is called with a timeout equal to the timeout
     * for finding a refutation, so it should be more than adequate barring
     * an unusual situation.
     */
    public static ClauseSet command2clauses(String id, Lexer lex, int timeout) throws ParseException, IOException {

        //System.out.println("INFO in Formula.command2clauses(): id: " + id);
        ClauseSet cs = new ClauseSet();
        if (id.equals("include")) {
            lex.next();
            lex.next();
            if (lex.type != Lexer.OpenPar)
                throw new ParseException("#Error in Formula.command2clauses(): expected '(', found " + lex.literal,0);
            lex.next();
            String name = lex.literal;
            if (name.charAt(0) == '\'') {
                String filename = null;
                if (includePath == null)
                    filename = defaultPath + File.separator + name.substring(1,name.length()-1);
                else
                    filename = includePath + File.separator + name.substring(1,name.length()-1);
                File f = new File(filename);
                System.out.println("#INFO in Formula.command2clauses(): start reading file: " + filename);
                Lexer lex2 = new Lexer(f);
                lex2.filename = filename;
                System.out.println();
                ClauseSet newcs = lexer2clauses(lex2,timeout);
                System.out.println("#INFO in Formula.command2clauses(): completed reading file: " + filename);
                lex.next();
                if (lex.type != Lexer.ClosePar)
                    throw new ParseException("#Error in Formula.command2clauses(): expected ')', found " + lex.literal,0);
                lex.next();
                if (lex.type != Lexer.FullStop)
                    throw new ParseException("#Error in Formula.command2clauses(): expected '.', found " + lex.literal,0);
                if (newcs != null)
                    return newcs;
                else
                    return null;
            }
            else 
            	return null;
        }
        else if (id.equals("fof")) {
            Formula f = Formula.parse(lex);
            //System.out.println("# INFO in Formula.command2clauses(): fof: " + f);
            if (f.form != null) {
                cs.addAll(Clausifier.clausify(f));
                return cs;
            }
        }
        else if (id.equals("cnf")) {
            Clause clause = new Clause();
            clause = clause.parse(lex);
            //System.out.println("INFO in Formula.command2clauses(): cnf: " + clause);
            cs.add(clause);
            return cs; 
        }
        else if (lex.type == Lexer.EOFToken) {
            System.out.println();
            return cs;
        }
        else
            throw new ParseException("#Error in Formula.command2clauses: bad id: " + 
                    id + " at line " + lex.input.getLineNumber() + " in file " + lex.filename,0);
        return cs;
    }
    
    /** ***************************************************************
     * timeout if the total time to process the file exceeds a certain
     * amount.  Typically, this is called with a timeout equal to the timeout
     * for finding a refutation, so it should be more than adequate barring
     * an unusual situation.
     */
    public static ClauseSet lexer2clauses(Lexer lex, int timeout) {
        
        long t1 = System.currentTimeMillis();
        ClauseSet cs = new ClauseSet();
        System.out.println("# INFO in Formula.lexer2clauses(): reading file: " + lex.filename +
                " with read timeout: " + timeout); 
        System.out.print("#");  
        while (lex.type != Lexer.EOFToken) {
            try {
                if (lex.input.getLineNumber() % 1000 == 0)
                    System.out.print(".");
                if (((System.currentTimeMillis() - t1) / 1000.0) > timeout) {
                    System.out.println("# Error in Formula.lexer2clauses(): timeout");
                    return null;
                }
                String id = lex.look();
                cs.addAll(command2clauses(id,lex,timeout));
            }
            catch (Exception p) {
                System.out.println();
                System.out.println("# Error in Formula.lexer2clauses()");
                System.out.println(p.getMessage());
                p.printStackTrace();
                return cs;
            }
        }
        System.out.println();
        cs.SZS = lex.SZS;
        return cs;
    }

    /** ***************************************************************
     * timeout if the total time to process the file exceeds a certain
     * amount.  Typically, this is called with a timeout equal to the timeout
     * for finding a refutation, so it should be more than adequate barring
     * an unusual situation.
     */
    public static ArrayList<Formula> lexer2formulas(Lexer lex, int timeout) {
        
    	ArrayList<Formula> result = new ArrayList<Formula>();
        long t1 = System.currentTimeMillis();
        System.out.println("# INFO in Formula.lexer2formulas(): reading file: " + lex.filename +
                " with read timeout: " + timeout); 
        System.out.print("#");  
        while (lex.type != Lexer.EOFToken) {
            try {
                if (lex.input.getLineNumber() % 1000 == 0)
                    System.out.print(".");
                if (((System.currentTimeMillis() - t1) / 1000.0) > timeout) {
                    System.out.println("# Error in Formula.lexer2formulas(): timeout");
                    return null;
                }
                String id = lex.look();
                result.add(parse(lex));
            }
            catch (Exception p) {
                System.out.println();
                System.out.println("# Error in Formula.lexer2formulas()");
                System.out.println(p.getMessage());
                p.printStackTrace();
                return result;
            }
        }
        System.out.println();
        return result;
    }
    
    /** ***************************************************************
     */
    public static ClauseSet lexer2clauses(Lexer lex) {
        return lexer2clauses(lex,10000);
    }
          
    /** ***************************************************************
     */
    public static ClauseSet file2clauses(String filename, int timeout) {
        
        FileReader fr = null;
        try {
            File fin = new File(filename);
            fr = new FileReader(fin);
            if (fr != null && fin.length() > 0) {
                Lexer lex = new Lexer(fin);
                lex.filename = filename;
                return lexer2clauses(lex);
            }
        }
        catch (IOException e) {
            System.out.println("#Error in Formula.file2clauses(): File error reading " + filename + ": " + e.getMessage());
            return null;
        }
        finally {
            try {
                if (fr != null) fr.close();
            }
            catch (Exception e) {
                System.out.println("#Exception in Formula.file2clauses()" + e.getMessage());
            }
        }  
        return null;
    }
    
    /** ***************************************************************
     */
    public static ArrayList<Formula> file2formulas(String filename, int timeout) {
        
    	ArrayList<Formula> result = new ArrayList<Formula>();
        FileReader fr = null;
        try {
            File fin = new File(filename);
            fr = new FileReader(fin);
            if (fr != null && fin.length() > 0) {
                Lexer lex = new Lexer(fin);
                lex.filename = filename;
                return lexer2formulas(lex,timeout);
            }
        }
        catch (IOException e) {
            System.out.println("#Error in Formula.file2formulas(): File error reading " + filename + ": " + e.getMessage());
            return null;
        }
        finally {
            try {
                if (fr != null) fr.close();
            }
            catch (Exception e) {
                System.out.println("#Exception in Formula.file2formulas()" + e.getMessage());
            }
        }  
        return null;
    }
    
    /** ***************************************************************
     */
    public static ClauseSet file2clauses(String filename) {
        return file2clauses(filename,10000);
    }
    
    /** ***************************************************************
     */
    public static ClauseSet string2clauses(String formula, int timeout) {
        
        Lexer lex = new Lexer(formula);
        return lexer2clauses(lex,timeout);
    }
    
    /** ***************************************************************
     */
    public static ClauseSet string2clauses(String formula) {
        return string2clauses(formula,10000);
    }

    /** ***************************************************************
     */
    public static Formula string2form(String formula) {
    	
        Lexer lex = new Lexer(formula);
        try {
        	return parse(lex);
        }
        catch (Exception e) {
            System.out.println("Error in Formula.string2form()");
            System.out.println(e.getMessage());
            e.printStackTrace();  
            return null;
        }
    }
    
    /** ***************************************************************
     */
    public static String removeQuotes(String s) {
        
        int start = 0;
        int end = s.length();
        if (s.charAt(0) == '"' || s.charAt(0) == '\'')
            start = 1;
        if (s.charAt(end-1) == '"' || s.charAt(end-1) == '\'')
            end = end - 1;
        return s.substring(start,end);
    }
    
    /** ***************************************************************
     * Setup function for clause/literal unit tests. Initialize
     * variables needed throughout the tests.
     */
    public static String wformulas = "fof(small, axiom, ![X]:(a(x) | ~a=b))." + 
        "fof(complex, conjecture, (![X]:a(X)|b(X)|?[X,Y]:(p(X,f(Y))))<=>q(g(a),X))." + 
        "fof(clean, conjecture, ((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))<=>q(g(a),X)))." + 
        "fof(queens_p,axiom,(queens_p => ![I,J]:((le(s(n0),I)& le(I,n) & le(s(I),J) & le(J,n) )=> ( p(I) != p(J) & plus(p(I),I) != plus(p(J),J) & minus(p(I),I) != minus(p(J),J) ) ) )).";
            
    /** ***************************************************************
     */
    public static void testWrappedFormula() {
        
        System.out.println("---------------------");
        System.out.println("INFO in testWrappedFormula()");
        try {
            Lexer lex = new Lexer(wformulas); 
            Formula f1 = Formula.parse(lex);
            System.out.println("Result 1: " + f1);
            System.out.println();
            Formula f2 = Formula.parse(lex);
            System.out.println("Result 2: " + f2);
            System.out.println();
            Formula f3 = Formula.parse(lex);
            System.out.println("Result 3: " + f3);
            System.out.println();
            Formula f4 = Formula.parse(lex);
            System.out.println("Result 4: " + f4);
            System.out.println();
        }
        catch (Exception e) {
            System.out.println("Error in Formula.testWrappedFormula()");
            System.out.println(e.getMessage());
            e.printStackTrace();            
        }
    }
    
    /** ***************************************************************
     */
    public static void testEqAxioms() {
        
        System.out.println("---------------------");
        System.out.println("INFO in testEqAxioms()");
        try {
        	String testeq = "fof(eqab, axiom, a=b)." + 
        	        "fof(pa, axiom, p(a))." + 
        	        "fof(fb, axiom, ![X]:f(X)=b)." +
        	        "fof(pa, conjecture, ?[X]:p(f(X))).";
            Lexer lex = new Lexer(testeq); 
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            cs = cs.addEqAxioms();
            System.out.println(cs);
        }
        catch (Exception e) {
            System.out.println("Error in Formula.testEqAxioms()");
            System.out.println(e.getMessage());
            e.printStackTrace();            
        }
    }
    
    /** ***************************************************************
     */
    public static void testEqAxiomProving() {
    	
        System.out.println("---------------------");
        System.out.println("INFO in testEqAxiomProving()");
        try {
        	String testeq = "fof(eqab, axiom, a=b)." + 
        	        "fof(pa, axiom, p(a))." + 
        	        "fof(fb, axiom, ![X]:f(X)=b)." +
        	        "fof(pa, conjecture, ?[X]:p(f(X))).";
            Lexer lex = new Lexer(testeq); 
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            cs = cs.addEqAxioms();
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
    	    ProofState state = new ProofState(cs,ClauseEvaluationFunction.PickGiven2); 
    	    state.evalFunctionName = ClauseEvaluationFunction.PickGiven2.name;  
            state.delete_tautologies = true;
            state.forward_subsumption = true;
            state.backward_subsumption = true;
            state.verbose = true;
    	    state.res = state.saturate(100);
    	    if (state.res != null)
    	        System.out.println(state);
    	    else
    	        System.out.println("# SZS GaveUp");
        }
        catch (Exception e) {
            System.out.println("Error in testEqAxiomProving()");
            System.out.println(e.getMessage());
            e.printStackTrace();            
        }
    }
    
    /** ***************************************************************
     */
    public static void testEqAxiomProving2() {
    	
        System.out.println("---------------------");
        System.out.println("INFO in testEqAxiomProving2()");
        try {
        	String testeq = "cnf(00004,plain,~X79=X77|~X77=X80|X79=X80). " +
        		"cnf(00005,input,apply(apply(apply(n1, X4), X5), X6)=apply(apply(apply(X4, X5), X5), X6))."; 
        		// "cnf(00076,plain,~X159=apply(apply(apply(n1, X160), X161), X162)|X159=apply(apply(apply(X160, X161), X161), X162)).";
            Lexer lex = new Lexer(testeq); 
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            System.out.println(ResControl.computeAllResolvents(cs.get(0), cs));
        }
        catch (Exception e) {
            System.out.println("Error in testEqAxiomProving2()");
            System.out.println(e.getMessage());
            e.printStackTrace();            
        }
    }
    
    /** ***************************************************************
     */
    public static void testEqAxiomProving3() {
    	
        System.out.println("---------------------");
        System.out.println("INFO in testEqAxiomProving3()");
        try {
        	String testeq = "cnf(00004,plain,X=X). " +
        		"cnf(00005,input,Y!=m | Y=n)."; 
        		// "cnf(00076,plain,~X159=apply(apply(apply(n1, X160), X161), X162)|X159=apply(apply(apply(X160, X161), X161), X162)).";
            Lexer lex = new Lexer(testeq); 
            ClauseSet cs = Formula.lexer2clauses(lex);
            System.out.println(cs);
            ClauseEvaluationFunction.setupEvaluationFunctions();
            System.out.println(ResControl.computeAllResolvents(cs.get(0), cs));
        }
        catch (Exception e) {
            System.out.println("Error in testEqAxiomProving3()");
            System.out.println(e.getMessage());
            e.printStackTrace();            
        }
    }

    /** ***************************************************************
     */
    public static void main(String[] args) {
                
        if (args.length < 1) {
        	//testWrappedFormula();
        	//testEqAxioms();
        	testEqAxiomProving2();
        }
        else
            System.out.println(file2clauses(args[0]));
    }
}
