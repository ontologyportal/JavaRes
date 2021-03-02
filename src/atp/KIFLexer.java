/*
A simple lexical analyser that converts a string into a sequence of
tokens.  Java's StreamTokenizer can't be used since it only can
"push back" one token.
     
This will convert a string into a sequence of
tokens that can be inspected and processed in-order. It is a bit
of an overkill for the simple application, but makes actual
parsing later much easier and more robust than a quicker hack.
        
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

import java.io.*;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KIFLexer {

    public int ttype = 0;
    public String sval = "";
    public static boolean inBlockComment = false;

    public static final String NoToken        = "No Token";
    public static final String WhiteSpace     = "White Space";
    public static final String Newline        = "Newline";
    public static final String Comment        = "Comment";
    public static final String IdentUpper     = "Identifier starting with capital letter";
    public static final String IdentLower     = "Identifier starting with lower case letter";
    public static final String RegularVar     = "regular variable";
    public static final String RowVar         = "row variable";
    public static final String Number         = "Positive or negative Integer or real";
    public static final String OpenPar        = "(";
    public static final String ClosePar       = ")";
    public static final String Implies        = "=>";
    public static final String Equiv          = "<=>";
    public static final String Negation       = "not";
    public static final String Exists         = "exists";
    public static final String Forall         = "forall";
    public static final String And            = "and";
    public static final String Or             = "or";
    public static final String DQString       = "String in \"double quotes\"";
    public static final String EOFToken       = "*EOF*";

    public String filename = "";
    public String type = "";
    public String literal = "";
    public String line = null;
    public String SZS = "";
    public int pos = 0;  // character position on the current line
    public LineNumberReader input = null;
    public ArrayDeque<String> tokenStack = new ArrayDeque<String>();

    /** This array contains all of the compiled Pattern objects that
     * will be used by methods in this file. */
    public static LinkedHashMap<String,Pattern> tokenDefs = new LinkedHashMap<String,Pattern>();

    public static ArrayList<String> andOr = new ArrayList<String>();
    public static ArrayList<String> binaryRel = new ArrayList<String>();
    public static ArrayList<String> quant = new ArrayList<String>();

    /** ***************************************************************
     */
    public KIFLexer() {
        init();
    }

    /** ***************************************************************
     */
    public KIFLexer(String s) {

        init();
        //source = s;
        input = new LineNumberReader(new StringReader(s));
    }

    /** ***************************************************************
     */
    public KIFLexer(File f) {
        
        init();
        //source = file2string(f);
        try {
            input = new LineNumberReader(new FileReader(f));
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Error in Lexer(): File not found: " + f);
            System.out.println(fnf.getMessage());
            fnf.printStackTrace();
        }
    }
    
    /** ***************************************************************
     */
    public String file2string(File f) {

        String result = null;
        DataInputStream in = null;

        try {
            byte[] buffer = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            in.readFully(buffer);
            result = new String(buffer);
        } 
        catch (IOException e) {
            throw new RuntimeException("IO problem in fileToString", e);
        } 
        finally {
            try {
                in.close();
            } 
            catch (IOException e) { /* ignore it */
            }
        }
        return result;
    }
    
    /** ***************************************************************
     * Return the line number of the token by counting all the
     * newlines in the position up to the current token.
     */
    private int linepos() {

        return input.getLineNumber();
        //return source.substring(0,pos).split(" ").length + 1;
    }        

    /** ***************************************************************
     */
    private static void init() {

        tokenDefs.put(OpenPar,     Pattern.compile("\\("));                   
        tokenDefs.put(ClosePar,    Pattern.compile("\\)"));
        tokenDefs.put(Implies,     Pattern.compile("=>"));              
        tokenDefs.put(Equiv,       Pattern.compile("<=>"));
        tokenDefs.put(Negation,    Pattern.compile("not"));
        tokenDefs.put(And,         Pattern.compile("and"));
        tokenDefs.put(Or,          Pattern.compile("or"));
        tokenDefs.put(Exists,      Pattern.compile("Exists"));
        tokenDefs.put(Forall,      Pattern.compile("Forall"));
        tokenDefs.put(Newline,     Pattern.compile("\\n"));
        tokenDefs.put(WhiteSpace,  Pattern.compile("\\s+"));
        tokenDefs.put(IdentLower,  Pattern.compile("[a-z][_a-z0-9_A-Z]*"));
        tokenDefs.put(IdentUpper,  Pattern.compile("[_A-Z][_a-z0-9_A-Z]*"));
        tokenDefs.put(RegularVar,  Pattern.compile("\\?[_A-Za-z][_a-z0-9_A-Z]*"));
        tokenDefs.put(RowVar,      Pattern.compile("@[_A-Za-z][_a-z0-9_A-Z]*"));
        tokenDefs.put(Number,      Pattern.compile("-?[0-9]?[0-9\\.]+E?-?[0-9]*"));
        tokenDefs.put(Comment, Pattern.compile(";[^\\n]*"));
        tokenDefs.put(DQString,Pattern.compile("\"(\\\\\"|[^\"])*\""));

        binaryRel.add(Equiv); 
        binaryRel.add(Implies); 

        inBlockComment = false;
    }

    /** ***************************************************************
     */
    public static boolean isKIFLogOp(String s) {
        if (s.equals(And) ||s.equals(Or) || s.equals(Negation) || s.equals(Implies) ||
                s.equals(Equiv) || s.equals(Exists) || s.equals(Forall))
            return true;
        else
            return false;
    }

    /** ***************************************************************
     * Return the next token without consuming it.
     */
    public String look() throws ParseException {

        String res = next();
        //System.out.println("INFO in Lexer.look(): " + res);
        tokenStack.push(res);
        return res;
    }

    /** ***************************************************************
     * Return the literal value of the next token, i.e. the string
     * generating the token.
     */
    public String lookLit() throws ParseException {

        look();
        return literal;
    }
            
    /** ***************************************************************
     * Take a list of expected token types. Return True if the
     * next token is expected, False otherwise.
     */
    public boolean testTok(ArrayList<String> tokens) throws ParseException {

        look();
        for (int i = 0; i < tokens.size(); i++) {
            if (type.equals(tokens.get(i))) {
                //System.out.println("INFO in Lexer.testTok(): found token");
                return true;
            }
        }
        //System.out.println("INFO in Lexer.testTok(): didn't find tokens with type: " + type + " for list " + tokens);
        return false;
    }

    /** ***************************************************************
     * Convenience method
     */
    public boolean testTok(String tok) throws ParseException {

        ArrayList<String> tokens = new ArrayList<String>();
        tokens.add(tok);
        return testTok(tokens);
    }

    /** ***************************************************************
     * Take a list of expected token types. If the next token is
     * not among the expected ones, exit with an error. Otherwise do
     * nothing. 
     */
    public void checkTok(String tok) throws ParseException {

        ArrayList<String> tokens = new ArrayList<String>();
        tokens.add(tok);
        checkTok(tokens);
    }

    /** ***************************************************************
     * Take a list of expected token types. If the next token is
     * not among the expected ones, exit with an error. Otherwise do
     * nothing. 
     */
    public void checkTok(ArrayList<String> tokens) throws ParseException {

        look();
        for (int i = 0; i < tokens.size(); i++) {
            if (type.equals(tokens.get(i)))
                return;
        }
        throw new ParseException("Error in Lexer.checkTok(): Unexpected token '" + type + "'",linepos());
    }

    /** ***************************************************************
     */
    public String acceptTok(String token) throws ParseException {

        ArrayList<String> tokens = new ArrayList<String>();
        tokens.add(token);
        checkTok(tokens);
        return next();
    }

    /** ***************************************************************
     * Take a list of expected token types. If the next token is
     * among the expected ones, consume and return it. Otherwise, exit 
     * with an error. 
     */
    public String acceptTok(ArrayList<String> tokens) throws ParseException {

        checkTok(tokens);
        return next();
    }

    /** ***************************************************************
     */
    public boolean testLit(String litval) throws ParseException {

        ArrayList<String> litvals = new ArrayList<String>();
        litvals.add(litval);
        return testLit(litvals);
    }
    
    /** ***************************************************************
     * Take a list of expected literal strings. Return True if the
     * next token's string value is among them, False otherwise. 
     */
    public boolean testLit(ArrayList<String> litvals) throws ParseException {

        lookLit();
        for (int i = 0; i < litvals.size(); i++) {
            if (literal.equals(litvals.get(i)))
                return true;
        }
        return false;
    }
    
    /** ***************************************************************
     */
    public void checkLit(String litval) throws ParseException {

        ArrayList<String> litvals = new ArrayList<String>();
        litvals.add(litval);
        checkLit(litvals);
    }

    /** ***************************************************************
     * Take a list of expected literal strings. If the next token's
     * literal is not among the expected ones, exit with an
     * error. Otherwise do nothing. 
     */
    private void checkLit(ArrayList<String> litvals) throws ParseException {

        if (!testLit(litvals)) {
            look();
            throw new ParseException("Error in Lexer.checkLit(): " + literal + " not in " + litvals, linepos());
        }
    }

    /** ***************************************************************
     * Take a list of expected literal strings. If the next token's
     * literal is among the expected ones, consume and return the
     * literal. Otherwise, exit with an error. 
     */
    public String acceptLit(ArrayList<String> litvals) throws ParseException {

        checkLit(litvals);
        return next();
    }
    
    /** ***************************************************************
     * Take a list of expected literal strings. If the next token's
     * literal is among the expected ones, consume and return the
     * literal. Otherwise, exit with an error. 
     */
    public String acceptLit(String litval) throws ParseException {

        ArrayList<String> litvals = new ArrayList<String>();
        litvals.add(litval);
        checkLit(litvals);
        return next();
    }

    /** ***************************************************************
     */
    public void processComment(String line) {

        //System.out.println("INFO in processComment(): " + line);
        Pattern value = Pattern.compile("\\%\\sStatus[\\s:]+([^\\n]*)");
        //Pattern value = Pattern.compile("\\%\\sStatus");
        Matcher m = value.matcher(line);
        //System.out.println("INFO in processComment(): comment: " + line);
        if (m.lookingAt()) {
            //System.out.println("INFO in processComment(): found match: " + m.group(1));
        	if (m.group(1).indexOf("Unsatisfiable") > -1 || m.group().indexOf("Theorem") > -1) 
        		SZS = m.group(1);
        	if (m.group(1).indexOf("Satisfiable") > -1 || m.group().indexOf("CounterSatisfiable") > -1) 
        		SZS = m.group(1);        
        	//System.out.println("# processComment() problem SZS status: " + SZS);
        }
    }
    
    /** ***************************************************************
     * Return next semantically relevant token. 
     */
    public String next() throws ParseException {

        String res = nextUnfiltered();
        while ((type.equals(WhiteSpace) || type.equals(Comment)) && !res.equals(EOFToken)) {
        	//System.out.println(type + ":" + line);
        	if (type.equals(Comment))
        		processComment(line);
            res = nextUnfiltered();
        }
        //System.out.println("INFO in next(): returning token: " + res);
        return res;
    }
    
    /** ***************************************************************
     * Return next token, including tokens ignored by most languages. 
     */
    private String nextUnfiltered() throws ParseException {

        if (tokenStack.size() > 0)
            return tokenStack.pop();
        else {
            if (line == null || line.length() <= pos) {
                try {
                    do {
                        line = input.readLine();
                        if (inBlockComment) {
                            do {
                                line = input.readLine();
                            } while (line != null && line.indexOf("*/") == -1);
                            if (line != null && line.indexOf("*/") != -1) {
                                if (line.length() == 2)
                                    line = "";
                                else
                                    line = line.substring(line.indexOf("*/") + 2);
                            }
                            line = line.trim();
                            inBlockComment = false;
                        }
                        else {
                            if (line != null && line.indexOf("/*") != -1) {
                                inBlockComment = true;
                                //System.out.println("INFO in Lexer.nextUnfiltered(): in block comment: " + line);
                                line = line.substring(0,line.indexOf("/*"));
                                line = line.trim();
                            }
                        }
                    } while (line != null && line.length() == 0);
                    //System.out.println("INFO in Lexer.nextUnfiltered(): " + line);
                    pos = 0;
                }
                catch (IOException ioe) {
                    System.out.println("Error in Lexer.nextUnfiltered()");
                    System.out.println(ioe.getMessage());
                    ioe.printStackTrace();
                    return EOFToken;
                }
                if (line == null) {
                    //System.out.println("INFO in Lexer.nextUnfiltered(): returning eof");
                    type = EOFToken;
                    return EOFToken;
                }
            }
            for (String key : tokenDefs.keySet()) {  // Go through all the token definitions and process the first one that matches
                Pattern value = tokenDefs.get(key);
                Matcher m = value.matcher(line.substring(pos));
                //System.out.println("INFO in Lexer.nextUnfiltered(): checking: " + key + " against: " + line.substring(pos));
                if (m.lookingAt()) {
                    //System.out.println("INFO in Lexer.nextUnfiltered(): got token against source: " + line.substring(pos));
                    literal = line.substring(pos + m.start(),pos + m.end());
                    pos = pos + m.end();
                    type = key;
                    //System.out.println("INFO in Lexer.nextUnfiltered(): got token: " + literal + " type: " + type +
                    //        " at pos: " + pos + " with regex: " + value);
                    return m.group();
                }
            }
            if (pos + 4 > line.length())
                if (pos - 4 < 0)
                    throw new ParseException("Error in Lexer.nextUnfiltered(): no matches in token list for " + 
                            line.substring(0,line.length()) + "... at line " + input.getLineNumber() + " full line " + line,pos);
                else
                    throw new ParseException("Error in Lexer.nextUnfiltered(): no matches in token list for " +
                            line.substring(0,line.length()) + "... at line " + input.getLineNumber() + " full line " + line,pos);
            else
                throw new ParseException("Error in Lexer.nextUnfiltered(): no matches in token list for " +
                        line.substring(0,line.length()) + "... at line " + input.getLineNumber() + " full line " + line,pos);
        }
    }

    /** ***************************************************************
     * Return a list of all tokens in the source. 
     */
    public ArrayList<String> lex() throws ParseException {

        ArrayList<String> res = new ArrayList<String>();
        while (!testTok(EOFToken)) {
            String tok = next();
            //System.out.println("INFO in Lexer.lex(): " + tok);
            res.add(tok);
        }
        return res;
    }
}
