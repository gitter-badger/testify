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
import com.fitbur.testify.Module;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedContext;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedProvider;
import com.fitbur.testify.app.ServerDescriptor;
import com.fitbur.testify.system.interceptor.AnnotationInterceptor;
import com.fitbur.testify.system.support.UndertowSystemTestServer;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import java.net.URI;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import javax.servlet.ServletContainerInitializer;
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

    private final TestContext testContext;
    private final ServiceAnnotations serviceAnnotations;
    private final Logger logger;
    private SpringServiceLocator serviceLocator;
    private Set<NeedContext> needContexts;
    private Undertow server;

    SpringSystemTestRunListener(TestContext testContext, ServiceAnnotations serviceAnnotations, Logger logger) {
        this.testContext = testContext;
        this.serviceAnnotations = serviceAnnotations;
        this.logger = logger;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        logger.info("Running {}", description.getMethodName());
        Object testInstance = testContext.getTestInstance();
        SpringSystemDescriptor systemDescriptor = new SpringSystemDescriptor();
        ByteBuddy byteBuddy = new ByteBuddy();

        AnnotationInterceptor interceptor
                = new AnnotationInterceptor(testContext,
                        systemDescriptor,
                        SpringSystemPostProcessor.class);

        Set<Class<?>> handles = testContext.getAnnotations(Module.class)
                .stream()
                .sequential()
                .distinct()
                .map(Module::value)
                .map(p -> {
                    return byteBuddy.subclass(p)
                            .method(not(isDeclaredBy(Object.class)))
                            .intercept(to(interceptor).filter(not(isDeclaredBy(Object.class)))).make()
                            .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                            .getLoaded();

                }).collect(toSet());

        SpringServletContainerInitializer initializer = new SpringServletContainerInitializer();
        ImmediateInstanceFactory<SpringServletContainerInitializer> factory = new ImmediateInstanceFactory<>(initializer);
        Class<? extends ServletContainerInitializer> type = SpringServletContainerInitializer.class;

        ServletContainerInitializerInfo info
                = new ServletContainerInitializerInfo(type, factory, handles);
        String name = testContext.getName();

        URI uri = URI.create("http://0.0.0.0:0/");

        DeploymentInfo deploymentInfo = Servlets.deployment()
                .addServletContainerInitalizer(info)
                .setClassLoader(SpringSystemTestRunListener.class.getClassLoader())
                .setContextPath(uri.getPath())
                .setDeploymentName(name)
                .addServlet(Servlets.servlet(name, DefaultServlet.class));

        DeploymentManager manager = Servlets.defaultContainer()
                .addDeployment(deploymentInfo);

        manager.deploy();
        HttpHandler httpHandler = manager.start();

        RedirectHandler defaultHandler = Handlers.redirect(uri.getPath());
        PathHandler pathHandler = Handlers.path(defaultHandler);
        pathHandler.addPrefixPath(uri.getPath(), httpHandler);

        this.server = Undertow.builder()
                .addHttpListener(uri.getPort(), uri.getHost(), pathHandler)
                .build();

        server.start();

        ServerDescriptor serverContext = new UndertowSystemTestServer(uri, server).create();

        SystemTestVerifier verifier = new SystemTestVerifier(testContext, logger);
        this.serviceLocator = new SpringServiceLocator(systemDescriptor.getServletAppContext(), serviceAnnotations);

        this.needContexts = testContext.getAnnotations(Need.class).parallelStream().map(p -> {
            Class<? extends NeedProvider> providerClass = p.value();
            try {
                NeedProvider provider = providerClass.newInstance();
                NeedDescriptor descriptor
                        = new SpringSystemNeedDescriptor(p, testContext, serviceLocator);
                Object context = provider.configure(descriptor);
                provider.init(descriptor, context);

                return new NeedContext(provider, descriptor, context);
            } catch (InstantiationException | IllegalAccessException ex) {
                checkState(false, "Need provider '%s' could not be instanticated.", providerClass.getSimpleName());
                return null;
            }
        }).collect(toSet());

        SystemTestReifier reifier
                = new SystemTestReifier(testContext, serviceLocator, testInstance);
        SystemTestCreator creator
                = new SystemTestCreator(testContext, reifier, serviceLocator);

        //if we are not testing a specific cut class we can still inject
        //services for testing purpose. this is useful for testing things
        //like JPA entities which aren't really services.
        if (testContext.getCutDescriptor() == null) {
            Set<FieldDescriptor> real = testContext.getFieldDescriptors()
                    .values()
                    .parallelStream()
                    .filter(p -> p.hasAnnotations(serviceAnnotations.getInjectors()))
                    .collect(toSet());
            creator.real(real);
        } else {
            creator.cut();
        }

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
        this.done(description);
    }

    private void done(Description description) {
        server.stop();

        needContexts.parallelStream().forEach(p -> {
            p.getProvider().destroy(p.getDescriptor(), p.getContext());
        });

    }


}
