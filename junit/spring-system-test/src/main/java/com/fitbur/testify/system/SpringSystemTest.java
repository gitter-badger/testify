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
import com.fitbur.testify.App;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.client.ClientContext;
import com.fitbur.testify.client.ClientInstance;
import com.fitbur.testify.client.ClientProvider;
import com.fitbur.testify.client.jersey.JerseyClientProvider;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.junit.core.JUnitTestNotifier;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedContext;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedProvider;
import com.fitbur.testify.server.ServerContext;
import com.fitbur.testify.server.ServerInstance;
import com.fitbur.testify.server.ServerProvider;
import com.fitbur.testify.server.undertow.UndertowServerProvider;
import com.fitbur.testify.system.interceptor.AnnotationInterceptor;
import com.fitbur.testify.system.interceptor.InterceptorContext;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
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
 * A JUnit spring integration test runner. This class is the main entry point
 * for running a unit test using {@link org.junit.runner.RunWith} and provides
 * means of creating your class under test and substituting mock instances or
 * real instances of its collaborators in a spring application context.
 *
 * @author saden
 */
public class SpringSystemTest extends BlockJUnit4ClassRunner {

    private final static ByteBuddy BYTE_BUDDY = new ByteBuddy();
    private static final Logger LOGGER = getLogger("testify");
    protected Map<Class, TestContext> testClassContexts = new ConcurrentHashMap<>();
    public Map<Class, SpringServiceLocator> applicationContexts = new ConcurrentHashMap<>();
    public Map<Class, List<NeedProvider>> needProvider = new ConcurrentHashMap<>();
    private ServiceAnnotations serviceAnnotations;
    private Set<NeedContext> needContexts;
    private ServerContext serverContext;
    private ClientContext clientContext;

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
        TestContext testContext = testClassContexts.computeIfAbsent(javaClass, p -> {
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

        this.serverContext = getServerContext(testContext);
        this.clientContext = getClientContext(testContext, serverContext);

        ServiceLocator serviceLocator = serverContext.getLocator();

        this.needContexts = getNeeds(testContext, serviceLocator);

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

    public ServerContext getServerContext(TestContext testContext) {
        return testContext.getAnnotation(App.class).map(p -> {
            Class<?> appType = p.value();
            Class<? extends ServerProvider> providerType = p.server();
            try {
                ServerProvider provider;
                if (providerType.equals(ServerProvider.class)) {
                    provider = UndertowServerProvider.class.newInstance();
                } else {
                    provider = providerType.newInstance();

                }

                InterceptorContext interceptorContext = new InterceptorContext();
                AnnotationInterceptor interceptor
                        = new AnnotationInterceptor(testContext,
                                interceptorContext,
                                SpringSystemPostProcessor.class);

                Class<?> proxyAppType = BYTE_BUDDY.subclass(appType)
                        .method(not(isDeclaredBy(Object.class)))
                        .intercept(to(interceptor).filter(not(isDeclaredBy(Object.class))))
                        .make()
                        .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();

                Set<Class<?>> handles = new HashSet<>();
                handles.add(proxyAppType);

                SpringSystemServerDescriptor descriptor
                        = new SpringSystemServerDescriptor(p,
                                testContext,
                                SpringServletContainerInitializer.class,
                                handles
                        );

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

                ServerInstance instance = provider.init(descriptor, context);
                instance.start();
                Object server = instance.getServer();

                SpringServiceLocator serviceLocator
                        = new SpringServiceLocator(interceptorContext.getServletAppContext(),
                                serviceAnnotations);

                serviceLocator.addConstant(context.getClass().getSimpleName(), context);
                serviceLocator.addConstant(instance.getClass().getSimpleName(), instance);
                serviceLocator.addConstant(server.getClass().getSimpleName(), server);

                return new ServerContext(provider, descriptor, instance, serviceLocator, context);
            } catch (Exception e) {
                checkState(false, "Server provider '%s' could not be instanticated.",
                        providerType.getSimpleName());
                return null;
            }
        }).get();
    }

    public ClientContext getClientContext(TestContext testContext, ServerContext serverContext) {
        return testContext.getAnnotation(App.class).map(p -> {
            Class<?> appType = p.value();
            Class<? extends ClientProvider> providerType = p.client();
            try {
                ClientProvider provider;
                if (providerType.equals(ClientProvider.class)) {
                    provider = JerseyClientProvider.class.newInstance();
                } else {
                    provider = providerType.newInstance();

                }

                SpringSystemClientDescriptor descriptor
                        = new SpringSystemClientDescriptor(p,
                                testContext,
                                serverContext.getInstance().getURI()
                        );

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

                ClientInstance instance = provider.init(descriptor, context);
                ServiceLocator serviceLocator = serverContext.getLocator();
                Object client = instance.getClient();

                serviceLocator.addConstant(context.getClass().getSimpleName(), context);
                serviceLocator.addConstant(instance.getClass().getSimpleName(), instance);
                serviceLocator.addConstant(client.getClass().getSimpleName(), client);

                return new ClientContext(provider, descriptor, instance, context);
            } catch (Exception e) {
                checkState(false, "Server provider '%s' could not be instanticated.",
                        providerType.getSimpleName());
                return null;
            }
        }).get();
    }

    public Set<NeedContext> getNeeds(TestContext testContext, ServiceLocator serviceLocator) {
        return testContext.getAnnotations(Need.class).parallelStream().map(p -> {
            Class<? extends NeedProvider> providerClass = p.value();
            try {
                NeedProvider provider = providerClass.newInstance();
                NeedDescriptor descriptor
                        = new SpringSystemNeedDescriptor(p, testContext);
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

                serviceLocator.addConstant(context.getClass().getSimpleName(), context);

                provider.init(descriptor, context);

                return new NeedContext(provider, descriptor, serviceLocator, context);
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

            clientContext.getInstance().closeClient();
            serverContext.getInstance().stop();
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
