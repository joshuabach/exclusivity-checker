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

        // Attempt to apply all assignment rules, use first one that works
        TypeRule[] rules = new TypeRule[]{
                new TRefNew(store, factory, analysis),
                new TRefCopy(store, factory, analysis),
                new TRefSplitMut(store, factory, analysis),
                new TRefSplitImmut(store, factory, analysis),
                new TRefTransfer(store, factory, analysis),
                new TRefCopyRo(store, factory, analysis),
        };

        boolean anyRuleWasApplied = false;
        for (TypeRule rule : rules) {
            try {
                rule.apply(node);
                anyRuleWasApplied = true;
                break;
            } catch (RuleNotApplicable ignored) {}
        }
        if (!anyRuleWasApplied) {
            // No valid rule to refine assignment, so it must be invalid.
            // ExclusivityVisitor will report the error.
            new TInvalidate(store, factory, analysis).apply(node.getTarget());
        }

        return new RegularTransferResult<>(null, in.getRegularStore());
    }
}
