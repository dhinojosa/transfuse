package org.androidtransfuse.analysis.adapter;

import com.google.common.collect.ImmutableCollection;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class specific AST Type
 *
 * @author John Ericksen
 */
public class ASTClassType implements ASTType {

    private final Class<?> clazz;
    private final ImmutableCollection<ASTAnnotation> annotationList;
    private final ImmutableCollection<ASTMethod> methods;
    private final ImmutableCollection<ASTConstructor> constructors;
    private final ImmutableCollection<ASTField> fields;
    private final ASTType superClass;
    private final ImmutableCollection<ASTType> interfaces;

    public ASTClassType(Class<?> clazz,
                        ImmutableCollection<ASTAnnotation> annotationList,
                        ImmutableCollection<ASTConstructor> constructors,
                        ImmutableCollection<ASTMethod> methods,
                        ImmutableCollection<ASTField> fields,
                        ASTType superClass,
                        ImmutableCollection<ASTType> interfaces) {
        this.clazz = clazz;
        this.annotationList = annotationList;
        this.constructors = constructors;
        this.methods = methods;
        this.fields = fields;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return clazz.getAnnotation(annotation);
    }

    @Override
    public boolean isAnnotated(Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    @Override
    public Collection<ASTMethod> getMethods() {
        return methods;
    }

    @Override
    public Collection<ASTField> getFields() {
        return fields;
    }

    @Override
    public Collection<ASTConstructor> getConstructors() {
        return constructors;
    }

    @Override
    public String getName() {
        return clazz.getCanonicalName();
    }

    @Override
    public boolean isConcreteClass() {
        return !clazz.isInterface() && !clazz.isSynthetic();
    }

    @Override
    public Collection<ASTAnnotation> getAnnotations() {
        return annotationList;
    }

    @Override
    public ASTType getSuperClass() {
        return superClass;
    }

    @Override
    public Collection<ASTType> getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean isArray() {
        return clazz.isArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ASTType)) {
            return false;
        }

        ASTType that = (ASTType) o;

        return new EqualsBuilder().append(getName(), that.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).hashCode();
    }

    @Override
    public List<ASTType> getGenericParameters() {
        return Collections.emptyList();
    }

    @Override
    public boolean inheritsFrom(ASTType type) {
        return ASTUtils.getInstance().inherits(this, type, true, true);
    }

    @Override
    public boolean extendsFrom(ASTType type) {
        return ASTUtils.getInstance().inherits(this, type, false, true);
    }

    @Override
    public boolean implementsFrom(ASTType type) {
        return ASTUtils.getInstance().inherits(this, type, true, false);
    }

    @Override
    public ASTAnnotation getASTAnnotation(Class annotation) {
        return ASTUtils.getInstance().getAnnotation(annotation, getAnnotations());
    }

    @Override
    public String toString(){
        return getName();
    }
}
