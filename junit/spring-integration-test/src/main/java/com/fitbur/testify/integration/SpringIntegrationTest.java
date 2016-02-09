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
import com.fitbur.testify.TestNeedContainers;
import com.fitbur.testify.TestNeeds;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.di.spring.SpringServicePostProcessor;
import com.fitbur.testify.junit.core.JUnitTestNotifier;
import com.fitbur.testify.need.NeedProvider;
import com.fitbur.testify.need.NeedScope;
import com.fitbur.testify.need.docker.DockerContainerNeedProvider;
import java.util.List;
import java.util.Map;
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
    private SpringServiceLocator serviceLocator;
    private TestNeedContainers methodTestNeedContainers;
    private TestNeedContainers classTestNeedContainers;
    private TestNeeds methodTestNeeds;
    private TestNeeds classTestNeeds;

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
            Object testInstance = createTest();
            testContext.setTestInstance(testInstance);

            classTestNeeds = new TestNeeds(testContext,
                    javaClass.getSimpleName(),
                    NeedScope.CLASS);
            classTestNeeds.init();

            classTestNeedContainers = new TestNeedContainers(testContext,
                    javaClass.getSimpleName(),
                    NeedScope.CLASS,
                    DockerContainerNeedProvider.class);

            classTestNeedContainers.init();

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
            testNotifier.addFailure(e);
            testNotifier.pleaseStop();
        } catch (Throwable e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.addFailure(e);
        } finally {
            if (SLF4JBridgeHandler.isInstalled()) {
                SLF4JBridgeHandler.uninstall();
            }

            if (javaClass.getAnnotation(Ignore.class) == null) {
                classTestNeeds.destory();
                classTestNeedContainers.destory();
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

        serviceLocator = new SpringServiceLocator(appContext, serviceAnnotations);

        methodTestNeeds = new TestNeeds(testContext,
                method.getName(),
                NeedScope.METHOD);
        methodTestNeeds.init();

        methodTestNeedContainers = new TestNeedContainers(testContext,
                method.getName(),
                NeedScope.METHOD,
                DockerContainerNeedProvider.class);
        methodTestNeedContainers.init();

        SpringServicePostProcessor postProcessor = new SpringServicePostProcessor(
                serviceLocator,
                methodTestNeeds,
                methodTestNeedContainers,
                classTestNeeds,
                classTestNeedContainers);

        appContext.addBeanFactoryPostProcessor(postProcessor);

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

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        super.runChild(method, notifier);
        if (method.getAnnotation(Ignore.class) == null) {
            serviceLocator.destroy();
            methodTestNeeds.destory();
            methodTestNeedContainers.destory();
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
