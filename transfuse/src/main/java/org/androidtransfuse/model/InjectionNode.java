package org.androidtransfuse.model;

import org.androidtransfuse.analysis.adapter.ASTType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class InjectionNode {

    private final ASTType usageType;
    private final ASTType astType;
    private final Map<Class, Object> aspects = new HashMap<Class, Object>();

    public InjectionNode(ASTType astType) {
        this(astType, astType);
    }

    public InjectionNode(ASTType usageType, ASTType astType) {
        this.astType = astType;
        this.usageType = usageType;
    }

    public ASTType getUsageType() {
        return usageType;
    }

    public String getClassName() {
        return astType.getName();
    }

    public ASTType getASTType() {
        return astType;
    }

    public <T> T getAspect(Class<T> clazz){
        return (T) aspects.get(clazz);
    }

    public void addAspect(Object object) {
        aspects.put(object.getClass(), object);
    }

    public <T> void addAspect(Class<T> clazz, T object) {
        aspects.put(clazz, object);
    }

    public boolean containsAspect(Class<?> clazz) {
        return aspects.containsKey(clazz);
    }

    public Map<Class, Object> getAspects() {
        return aspects;
    }
}
