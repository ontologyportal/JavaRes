package atp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BareFormulaTest.class,
        ClauseEvaluationFunctionTest.class,
        ClauseSetTest.class,
        ClauseTest.class,
        LexerTest.class,
        LiteralTest.class,
        LitSelectionTest.class,
        SignatureTest.class,
        SubstitutionsTest.class,
        TermTest.class,
        UnificationTest.class,
})

public class UnitTestAll {
}
