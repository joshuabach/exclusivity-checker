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
        apply(node.getTarget(), node.getExpression());
    }

    public final void apply(Node lhs, Node rhs) throws RuleNotApplicable {
        AnnotationMirror oldLhs = getRefinedTypeAnnotation(lhs);
        AnnotationMirror oldRhs = getRefinedTypeAnnotation(rhs);
        applyInternal(lhs, rhs);
        printAssignment(lhs, oldLhs, rhs, oldRhs);
    }

    public final void apply(AnnotationMirror lhsType, Node rhs) throws RuleNotApplicable {
        AnnotationMirror oldRhs = getRefinedTypeAnnotation(rhs);
        applyInternal(lhsType, rhs);
        printTypeChange(rhs, oldRhs);
        System.out.print(",");
    }

    protected abstract void applyInternal(Node lhsNode, Node rhsNode) throws RuleNotApplicable;
    protected abstract void applyInternal(AnnotationMirror lhsType, Node rhsNode) throws RuleNotApplicable;

    private void printAssignment(Node lhsNode, AnnotationMirror oldLhsTypeAnno,
                                 Node rhsNode, AnnotationMirror oldRhsTypeAnno) {
        printTypeChange(lhsNode, oldLhsTypeAnno);
        System.out.print(" = ");
        printTypeChange(rhsNode, oldRhsTypeAnno);
        System.out.println(";");
    }

    private void printTypeChange(Node node, AnnotationMirror oldTypeAnno) {
        if (store != null && !(JavaExpression.fromNode(node) instanceof Unknown)) {
            AnnotationMirror newTypeAnno = oldTypeAnno;
            CFValue value = store.getValue(JavaExpression.fromNode(node));
            if (value != null) {
                newTypeAnno = factory.getExclusivityAnnotation(value.getAnnotations());
            }
            System.out.printf("[%s ~> %s] ",
                    prettyPrint(oldTypeAnno),
                    prettyPrint(newTypeAnno));
        }
        System.out.print(node);
    }

    protected final ChainRule<AssignmentRule> getAssignmentRules() {
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
