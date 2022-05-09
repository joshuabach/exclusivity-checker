package edu.kit.kastel.checker.exclusivity;

import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.Unknown;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.QualifierHierarchy;

import javax.lang.model.element.AnnotationMirror;
import java.util.Collections;

public class ExclusivityTransfer extends CFTransfer {

    private final QualifierHierarchy hierarchy;
    private final ExclusivityAnnotatedTypeFactory factory;

    public ExclusivityTransfer(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis,
                               ExclusivityAnnotatedTypeFactory factory) {
        super(analysis);
        assert factory == analysis.getTypeFactory();
        this.factory = factory;
        this.hierarchy = factory.getQualifierHierarchy();
    }

    @Override
    public TransferResult<CFValue, CFStore> visitAssignment(
            AssignmentNode n, TransferInput<CFValue, CFStore> in) {
        visitPseudoAssignment(in.getRegularStore(), n.getTarget(), n.getExpression());
        return new RegularTransferResult<>(null, in.getRegularStore());
    }

    private void visitPseudoAssignment(CFStore store, Node lhsNode, Node rhsNode) {

        CFValue lhsValue = getOrInsert(store, lhsNode);
        CFValue rhsValue = getOrInsert(store, rhsNode);

        AnnotationMirror oldRhsTypeAnno = hierarchy.findAnnotationInHierarchy(
                rhsValue.getAnnotations(), factory.READ_ONLY);
        assert oldRhsTypeAnno != null;

        AnnotationMirror oldLhsTypeAnno = hierarchy.findAnnotationInHierarchy(
                lhsValue.getAnnotations(), factory.READ_ONLY);
        assert oldLhsTypeAnno != null;

        boolean printRhsTypeChange;  // only if rhs is location (this, field or variable)
        if (rhsNode instanceof ObjectCreationNode) {
            // T-Ref-New
            safelyUpdateType(store, lhsNode, factory.EXCL_MUT);
            System.out.println("Applied T-Ref-New");
            printRhsTypeChange = false;
        } else if (hierarchy.isSubtype(oldRhsTypeAnno, factory.EXCL_MUT)) {
            try {
                // T-Ref-Split-Mut
                attemptUpdateType(store, lhsNode, factory.SHR_MUT);
                attemptUpdateType(store, rhsNode, factory.SHR_MUT);
                System.out.println("Applied T-Ref-Split-Mut");
            } catch (RefinementViolatesDeclaration ignored) {
                try {
                    // T-Ref-Split-Immut
                    attemptUpdateType(store, lhsNode, factory.IMMUTABLE);
                    attemptUpdateType(store, rhsNode, factory.IMMUTABLE);
                    System.out.println("Applied T-Ref-Split-Immut");
                } catch (RefinementViolatesDeclaration ignored2) {
                    // T-Ref-Transfer
                    safelyUpdateType(store, lhsNode, factory.EXCL_MUT);
                    safelyUpdateType(store, rhsNode, factory.READ_ONLY);
                    System.out.println("Applied T-Ref-Transfer");
                }
            }
            printRhsTypeChange = true;
        } else if (factory.isCopyable(oldRhsTypeAnno)) {
            // T-Ref-Copy
            safelyUpdateType(store, lhsNode, oldRhsTypeAnno);
            System.out.println("Applied T-Ref-Copy");
            printRhsTypeChange = true;
        } else {
            // T-Ref-Copy-Ro (default case, since this is always safe and sound)
            safelyUpdateType(store, lhsNode, factory.READ_ONLY);
            System.out.println("Applied T-Ref-Copy-Ro");
            printRhsTypeChange = false;
        }

        printAssignment(store, lhsNode, oldLhsTypeAnno, rhsNode, oldRhsTypeAnno, printRhsTypeChange);
    }

    /**
     * Update type of node in store to newRefinedType, ensuring that newRefinedType
     * is a subtype of node's declared type.
     *
     * @param store The abstract value store
     * @param node The node whose type is to be updated
     * @param refinedType The new, refined type of node
     * @throws RefinementViolatesDeclaration if the refinement would violate the declaration
     */
    private void safelyUpdateType(
            CFStore store, Node node, AnnotationMirror refinedType,
            boolean acceptInvalidType
    ) throws RefinementViolatesDeclaration {
        AnnotationMirror declaredType = getDeclaredTypeAnnotation(node);
        assert declaredType != null;

        AnnotationMirror newRefinedType;
        if (!hierarchy.isSubtype(refinedType, declaredType)) {
            if (acceptInvalidType) {
                newRefinedType = factory.EXCLUSIVITY_BOTTOM; // hierarchy.greatestLowerBound(newRefinedType, declaredType);
            } else {
                throw new RefinementViolatesDeclaration(node, refinedType);
            }
        } else {
            newRefinedType = refinedType;
        }

        store.replaceValue(JavaExpression.fromNode(node),
                analysis.createAbstractValue(Collections.singleton(newRefinedType), node.getType()));
    }

    private void attemptUpdateType(CFStore store, Node node, AnnotationMirror newRefinedType)
            throws RefinementViolatesDeclaration {
        safelyUpdateType(store, node, newRefinedType, false);
    }

    private void safelyUpdateType(CFStore store, Node node, AnnotationMirror newRefinedType) {
        try {
            safelyUpdateType(store, node, newRefinedType, true);
        } catch (RefinementViolatesDeclaration e) {
            assert false: "Can never happen with acceptInvalidType=true";
        }
    }

    private AnnotationMirror getDeclaredTypeAnnotation(Node node) {
        return hierarchy.findAnnotationInHierarchy(
                factory.getAnnotatedTypeLhs(node.getTree()).getAnnotations(),
                factory.READ_ONLY);
    }

    private CFValue getOrInsert(CFStore store, Node node) {
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

    private void printAssignment(CFStore store,
                                 Node lhsNode, AnnotationMirror oldLhsTypeAnno,
                                 Node rhsNode, AnnotationMirror oldRhsTypeAnno,
                                 boolean printRhsTypeChange) {
        System.out.printf("[%s ~> %s] %s = ",
                prettyPrint(oldLhsTypeAnno),
                prettyPrint(store.getValue(JavaExpression.fromNode(lhsNode)).getAnnotations().stream().findAny().get()),
                lhsNode);
        if (printRhsTypeChange)
            System.out.printf("[%s ~> %s] ",
                    prettyPrint(oldRhsTypeAnno),
                    prettyPrint(store.getValue(JavaExpression.fromNode(rhsNode)).getAnnotations().stream().findAny().get()));
        System.out.printf("%s;%n", rhsNode);
    }

    private String prettyPrint(AnnotationMirror anno) {
        // TODO: There has to be a better way to do this
        String pkgName = "edu.kit.kastel.checker.exclusivity.qual.";
        StringBuilder s = new StringBuilder(anno.toString());
        s.delete(s.indexOf(pkgName), s.indexOf(pkgName) + pkgName.length());
        return s.toString();
    }

    @SuppressWarnings("serial")
    private class RefinementViolatesDeclaration extends Exception {
        public RefinementViolatesDeclaration(Node node, AnnotationMirror attemptedRefinement) {
            super(String.format("New type %s of %s is not a refinement of declared type %s",
                    attemptedRefinement, node, getDeclaredTypeAnnotation(node)
            ));
        }

    }
}
