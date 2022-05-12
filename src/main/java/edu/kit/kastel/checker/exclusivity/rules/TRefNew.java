package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

public class TRefNew extends AssignmentRule {

    public TRefNew(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    public String getName() {
        return "T-Ref-New";
    }

    @Override
    protected void applyInternal(Node lhsNode, Node rhsNode) throws RuleNotApplicable {
        if (rhsNode instanceof ObjectCreationNode) {
            updateType(lhsNode, factory.EXCL_MUT);
        } else {
            throw new RuleNotApplicable(getName(), rhsNode, "rhs node is no object creation");
        }
    }
}
