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
package com.fitbur.testify.system;

import com.fitbur.asm.ClassReader;
import com.fitbur.bytebuddy.ByteBuddy;
import com.fitbur.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import static com.fitbur.bytebuddy.implementation.MethodDelegation.to;
import static com.fitbur.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static com.fitbur.bytebuddy.matcher.ElementMatchers.not;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.guava.common.base.Throwables;
import com.fitbur.guava.common.collect.Lists;
import com.fitbur.testify.App;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestNeedContainers;
import com.fitbur.testify.TestNeeds;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.client.ClientContext;
import com.fitbur.testify.client.ClientInstance;
import com.fitbur.testify.client.ClientProvider;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.junit.core.JUnitTestNotifier;
import com.fitbur.testify.need.NeedScope;
import com.fitbur.testify.server.ServerContext;
import com.fitbur.testify.server.ServerInstance;
import com.fitbur.testify.server.ServerProvider;
import com.fitbur.testify.system.internal.SpringSystemClientDescriptor;
import com.fitbur.testify.system.internal.SpringSystemServerDescriptor;
import com.fitbur.testify.system.internal.SpringSystemServletInterceptor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toSet;
import javax.inject.Inject;
import javax.inject.Named;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.SpringServletContainerInitializer;

/**
 * A JUnit Spring system test runner. This class is the main entry point for
 * running a Spring system tests using {@link org.junit.runner.RunWith}. It
 * provides means of creating your class under test, faking certain
 * collaborators or using real collaborators in the Spring application context.
 *
 * @author saden
 */
public class SpringSystemTest extends BlockJUnit4ClassRunner {

    static final ByteBuddy BYTE_BUDDY = new ByteBuddy();
    static final Logger LOGGER = getLogger("testify");
    static final Map<Class, TestContext> TEST_CONTEXTS = new ConcurrentHashMap<>();
    private ServiceAnnotations serviceAnnotations;
    private ServerContext serverContext;
    private ClientContext clientContext;
    private TestNeeds classTestNeeds;
    private TestNeedContainers classTestNeedContainers;
    private SpringSystemServletInterceptor interceptor;

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public SpringSystemTest(Class<?> testClass) throws InitializationError {
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
                    NeedScope.CLASS);

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

        serverContext = getServerContext(testContext, method.getName());
        clientContext = getClientContext(testContext, serverContext);

        ServiceLocator serviceLocator = serverContext.getLocator();

        SystemTestReifier reifier
                = new SystemTestReifier(testContext, serviceLocator, testInstance);
        SystemTestCreator creator
                = new SystemTestCreator(testContext, reifier, serviceLocator);

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

        SystemTestVerifier verifier = new SystemTestVerifier(testContext, LOGGER);
        verifier.wiring();

        Statement statement = methodInvoker(method, testInstance);
        statement = possiblyExpectingExceptions(method, testInstance, statement);
        statement = withBefores(method, testInstance, statement);
        statement = withAfters(method, testInstance, statement);
        statement = withRules(method, testInstance, statement);

        return statement;
    }

    public ServerContext getServerContext(TestContext testContext, String methodName) {
        return testContext.getAnnotation(App.class).map(p -> {
            Class<?> appType = p.value();
            ServiceLoader<ServerProvider> serviceLoader = ServiceLoader.load(ServerProvider.class);
            ArrayList<ServerProvider> serverProviders = Lists.newArrayList(serviceLoader);

            checkState(!serverProviders.isEmpty(),
                    "ClientInstance provider not found in the classpath");
            checkState(serverProviders.size() == 1,
                    "Multiple ClientInstance provider found in the classpath. "
                    + "Please insure there is only one ClientInstance provider in the classpath.");
            ServerProvider provider = serverProviders.get(0);

            interceptor = new SpringSystemServletInterceptor(testContext,
                    methodName,
                    serviceAnnotations,
                    classTestNeeds,
                    classTestNeedContainers);

            Class<?> proxyAppType = BYTE_BUDDY.subclass(appType)
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(to(interceptor).filter(not(isDeclaredBy(Object.class))))
                    .make()
                    .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();

            Set<Class<?>> handles = new HashSet<>();
            handles.add(proxyAppType);
            SpringServletContainerInitializer initializer = new SpringServletContainerInitializer();

            SpringSystemServerDescriptor descriptor
                    = new SpringSystemServerDescriptor(p,
                            testContext,
                            initializer,
                            handles
                    );

            Object configuration = provider.configuration(descriptor);
            testContext.getConfigMethod(configuration.getClass())
                    .map(m -> m.getMethod())
                    .ifPresent(m -> {
                        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                            try {
                                m.setAccessible(true);
                                m.invoke(descriptor.getTestInstance(), configuration);
                            } catch (Exception e) {
                                checkState(false, "Call to config method '%s' in test class '%s' failed.",
                                        m.getName(), descriptor.getTestClassName());
                                throw Throwables.propagate(e);
                            }

                            return null;
                        });
                    });

            ServerInstance instance = provider.init(descriptor, configuration);
            instance.start();
            SpringServiceLocator serviceLocator = interceptor.getServiceLocator();
            ServerContext context = new ServerContext(provider, descriptor, instance, serviceLocator, configuration);

            serviceLocator.addConstant(context.getClass().getSimpleName(), context);
            serviceLocator.addConstant(instance.getClass().getSimpleName(), instance);

            return context;

        }).get();
    }

    public ClientContext getClientContext(TestContext testContext, ServerContext serverContext) {
        return testContext.getAnnotation(App.class).map(app -> {
            SpringSystemClientDescriptor descriptor
                    = new SpringSystemClientDescriptor(app,
                            testContext,
                            serverContext.getInstance().getURI()
                    );

            ServiceLoader<ClientProvider> clientProviderLoader = ServiceLoader.load(ClientProvider.class);
            ArrayList<ClientProvider> clientProviders = Lists.newArrayList(clientProviderLoader);

            checkState(!clientProviders.isEmpty(),
                    "ClientInstance provider not found in the classpath");
            checkState(clientProviders.size() == 1,
                    "Multiple ClientInstance provider found in the classpath. "
                    + "Please insure there is only one ClientInstance provider in the classpath.");

            ClientProvider clientProvider = clientProviders.get(0);
            Object configuration = clientProvider.configuration(descriptor);

            testContext.getConfigMethod(configuration.getClass()).map(m -> m.getMethod()).ifPresent(m -> {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    try {
                        m.setAccessible(true);
                        m.invoke(testContext.getTestInstance(), configuration);
                    } catch (Exception e) {
                        checkState(false,
                                "Call to config method '%s' in test class '%s' failed due to: ",
                                m.getName(), descriptor.getTestClassName(), e.getMessage());
                    }

                    return null;
                });
            });

            ClientInstance instance = clientProvider.init(descriptor, configuration);
            ServiceLocator serviceLocator = serverContext.getLocator();
            ClientContext context = new ClientContext(clientProvider, descriptor, instance, configuration);

            serviceLocator.addConstant(context.getClass().getSimpleName(), context);
            serviceLocator.addConstant(instance.getClass().getSimpleName(), instance);

            return context;

        }).get();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        super.runChild(method, notifier);
        if (method.getAnnotation(Ignore.class) == null) {
            clientContext.getInstance().close();
            serverContext.getInstance().stop();
            interceptor.getMethodTestNeeds().destory();
            interceptor.getMethodTestNeedContainers().destory();
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
