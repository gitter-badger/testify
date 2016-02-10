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
package com.fitbur.testify.junit.system;

import com.fitbur.asm.ClassReader;
import com.fitbur.bytebuddy.ByteBuddy;
import com.fitbur.bytebuddy.description.type.TypeDescription;
import com.fitbur.bytebuddy.dynamic.ClassFileLocator;
import com.fitbur.bytebuddy.dynamic.DynamicType;
import com.fitbur.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import static com.fitbur.bytebuddy.implementation.MethodDelegation.to;
import com.fitbur.bytebuddy.implementation.bind.MethodNameEqualityResolver;
import com.fitbur.bytebuddy.implementation.bind.annotation.BindingPriority;
import static com.fitbur.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static com.fitbur.bytebuddy.matcher.ElementMatchers.not;
import com.fitbur.bytebuddy.pool.TypePool;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.guava.common.collect.Lists;
import com.fitbur.testify.App;
import com.fitbur.testify.Module;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.client.ClientInstance;
import com.fitbur.testify.client.ClientProvider;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.junit.core.JUnitTestNotifier;
import com.fitbur.testify.junit.system.internal.SpringBootClientDescriptor;
import com.fitbur.testify.junit.system.internal.SpringBootDescriptor;
import com.fitbur.testify.junit.system.internal.SpringBootInterceptor;
import com.fitbur.testify.junit.system.internal.SpringBootServerInstance;
import com.fitbur.testify.system.SystemTestCreator;
import com.fitbur.testify.system.SystemTestReifier;
import com.fitbur.testify.system.SystemTestVerifier;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toSet;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
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
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;

/**
 * A JUnit Spring Boot system test runner. This class is the main entry point
 * for running a Spring Boot system tests using
 * {@link org.junit.runner.RunWith}. It provides means of creating your class
 * under test, faking certain collaborators or using real collaborators in the
 * Spring application context.
 *
 * @author saden
 */
public class SpringBootSystemTest extends BlockJUnit4ClassRunner {

    static final Logger LOGGER = getLogger("testify");
    static final ByteBuddy BYTE_BUDDY = new ByteBuddy();

    static final Map<Class, TestContext> TEST_CONTEXTS = new ConcurrentHashMap<>();
    static final Map<SpringApplication, TestContext> APPLICATION_TEST_CONTEXTS;
    static final Map<AnnotationConfigEmbeddedWebApplicationContext, SpringBootDescriptor> CONTEXT_DESCRIPTORS;
    static final Map<SpringApplication, SpringBootDescriptor> APPLICATION_DESCRIPTORS;
    static final Map<String, DynamicType.Loaded<?>> REBASED_CLASSES;

    static {
        REBASED_CLASSES = new ConcurrentHashMap<>();
        APPLICATION_TEST_CONTEXTS = new ConcurrentHashMap<>();
        CONTEXT_DESCRIPTORS = new ConcurrentHashMap<>();
        APPLICATION_DESCRIPTORS = new ConcurrentHashMap<>();
    }

