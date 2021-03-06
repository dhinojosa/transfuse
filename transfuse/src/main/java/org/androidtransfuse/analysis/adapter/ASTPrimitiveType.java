package org.androidtransfuse.analysis.adapter;

import com.google.common.collect.ImmutableMap;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Element specific primitive AST type
 *
 * @author John Ericksen
 */
public enum ASTPrimitiveType implements ASTType {

    BOOLEAN("boolean", Boolean.class),
    BYTE("byte", Byte.class),
    SHORT("short", Short.class),
    CHAR("char", Character.class),
    INT("int", Integer.class),
    FLOAT("float", Float.class),
    LONG("long", Long.class),
    DOUBLE("double", Double.class);

    private static final ImmutableMap<String, ASTPrimitiveType> AUTOBOX_TYPE_MAP;

    private final Class clazz;
    private final String label;

    static {
        ImmutableMap.Builder<String, ASTPrimitiveType> autoboxTypeMapBuilder = ImmutableMap.builder();
        for (ASTPrimitiveType astPrimitive : ASTPrimitiveType.values()) {
            autoboxTypeMapBuilder.put(astPrimitive.getObjectClass().getName(), astPrimitive);
        }

        AUTOBOX_TYPE_MAP = autoboxTypeMapBuilder.build();
    }

    private ASTPrimitiveType(String label, Class clazz) {
        this.label = label;
        this.clazz = clazz;
    }

    @Override
    public boolean isAnnotated(Class<? extends Annotation> annotation) {
        return false;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return null;
    }

    @Override
    public Collection<ASTMethod> getMethods() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ASTField> getFields() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ASTConstructor> getConstructors() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return label;
    }

    public Class getObjectClass() {
        return clazz;
    }

    @Override
    public boolean isConcreteClass() {
        return false;
    }

    @Override
    public Collection<ASTAnnotation> getAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ASTType> getInterfaces() {
        return Collections.emptySet();
    }

    @Override
    public ASTType getSuperClass() {
        return null;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public List<ASTType> getGenericParameters() {
        return Collections.emptyList();
    }

    public static ASTPrimitiveType getAutoboxType(String name) {
        return AUTOBOX_TYPE_MAP.get(name);
    }

    @Override
    public boolean inheritsFrom(ASTType type) {
        return type == this;
    }

    @Override
    public boolean extendsFrom(ASTType type) {
        return type == this;
    }

    @Override
    public boolean implementsFrom(ASTType type) {
        return type == this;
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
