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

import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;

public class LexerTest {

    /** ***************************************************************
     ** ***************************************************************
     */
    private static String example1 = "f(X,g(a,b))";
    private static String example2 = "# Comment\nf(X,g(a,b))";
    private static String example3 = "cnf(test,axiom,p(a)|p(f(X))).";
    private static String example4 = "^";
    private static String example5 = "fof(test,axiom,![X,Y]:?[Z]:~p(X,Y,Z)).";

    /** ***************************************************************
     * Test that comments and whitespace are normally ignored.
     */
    @Test
    private void testLex() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testLex()");
        Lexer lex1 = new Lexer(example1);
        Lexer lex2 = new Lexer(example2);
        try {
            ArrayList<String> res1 = lex1.lex();
            System.out.println("INFO in Lexer.testLex(): completed parsing example 1: " + example1);
            ArrayList<String> res2 = lex2.lex();
            System.out.println("INFO in Lexer.testLex(): should be true: " + res1.equals(res2));
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
    private void testTerm() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testTerm()");
        Lexer lex1 = new Lexer(example1);
        try {
            lex1.acceptTok(Lexer.IdentLower); // f
            lex1.acceptTok(Lexer.OpenPar);    // (
            lex1.acceptTok(Lexer.IdentUpper); // X
            lex1.acceptTok(Lexer.Comma);      // ,
            lex1.acceptTok(Lexer.IdentLower); // g
            lex1.acceptTok(Lexer.OpenPar);    // (
            lex1.acceptTok(Lexer.IdentLower); // a
            lex1.acceptTok(Lexer.Comma);      // ,
            lex1.acceptTok(Lexer.IdentLower); // b
            lex1.acceptTok(Lexer.ClosePar);   // )
            lex1.acceptTok(Lexer.ClosePar);   // )
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
    private void testClause() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testClause()");
        Lexer lex = new Lexer(example3);
        try {
            ArrayList<String> toks = lex.lex();
            System.out.println(toks);
            System.out.println("Should be true: (tokens == 20): " + (toks.size() == 20) + " actual: " + toks);
            StringBuffer rebuild = new StringBuffer();
            for (int i = 0; i < toks.size(); i++)
                rebuild.append(toks.get(i));
            System.out.println(rebuild.toString() + " " + example3);
            System.out.println("Should be true: " + rebuild.toString().equals(example3));
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
    private void testFormula() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testFormula()");
        Lexer lex = new Lexer(example5);
        try {
            ArrayList<String> toks = lex.lex();
            System.out.println(toks);
            System.out.println("Should be true: (tokens == 29): " + (toks.size() == 29));
            StringBuffer rebuild = new StringBuffer();
            for (int i = 0; i < toks.size(); i++)
                rebuild.append(toks.get(i));
            System.out.println(rebuild.toString());
            System.out.println("Should be true: " + rebuild.toString().equals(example5));
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
    private void testAcceptLit() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testAcceptLit()");
        Lexer lex = new Lexer(example3);
        try {
            lex.acceptLit("cnf");
            lex.acceptLit("(");
            lex.acceptLit("test");
            lex.acceptLit(",");
            lex.acceptLit("axiom");
            lex.acceptLit(",");
            lex.acceptLit("p");
            lex.acceptLit("(");
            lex.acceptLit("a");
            lex.acceptLit(")");
            lex.acceptLit("|");
            lex.acceptLit("p");
            lex.acceptLit("(");
            lex.acceptLit("f");
            lex.acceptLit("(");
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
    private void testErrors() {

        System.out.println("-------------------------------------------------");
        System.out.println("INFO in Lexer.testErrors(): Should throw three errors");
        Lexer lex = null;
        try {
            lex = new Lexer(example4);
            lex.look();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            lex = new Lexer(example1);
            lex.checkTok(Lexer.EqualSign);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            lex = new Lexer(example1);
            lex.checkLit("abc");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}