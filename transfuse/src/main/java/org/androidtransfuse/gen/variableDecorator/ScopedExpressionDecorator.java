package org.androidtransfuse.gen.variableDecorator;

import com.google.inject.assistedinject.Assisted;
import org.androidtransfuse.analysis.astAnalyzer.ScopeAspect;
import org.androidtransfuse.gen.InjectionBuilderContext;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.TypedExpression;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
public class ScopedExpressionDecorator extends VariableExpressionBuilderDecorator {

    @Inject
    public ScopedExpressionDecorator(@Assisted VariableExpressionBuilder decorator) {
        super(decorator);
    }

    @Override
    public TypedExpression buildVariableExpression(InjectionBuilderContext injectionBuilderContext, InjectionNode injectionNode) {
        if (injectionNode.containsAspect(ScopeAspect.class)) {
            ScopeAspect scopeAspect = injectionNode.getAspect(ScopeAspect.class);
            return scopeAspect.getScopeBuilder().buildVariable(injectionBuilderContext, injectionNode);
        } else {
            return getDecorated().buildVariableExpression(injectionBuilderContext, injectionNode);
        }
    }
}
