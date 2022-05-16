package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.Unknown;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

public abstract class AssignmentRule extends AbstractTypeRule<AssignmentNode> {

    public AssignmentRule(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    protected final void applyInternal(AssignmentNode node) throws RuleNotApplicable {
        AnnotationMirror oldLhs = getRefinedTypeAnnotation(node.getTarget());
        AnnotationMirror oldRhs = getRefinedTypeAnnotation(node.getExpression());
        apply(node.getTarget(), node.getExpression());
        printAssignment(node.getTarget(), oldLhs, node.getExpression(), oldRhs);
    }

    public abstract void apply(Node lhsNode, Node rhsNode) throws RuleNotApplicable;

    private void printAssignment(Node lhsNode, AnnotationMirror oldLhsTypeAnno,
                                 Node rhsNode, AnnotationMirror oldRhsTypeAnno) {
        System.out.printf("[%s ~> %s] %s = ",
                prettyPrint(oldLhsTypeAnno),
                prettyPrint(store.getValue(JavaExpression.fromNode(lhsNode)).getAnnotations().stream().findAny().get()),
                lhsNode);
        if (!(JavaExpression.fromNode(rhsNode) instanceof Unknown)) {
            System.out.printf("[%s ~> %s] ",
                    prettyPrint(oldRhsTypeAnno),
                    prettyPrint(store.getValue(JavaExpression.fromNode(rhsNode)).getAnnotations().stream().findAny().get()));
        }
        System.out.printf("%s;%n", rhsNode);
    }
}
