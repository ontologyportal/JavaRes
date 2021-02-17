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

This is a Java rewrite of PyRes - https://github.com/eprover/PyRes
*/
package atp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KIFLexerTest {

    /** ***************************************************************
     ** ***************************************************************
     */
    private static String example1 = "(part ?X (MerologicalSumFn A B))";
    private static String example2 = "; Comment\n(part ?X (MerologicalSumFn A B))";
    private static String example3 = "(=> (likes ?A ?B) (acquiantance ?A ?B))";
    private static String example4 = "(subclass Object Entity)";
    private static String example5 = "(exists (?A ?B) (or (instance ?A ?B) (subclass ?A ?B)))";

    /** ***************************************************************
     * Test that comments and whitespace are normally ignored.
     */
    @Test
    public void testLex() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testLex()");
        KIFLexer lex1 = new KIFLexer(example1);
        KIFLexer lex2 = new KIFLexer(example2);
        try {
            ArrayList<String> res1 = lex1.lex();
            System.out.println("INFO in Lexer.testLex(): completed parsing example 1: " + example1);
            ArrayList<String> res2 = lex2.lex();
            System.out.println("INFO in Lexer.testLex(): should be true: " + res1.equals(res2));
            assertTrue(res1.equals(res2));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Test that self.example 1 is split into the expected tokens.
     */
    @Test
    public void testTerm() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testTerm()");
        KIFLexer lex1 = new KIFLexer(example4);
        try {
            lex1.acceptTok(KIFLexer.OpenPar);    // (
            lex1.acceptTok(KIFLexer.IdentUpper); // subclass
            lex1.acceptTok(KIFLexer.WhiteSpace); //
            lex1.acceptTok(KIFLexer.IdentUpper); // Object
            lex1.acceptTok(KIFLexer.WhiteSpace); //
            lex1.acceptTok(KIFLexer.IdentLower); // Entity
            lex1.acceptTok(KIFLexer.ClosePar);   // )
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     */
    private static boolean compareArrays(ArrayList<String> s1, ArrayList<String> s2) {

        if (s1.size() != s2.size())
            return false;
        for (int i = 0; i < s1.size(); i++)
            if (!s1.get(i).equals(s2.get(i)))
                return false;
        return true;
    }

    /** ***************************************************************
     * Perform lexical analysis of a clause, then rebuild it and
     * compare that the strings are the same.
     */
    @Test
    public void testClause() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testClause()");
        KIFLexer lex = new KIFLexer(example3);
        try {
            ArrayList<String> toks = lex.lex();
            System.out.println(toks);
            System.out.println("Should be true: (tokens == 13): " + (toks.size() == 13) + " actual: " + toks);
            assertEquals(13,toks.size());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Perform lexical analysis of a formula, then rebuild it and
     * compare that the strings are the same.
     */
    @Test
    public void testFormula() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in KIFLexer.testFormula()");
        KIFLexer lex = new KIFLexer(example5);
        try {
            ArrayList<String> toks = lex.lex();
            System.out.println(toks);
            System.out.println("Should be true: (tokens == 20): " + (toks.size() == 20));
            assertEquals(20,toks.size());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Check the positive case of AcceptLit().
     */
    @Test
    public void testAcceptLit() {

        // "(=> (likes ?A ?B) (acquiantance ?A ?B))";
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testAcceptLit()");
        KIFLexer lex = new KIFLexer(example3);
        try {
            lex.acceptLit("(");
            lex.acceptLit("=>");
            lex.acceptLit("(");
            lex.acceptLit("likes");
            lex.acceptLit("?A");
            lex.acceptLit("?B");
            lex.acceptLit(")");
            lex.acceptLit("(");
            lex.acceptLit("acquiantance");
            lex.acceptLit("?A");
            lex.acceptLit("?B");
            lex.acceptLit(")");
            lex.acceptLit(")");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Provoke different errors.
     */
    @Test
    public void testErrors() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testErrors(): Should throw three errors");
        KIFLexer lex = null;
        try {
            lex = new KIFLexer(example4);
            lex.look();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            lex = new KIFLexer(example1);
            lex.checkTok(KIFLexer.IdentUpper);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            lex = new KIFLexer(example1);
            lex.checkLit("abc");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}