    private SpringApplication application;
    private ClientInstance clientInstance;
    private SpringBootServerInstance serverInstance;

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public SpringBootSystemTest(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public TestContext getTestContext(Class<?> javaClass) {
        String name = javaClass.getSimpleName();
        TestContext testContext = TEST_CONTEXTS.computeIfAbsent(javaClass, p -> {
            try {
                TestContext context = new TestContext(name, javaClass, LOGGER);

                ClassReader testReader = new ClassReader(javaClass.getName());
                testReader.accept(new TestClassAnalyzer(context), ClassReader.SKIP_DEBUG);

                CutDescriptor cutDescriptor = context.getCutDescriptor();

                if (cutDescriptor != null) {
                    String typeName = context.getCutDescriptor().getField().getType().getName();
                    ClassReader cutReader = new ClassReader(typeName);
                    cutReader.accept(new CutClassAnalyzer(context), ClassReader.SKIP_DEBUG);
                }

                return context;
            } catch (Exception e) {
                checkState(false, "Analysis of test class '%s' failed.\n'%s'",
                        name, e.getMessage());
                //not reachable
                throw new IllegalStateException(e);
            }
        });

        final ClassFileLocator locator = ClassFileLocator.ForClassLoader.ofClassPath();
        final TypePool typePool = TypePool.Default.ofClassPath();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        SpringBootInterceptor interceptor = new SpringBootInterceptor(APPLICATION_TEST_CONTEXTS, APPLICATION_DESCRIPTORS, CONTEXT_DESCRIPTORS);

        String className = "org.springframework.boot.SpringApplication";

        REBASED_CLASSES.computeIfAbsent(className, p -> {
            TypeDescription typeDescription = typePool.describe(p).resolve();

            return BYTE_BUDDY
                    .rebase(typeDescription, locator)
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(
                            to(interceptor)
                            .filter(not(isDeclaredBy(Object.class)))
                            .defineAmbiguityResolver(
                                    MethodNameEqualityResolver.INSTANCE,
                                    BindingPriority.Resolver.INSTANCE)
                    )
                    .make()
                    .load(classLoader, ClassLoadingStrategy.Default.INJECTION);
        });

        className = "org.springframework.boot.context.embedded.EmbeddedWebApplicationContext";

        REBASED_CLASSES.computeIfAbsent(className, p -> {
            TypeDescription typeDescription = typePool.describe(p).resolve();

            return BYTE_BUDDY
                    .rebase(typeDescription, locator)
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(to(interceptor)
                            .filter(not(isDeclaredBy(Object.class)))
                            .defineAmbiguityResolver(
                                    MethodNameEqualityResolver.INSTANCE,
                                    BindingPriority.Resolver.INSTANCE)
                    )
                    .make()
                    .load(classLoader, ClassLoadingStrategy.Default.INJECTION);
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

        try {
            Object testInstance = createTest();
            testContext.setTestInstance(testInstance);

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
//                classTestNeeds.destory();
//                classTestNeedContainers.destory();
            }
        }
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();

        TestContext testContext = getTestContext(javaClass);
        SystemTestVerifier verifier = new SystemTestVerifier(testContext, LOGGER);
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

        Optional<App> appOptional = testContext.getAnnotation(App.class);
        checkState(appOptional.isPresent(),
                "Test class '%s' is not annotated with @App annotation.",
                testClass.getName());

        App app = appOptional.get();
        Set<Class<?>> modules = testContext.getAnnotations(Module.class)
                .parallelStream()
                .map(p -> p.value())
                .collect(toSet());

        Set<Object> sources = new LinkedHashSet<>();
        sources.add(app.value());
        sources.addAll(modules);

        application = new SpringApplicationBuilder(sources.toArray())
                .bannerMode(Banner.Mode.OFF)
                .build();

        APPLICATION_TEST_CONTEXTS.computeIfAbsent(application, p -> testContext);

        application.run();

        SpringBootDescriptor descriptor = APPLICATION_DESCRIPTORS.get(application);

        ServiceLocator serviceLocator = descriptor.getServiceLocator();

        try {
            URI baseURI = new URI("http",
                    null,
                    "0.0.0.0",
                    descriptor.getServletContainer().getPort(),
                    descriptor.getServletContext().getContextPath(),
                    null,
                    null);

            SpringBootClientDescriptor clientDescriptor = new SpringBootClientDescriptor(app, testContext, baseURI);
            ServiceLoader<ClientProvider> clientProviderLoader = ServiceLoader.load(ClientProvider.class);
            ArrayList<ClientProvider> clientProviders = Lists.newArrayList(clientProviderLoader);

            checkState(!clientProviders.isEmpty(),
                    "ClientInstance provider not found in the classpath");
            checkState(clientProviders.size() == 1,
                    "Multiple ClientInstance provider found in the classpath. "
                    + "Please insure there is only one ClientInstance provider in the classpath.");

            ClientProvider clientProvider = clientProviders.get(0);
            Object context = clientProvider.configuration(clientDescriptor);

            testContext.getConfigMethod(context.getClass()).map(m -> m.getMethod()).ifPresent(m -> {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    try {
                        m.setAccessible(true);
                        m.invoke(testInstance, context);
                    } catch (Exception e) {
                        checkState(false, "Call to config method '%s' in test class '%s' failed due to: ",
                                m.getName(), clientDescriptor.getTestClassName(), e.getMessage());
                    }

                    return null;
                });
            });

            serverInstance = new SpringBootServerInstance(descriptor.getServletContainer(), baseURI);
            clientInstance = clientProvider.init(clientDescriptor, context);

            serviceLocator.addConstant(descriptor.getClass().getSimpleName(), descriptor);
            serviceLocator.addConstant(serverInstance.getClass().getSimpleName(), serverInstance);
            serviceLocator.addConstant(clientInstance.getClass().getSimpleName(), clientInstance);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        SystemTestReifier reifier = new SystemTestReifier(testContext, serviceLocator, testInstance);
        SystemTestCreator creator = new SystemTestCreator(testContext, reifier, serviceLocator);

        if (testContext.getCutDescriptor() != null) {
            creator.cut();
        }

        Set<FieldDescriptor> real = testContext.getFieldDescriptors()
                .values()
                .parallelStream()
                .filter(p -> !p.getInstance().isPresent())
                .filter(p -> p.hasAnnotations(descriptor.getServiceAnnotations().getInjectors()))
                .collect(toSet());

        creator.real(real);

        SystemTestVerifier verifier = new SystemTestVerifier(testContext, LOGGER);
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
            SpringBootDescriptor descriptor = APPLICATION_DESCRIPTORS.get(application);
            clientInstance.close();
            serverInstance.stop();

            descriptor.getTestNeeds().destory();
            descriptor.getTestContainerNeeds().destory();
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
        for (org.junit.rules.MethodRule each : getMethodRules(target)) {
            if (!testRules.contains(each)) {
                result = each.apply(result, method, target);
            }
        }
        return result;
    }

    private List<org.junit.rules.MethodRule> getMethodRules(Object target) {
        return rules(target);
    }

    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules,
            Statement statement) {
        return testRules.isEmpty() ? statement
                : new RunRules(statement, testRules, describeChild(method));
    }

}
