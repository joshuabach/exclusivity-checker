package edu.kit.kastel.checker.exclusivity;

import com.sun.source.tree.Tree;
import edu.kit.kastel.checker.exclusivity.rules.*;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
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
        if (node.getExpression().getTree().getKind() == Tree.Kind.METHOD_INVOCATION) {
            new TMethodInvocationHelper(store, factory, analysis).applyOrInvalidate(node.getTarget(), node.getExpression());
        } else {
            new TAssign(store, factory, analysis).applyOrInvalidate(node.getTarget(), node.getExpression());
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
