/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fitbur.testify.integration;

import com.fitbur.asm.ClassReader;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestNeedDescriptor;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.junit.core.JUnitTestNotifier;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedContext;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedInstance;
import com.fitbur.testify.need.NeedProvider;
import com.fitbur.testify.need.NeedScope;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toSet;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * A JUnit spring integration test runner. This class is the main entry point
 * for running a unit test using {@link org.junit.runner.RunWith} and provides
 * means of creating your class under test and substituting mock instances or
 * real instances of its collaborators in a spring application context.
 *
 * @author saden
 */
public class SpringIntegrationTest extends BlockJUnit4ClassRunner {

    static final Logger LOGGER = getLogger("testify");
    protected Map<Class, TestContext> testClassContexts = new ConcurrentHashMap<>();
    public Map<Class, SpringServiceLocator> applicationContexts = new ConcurrentHashMap<>();
    public Map<Class, List<NeedProvider>> needProvider = new ConcurrentHashMap<>();
    private ServiceAnnotations serviceAnnotations;
    private Set<NeedContext> needContexts;
    private SpringServiceLocator serviceLocator;

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public SpringIntegrationTest(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public TestContext getTestContext(Class<?> javaClass) {
        String name = javaClass.getSimpleName();

        TestContext testContext = testClassContexts.computeIfAbsent(javaClass, p -> {
            try {
                TestContext context = new TestContext(name, javaClass, LOGGER);

                ClassReader testReader = new ClassReader(javaClass.getName());
                testReader.accept(new TestClassAnalyzer(context), ClassReader.SKIP_DEBUG);

                CutDescriptor cutDescriptor = context.getCutDescriptor();

                if (cutDescriptor != null) {
                    ClassReader cutReader = new ClassReader(context.getCutDescriptor().getField().getType().getName());
                    cutReader.accept(new CutClassAnalyzer(context), ClassReader.SKIP_DEBUG);
                }

                return context;
            } catch (Exception e) {
                checkState(false, "Analysis of test class '%s' failed.\n'%s'", name, e.getMessage());
                //not reachable
                throw new IllegalStateException(e);
            }
        });

        return testContext;
    }

    @Override
    public void run(RunNotifier notifier) {
        //register slf4j bridge
        if (!SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }

        Description description = getDescription();
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        TestContext testContext = getTestContext(javaClass);

        JUnitTestNotifier testNotifier
                = new JUnitTestNotifier(notifier, description, LOGGER, testContext);

        this.serviceAnnotations = new ServiceAnnotations();
        serviceAnnotations.addInjectors(Inject.class, Autowired.class, Real.class);
        serviceAnnotations.addNamedQualifier(Named.class, Qualifier.class);
        serviceAnnotations.addCustomQualfier(javax.inject.Qualifier.class, Qualifier.class);

        try {
            Statement statement = classBlock(testNotifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            LOGGER.error("{}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.pleaseStop();
        } catch (Throwable e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.addFailure(e);
        } finally {
            if (SLF4JBridgeHandler.isInstalled()) {
                SLF4JBridgeHandler.uninstall();
            }
        }
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();

        TestContext testContext = getTestContext(javaClass);
        IntegrationTestVerifier verifier = new IntegrationTestVerifier(testContext, LOGGER);
        verifier.dependency();
        verifier.configuration();
        initNeed(testContext, null, NeedScope.CLASS);
        return super.classBlock(notifier);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();

        Object testInstance;

        try {
            testInstance = createTest();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        TestContext testContext = getTestContext(javaClass);
        testContext.setTestInstance(testInstance);
        String testClassName = testContext.getTestClassName();

        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.setId(testClassName);
        appContext.setAllowBeanDefinitionOverriding(true);
        appContext.setAllowCircularReferences(false);
        appContext.register(SpringIntegrationPostProcessor.class);

        serviceLocator = new SpringServiceLocator(appContext, serviceAnnotations);
        initNeed(testContext, method.getName(), NeedScope.METHOD);

        IntegrationTestReifier reifier
                = new IntegrationTestReifier(testContext, serviceLocator, testInstance);
        IntegrationTestCreator creator
                = new IntegrationTestCreator(testContext, reifier, serviceLocator);

        if (testContext.getCutDescriptor() != null) {
            creator.cut();
        }

        Set<FieldDescriptor> real = testContext.getFieldDescriptors()
                .values()
                .parallelStream()
                .filter(p -> !p.getInstance().isPresent())
                .filter(p -> p.hasAnnotations(serviceAnnotations.getInjectors()))
                .collect(toSet());

        creator.real(real);

        IntegrationTestVerifier verifier = new IntegrationTestVerifier(testContext, LOGGER);
        verifier.wiring();

        Statement statement = methodInvoker(method, testInstance);
        statement = possiblyExpectingExceptions(method, testInstance, statement);
        statement = withBefores(method, testInstance, statement);
        statement = withAfters(method, testInstance, statement);
        statement = withRules(method, testInstance, statement);

        return statement;
    }

    private void initNeed(TestContext testContext, String methodName, NeedScope scope) {
        needContexts = testContext.getAnnotations(Need.class)
                .parallelStream()
                .filter(p -> p.scope() == scope)
                .map(p -> {
                    Class<? extends NeedProvider> providerClass = p.value();
                    try {
                        NeedProvider provider = providerClass.newInstance();
                        NeedDescriptor descriptor = new TestNeedDescriptor(p, testContext, methodName, serviceLocator);
                        Object context = provider.configuration(descriptor);
                        Optional<Method> configMethod = testContext.getConfigMethod(context.getClass())
                                .map(m -> m.getMethod());

                        if (configMethod.isPresent()) {
                            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                                Method m = configMethod.get();
                                try {
                                    m.setAccessible(true);
                                    m.invoke(descriptor.getTestInstance(), context);
                                } catch (Exception e) {
                                    checkState(false, "Call to config method '%s' in test class '%s' failed.",
                                            m.getName(), descriptor.getTestClassName());
                                }

                                return null;
                            });
                        }

                        Map<String, NeedInstance> instances = provider.init(descriptor, context);
                        NeedContext needContext
                                = new NeedContext(provider, descriptor, instances, serviceLocator, context);

                        if (serviceLocator != null) {
                            serviceLocator.addConstant(context.getClass().getSimpleName(), context);
                            serviceLocator.addConstant(needContext.getClass().getSimpleName(), needContext);
                            instances.forEach((k, v) -> serviceLocator.addConstant(k, v));
                        }

                        return needContext;
                    } catch (InstantiationException | IllegalAccessException ex) {
                        checkState(false, "Need provider '%s' could not be instanticated.",
                                providerClass.getSimpleName());
                        return null;
                    }
                }).collect(toSet());
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        super.runChild(method, notifier);

        if (method.getAnnotation(Ignore.class) == null) {
            needContexts.parallelStream().forEach(p -> {
                p.getProvider().destroy(p.getDescriptor(), p.getContext());
            });
            serviceLocator.destroy();
        }
    }

    private Statement withRules(FrameworkMethod method, Object target,
            Statement statement) {
        List<TestRule> testRules = getTestRules(target);
        Statement result = statement;
        result = withMethodRules(method, testRules, target, result);
        result = withTestRules(method, testRules, result);

        return result;
    }

    private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules,
            Object target, Statement result) {
        for (MethodRule each : getMethodRules(target)) {
            if (!testRules.contains(each)) {
                result = each.apply(result, method, target);
            }
        }
        return result;
    }

    private List<MethodRule> getMethodRules(Object target) {
        return rules(target);
    }

    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules,
            Statement statement) {
        return testRules.isEmpty() ? statement
                : new RunRules(statement, testRules, describeChild(method));
    }

}
