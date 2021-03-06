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
package com.fitbur.testify.system.internal;

import com.fitbur.bytebuddy.implementation.bind.annotation.AllArguments;
import com.fitbur.bytebuddy.implementation.bind.annotation.RuntimeType;
import com.fitbur.bytebuddy.implementation.bind.annotation.SuperCall;
import com.fitbur.testify.Module;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestNeedContainers;
import com.fitbur.testify.TestNeeds;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.di.spring.SpringServicePostProcessor;
import com.fitbur.testify.need.NeedScope;
import java.util.concurrent.Callable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * A class that intercepts methods of classes that extend
 * {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
 * This class is responsible for configuring the Spring application as well as
 * extracting information useful for test scaffolding.
 *
 * @author saden
 */
public class SpringSystemServletInterceptor {

    private final TestContext testContext;
    private final String methodName;
    private final ServiceAnnotations serviceAnnotations;
    private final TestNeeds classTestNeeds;
    private final TestNeedContainers classTestNeedContainers;
    private Class[] servletConfigClasses;
    private SpringServiceLocator serviceLocator;
    private TestNeeds methodTestNeeds;
    private TestNeedContainers methodTestNeedContainers;
    private AnnotationConfigWebApplicationContext context;

    public SpringSystemServletInterceptor(TestContext testContext,
            String methodName,
            ServiceAnnotations serviceAnnotations,
            TestNeeds classTestNeeds,
            TestNeedContainers classTestNeedContainers) {
        this.testContext = testContext;
        this.methodName = methodName;
        this.serviceAnnotations = serviceAnnotations;
        this.classTestNeedContainers = classTestNeedContainers;
        this.classTestNeeds = classTestNeeds;
    }

    @RuntimeType
    public Object anyMethod(@SuperCall Callable<?> zuper, @AllArguments Object... args)
            throws Exception {
        return zuper.call();
    }

    public WebApplicationContext createServletApplicationContext(@SuperCall Callable<WebApplicationContext> zuper)
            throws Exception {
        this.context = (AnnotationConfigWebApplicationContext) zuper.call();
        context.setId(testContext.getName());
        context.setAllowBeanDefinitionOverriding(true);
        context.setAllowCircularReferences(false);

        Class<?>[] modules = testContext.getAnnotations(Module.class)
                .stream()
                .map(Module::value)
                .toArray(Class[]::new);

        if (modules != null && modules.length != 0) {
            context.register(modules);
        }

        serviceLocator = new SpringServiceLocator(context, serviceAnnotations);

        methodTestNeeds = new TestNeeds(testContext,
                methodName,
                NeedScope.METHOD);
        methodTestNeeds.init();

        methodTestNeedContainers = new TestNeedContainers(testContext,
                methodName,
                NeedScope.METHOD);
        methodTestNeedContainers.init();

        SpringServicePostProcessor postProcessor = new SpringServicePostProcessor(
                serviceLocator,
                methodTestNeeds,
                methodTestNeedContainers,
                classTestNeeds,
                classTestNeedContainers);

        context.addBeanFactoryPostProcessor(postProcessor);

        return context;
    }

    public Class<?>[] getServletConfigClasses(@SuperCall Callable<Class<?>[]> zuper)
            throws Exception {
        this.servletConfigClasses = zuper.call();

        return servletConfigClasses;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return context;
    }

    public TestNeedContainers getClassTestNeedContainers() {
        return classTestNeedContainers;
    }

    public TestNeeds getClassTestNeeds() {
        return classTestNeeds;
    }

    public TestNeedContainers getMethodTestNeedContainers() {
        return methodTestNeedContainers;
    }

    public TestNeeds getMethodTestNeeds() {
        return methodTestNeeds;
    }

    public SpringServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public Class[] getServletConfigClasses() {
        return servletConfigClasses;
    }

}
