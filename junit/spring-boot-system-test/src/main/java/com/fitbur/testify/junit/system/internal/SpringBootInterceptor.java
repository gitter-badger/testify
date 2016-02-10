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
package com.fitbur.testify.junit.system.internal;

import com.fitbur.bytebuddy.implementation.bind.annotation.AllArguments;
import com.fitbur.bytebuddy.implementation.bind.annotation.BindingPriority;
import com.fitbur.bytebuddy.implementation.bind.annotation.IgnoreForBinding;
import com.fitbur.bytebuddy.implementation.bind.annotation.RuntimeType;
import com.fitbur.bytebuddy.implementation.bind.annotation.SuperCall;
import com.fitbur.bytebuddy.implementation.bind.annotation.This;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.guava.common.base.Throwables;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestNeedContainers;
import com.fitbur.testify.TestNeeds;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.di.spring.SpringServicePostProcessor;
import com.fitbur.testify.need.NeedScope;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot application interceptor. This class is responsible for
 * configuring the Spring Boot application as well as extracting information
 * useful for test scaffolding.
 *
 * @author saden
 */
public class SpringBootInterceptor {

    private final Map<SpringApplication, TestContext> testContexts;
    private final Map<SpringApplication, SpringBootDescriptor> apps;
    private final Map<AnnotationConfigEmbeddedWebApplicationContext, SpringBootDescriptor> descriptors;

    public SpringBootInterceptor(
            Map<SpringApplication, TestContext> testContext,
            Map<SpringApplication, SpringBootDescriptor> apps,
            Map<AnnotationConfigEmbeddedWebApplicationContext, SpringBootDescriptor> contexts) {
        this.testContexts = testContext;
        this.apps = apps;
        this.descriptors = contexts;
    }

    @RuntimeType
    @BindingPriority(Integer.MAX_VALUE)
    public Object anyMethod(
            @SuperCall Callable<?> zuper,
            @This(optional = true) Object object,
            @AllArguments Object[] args)
            throws Exception {
        return zuper.call();
    }

    public ConfigurableApplicationContext run(
            @SuperCall Callable<ConfigurableApplicationContext> zuper,
            @This Object object,
            @AllArguments Object[] args) throws Exception {
        AnnotationConfigEmbeddedWebApplicationContext context
                = (AnnotationConfigEmbeddedWebApplicationContext) zuper.call();

        SpringBootDescriptor descriptor = descriptors.get(context);

        descriptor.setApplication((SpringApplication) object);
        descriptor.setSources(args[0]);

        if (args.length == 2) {
            descriptor.setArgs((String[]) args[1]);
        }

        return context;
    }

    public ConfigurableApplicationContext createApplicationContext(
            @SuperCall Callable<ConfigurableApplicationContext> zuper,
            @This Object object) throws Exception {
        SpringApplication app = (SpringApplication) object;

        AnnotationConfigEmbeddedWebApplicationContext context
                = (AnnotationConfigEmbeddedWebApplicationContext) zuper.call();

        context.setAllowBeanDefinitionOverriding(true);
        context.setAllowCircularReferences(false);

        SpringBootDescriptor descriptor
                = descriptors.computeIfAbsent(context, SpringBootDescriptor::new);
        apps.putIfAbsent(app, descriptor);

        ServiceAnnotations serviceAnnotations = new ServiceAnnotations();
        serviceAnnotations.addInjectors(Inject.class, Autowired.class, Real.class);
        serviceAnnotations.addNamedQualifier(Named.class, Qualifier.class);
        serviceAnnotations.addCustomQualfier(javax.inject.Qualifier.class, Qualifier.class);
        descriptor.setServiceAnnotations(serviceAnnotations);

        TestContext testContext = testContexts.get(app);
        descriptor.setTestContext(testContext);

        SpringServiceLocator serviceLocator = new SpringServiceLocator(context, serviceAnnotations);
        descriptor.setServiceLocator(serviceLocator);

        TestNeeds testNeeds = new TestNeeds(testContext,
                testContext.getName(),
                NeedScope.METHOD);
        testNeeds.init();

        descriptor.setTestNeeds(testNeeds);

        TestNeedContainers testContainerNeeds = new TestNeedContainers(testContext,
                testContext.getName(),
                NeedScope.METHOD);
        testContainerNeeds.init();
        descriptor.setTestContainerNeeds(testContainerNeeds);

        SpringServicePostProcessor postProcessor = new SpringServicePostProcessor(
                serviceLocator,
                testNeeds,
                testContainerNeeds,
                null,
                null);

        context.addBeanFactoryPostProcessor(postProcessor);

        return context;
    }

    public EmbeddedServletContainerFactory getEmbeddedServletContainerFactory(
            @SuperCall Callable<EmbeddedServletContainerFactory> zuper,
            @This Object object) throws Exception {
        EmbeddedServletContainerFactory containerFactory = zuper.call();
        ConfigurableEmbeddedServletContainer configurableServletContainer
                = (ConfigurableEmbeddedServletContainer) containerFactory;
        configurableServletContainer.setPort(0);

        SpringBootDescriptor descriptor = getDescriptor(object);
        descriptor.setContainerFactory(containerFactory);
        descriptor.setConfigurableServletContainer(configurableServletContainer);
        TestContext testContext = descriptor.getTestContext();

        testContext.getConfigMethod(ConfigurableEmbeddedServletContainer.class)
                .map(m -> m.getMethod())
                .ifPresent(m -> {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        try {
                            m.setAccessible(true);
                            m.invoke(testContext.getTestInstance(), configurableServletContainer);
                        } catch (Exception e) {
                            checkState(false,
                                    "Call to config method '%s' in test class '%s' failed.",
                                    m.getName(), testContext.getTestClassName());
                            throw Throwables.propagate(e);
                        }

                        return null;
                    });
                });

        return containerFactory;
    }

    public EmbeddedServletContainer startEmbeddedServletContainer(
            @SuperCall Callable<EmbeddedServletContainer> zuper,
            @This Object object) throws Exception {
        EmbeddedServletContainer container = zuper.call();
        SpringBootDescriptor descriptor = getDescriptor(object);
        descriptor.setServletContainer(container);

        return container;
    }

    public void prepareEmbeddedWebApplicationContext(@SuperCall Callable<Void> zuper,
            @This Object object,
            @AllArguments Object[] args) throws Exception {
        zuper.call();
        SpringBootDescriptor descriptor = getDescriptor(object);
        descriptor.setServletContext((ServletContext) args[0]);
    }

    @IgnoreForBinding
    SpringBootDescriptor getDescriptor(Object object) {
        return descriptors.get((AnnotationConfigEmbeddedWebApplicationContext) object);
    }
}
