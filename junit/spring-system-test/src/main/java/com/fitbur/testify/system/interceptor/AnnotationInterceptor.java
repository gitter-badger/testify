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
import static com.fitbur.guava.common.collect.ObjectArrays.concat;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.system.SpringSystemPostProcessor;
import java.util.concurrent.Callable;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * A class that intercepts methods of classes that extend
 * {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
 *
 * @author saden
 */
public class AnnotationInterceptor {

    private final TestContext testContext;
    private final InterceptorContext interceptorContext;
    private final Class<?>[] components;

    public AnnotationInterceptor(TestContext testContext,
            InterceptorContext interceptorContext, Class<?>... components) {
        this.testContext = testContext;
        this.interceptorContext = interceptorContext;
        this.components = components;
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
        servletAppContext.register(SpringSystemPostProcessor.class);

        interceptorContext.setServletAppContext(servletAppContext);

        return servletAppContext;

    }

    public Class<?>[] getServletConfigClasses(@SuperCall Callable<Class<?>[]> zuper)
            throws Exception {
        Class<?>[] result = concat(components, zuper.call(), Class.class);
        interceptorContext.setServletConfigClasses(result);

        return result;
    }

}
