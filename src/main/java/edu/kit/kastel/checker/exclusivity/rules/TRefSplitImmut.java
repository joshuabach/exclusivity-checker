package edu.kit.kastel.checker.exclusivity.rules;

import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;

public class TRefSplitImmut extends ExclMutAssignmentRule {
    public TRefSplitImmut(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    protected AnnotationMirror getNewLhsTypeAnnotation() {
        return factory.IMMUTABLE;
    }

    @Override
    protected AnnotationMirror getNewRhsTypeAnnotation() {
        return factory.IMMUTABLE;
    }

    @Override
    public String getName() {
        return "T-Ref-Split-Immut";
    }
}
