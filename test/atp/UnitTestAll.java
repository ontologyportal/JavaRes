package atp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BacktrackSubstitutionTest.class,
        BareFormulaTest.class,
        ClauseEvaluationFunctionTest.class,
        ClauseSetTest.class,
        ClauseTest.class,
        ClausifierTest.class,
        DerivationTest.class,
        EqAxiomsTest.class,
        FormulaTest.class,
        HeuristicClauseSetTest.class,
        IndexedClauseSetTest.class,
        KIFTest.class,
        LexerTest.class,
        LiteralTest.class,
        LitSelectionTest.class,
        PredicateAbstractionTest.class,
        ProofStateTest.class,
        ProverCNFTest.class,
        ProverSimpleTest.class,
        ProverFOFTest.class,
        ResControlTest.class,
        ResolutionTest.class,
        ResolutionIndexTest.class,
        SearchParamsTest.class,
        SignatureTest.class,
        SimpleProofStateTest.class,
        SmallCNFizationTest.class,
        SubstitutionsTest.class,
        SubsumptionTest.class,
        SubsumptionIndexTest.class,
        TermTest.class,
        UnificationTest.class,
})

public class UnitTestAll {
}
