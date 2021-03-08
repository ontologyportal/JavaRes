package atp;

import org.junit.*;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProverSimpleTest {

    /** ***************************************************************
     * Set up test content.
     */
    @BeforeClass
    public static void setupTests() {

        Formula.defaultPath = System.getenv("TPTP");
        System.out.println("Using default include path : " + Formula.defaultPath);
        ClauseEvaluationFunction.setupEvaluationFunctions();
    }

    /** ***************************************************************
     */
    public static void runTest(String filename) {

        String input = filename;
        System.out.println("-----------------------------");
        System.out.println("# INFO in ProverSimple.main(): Processing file " + filename);
        ClauseSet cs = ProverSimple.load(filename);
        String result = ProverSimple.run(cs);
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
                // "FLD/FLD006-1.p",   // cnf, has an include
                //   "GRP/GRP012+5.p",   // fof
                //   "GRP/GRP182-1.p",   // cnf, has an include
                //   "GRP/GRP182-3.p",   // cnf, has an include
                //   "GRP/GRP188-1.p",   // cnf, has an include
                // "GRP/GRP188-2.p",   // cnf, has an include
                // "GRP/GRP189-1.p",   // cnf, has an include
                // "GRP/GRP541-1.p",   // cnf with equality
                //   "KRS/KRS194+1.p",   // fof
                //   "LCL/LCL662+1.001.p",   // fof
                //   "MGT/MGT023+1.p",   // fof
                //   "SEU/SEU219+1.p",   // fof
                "NLP/NLP001-1.p",   // cnf
                //   "NLP/NLP220+1.p",   // fof
                "NLP/NLP220-1.p",   // cnf
                "NLP/NLP221-1.p",   // cnf
                // "NLP/NLP225+1.p",   // fof
                "NLP/NLP226-1.p",   // cnf
                "NLP/NLP227-1.p",   // cnf
                "NLP/NLP228-1.p",   // cnf
                //  "PUZ/PUZ001+1.p",   // fof
                "PUZ/PUZ001-1.p",   // cnf
                "PUZ/PUZ001-3.p",   // cnf
                "PUZ/PUZ002-1.p",   // cnf
                "PUZ/PUZ003-1.p"};   // cnf
                // "SYN/SYN060+1.p",   // fof
                // "SYN/SYN954+1.p"};  // fof
        for (String f : probs)
            runTest(System.getenv("TPTP") + sep + "Problems" + sep + f);
    }
}
