package edu.kit.kastel.checker.exclusivity;

import edu.kit.kastel.checker.exclusivity.qual.*;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;

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

    public boolean isCopyable(AnnotationMirror annotationMirror) {
        return !qualHierarchy.isSubtype(
                EXCL_MUT,
                annotationMirror
        );
    }

    public boolean isMutable(AnnotationMirror annotationMirror) {
        return !qualHierarchy.isSubtype(
                IMMUTABLE,
                annotationMirror
        );
    }

    public boolean mayHoldProperty(AnnotationMirror annotationMirror) {
        return qualHierarchy.isSubtype(
                RESTRICTED,
                annotationMirror
        );
    }

    public boolean isMutable(AnnotatedTypeMirror annotatedType) {
        return isMutable(qualHierarchy.findAnnotationInHierarchy(
                annotatedType.getAnnotations(), READ_ONLY
        ));
    }

    public boolean isValid(AnnotationMirror annotationMirror) {
        return !qualHierarchy.isSubtype(
                annotationMirror,
                EXCLUSIVITY_BOTTOM
        );
    }

    public boolean isValid(AnnotatedTypeMirror annotatedType) {
        return isValid(qualHierarchy.findAnnotationInHierarchy(
                annotatedType.getAnnotations(), READ_ONLY
        ));
    }
}
