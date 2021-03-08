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

This is a Java rewrite of PyRes - https://github.com/eprover/PyRes

Test the prover with a selection of problems from TPTP
*/

import org.junit.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ProverCNFTest {

    public static ArrayList<SearchParams> evals = new ArrayList<>();
    public static HashMap<String, String> opts = null;

    /** ***************************************************************
     * Set up test content.
     */
    @BeforeClass
    public static void setupTests() {

        String[] args = "--delete-tautologies --forward-subsumption --backward-subsumption --delete-tautologies".split(" ");
        Formula.defaultPath = System.getenv("TPTP");
        System.out.println("Using default include path : " + Formula.defaultPath);

        ClauseEvaluationFunction.setupEvaluationFunctions();

        ArrayList<String> opts = new ArrayList<>();
        opts.addAll(Arrays.asList(args));
        SearchParams sp = ProverCNF.processOptions(opts);  // canonicalize options
        sp.heuristics = ClauseEvaluationFunction.PickGiven5;
        evals.add(sp);
    }

    /** ***************************************************************
     */
    public static void runTest(String filename) {

        String input = "--delete-tautologies --forward-subsumption --backward-subsumption --delete-tautologies" +
                " " + filename;
        String[] args =  input.split(" ");
        System.out.println("# INFO in ProverFOF.main(): Processing file " + filename);
        ArrayList<String> opts = new ArrayList<>();
        opts.addAll(Arrays.asList(args));
        SearchParams sp = ProverCNF.processOptions(opts);  // canonicalize options
        ClauseSet cs = ProverCNF.load(sp);
        String result = ProverCNF.run(cs,sp);
        System.out.println("result: " + result);
        if (result != null)
            System.out.println("Success");
        else
            System.out.println("fail");
        assertTrue(result != null);
    }

    /** ***************************************************************
     */
    @Test
    public void test1() {

        String sep = File.separator;
        String[] probs = {
                "ALG/ALG002-1.p",   // cnf
                "ANA/ANA013-2.p",   // cnf
                "ANA/ANA029-2.p",   // cnf
                "ANA/ANA037-2.p",   // cnf
                "ANA/ANA038-2.p",   // cnf
                "ANA/ANA039-2.p",   // cnf
                "ANA/ANA041-2.p",   // cnf
                "COL/COL101-2.p",   // cnf
                "COL/COL113-2.p",   // cnf
                "COL/COL117-2.p",   // cnf
                "COL/COL121-2.p",   // cnf
                "COM/COM002-1.p",   // cnf
                "COM/COM002-2.p",   // cnf
             //   "CSR/CSR074+1.p",   // fof
             //   "CSR/CSR114+6.p",   // fof
                //   "FLD/FLD006-1.p",   // cnf, has an include
             //   "GRP/GRP012+5.p",   // fof, has an include
            //    "GRP/GRP182-1.p",   // cnf, has an include
            //    "GRP/GRP182-3.p",   // cnf, has an include
            //    "GRP/GRP188-1.p",   // cnf, has an include
            //    "GRP/GRP188-2.p",   // cnf, has an include
            //    "GRP/GRP189-1.p",   // cnf, has an include
                "GRP/GRP541-1.p",   // cnf with equality
             //   "KRS/KRS194+1.p",   // fof
             //   "LCL/LCL662+1.001.p",   // fof
             //   "MGT/MGT023+1.p",   // fof
             //   "SEU/SEU219+1.p",   // fof
                "NLP/NLP001-1.p",   // cnf
             //   "NLP/NLP220+1.p",   // fof
                "NLP/NLP220-1.p",   // cnf
                "NLP/NLP221-1.p",   // cnf
             //   "NLP/NLP225+1.p",   // fof
                "NLP/NLP226-1.p",   // cnf
                "NLP/NLP227-1.p",   // cnf
                "NLP/NLP228-1.p",   // cnf
              //  "PUZ/PUZ001+1.p",   // fof
                "PUZ/PUZ001-1.p",   // cnf
                "PUZ/PUZ001-3.p",   // cnf
                "PUZ/PUZ002-1.p",   // cnf
                "PUZ/PUZ003-1.p"};   // cnf
             //   "SYN/SYN060+1.p",   // fof
             //   "SYN/SYN954+1.p"  // fof
        for (String f : probs)
            runTest(System.getenv("TPTP") + sep + "Problems" + sep + f);
    }
}

