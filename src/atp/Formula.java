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
public class Formula extends Derivable {

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

    public static int maxFormulaCharLen = 2000;

    public static boolean smallCNF = true;  // use the SmallCNF algorithm or R&N's Clausifier if false

    public static boolean debug = false;

    /** ***************************************************************
     */
    public Formula(BareFormula f, String t) {

        super("",null);
        form = f;
        type = t;
    }

    /** ***************************************************************
     */
    public Formula(BareFormula f, String t, String n) {

        super(n,null);
        form = f;
        type = t;
        name = n;
    }

    /** ***************************************************************
     * Return a string representation of the formula.
     */
    @Override
    public Formula deepCopy() {

        Formula f = new Formula(this.form.deepCopy(),this.type);
        f.name = this.name;
        f.derivation = this.derivation.deepCopy();
        f.refCount = this.refCount;
        return f;
    }

    /** ***************************************************************
     * Return a string representation of the formula.
     */
    public String toString() {

        return "fof(" + name + "," + type + "," + form + "," + strDerivation() + ").";
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

        BareFormula.level = 0; // trap pathological cases of nested formulas with a nesting level counter
        lex.acceptLit("fof");
        lex.acceptTok(Lexer.OpenPar);
        //String name = lex.lookLit();  
        //lex.acceptTok(Lexer.IdentLower);    used to require alphanumeric ID but E generates number ID
        String name = lex.next();   // accept any ID
        //System.out.println("Formula.parse(): parsing id: " + name);
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
        f.setDerivation(new Derivation("input",null,null));
        return f;
    }

    /** ***************************************************************
     *     If wform is a conjecture, return its negation. Otherwise
     *     return it unchanged.
     */
    public static Formula negateConjecture(Formula wform) {

        if (wform.type.equals("conjecture")) {
            BareFormula negf = new BareFormula("~", wform.form);
            Formula negw = new Formula(negf, "negated_conjecture");
            //negw.setDerivation(flatDerivation("assume_negation",[wform],"status(cth)"));
            return negw;
        }
        else
            return wform;
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
                throw new ParseException("# Error in Formula.command2clauses(): expected '(', found " + lex.literal,0);
            lex.next();
            String name = lex.literal;
            if (name.charAt(0) == '\'') {
                String filename = null;
                if (includePath == null)
                    filename = defaultPath + File.separator + name.substring(1,name.length()-1);
                else
                    filename = includePath + File.separator + name.substring(1,name.length()-1);
                File f = new File(filename);
                System.out.println("# INFO in Formula.command2clauses(): include file: " + filename);
                if (!f.exists())
                    throw new IOException("# Error in Formula.command2clauses(): no file " + filename);
                Lexer lex2 = new Lexer(f);
                lex2.filename = filename;
                //System.out.println();
                ClauseSet newcs = lexer2clauses(lex2,timeout);
                //System.out.println("# INFO in Formula.command2clauses(): completed reading file: " + filename);
                lex.next();
                if (lex.type != Lexer.ClosePar)
                    throw new ParseException("# Error in Formula.command2clauses(): expected ')', found " + lex.literal,0);
                lex.next();
                if (lex.type != Lexer.FullStop)
                    throw new ParseException("# Error in Formula.command2clauses(): expected '.', found " + lex.literal,0);
                if (newcs != null)
                    return newcs;
                else
                    return null;
            }
            else 
            	return null;
        }
        else if (id.equals("fof")) {
            Formula f = null;
            cs.isFOF = true;
            try {
                f = Formula.parse(lex);
            }
            catch (Exception e) {
                cs.SZSresult = "Input Error";
                return cs;
            }
            if (f.type.equals("conjecture")) {
                cs.hasConjecture = true;
                f = negateConjecture(f);
            }
            if (f.type.equals("negated_conjecture"))
                cs.hasConjecture = true;
            if (debug) System.out.println("# Formula.command2clauses(): hasConjecture: " + cs.hasConjecture);
            String fstring = f.toString();
            if (fstring.length() > maxFormulaCharLen) {
                cs.SZSresult = "InputError (INE) input error: clause too large: "; // + fstring;
                //System.out.println("# INFO in Formula.command2clauses(): fof: " + cs.SZS);
                return cs;
            }
            //System.out.println("# INFO in Formula.command2clauses(): f: " + f);
            if (f.form != null) {
                if (smallCNF)
                    cs.addAll(SmallCNFization.wFormulaClausify(f));
                else
                    cs.addAll(Clausifier.clausify(f));
                //System.out.println("# INFO in Formula.command2clauses(): result: " + cs);
                return cs;
            }
        }
        else if (id.equals("cnf")) {
            Clause clause = new Clause();
            clause = clause.parse(lex);
            if (clause.type.equals("negated_conjecture"))
                cs.hasConjecture = true;
            //System.out.println("INFO in Formula.command2clauses(): cnf: " + clause);
            cs.addClause(clause);
            return cs; 
        }
        else if (lex.type == Lexer.EOFToken) {
            //System.out.println();
            return cs;
        }
        else
            throw new ParseException("# Error in Formula.command2clauses: bad id: " +
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
        //System.out.println("# INFO in Formula.lexer2clauses(): reading file: " + lex.filename +
        //        " with read timeout: " + timeout);
        //System.out.print("#");
        while (lex.type != Lexer.EOFToken) {
            try {
                //if (lex.input.getLineNumber() % 1000 == 0)
                //    System.out.print(".");
                if (((System.currentTimeMillis() - t1) / 1000.0) > timeout) {
                    cs.SZSresult = "ResourceOut (RSO) reading timeout";
                    //System.out.println("# Error in Formula.lexer2clauses(): timeout");
                    return cs;
                }
                String id = lex.look();
                if (lex.type == Lexer.PerComment || !Term.emptyString(lex.SZS)) {
                    //System.out.println("# Formula.lexer2clauses(): found comment: " + lex.SZS);
                    if (!Term.emptyString(lex.SZS))
                        cs.SZSexpected = lex.SZS;
                }
                ClauseSet csnew = command2clauses(id,lex,timeout);
                if (debug) System.out.println("# Formula.lexer2clauses(): hasConjecture: " + csnew.hasConjecture);
                if (csnew.SZSresult.toLowerCase().contains("error")) {
                    //System.out.println("# Error in Formula.lexer2clauses(): " + cs.SZSresult);
                    cs.SZSresult = csnew.SZSresult;
                    return cs;
                }
                cs.hasConjecture = cs.hasConjecture || csnew.hasConjecture;
                cs.isFOF = cs.isFOF || csnew.isFOF;
                cs.addAll(csnew);
            }
            catch (Exception p) {
                if (p != null && p.getMessage() != null && p.getMessage().contains("bad id")) {
                    cs.SZSresult = "InputError (INE): bad id";
                    return cs;
                }
                System.out.println();
                System.out.println("# Error in Formula.lexer2clauses()");
                System.out.println(p.getMessage());
                p.printStackTrace();
                return cs;
            }
        }
        //System.out.println();
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
        //System.out.println();
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
                ClauseSet cs = lexer2clauses(lex);
                if (debug) System.out.println("# Formula.file2clauses(): hasConjecture: " + cs.hasConjecture);
                //System.out.println("# Formula.file2clauses(): SZSresult " + cs.SZSresult);
                //System.out.println("# Formula.file2clauses(): SZSexpected " + cs.SZSexpected);
                return cs;
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
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            Formula.file2clauses(args[0]);
        }
        else
            System.out.println("no file specified");
    }
}
