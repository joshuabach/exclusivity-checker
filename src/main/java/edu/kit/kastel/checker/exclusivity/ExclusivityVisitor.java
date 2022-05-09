package edu.kit.kastel.checker.exclusivity;

import com.sun.source.tree.Tree;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

public class ExclusivityVisitor extends BaseTypeVisitor<ExclusivityAnnotatedTypeFactory> {
    public ExclusivityVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType, AnnotatedTypeMirror valueType, Tree valueTree, @CompilerMessageKey String errorKey, Object... extraArgs) {
        if (varType.getAnnotationInHierarchy(atypeFactory.READ_ONLY).equals(atypeFactory.EXCLUSIVITY_BOTTOM)) {
            checker.reportError(varType, errorKey);
        }
        if (valueType.getAnnotationInHierarchy(atypeFactory.READ_ONLY).equals(atypeFactory.EXCLUSIVITY_BOTTOM)) {
            checker.reportError(valueType, errorKey);
        }
        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey, extraArgs);
    }
}
