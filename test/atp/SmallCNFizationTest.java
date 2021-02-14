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

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import static org.junit.Assert.*;

public class SmallCNFizationTest {

    /** ***************************************************************
     * ************ UNIT TESTS *****************
     */
    private static BareFormula f1 = null;
    private static BareFormula f2 = null;
    private static BareFormula f3 = null;
    private static BareFormula f4 = null;
    private static BareFormula f5 = null;
    private static String testFormulas = null;
    private static String covformulas = null;
    private static ArrayList<String> simple_ops = new ArrayList<String>();
    private static ArrayList<String> nnf_ops = new ArrayList<String>();

    /** ***************************************************************
     * Setup function for clause/literal unit tests. Initialize
     * variables needed throughout the tests.
     */
    @BeforeClass
    public static void setup() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.setup()");
        String formulas = "![X]:(a(X) ~| ~a=b)\n" +
                "![X]:(a(X)|b(X)|?[X,Y]:(p(X,f(Y))<~>q(g(a),X)))\n" +
                "![X]:(a(X) <= ~a=b)\n" +
                "((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))~&q(g(a),X))\n" +
                "![X]:(a(X)|$true)";
        Lexer lex = new Lexer(formulas);
        try {
            BareFormula.level = 0;
            f1 = BareFormula.parse(lex);
            System.out.println(f1.toStructuredString());
            BareFormula.level = 0;
            f2 = BareFormula.parse(lex);
            System.out.println(f2);
            BareFormula.level = 0;
            f3 = BareFormula.parse(lex);
            System.out.println(f3);
            BareFormula.level = 0;
            f4 = BareFormula.parse(lex);
            System.out.println(f4);
            BareFormula.level = 0;
            f5 = BareFormula.parse(lex);
            System.out.println(f5);
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.setup()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        String[] ops = {"", "!", "?", "~", "&","|", "=>", "<=>"};
        simple_ops = new ArrayList(Arrays.asList(ops));
        String[] ops2 = {"", "!", "?", "&","|"};
        nnf_ops = new ArrayList(Arrays.asList(ops2));

        covformulas ="(a|$true)\n" +
                "($true|a)\n" +
                "(a|$false)\n" +
                "($false|a)\n" +
                "(a|a)\n" +
                "(a&$true)\n" +
                "($true&a)\n" +
                "(a&$false)\n" +
                "($false&a)\n" +
                "(a&a)\n" +
                "(a=>$true)\n" +
                "($true=>a)\n" +
                "(a=>$false)\n" +
                "($false=>a)\n" +
                "(a=>a)\n" +
                "(a<=>$true)\n" +
                "($true<=>a)\n" +
                "(a<=>$false)\n" +
                "($false<=>a)\n" +
                "(a<=>a)\n" +
                "![X]:(a<=>a)\n" +
                "?[X]:(a<=>a)\n" +
                "a<=>b";

        testFormulas = "fof(t12_autgroup,conjecture,(\n" +
                "! [A] :\n" +
                    "( ( ~ v3_struct_0(A)\n" +
                    "& v1_group_1(A)\n" +
                    "& v3_group_1(A)\n" +
                    "& v4_group_1(A)\n" +
                    "& l1_group_1(A) )\n" +
                "=> r1_tarski(k4_autgroup(A),k1_fraenkel(u1_struct_0(A),u1_struct_0(A))) ) )).\n" +
                "\n" +
                "fof(abstractness_v1_group_1,axiom,(\n" +
                    "! [A] :\n" +
                        "( l1_group_1(A)\n" +
                        "=> ( v1_group_1(A)\n" +
                            "=> A = g1_group_1(u1_struct_0(A),u1_group_1(A)) ) ) )).\n" +
                "\n" +
                "fof(antisymmetry_r2_hidden,axiom,(\n" +
                    "! [A,B] :\n" +
                        "( r2_hidden(A,B)\n" +
                        "=> ~ r2_hidden(B,A) ) )).\n" +
                "fof(cc1_fraenkel,axiom,(\n" +
                    "! [A] :\n" +
                        "( v1_fraenkel(A)\n" +
                        "=> ! [B] :\n" +
                            "( m1_subset_1(B,A)\n" +
                            "=> ( v1_relat_1(B)\n" +
                                "& v1_funct_1(B) ) ) ) )).\n" +
                "\n" +
                "fof(cc1_funct_1,axiom,(\n" +
                    "! [A] :\n" +
                        "( v1_xboole_0(A)\n" +
                        "=> v1_funct_1(A) ) )).\n" +
                "\n" +
                "fof(cc1_funct_2,axiom,(\n" +
                    "! [A,B,C] :\n" +
                        "( m1_relset_1(C,A,B)\n" +
                        "=> ( ( v1_funct_1(C)\n" +
                            "& v1_partfun1(C,A,B) )\n" +
                            "=> ( v1_funct_1(C)\n" +
                                "& v1_funct_2(C,A,B) ) ) ) )).\n" +
                "fof(testscosko, axiom, (![X]:?[Y]:((p(X)&q(X))|q(X,Y))|a)).";
    }

