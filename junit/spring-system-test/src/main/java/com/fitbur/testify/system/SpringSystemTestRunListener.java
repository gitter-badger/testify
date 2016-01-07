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

import com.fitbur.bytebuddy.ByteBuddy;
import com.fitbur.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import static com.fitbur.bytebuddy.implementation.MethodDelegation.to;
import static com.fitbur.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static com.fitbur.bytebuddy.matcher.ElementMatchers.not;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.App;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.spring.SpringServiceLocator;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.springframework.web.SpringServletContainerInitializer;

/**
 * A JUnit run listener that listens for test case execution life-cycle and
 * manages spring application context.
 *
 * @author saden
 */
public class SpringSystemTestRunListener extends RunListener {

    private final static ByteBuddy BYTE_BUDDY = new ByteBuddy();
    private final TestContext testContext;
    private final ServiceAnnotations serviceAnnotations;
    private final Logger logger;
    private Set<NeedContext> needContexts;
    private ServerContext serverContext;

    SpringSystemTestRunListener(TestContext testContext, ServiceAnnotations serviceAnnotations, Logger logger) {
        this.testContext = testContext;
        this.serviceAnnotations = serviceAnnotations;
        this.logger = logger;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        logger.info("Running {}", description.getMethodName());
        Object testInstance = testContext.getTestInstance();

        this.serverContext = testContext.getAnnotation(App.class)
                .map(p -> {
                    Class<?> appType = p.value();
                    Class<? extends ServerProvider> providerType = p.provider();
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
                                .intercept(to(interceptor).filter(not(isDeclaredBy(Object.class)))).make()
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
                                Method method = configMethod.get();
                                try {
                                    method.setAccessible(true);
                                    method.invoke(descriptor.getTestInstance(), context);
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    checkState(false, "Call to config method '%s' in test class '%s' failed.",
                                            method.getName(), descriptor.getTestClassName());
                                }

                                return null;
                            });
                        }

                        ServerInstance instance = provider.init(descriptor, context);
                        instance.start();

                        SpringServiceLocator serviceLocator
                                = new SpringServiceLocator(interceptorContext.getServletAppContext(),
                                        serviceAnnotations);

                        serviceLocator.addConstant(context.getClass().getSimpleName(), context);
                        serviceLocator.addConstant(instance.getClass().getSimpleName(), instance);

                        return new ServerContext(provider, descriptor, instance, serviceLocator, context);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        checkState(false, "Server provider '%s' could not be instanticated.", providerType.getSimpleName());
                        return null;
                    }
                }).get();

        ServiceLocator serviceLocator = serverContext.getLocator();

        this.needContexts = testContext.getAnnotations(Need.class).parallelStream().map(p -> {
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
                        Method method = configMethod.get();
                        try {
                            method.setAccessible(true);
                            method.invoke(descriptor.getTestInstance(), context);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            checkState(false, "Call to config method '%s' in test class '%s' failed.",
                                    method.getName(), descriptor.getTestClassName());
                        }

                        return null;
                    });
                }

                serviceLocator.addConstant(context.getClass().getSimpleName(), context);

                provider.init(descriptor, context);

                return new NeedContext(provider, descriptor, serviceLocator, context);
            } catch (InstantiationException | IllegalAccessException ex) {
                checkState(false, "Need provider '%s' could not be instanticated.", providerClass.getSimpleName());
                return null;
            }
        }).collect(toSet());

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

        SystemTestVerifier verifier = new SystemTestVerifier(testContext, logger);
        verifier.wiring();

    }

    @Override
    public void testFinished(Description description) throws Exception {
        logger.debug("Finished {}", description.getMethodName());
        this.done(description);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        String methodName = failure.getDescription().getMethodName();
        String traceMessage = failure.getTrace();
        logger.error("{} Failed\n{}", methodName, traceMessage);
        this.done(failure.getDescription());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        String methodName = failure.getDescription().getMethodName();
        String traceMessage = failure.getTrace();
        logger.error("{} Failed\n{}", methodName, traceMessage);
        this.done(failure.getDescription());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        logger.warn("Ignored {}", description.getMethodName());
    }

    private void done(Description description) {
        needContexts.parallelStream().forEach(p -> {
            p.getProvider().destroy(p.getDescriptor(), p.getContext());
        });

        serverContext.getInstance().stop();
    }

}
