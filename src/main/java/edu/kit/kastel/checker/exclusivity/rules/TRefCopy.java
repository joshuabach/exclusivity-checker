package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

public class TRefCopy extends AssignmentRule {
    public TRefCopy(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    public void apply(Node lhsNode, Node rhsNode) throws RuleNotApplicable {
        AnnotationMirror oldRhsTypeAnno = getRefinedTypeAnnotation(rhsNode);
        if (factory.isCopyable(oldRhsTypeAnno)) {
            updateType(lhsNode, oldRhsTypeAnno);
        } else {
            throw new RuleNotApplicable(getName(), rhsNode, "rhs node is not copyable");
        }
    }

    @Override
    public String getName() {
        return "T-Ref-Copy";
    }
}
