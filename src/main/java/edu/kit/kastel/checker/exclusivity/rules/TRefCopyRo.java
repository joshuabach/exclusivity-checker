package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

public class TRefCopyRo extends AssignmentRule {
    public TRefCopyRo(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    public void applyInternal(Node lhsNode, Node rhsNode) throws RuleNotApplicable {
        updateType(lhsNode, factory.READ_ONLY);
    }

    @Override
    protected void applyInternal(AnnotationMirror lhsType, Node rhsNode) throws RuleNotApplicable {
        updateType(lhsType, factory.READ_ONLY);
    }

    @Override
    public String getName() {
        return "T-Ref-Copy-Ro";
    }
}
