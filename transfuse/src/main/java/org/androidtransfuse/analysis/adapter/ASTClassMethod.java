package org.androidtransfuse.analysis.adapter;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Class specific AST Method
 *
 * @author John Ericksen
 */
public class ASTClassMethod implements ASTMethod {

    private final Method method;
    private final ImmutableList<ASTParameter> parameters;
    private final ASTType returnType;
    private final ASTAccessModifier modifier;
    private final ImmutableCollection<ASTAnnotation> annotations;
    private final ImmutableList<ASTType> throwTypes;

    public ASTClassMethod(Method method,
                          ASTType returnType,
                          ImmutableList<ASTParameter> parameters,
                          ASTAccessModifier modifier,
                          ImmutableCollection<ASTAnnotation> annotations,
                          ImmutableList<ASTType> throwTypes) {
        this.method = method;
        this.parameters = parameters;
        this.returnType = returnType;
        this.modifier = modifier;
        this.annotations = annotations;
        this.throwTypes = throwTypes;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return method.getAnnotation(annotation);
    }

    @Override
    public boolean isAnnotated(Class<? extends Annotation> annotation) {
        return method.isAnnotationPresent(annotation);
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public List<ASTParameter> getParameters() {
        return parameters;
    }

    @Override
    public ASTType getReturnType() {
        return returnType;
    }

    @Override
    public Collection<ASTAnnotation> getAnnotations() {
        return annotations;
    }

    public ASTAccessModifier getAccessModifier() {
        return modifier;
    }

    @Override
    public List<ASTType> getThrowsTypes() {
        return throwTypes;
    }

    @Override
    public ASTAnnotation getASTAnnotation(Class annotation) {
        return ASTUtils.getInstance().getAnnotation(annotation, getAnnotations());
    }
}
