package atp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BareFormulaTest.class,
        ClauseEvaluationFunctionTest.class,
        ClauseSetTest.class,
        ClauseTest.class,
        ClausifierTest.class,
        FormulaTest.class,
        KIFTest.class,
        LexerTest.class,
        LiteralTest.class,
        LitSelectionTest.class,
        ProofStateTest.class,
        ResControlTest.class,
        ResolutionTest.class,
        SignatureTest.class,
        SimpleProofStateTest.class,
        SubstitutionsTest.class,
        SubsumptionTest.class,
        TermTest.class,
        UnificationTest.class,
})

public class UnitTestAll {
}
