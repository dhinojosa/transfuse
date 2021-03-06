package org.androidtransfuse.analysis.astAnalyzer;

import android.app.Activity;
import android.view.View;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.codemodel.JCodeModel;
import org.androidtransfuse.analysis.AnalysisContext;
import org.androidtransfuse.analysis.Analyzer;
import org.androidtransfuse.analysis.InjectionPointFactory;
import org.androidtransfuse.analysis.TransfuseAnalysisException;
import org.androidtransfuse.analysis.adapter.*;
import org.androidtransfuse.annotations.RegisterListener;
import org.androidtransfuse.gen.componentBuilder.*;
import org.androidtransfuse.gen.variableBuilder.VariableBuilder;
import org.androidtransfuse.gen.variableBuilder.VariableInjectionBuilderFactory;
import org.androidtransfuse.listeners.*;
import org.androidtransfuse.model.InjectionNode;

import javax.inject.Inject;
import java.util.*;

/**
 * @author John Ericksen
 */
public class RegistrationAnalyzer implements ASTAnalysis {

    private final ImmutableMap<ASTType, RegistrationGeneratorFactory> generatorFactories;
    private final ASTClassFactory astClassFactory;
    private final Analyzer analyzer;
    private final JCodeModel codeModel;
    private final VariableInjectionBuilderFactory variableInjectionBuilderFactory;
    private final InjectionPointFactory injectionPointFactory;
    private final ComponentBuilderFactory componentBuilderFactory;

    @Inject
    public RegistrationAnalyzer(Analyzer analyzer,
                                ASTClassFactory astClassFactory,
                                JCodeModel codeModel,
                                VariableInjectionBuilderFactory variableInjectionBuilderFactory,
                                InjectionPointFactory injectionPointFactory,
                                ComponentBuilderFactory componentBuilderFactory) throws NoSuchMethodException {
        this.analyzer = analyzer;
        this.astClassFactory = astClassFactory;
        this.codeModel = codeModel;
        this.variableInjectionBuilderFactory = variableInjectionBuilderFactory;
        this.injectionPointFactory = injectionPointFactory;
        this.componentBuilderFactory = componentBuilderFactory;

        Map<ASTType, String> listenerMethodMapping = new HashMap<ASTType, String>();

        listenerMethodMapping.put(astType(View.OnClickListener.class), "setOnClickListener");
        listenerMethodMapping.put(astType(View.OnLongClickListener.class), "setOnLongClickListener");
        listenerMethodMapping.put(astType(View.OnCreateContextMenuListener.class), "setOnCreateContextMenuListener");
        listenerMethodMapping.put(astType(View.OnKeyListener.class), "setOnKeyListener");
        listenerMethodMapping.put(astType(View.OnTouchListener.class), "setOnTouchListener");
        listenerMethodMapping.put(astType(View.OnFocusChangeListener.class), "setOnFocusChangeListener");

        Set<ASTType> callthroughMapping = new HashSet<ASTType>();

        callthroughMapping.add(astType(ActivityOnKeyDownListener.class));
        callthroughMapping.add(astType(ActivityOnKeyLongPressListener.class));
        callthroughMapping.add(astType(ActivityOnKeyUpListener.class));
        callthroughMapping.add(astType(ActivityOnKeyMultipleListener.class));
        callthroughMapping.add(astType(ActivityOnTouchEventListener.class));
        callthroughMapping.add(astType(ActivityOnTrackballEventListener.class));
        callthroughMapping.add(astType(ActivityMenuComponent.class));

        ImmutableMap.Builder<ASTType, RegistrationGeneratorFactory> generatorbuilder = ImmutableMap.builder();

        generatorbuilder.putAll(Maps.transformValues(listenerMethodMapping, new ListenerMethodMappingTransformer()));
        generatorbuilder.putAll(Maps.transformValues(Maps.uniqueIndex(callthroughMapping, Functions.<ASTType>identity()),
                new CallthroughMethodMappingFunction()));

        generatorFactories = generatorbuilder.build();
    }

