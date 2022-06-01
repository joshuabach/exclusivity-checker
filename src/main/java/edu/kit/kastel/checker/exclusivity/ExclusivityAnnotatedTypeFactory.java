package edu.kit.kastel.checker.exclusivity;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import edu.kit.kastel.checker.exclusivity.qual.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.cfg.node.*;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.Unknown;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import java.util.Objects;
import java.util.Set;

public class ExclusivityAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public final AnnotationMirror EXCL_MUT =
            AnnotationBuilder.fromClass(elements, ExclMut.class);
    public final AnnotationMirror EXCLUSIVITY_BOTTOM =
            AnnotationBuilder.fromClass(elements, ExclusivityBottom.class);
    public final AnnotationMirror IMMUTABLE =
            AnnotationBuilder.fromClass(elements, Immutable.class);
    public final AnnotationMirror READ_ONLY =
            AnnotationBuilder.fromClass(elements, ReadOnly.class);
    public final AnnotationMirror RESTRICTED =
            AnnotationBuilder.fromClass(elements, Restricted.class);
    public final AnnotationMirror SHR_MUT =
            AnnotationBuilder.fromClass(elements, ShrMut.class);
    @Nullable
    private Tree useIFlowAfter;


    public ExclusivityAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        if (this.getClass() == ExclusivityAnnotatedTypeFactory.class) {
            this.postInit();
        }
    }

    @Override
    public CFTransfer createFlowTransferFunction(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        return new ExclusivityTransfer(analysis, this);
    }

    public AnnotationMirror getExclusivityAnnotation(Set<AnnotationMirror> qualifiers) {
        return qualHierarchy.findAnnotationInHierarchy(qualifiers, READ_ONLY);
    }

    public AnnotationMirror getExclusivityAnnotation(AnnotatedTypeMirror annotatedTypeMirror) {
        return getExclusivityAnnotation(annotatedTypeMirror.getAnnotations());
    }

    public boolean isCopyable(@NonNull AnnotationMirror annotationMirror) {
        return !qualHierarchy.isSubtype(
                EXCL_MUT,
                annotationMirror
        );
    }

    public boolean isMutable(@NonNull AnnotationMirror annotationMirror) {
        return !qualHierarchy.isSubtype(
                IMMUTABLE,
                annotationMirror
        );
    }

    public boolean mayHoldProperty(@NonNull AnnotationMirror annotationMirror) {
        return qualHierarchy.isSubtype(
                RESTRICTED,
                annotationMirror
        );
    }

    public boolean isMutable(AnnotatedTypeMirror annotatedType) {
        AnnotationMirror annotation = annotatedType.getAnnotation();
        assert annotation != null;
        return isMutable(annotation);
    }

    public boolean isValid(@NonNull AnnotationMirror annotationMirror) {
        return !qualHierarchy.isSubtype(
                annotationMirror,
                EXCLUSIVITY_BOTTOM
        );
    }

    public boolean isValid(AnnotatedTypeMirror annotatedType) {
        AnnotationMirror annotation = annotatedType.getAnnotation();
        return annotation == null || isValid(annotation);
    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                super.createTreeAnnotator(),
                new ExclusivityTreeAnnotator(this)
        );
    }

    public void useIFlowAfter(@NonNull Tree useIFlowAfter) {
        this.useIFlowAfter = useIFlowAfter;
    }

    public void useRegularIFlow() {
        this.useIFlowAfter = null;
    }

    private class ExclusivityTreeAnnotator extends TreeAnnotator {
        protected ExclusivityTreeAnnotator(AnnotatedTypeFactory aTypeFactory) {
            super(aTypeFactory);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
            // new C() is always @ExclMut
            annotatedTypeMirror.replaceAnnotation(EXCL_MUT);
            return super.visitNewClass(node, annotatedTypeMirror);
        }
    }

    @Override
    public @Nullable CFValue getInferredValueFor(Tree tree) {
        if (useIFlowAfter != null) {
            Tree oldUseIFlowAfter = useIFlowAfter;
            // getStoreAfter needs to use the regular information flow
            useRegularIFlow();
            CFStore store = flowResult.getStoreAfter(oldUseIFlowAfter);
            Set<Node> nodes = flowResult.getNodesForTree(tree);
            if (store == null || nodes == null) {
                return null;
            }
            useIFlowAfter(oldUseIFlowAfter);

            return nodes.stream()
                    .map(node -> {
                        JavaExpression expr;
                        if (!((expr = JavaExpression.fromNode(node)) instanceof Unknown)) {
                            return store.getValue(expr);
                        } else if (node instanceof MethodInvocationNode) {
                            return store.getValue((MethodInvocationNode) node);
                        } else if (node instanceof FieldAccessNode) {
                            return store.getValue((FieldAccessNode) node);
                        } else if (node instanceof ArrayAccessNode) {
                            return store.getValue((ArrayAccessNode) node);
                        } else if (node instanceof LocalVariableNode) {
                            return store.getValue((LocalVariableNode) node);
                        } else if (node instanceof ThisNode) {
                            return store.getValue((ThisNode) node);
                        } else {
                            return null;  // No refined value available
                        }})
                    .filter(Objects::nonNull)
                    .reduce(CFValue::leastUpperBound)
                    .orElse(null);
        } else {
            return super.getInferredValueFor(tree);
        }
    }
}
