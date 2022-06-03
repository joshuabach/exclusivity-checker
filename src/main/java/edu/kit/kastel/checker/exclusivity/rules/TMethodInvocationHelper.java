package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

public class TMethodInvocationHelper extends AssignmentRule {
    public TMethodInvocationHelper(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    protected void applyInternal(Node lhsNode, Node rhsNode) throws RuleNotApplicable {
        AnnotationMirror rhsNodeAnno = factory.getExclusivityAnnotation(rhsNode.getType().getAnnotationMirrors());
        updateType(lhsNode, rhsNodeAnno);
    }

    public void applyOrInvalidate(Node lhsNode, Node rhsNode) {
        try {
            applyInternal(lhsNode, rhsNode);
        } catch (RuleNotApplicable ignored) {
            new TInvalidate(store, factory, analysis).apply(lhsNode);
        }
    }

    @Override
    protected void applyInternal(AnnotationMirror lhsType, Node rhsNode) {
        assert false : "Cannot be called";
    }

    @Override
    public String getName() {
        return null;
    }
}