    private ASTType astType(Class<?> clazz) {
        return astClassFactory.buildASTClassType(clazz);
    }

    private final class ListenerMethodMappingTransformer implements Function<String, RegistrationGeneratorFactory>{
        @Override
        public RegistrationGeneratorFactory apply(String value) {
            return new ViewRegistrationGeneratorFactory(value) ;
        }
    }

    private final class CallthroughMethodMappingFunction implements Function<ASTType, RegistrationGeneratorFactory>{
        @Override
        public RegistrationGeneratorFactory apply(ASTType astType) {
            return buildActivityDelegateRegistrationGeneratorFactory(astType);
        }
    }

    private interface RegistrationGeneratorFactory{

        RegistrationGenerator buildRegistrationGenerator(InjectionNode injectionNode, ASTBase astBase, ASTAnnotation registerAnnotation, AnalysisContext context);
    }

    private final class ViewRegistrationGeneratorFactory implements RegistrationGeneratorFactory {

        private String listenerMethod;

        private ViewRegistrationGeneratorFactory(String listenerMethod) {
            this.listenerMethod = listenerMethod;
        }

        @Override
        public RegistrationGenerator buildRegistrationGenerator(InjectionNode injectionNode, ASTBase astBase, ASTAnnotation registerAnnotation, AnalysisContext context) {
            Integer viewId = registerAnnotation.getProperty("value", Integer.class);
            String viewTag = registerAnnotation.getProperty("tag", String.class);

            InjectionNode viewInjectionNode = buildViewInjectionNode(viewId, viewTag, context);

            ViewRegistrationInvocationBuilder invocationBuilder;
            if(astBase instanceof ASTType){
                invocationBuilder = new ViewTypeRegistrationInvocationBuilderImpl();
            }
            else if(astBase instanceof ASTMethod){
                invocationBuilder = componentBuilderFactory.buildViewMethodRegistrationInvocationBuilder((ASTMethod) astBase);
            }
            else if(astBase instanceof ASTField){
                invocationBuilder = componentBuilderFactory.buildViewFieldRegistrationInvocationBuilder((ASTField) astBase);
            }
            else{
                throw new TransfuseAnalysisException("ASTBase type not mapped");
            }

            return componentBuilderFactory.buildViewRegistrationGenerator(viewInjectionNode, listenerMethod, injectionNode, invocationBuilder);
        }
    }

    private final class ActivityDelegateRegistrationGeneratorFactory implements RegistrationGeneratorFactory {

        private ImmutableList<ASTMethod> methods;

        private ActivityDelegateRegistrationGeneratorFactory(ImmutableList<ASTMethod> methods) {
            this.methods = methods;
        }

        @Override
        public RegistrationGenerator buildRegistrationGenerator(InjectionNode injectionNode, ASTBase astBase, ASTAnnotation registerAnnotation, AnalysisContext context) {

            ActivityDelegateASTReference delegateASTReference;

            if(astBase instanceof ASTType){
                delegateASTReference = componentBuilderFactory.buildActivityTypeDelegateASTReference();
            }
            else if(astBase instanceof ASTMethod){
                delegateASTReference = componentBuilderFactory.buildActivityMethodDelegateASTReference((ASTMethod) astBase);
            }
            else if(astBase instanceof ASTField){
                delegateASTReference = componentBuilderFactory.buildActivityFieldDelegateASTReference((ASTField) astBase);
            }
            else{
                throw new TransfuseAnalysisException("ASTBase type not mapped");
            }

            //set injection node to field
            if(!injectionNode.containsAspect(ASTInjectionAspect.class)){
                injectionNode.addAspect(new ASTInjectionAspect());
            }
            injectionNode.getAspect(ASTInjectionAspect.class).setAssignmentType(ASTInjectionAspect.InjectionAssignmentType.FIELD);

            return componentBuilderFactory.buildActivityRegistrationGenerator(delegateASTReference, methods);
        }
    }

