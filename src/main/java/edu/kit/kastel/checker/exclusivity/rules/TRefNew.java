package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

public class TRefNew extends AssignmentRule {

    public TRefNew(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    public String getName() {
        return "T-Ref-New";
    }

    @Override
    public void applyInternal(Node lhsNode, Node rhsNode) throws RuleNotApplicable {
        checkRhsNode(rhsNode);
        updateType(lhsNode, factory.EXCL_MUT);
    }

    @Override
    protected void applyInternal(AnnotationMirror lhsType, Node rhsNode) throws RuleNotApplicable {
        checkRhsNode(rhsNode);
        updateType(lhsType, factory.EXCL_MUT);
    }

    private void checkRhsNode(Node rhsNode) throws RuleNotApplicable {
        if (!(rhsNode instanceof ObjectCreationNode)) {
            throw new RuleNotApplicable(getName(), rhsNode, "rhs node is no object creation");
        }
    }
}
