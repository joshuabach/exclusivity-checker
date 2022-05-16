package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

abstract class ExclMutAssignmentRule extends AssignmentRule {
    public ExclMutAssignmentRule(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    public final void apply(Node lhsNode, Node rhsNode) throws RuleNotApplicable {
        if (hierarchy.isSubtype(getRefinedTypeAnnotation(rhsNode), factory.EXCL_MUT)) {
            updateType(lhsNode, getNewLhsTypeAnnotation());
            updateType(rhsNode, getNewRhsTypeAnnotation());
        } else {
            throw new RuleNotApplicable(getName(), rhsNode, "rhs is not ExclMut");
        }
    }

    protected abstract AnnotationMirror getNewLhsTypeAnnotation();
    protected abstract AnnotationMirror getNewRhsTypeAnnotation();
}
