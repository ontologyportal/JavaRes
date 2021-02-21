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
import static org.junit.Assert.*;

public class ProverTest {

    public static ArrayList<SearchParams> evals = null;
    public static HashMap<String, String> opts = null;

    /** ***************************************************************
     * Set up test content.
     */
    @BeforeClass
    public static void setupTests() {

        String[] args = "--eqax --proof --delete-tautologies --forward-subsumption --backward_subsumption --delete-tautologies --timeout 600".split(" ");
        Formula.defaultPath = System.getenv("TPTP");
        System.out.println("Using default include path : " + Formula.defaultPath);

        ClauseEvaluationFunction.setupEvaluationFunctions();

        opts = Prover2.processOptions(args);  // canonicalize options

        evals = new ArrayList<SearchParams>();
        SearchParams sp = new SearchParams();
        sp.heuristics = ClauseEvaluationFunction.PickGiven5;
        evals.add(sp);

    }

    /** ***************************************************************
     */
    public static void runTest(String filename) {

        System.out.println("# INFO in Prover2.main(): Processing file " + filename);
        ProofState state = Prover2.processTestFile(filename,opts,evals);
        //System.out.println("# INFO in Prover2.main(): state: " + state);
        if (state != null && state.res != null) {
            Prover2.printStateResults(opts,state,null);
            //System.out.println("# SZS status Theorem for problem " + filename);
        }
        else {
            if (state == null || Term.emptyString(state.SZSresult))
                System.out.println("# SZS status GaveUp for problem " + filename);
            else
                Prover2.printStateResults(opts,state,null);
        }
        if (state.SZSresult.equals(state.SZSexpected))
            System.out.println("Success");
        else
            System.out.println("fail");
        assertEquals(state.SZSexpected,state.SZSresult);
    }

    /** ***************************************************************
     */
    @Test
    public void test1() {

        String sep = File.separator;
        String[] probs = {
                "ALG/ALG002-1.p",
                "ANA/ANA013-2.p",
                "ANA/ANA029-2.p",
                "ANA/ANA037-2.p",
                "ANA/ANA038-2.p",
                "ANA/ANA039-2.p",
                "ANA/ANA041-2.p",
                "COL/COL101-2.p",
                "COL/COL113-2.p",
                "COL/COL117-2.p",
                "COL/COL121-2.p",
                "COM/COM002-1.p",
                "COM/COM002-2.p",
                "CSR/CSR074+1.p",
                "CSR/CSR114+6.p",
                "FLD/FLD006-1.p",
                "GRP/GRP012+5.p",
                "GRP/GRP182-1.p",
                "GRP/GRP182-3.p",
                "GRP/GRP188-1.p",
                "GRP/GRP188-1.p",
                "GRP/GRP188-2.p",
                "GRP/GRP189-1.p",
                "GRP/GRP541-1.p",
                "KRS/KRS194+1.p",
                "LCL/LCL662+1.001.p",
                "NLP/NLP001-1.p",
                "NLP/NLP220+1.p",
                "NLP/NLP220-1.p",
                "NLP/NLP221-1.p",
                "NLP/NLP225+1.p",
                "NLP/NLP226-1.p",
                "NLP/NLP227-1.p",
                "NLP/NLP228-1.p",
                "PUZ/PUZ001+1.p",
                "PUZ/PUZ001-1.p",
                "PUZ/PUZ001-3.p",
                "PUZ/PUZ002-1.p",
                "PUZ/PUZ003-1.p",
                "SYN/SYN060+1.p",
                "SYN/SYN954+1.p"};
        for (String f : probs)
            runTest(System.getenv("TPTP") + sep + "Problems" + sep + f);
    }
}
