package edu.kit.kastel.checker.exclusivity;

import edu.kit.kastel.checker.exclusivity.rules.*;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

public class ExclusivityTransfer extends CFTransfer {

    private final ExclusivityAnnotatedTypeFactory factory;

    public ExclusivityTransfer(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis,
                               ExclusivityAnnotatedTypeFactory factory) {
        super(analysis);
        assert factory == analysis.getTypeFactory();
        this.factory = factory;
    }

    @Override
    public TransferResult<CFValue, CFStore> visitAssignment(
            AssignmentNode node, TransferInput<CFValue, CFStore> in) {
        CFStore store = in.getRegularStore();

        ChainRule<AssignmentRule> rules = getAssignmentRules(store);

        try {
            // Attempt to apply all assignment rules, use first one that works
            rules.apply(node);
        } catch (RuleNotApplicable ignored) {
            // No valid rule to refine assignment, so it must be invalid.
            // ExclusivityVisitor will report the error.
            new TInvalidate(store, factory, analysis).apply(node.getTarget());
            new TInvalidate(store, factory, analysis).apply(node.getExpression());
        }

        return new RegularTransferResult<>(null, in.getRegularStore());
    }

    private ChainRule<AssignmentRule> getAssignmentRules(CFStore store) {
        return new ChainRule<>(
                new TRefNew(store, factory, analysis),
                new TRefCopy(store, factory, analysis),
                new TRefSplitMut(store, factory, analysis),
                new TRefSplitImmut(store, factory, analysis),
                new TRefTransfer(store, factory, analysis),
                new TRefCopyRo(store, factory, analysis)
        );
    }
}