    /** ***************************************************************
     * Test that operator simplification works.
     */
    @Test
    public void testOpSimplification() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testOpSimplification()");
        System.out.println("Simplifying: " + f1);
        BareFormula newform = SmallCNFization.formulaOpSimplify(f1.deepCopy());
        System.out.println("Simplified form: " + newform);
        assertTrue(newform != null);
        //assert(newform != null);
        ArrayList<String> ops = newform.collectOps();
        System.out.println("collect ops " + ops);
        ops.removeAll(simple_ops);
        System.out.println("remove simple ops");
        if (ops.size() != 0)
            System.out.println("error: remaining operator " + ops);
        else
            System.out.println("Success: ops.size()=0 " + ops);
        assertEquals(0,ops.size());

        System.out.println();
        System.out.println("Simplifying: " + f2);
        BareFormula f = SmallCNFization.formulaOpSimplify(f2);
        System.out.println("Simplified form: " + f);
        assertTrue(f != null);
        if (f == null)
            f = f2;
        ops = f.collectOps();
        System.out.println("collect ops " + ops);
        ops.removeAll(simple_ops);
        System.out.println("remove simple ops");
        if (ops.size() != 0)
            System.out.println("error: remaining operator " + ops);
        else
            System.out.println("Success: ops.size()=0 " + ops);
        assertEquals(0,ops.size());

        System.out.println();
        System.out.println("Simplifying: " + f3);
        f = SmallCNFization.formulaOpSimplify(f3);
        System.out.println("Simplified form: " + f);
        assertTrue(f != null);
        if (f == null)
            f = f3;
        ops = f.collectOps();
        ops.removeAll(simple_ops);
        if (ops.size() != 0)
            System.out.println("error: remaining operator " + ops);
        else
            System.out.println("Success: ops.size()=0 " + ops);
        assertEquals(0,ops.size());

        System.out.println();
        System.out.println("Simplifying: " + f4);
        f = SmallCNFization.formulaOpSimplify(f4);
        System.out.println("Simplified form: " + f);
        assertTrue(f != null);
        if (f == null)
            f = f4;
        ops = f.collectOps();
        ops.removeAll(simple_ops);
        if (ops.size() != 0)
            System.out.println("error: remaining operator " + ops);
        else
            System.out.println("Success: ops.size()=0 " + ops);
        assertEquals(0,ops.size());

