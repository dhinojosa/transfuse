package org.androidtransfuse.gen.variableDecorator;

/**
 * @author John Ericksen
 */
public abstract class VariableExpressionBuilderDecorator implements VariableExpressionBuilder {

    private final VariableExpressionBuilder decorated;

    public VariableExpressionBuilderDecorator(VariableExpressionBuilder decorated) {
        this.decorated = decorated;
    }

    public VariableExpressionBuilder getDecorated() {
        return decorated;
    }
}
