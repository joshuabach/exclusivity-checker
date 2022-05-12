package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.Unknown;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.QualifierHierarchy;

import javax.lang.model.element.AnnotationMirror;
import java.util.Collections;

abstract class AbstractTypeRule<N extends Node> implements TypeRule {
    protected final CFStore store;
    protected final QualifierHierarchy hierarchy;
    protected final ExclusivityAnnotatedTypeFactory factory;
    protected CFAbstractAnalysis<CFValue, CFStore, CFTransfer>  analysis;

    public AbstractTypeRule(CFStore store, ExclusivityAnnotatedTypeFactory factory,
                            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        this.store = store;
        this.analysis = analysis;
        this.hierarchy = factory.getQualifierHierarchy();
        this.factory = factory;
    }

    @Override
    public final void apply(Node abstractNode) throws RuleNotApplicable {
        N node;
        try {
            // TODO Why does javac produce warning even if ClassCastException is caught?
            @SuppressWarnings("unchecked")
            N concreteNode = (N) abstractNode;
            node = concreteNode;
        } catch (ClassCastException e) {
            throw new RuleNotApplicable(getName(), abstractNode, "wrong node type");
        }
        applyInternal(node);
        System.out.println("Applied " + getName());
    }

    protected abstract void applyInternal(N node) throws RuleNotApplicable;

    /**
     * Update type of node in store to newRefinedType, ensuring that newRefinedType
     * is a subtype of node's declared type.
     *
     * @param node        The node whose type is to be updated
     * @param refinedType The new, refined type of node
     * @throws RuleNotApplicable if the refinement would violate the declaration
     */
    protected final void updateType(
            Node node, AnnotationMirror refinedType
    ) throws RuleNotApplicable {
        AnnotationMirror declaredType = getDeclaredTypeAnnotation(node);
        assert declaredType != null;

        AnnotationMirror newRefinedType;
        if (!hierarchy.isSubtype(refinedType, declaredType)) {
            throw new RuleNotApplicable(getName(), node, "refinement violates declaration");
        } else {
            newRefinedType = refinedType;
        }

        CFValue abstractValue = analysis.createAbstractValue(
                Collections.singleton(newRefinedType), node.getType());
        store.replaceValue(JavaExpression.fromNode(node),
                abstractValue);
    }

    private AnnotationMirror getDeclaredTypeAnnotation(Node node) {
        return hierarchy.findAnnotationInHierarchy(
                // TODO Do we need to get declared types for nodes not supported by getAnnotatedTypeLhs?
                factory.getAnnotatedTypeLhs(node.getTree()).getAnnotations(),
                factory.READ_ONLY);
    }

    protected final CFValue getOrInsert(Node node) {
        JavaExpression expr = JavaExpression.fromNode(node);
        CFValue value;
        if (expr instanceof Unknown)
            value = null;
        else
            value = store.getValue(expr);

        if (value == null) {
            value = analysis.createAbstractValue(factory.getAnnotatedType(node.getTree()));
            assert value != null;
            store.insertValue(expr, value);
        }

        return value;
    }

    protected final AnnotationMirror getRefinedTypeAnnotation(Node node) {
        AnnotationMirror oldAnno = hierarchy.findAnnotationInHierarchy(
                getOrInsert(node).getAnnotations(), factory.READ_ONLY);
        assert oldAnno != null;
        return oldAnno;
    }

    protected final String prettyPrint(AnnotationMirror anno) {
        // TODO: There has to be a better way to do this
        String pkgName = "edu.kit.kastel.checker.exclusivity.qual.";
        StringBuilder s = new StringBuilder(anno.toString());
        s.delete(s.indexOf(pkgName), s.indexOf(pkgName) + pkgName.length());
        return s.toString();
    }
}
