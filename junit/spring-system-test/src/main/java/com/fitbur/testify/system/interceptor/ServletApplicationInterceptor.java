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
package com.fitbur.testify.system.interceptor;

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
import com.fitbur.testify.need.docker.DockerContainerNeedProvider;
import java.util.concurrent.Callable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * A class that intercepts methods of classes that extend
 * {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
 *
 * @author saden
 */
public class ServletApplicationInterceptor {

    private final TestContext testContext;
    private final String methodName;
    private final ServiceAnnotations serviceAnnotations;
    private final TestNeeds classTestNeeds;
    private final TestNeedContainers classTestNeedContainers;
    private ConfigurableApplicationContext applicationContext;
    private Class[] servletConfigClasses;
    private SpringServiceLocator serviceLocator;
    private TestNeeds methodTestNeeds;
    private TestNeedContainers methodTestNeedContainers;

    public ServletApplicationInterceptor(TestContext testContext,
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
    public Object delegate(@SuperCall Callable<?> zuper, @AllArguments Object... args)
            throws Exception {
        return zuper.call();
    }

    public WebApplicationContext createServletApplicationContext(@SuperCall Callable<WebApplicationContext> zuper)
            throws Exception {
        AnnotationConfigWebApplicationContext servletAppContext = (AnnotationConfigWebApplicationContext) zuper.call();
        servletAppContext.setId(testContext.getName());
        servletAppContext.setAllowBeanDefinitionOverriding(true);
        servletAppContext.setAllowCircularReferences(false);

        Class<?>[] modules = testContext.getAnnotations(Module.class)
                .stream()
                .map(Module::value)
                .toArray(Class[]::new);

        if (modules != null && modules.length != 0) {
            servletAppContext.register(modules);
        }

        applicationContext = servletAppContext;
        serviceLocator = new SpringServiceLocator(servletAppContext, serviceAnnotations);

        methodTestNeeds = new TestNeeds(testContext,
                methodName,
                NeedScope.METHOD,
                serviceLocator);
        methodTestNeeds.init();

        methodTestNeedContainers = new TestNeedContainers(testContext,
                methodName,
                NeedScope.METHOD,
                serviceLocator,
                DockerContainerNeedProvider.class);
        methodTestNeedContainers.init();

        SpringServicePostProcessor postProcessor = new SpringServicePostProcessor(
                serviceLocator,
                methodTestNeeds,
                methodTestNeedContainers,
                classTestNeeds,
                classTestNeedContainers);

        servletAppContext.addBeanFactoryPostProcessor(postProcessor);

        return servletAppContext;
    }

    public Class<?>[] getServletConfigClasses(@SuperCall Callable<Class<?>[]> zuper)
            throws Exception {
        this.servletConfigClasses = zuper.call();

        return servletConfigClasses;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
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
