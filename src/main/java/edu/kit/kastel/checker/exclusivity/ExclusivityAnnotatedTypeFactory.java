package edu.kit.kastel.checker.exclusivity;

import edu.kit.kastel.checker.exclusivity.qual.ExclMut;
import edu.kit.kastel.checker.exclusivity.qual.Immutable;
import edu.kit.kastel.checker.exclusivity.qual.Restricted;
import org.checkerframework.common.aliasing.AliasingAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.NoElementQualifierHierarchy;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.util.defaults.QualifierDefaults;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Collection;

public class ExclusivityAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    public ExclusivityAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        if (this.getClass() == ExclusivityAnnotatedTypeFactory.class) {
            this.postInit();
        }
    }

    @Override
    protected QualifierHierarchy createQualifierHierarchy() {
        return new ExclusivityQualifierHierarchy(this.getSupportedTypeQualifiers(), elements);
    }

    protected class ExclusivityQualifierHierarchy extends NoElementQualifierHierarchy {
        public ExclusivityQualifierHierarchy(Collection<Class<? extends Annotation>> qualifierClasses, Elements elements) {
            super(qualifierClasses, elements);
        }

        public boolean isCopyable(AnnotationMirror annotationMirror) {
            return !isSubtype(
                    getAnnotationByClass(qualifiers, ExclMut.class),
                    annotationMirror
            );
        }

        public boolean isMutable(AnnotationMirror annotationMirror) {
            return !isSubtype(
                    getAnnotationByClass(qualifiers, Immutable.class),
                    annotationMirror
            );
        }

        public boolean mayHoldProperty(AnnotationMirror annotationMirror) {
            return isSubtype(
                    getAnnotationByClass(qualifiers, Restricted.class),
                    annotationMirror
            );
        }
    }
}