        System.out.println();
        System.out.println("Simplifying: " + f5); //                 "![X]:(a(X)|$true)";
        f = SmallCNFization.formulaOpSimplify(f5);
        System.out.println("Simplified form: should be null: " + f);
        assertTrue(f == null); // form should be unchanged since there are no operators other than simple_ops
        if (f == null)
            f = f5;
        ops = f.collectOps();
        ops.removeAll(simple_ops);
        if (ops.size() != 0)
            System.out.println("error: remaining operator " + ops);
        else
            System.out.println("Success: ops.size()=0 " + ops);
        assertEquals(0,ops.size());
    }

    /** ***************************************************************
     * A simplified formula has no $true/$false, or it is a literal
     * (in which case it's either true or false).
     */
    public static void checkSimplificationResult(BareFormula f) {

        System.out.println("INFO in SmallCNFization.checkSimplificationResult(): f: " + f.toStructuredString());
        ArrayList<String> funs = f.collectFuns();
        System.out.println("function symbols: " + funs);
        System.out.println("must be either just $true, just $false or a list of terms that doesn't contain $true or $false");
        if (f.isPropConst(true) || f.is1PropConst(false))
            System.out.println("Success - is $true or $false");
        else {
            if (!funs.contains("$true") && !funs.contains("$false"))
                System.out.println("success - doesn't contain a truth constant");
            else
                System.out.println("fail - has a truth constant and other (function) constants");
            assertTrue(!funs.contains("$true") && !funs.contains("$false"));
        }
    }

    /** ***************************************************************
     * Test that simplification works.
     */
    @Test
    public void testSimplification() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testSimplification()");
        System.out.println("Simplifying: " + f1);
        BareFormula f = SmallCNFization.formulaOpSimplify(f1);
        if (f == null) // null if unchanged
            f = f1;
        BareFormula tf = SmallCNFization.formulaSimplify(f);
        if (tf == null)  // null if unchanged
            tf = f;
        checkSimplificationResult(tf);

        System.out.println();
        System.out.println("Simplifying: " + f2);
        f = SmallCNFization.formulaOpSimplify(f2);
        if (f == null)
            f = f2;
        f = SmallCNFization.formulaSimplify(f);
        if (f == null)
            f = f2;
        checkSimplificationResult(f);

        System.out.println();
        System.out.println("Simplifying: " + f3);
        f = SmallCNFization.formulaOpSimplify(f3);
        if (f == null)
            f = f3;
        f = SmallCNFization.formulaSimplify(f);
        if (f == null)
            f = f3;
        checkSimplificationResult(f);

        System.out.println();
        System.out.println("Simplifying: " + f4);
        f = SmallCNFization.formulaOpSimplify(f4);
        if (f == null)
            f = f4;
        f = SmallCNFization.formulaSimplify(f);
        if (f == null)
            f = f4;
        checkSimplificationResult(f);

        System.out.println();
        System.out.println("Simplifying: " + f5);
        f = SmallCNFization.formulaOpSimplify(f5);
        if (f == null)
            f = f5;
        f = SmallCNFization.formulaSimplify(f);
        if (f == null)
            f = f5;
        checkSimplificationResult(f);

        System.out.println();
        System.out.println("INFO in SmallCNFizationTest.testSimplification(): check formulas");
        try {
            Lexer lex = new Lexer(covformulas);
            while (!lex.testTok(Lexer.EOFToken)) {
                BareFormula.level = 0;  // reset the counter that traps too much nesting
                f = BareFormula.parse(lex);
                System.out.println();
                System.out.println("***** Simplifying: " + f);
                BareFormula newf = SmallCNFization.formulaOpSimplify(f);
                System.out.println("opSimplify result " + newf);
                if (newf == null) // formulaOpSimplify returns null if no change
                    newf = f;
                f = SmallCNFization.formulaSimplify(newf);
                System.out.println("simplify result " + f);
                if (f == null) // formulaSimplify returns null if no change
                    f = newf;
                checkSimplificationResult(f);
                System.out.println();
            }
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.testSimplification()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * A simplified formula is either $true/$false, or it only
     * contains &, |, !, ? as operators (~ is shifted into the literals).
     */
    public static void checkNNFResult(BareFormula f) {

        System.out.println("INFO in SmallCNFizationTest.checkNNFResult()");
        System.out.println("NNF:" + f.toStructuredString());
        if (f == null)
            System.out.println("fail: null input: ");
        assertTrue(f != null);
        System.out.println("ops:" + f.collectOps());

        if (f.isPropConst(true) || f.isPropConst(false)) {
            System.out.println("success: formula is just $true or $false");
        }
        else {
            ArrayList<String> ops = f.collectOps();
            ops.removeAll(nnf_ops);
            if (ops.size() > 0)
                System.out.println("fail: operators other than &, |, !, ?: " + ops);
            else
                System.out.println("Success: only allowed op" + nnf_ops);
            assertTrue(ops.size() == 0);
        }
    }

    /** ***************************************************************
     * Test NNF transformation
     */
    @Test
    public void testNNF() {

        BareFormula.level = 0;
        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testNNF()");
        System.out.println("NNF conversion: " + f1.toStructuredString());
        BareFormula f = null;
        f = SmallCNFization.formulaOpSimplify(f1);
        if (f == null)
            f = f1;
        BareFormula newf = null;
        newf = SmallCNFization.formulaSimplify(f);
        if (newf == null)
            newf = f;
        System.out.println("start NNF conversion with: " + newf);
        f = SmallCNFization.formulaNNF(newf, true);
        checkNNFResult(f);

        System.out.println();
        System.out.println("***** NNF conversion: " + f2);
        f = SmallCNFization.formulaOpSimplify(f2);
        if (f == null)
            f = f2;
        newf = SmallCNFization.formulaSimplify(f);
        if (newf == null)
            newf = f;
        f = SmallCNFization.formulaNNF(newf, true);
        checkNNFResult(f);

        System.out.println();
        System.out.println("***** NNF conversion: " + f3);
        f = SmallCNFization.formulaOpSimplify(f3);
        if (f == null)
            f = f3;
        newf = SmallCNFization.formulaSimplify(f);
        if (newf == null)
            newf = f;
        f = SmallCNFization.formulaNNF(newf, true);
        checkNNFResult(f);

        System.out.println();
        System.out.println("***** NNF conversion: " + f4); // ((((![X]:a(X))|b(X))|(?[X]:(?[Y]:p(X,f(Y)))))~&q(g(a),X))
        f = SmallCNFization.formulaOpSimplify(f4);
        if (f == null)
            f = f4;
        newf = SmallCNFization.formulaSimplify(f);
        if (newf == null)
            newf = f;
        f = SmallCNFization.formulaNNF(newf, true);
        checkNNFResult(f);

        System.out.println();
        System.out.println("***** NNF conversion: " + f5);   // ![X]:(a(X)|$true)
        f = SmallCNFization.formulaOpSimplify(f5);
        assertTrue(f == null);
        if (f == null)
            f = f5;
        System.out.println("start simplify with: " + f);
        newf = SmallCNFization.formulaSimplify(f);
        if (newf == null)
            newf = f;
        System.out.println("start NNF conversion with: " + newf);
        f = SmallCNFization.formulaNNF(newf, true);
        if (f == null)
            f = newf;
        checkNNFResult(f);

        try {
            Lexer lex = new Lexer(covformulas);
            while (!lex.testTok(Lexer.EOFToken)) {
                BareFormula.level = 0;
                BareFormula fnew = BareFormula.parse(lex);
                System.out.println();
                System.out.println("***** NNF conversion: " + fnew);
                f = SmallCNFization.formulaOpSimplify(fnew);
                if (f == null)
                    f = fnew;
                System.out.println("start simplify with: " + f);
                newf = SmallCNFization.formulaSimplify(f);
                if (newf == null)
                    newf = f;
                System.out.println("start formulaNNF with: " + newf);
                f = SmallCNFization.formulaNNF(newf, true);
                if (f == null)
                    f = newf;
                checkNNFResult(f);
            }
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.testNNF()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Test Miniscoping.
     */
    @Test
    public void testMiniScope() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testMiniScope()");
        Lexer lex = new Lexer("![X]:(p(X)|q(a))\n" +
                "?[X]:(p(a)&q(X))\n" +
                "![X]:(p(X)&q(X))\n" +
                "?[X]:(p(X)|q(X))\n" +
                "![X]:(p(X)|q(X))\n" +
                "![X,Y]:?[Z]:(p(Z)|q(X))");

        boolean[] res = {true, true, true, true, false, true};

        int counter = 0;
        try {
            while (!lex.testTok(Lexer.EOFToken)) {
                System.out.println();
                System.out.println("++++++++++++++");
                boolean expected = res[counter++];
                BareFormula f = BareFormula.parse(lex);
                System.out.println(f.toStructuredString());
                f1 = SmallCNFization.formulaMiniScope(f);
                if (f != null)
                    System.out.print(f.toStructuredString());
                else
                    System.out.print("null");
                System.out.print(" transformed to: ");
                if (f1 != null)
                    System.out.print(f1.toStructuredString());
                System.out.println(" expected: " + expected);
                if (expected == (f1 != null))
                    System.out.println("success");
                else
                    System.out.println("fail");
                assertTrue(expected == (f1 != null));
                if (f1 != null) {
                    if (!f1.isQuantified())
                        System.out.println("success");
                    else
                        System.out.println("fail");
                    assertTrue(!f1.isQuantified());
                }
            }
        }
        catch (ParseException e) {
            System.out.println("Error in SmallCNFizationTest.testMiniScope()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Error in SmallCNFizationTest.testMiniScope()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Test variable renaming
     */
    @Test
    public void testRenaming() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testRenaming()");
        Lexer lex = new Lexer("![X]:(p(X)|![X]:(q(X)&?[X]:r(X)))");
        BareFormula f = null;
        try {
            f = BareFormula.parse(lex);
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.testRenaming()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("SmallCNFizationTest.testRenaming(): f: " + f);
        LinkedHashSet<Term> v1 = f.collectVars();
        System.out.println("SmallCNFizationTest.testRenaming(): should be one var: " + v1);
        if ((v1.size() == 1) && v1.iterator().next().equals(Term.string2Term("X")))
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue((v1.size() == 1) && v1.iterator().next().equals(Term.string2Term("X")));

        LinkedHashSet<Term> v2 = f.collectFreeVars();
        System.out.println("SmallCNFizationTest.testRenaming(): should be no free vars: " + v2);
        if (v2.size() == 0)
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(v2.size() == 0);

        f1 = Clausifier.standardizeVariables(f);
        // f1 = formulaVarRename(f);
        System.out.println(f + " " + f1);

        v1 = f1.collectVars();
        System.out.println("SmallCNFizationTest.testRenaming(): should be three vars: " + v1);
        if (v1.size() == 3)
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(v1.size() == 3);

        v2 = f1.collectFreeVars();
        System.out.println("SmallCNFizationTest.testRenaming(): should be no free vars: " + v2);
        if (v2.size() == 0)
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(v2.size() == 0);
    }

    /** ***************************************************************
     * Check if Skolem symbol construction works.
     */
    @Test
    public void testSkolemSymbols() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testSkolemSymbols()");
        ArrayList<String> symbols = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            String newsymbol = SmallCNFization.newSkolemSymbol();
            System.out.println("newsymbol: " + newsymbol);
            System.out.println("symbols: " + symbols);
            System.out.println("symbols doesn't contain new symbol: " + !symbols.contains(newsymbol));
            if (!symbols.contains(newsymbol))
                System.out.println("success");
            else
                System.out.println("fail");
            assertTrue(!symbols.contains(newsymbol));
            symbols.add(newsymbol);
        }
        LinkedHashSet<Term> varlist = new LinkedHashSet<Term>();
        varlist.add(Term.string2Term("X"));
        varlist.add(Term.string2Term("Y"));
        for (int i = 0; i < 10; i++) {
            Term t = SmallCNFization.newSkolemTerm(varlist);
            System.out.println("t: " + t);
            System.out.println("t is compound: " + t.isCompound());
            if (t.isCompound())
                System.out.println("success");
            else
                System.out.println("fail");
            assertTrue(t.isCompound());

            System.out.println("t: " + t);
            System.out.println("t.getArgs(): " + t.getArgs());
            System.out.println("varlist: " + varlist);
            System.out.println("t args are varlist: " + t.getArgs().equals(varlist));
            if (t.getArgs().toString().equals(varlist.toString()))
                System.out.println("success");
            else
                System.out.println("fail");
            assertTrue(t.getArgs().toString().equals(varlist.toString()));
        }
    }

    /** ***************************************************************
     * Bring formula into miniscoped variable normalized NNF.
     */
    public static BareFormula preprocFormula(BareFormula f) {

        BareFormula newf = SmallCNFization.formulaOpSimplify(f);
        if (newf != null)
            f = newf;
        newf = SmallCNFization.formulaSimplify(f);
        if (newf != null)
            f = newf;
        newf = SmallCNFization.formulaNNF(f,true);
        if (newf != null)
            f = newf;
        newf = SmallCNFization.formulaMiniScope(f);
        if (newf != null)
            f = newf;
        // f = formulaVarRename(f);
        newf = Clausifier.standardizeVariables(f);
        if (newf != null)
            f = newf;
        return newf;
    }

    /** ***************************************************************
     * Test skolemization.
     */
    @Test
    public void testSkolemization() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testSkolemization()");
        BareFormula f = preprocFormula(f2);
        f = SmallCNFization.formulaSkolemize(f);
        System.out.println(f);
        if (!f.collectOps().contains("?"))
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(!f.collectOps().contains("?"));


        f = preprocFormula(f3);
        f = SmallCNFization.formulaSkolemize(f);
        System.out.println(f);
        if (!f.collectOps().contains("?"))
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(!f.collectOps().contains("?"));


        f = preprocFormula(f4);
        f = SmallCNFization.formulaSkolemize(f);
        System.out.println(f);
        if (!f.collectOps().contains("?"))
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(!f.collectOps().contains("?"));
    }

    /** ***************************************************************
     * Test shifting of quantors out.
     */
    @Test
    public void testShiftQuantors() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testShiftQuantors()");
        BareFormula f = preprocFormula(f2);
        f = SmallCNFization.formulaSkolemize(f);
        f = SmallCNFization.formulaShiftQuantorsOut(f);
        if (f.collectOps().contains("!"))
            assert(f.op.equals("!"));

        f = preprocFormula(f3);
        f = SmallCNFization.formulaSkolemize(f);
        f = SmallCNFization.formulaShiftQuantorsOut(f);
        if (f.collectOps().contains("!"))
            assert(f.op.equals("!"));

        f = preprocFormula(f4);
        f = SmallCNFization.formulaSkolemize(f);
        f = SmallCNFization.formulaShiftQuantorsOut(f);
        if (f.collectOps().contains("!"))
            assert(f.op.equals("!"));
    }

    /** ***************************************************************
     * Test ConjunctiveNF.
     */
    @Test
    public void testDistributeDisjunctions() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testDistributeDisjunctions()");
        BareFormula f = preprocFormula(f2);
        f = SmallCNFization.formulaSkolemize(f);
        f = SmallCNFization.formulaShiftQuantorsOut(f);
        f = SmallCNFization.formulaDistributeDisjunctions(f);
        System.out.println(f);
        assert(f.isCNF());

        f = preprocFormula(f3);
        f = SmallCNFization.formulaSkolemize(f);
        f = SmallCNFization.formulaShiftQuantorsOut(f);
        f = SmallCNFization.formulaDistributeDisjunctions(f);
        System.out.println(f);
        assert(f.isCNF());

        f = preprocFormula(f4);
        f = SmallCNFization.formulaSkolemize(f);
        f = SmallCNFization.formulaShiftQuantorsOut(f);
        f = SmallCNFization.formulaDistributeDisjunctions(f);
        System.out.println(f);
        assert(f.isCNF());
    }

    /** ***************************************************************
     * Test conversion of wrapped formulas into conjunctive normal form.
     */
    @Test
    public void testCNFization() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testCNFization()");
        try {
            Lexer lex = new Lexer(testFormulas);
            System.out.println("INFO in SmallCNFizationTest.testCNFization(): testFormulas: " + testFormulas);
            while (!lex.testTok(Lexer.EOFToken)) {
                Formula wf = Formula.parse(lex);
                System.out.println();
                System.out.println("####################");
                System.out.println("SmallCNFizationTest.testCNFization(): wf input: " + wf);
                wf = SmallCNFization.wFormulaCNF(wf);
                System.out.println("SmallCNFizationTest.testCNFization(): wf result: " + wf);
                System.out.println("SmallCNFizationTest.testCNFization(): rationale: " + wf.rationale);
                System.out.println("SmallCNFizationTest.testCNFization(): support: " + wf.support);
                boolean isCNF = wf.form.isCNF();
                System.out.println("SmallCNFizationTest.testCNFization(): isCNF: " + isCNF);
                if (isCNF)
                    System.out.println("success");
                else
                    System.out.println("fail");
                assertTrue(isCNF);
            }
        }
        catch (ParseException|IOException e) {
            System.out.println("Error in SmallCNFizationTest.testCNFization()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Test conversion of wrapped formulas into lists of clauses.
     */
    @Test
    public void testClausification() {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testClausification()");
        try {
            Lexer lex = new Lexer(testFormulas);
            while (!lex.testTok(Lexer.EOFToken)) {
                Formula wf = Formula.parse(lex);
                ArrayList<Clause> clauses = SmallCNFization.wFormulaClausify(wf);
                System.out.println("==================");
                for (Clause c : clauses)
                    System.out.println("SmallCNFizationTest.testClausification(): clause: " + c);
            }
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.testClausification()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Test splitting clauses
     */
    @Test
    public void testSplit() {

        System.out.println("--------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testSplit():");
        String formstr = "fof(a1,axiom,(((p(VAR12)|q(VAR12,skolem0001(VAR12)))|a)&((q(VAR12)|q(VAR12,skolem0001(VAR12)))|a))).";
        Lexer lex = new Lexer(formstr);
        try {
            Formula wf = Formula.parse(lex);
            ArrayList<Clause> clauses = SmallCNFization.formulaCNFSplit(wf);
            System.out.println(clauses);
            String expected = "[cnf(,axiom,p(VAR12)|q(VAR12,skolem0001(VAR12))|a)., cnf(,axiom,q(VAR12)|q(VAR12,skolem0001(VAR12))|a).]";
            if (clauses.toString().equals(expected))
                System.out.println("Success");
            else
                System.out.println("fail");
            assertEquals(expected,clauses.toString());
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.testSplit()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** ***************************************************************
     * Compare the simple clausification in R&N AI text implemented in Clausify
     * with SmallCNF.  Note that we don't use any assert statements to throw
     * errors since we don't currently have a routine to check for equality
     * between ClauseSets, ignoring literal order and variable names.
     */
    @Test
    public void testCompare() {

        boolean[] resultsEq = {true,true};
        System.out.println("--------------------------------");
        System.out.println("INFO in SmallCNFizationTest.testCompare(): check formulas");
        BareFormula f = null;
        try {
            Lexer lex = new Lexer(testFormulas);
            while (!lex.testTok(Lexer.EOFToken)) {
                System.out.println("----");
                Formula wf = Formula.parse(lex);
                System.out.println("Formula: " + wf);

                SmallCNFization.countersReset();
                Formula fCNF = SmallCNFization.wFormulaCNF(wf);
                ArrayList<Clause> smallCNFclauses = SmallCNFization.formulaCNFSplit(fCNF);
                //System.out.println("Small CNF clauses: " + smallCNFclauses);
                ClauseSet smallCNFSet = new ClauseSet(smallCNFclauses);
                smallCNFSet.sort();
                smallCNFSet.normalizeVars();

                Clausifier.counterReset();
                ArrayList<Clause> rAndNClauses = Clausifier.clausify(wf);
                //System.out.println("Simple CNF clauses: " + rAndNClauses);
                ClauseSet cNFSet = new ClauseSet(rAndNClauses);
                cNFSet.sort();
                cNFSet.normalizeVars();

                System.out.println("SmallCNF result: " + smallCNFSet);
                System.out.println("Simple CNF result: " + cNFSet);

                if (!smallCNFSet.equals(cNFSet))
                    System.out.println("testCompare(): fail - Not string equal (but could be logically ok)");
                else
                    System.out.println("Success");
                assertEquals(smallCNFSet,cNFSet);
            }
        }
        catch (Exception e) {
            System.out.println("Error in SmallCNFizationTest.testCompare()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