    private RegistrationGeneratorFactory buildActivityDelegateRegistrationGeneratorFactory(ASTType listenerInterface) {

        ImmutableList.Builder<ASTMethod> delegateMethods = ImmutableList.builder();
        for (ASTMethod method : listenerInterface.getMethods()) {
            if(method.isAnnotated(Listener.class)){
                delegateMethods.add(method);
            }
        }

        return new ActivityDelegateRegistrationGeneratorFactory(delegateMethods.build());
    }

    @Override
    public void analyzeType(InjectionNode injectionNode, final ASTType astType, AnalysisContext context) {
        analyze(astType, astType, injectionNode, context);
    }

    @Override
    public void analyzeMethod(InjectionNode injectionNode, ASTType concreteType, final ASTMethod astMethod, AnalysisContext context) {
        analyze(astMethod, astMethod.getReturnType(), injectionNode, context);
    }

    @Override
    public void analyzeField(InjectionNode injectionNode, ASTType concreteType, final ASTField astField, AnalysisContext context) {
        analyze(astField, astField.getASTType(), injectionNode, context);
    }

    private <T> void analyze(ASTBase astBase, ASTType astType, InjectionNode injectionNode, AnalysisContext context) {
        if (astBase.isAnnotated(RegisterListener.class)) {
            ASTAnnotation registerAnnotation = astBase.getASTAnnotation(RegisterListener.class);

            ASTType[] interfaces = registerAnnotation.getProperty("interfaces", ASTType[].class);

            List<ASTType> interfaceList = new ArrayList<ASTType>();
            if (interfaces != null) {
                interfaceList.addAll(Arrays.asList(interfaces));
            }

            List<RegistrationGenerator> generators = getGeneratorFactories(injectionNode, astBase, astType, interfaceList, registerAnnotation, context);

            if (!generators.isEmpty()) {
                RegistrationAspect registrationAspect = getRegistrationAspect(injectionNode);
                registrationAspect.addRegistrationbuilders(generators);
            }
        }
    }

    private ImmutableList<RegistrationGenerator> getGeneratorFactories(InjectionNode injectionNode, ASTBase astBase, ASTType astType, List<ASTType> interfaceList, ASTAnnotation registerAnnotation, AnalysisContext context) {

        ImmutableList.Builder<RegistrationGenerator> generators = ImmutableList.builder();

        for (Map.Entry<ASTType, RegistrationGeneratorFactory> generatorFactoryEntry : generatorFactories.entrySet()) {
            if ((interfaceList.isEmpty() || interfaceList.contains(generatorFactoryEntry.getKey()))
                    && astType.inheritsFrom(generatorFactoryEntry.getKey())) {
                generators.add(generatorFactoryEntry.getValue().buildRegistrationGenerator(injectionNode, astBase, registerAnnotation, context));
            }
        }

        return generators.build();
    }

    private InjectionNode buildViewInjectionNode(Integer viewId, String viewTag, AnalysisContext context) {

        InjectionNode activityInjectionNode = buildActivityInjectionNode(context);

        ASTType viewAstType = astClassFactory.buildASTClassType(View.class);
        InjectionNode viewInjectionNode = analyzer.analyze(viewAstType, viewAstType, context);

        try {
            viewInjectionNode.addAspect(VariableBuilder.class, variableInjectionBuilderFactory.buildViewVariableBuilder(viewId, viewTag, activityInjectionNode, codeModel.parseType(viewAstType.getName())));

            return viewInjectionNode;

        } catch (ClassNotFoundException e) {
            throw new TransfuseAnalysisException("Unable to parse type " + viewAstType.getName(), e);
        }
    }

    private InjectionNode buildActivityInjectionNode(AnalysisContext context){
        return injectionPointFactory.buildInjectionNode(Activity.class, context);
    }

    private RegistrationAspect getRegistrationAspect(InjectionNode injectionNode) {
        if (!injectionNode.containsAspect(RegistrationAspect.class)) {
            injectionNode.addAspect(new RegistrationAspect());
        }
        return injectionNode.getAspect(RegistrationAspect.class);
    }
}