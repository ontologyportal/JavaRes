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
        LexerTest.class,
        LiteralTest.class,
        LitSelectionTest.class,
        ResolutionTest.class,
        SignatureTest.class,
        SubstitutionsTest.class,
        SubsumptionTest.class,
        TermTest.class,
        UnificationTest.class,
})

public class UnitTestAll {
}
