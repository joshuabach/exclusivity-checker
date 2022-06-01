package edu.kit.kastel.checker.exclusivity;

import com.sun.source.tree.Tree;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeValidator;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import javax.lang.model.element.AnnotationMirror;

public class ExclusivityValidator extends BaseTypeValidator {
    protected ExclusivityAnnotatedTypeFactory atypeFactory;
    public ExclusivityValidator(BaseTypeChecker checker, BaseTypeVisitor<?> visitor, ExclusivityAnnotatedTypeFactory atypeFactory) {
        super(checker, visitor, atypeFactory);
        this.atypeFactory = atypeFactory;
    }

    @Override
    public boolean isValid(AnnotatedTypeMirror type, Tree tree) {
        AnnotationMirror typeAnno = type.getAnnotationInHierarchy(atypeFactory.READ_ONLY);
        return super.isValid(type, tree) && isValid(typeAnno, tree);
    }

    public boolean isValid(AnnotationMirror typeAnno, Tree tree) {
        boolean isInvalid = atypeFactory.getQualifierHierarchy()
                .isSubtype(typeAnno, atypeFactory.EXCLUSIVITY_BOTTOM);
        if (isInvalid) {
            checker.reportError(tree, "type.invalid", typeAnno);
        }
        return !isInvalid;
    }

    @Override
    protected boolean shouldCheckTopLevelDeclaredOrPrimitiveType(AnnotatedTypeMirror type, Tree tree) {
        return true;
    }
}
