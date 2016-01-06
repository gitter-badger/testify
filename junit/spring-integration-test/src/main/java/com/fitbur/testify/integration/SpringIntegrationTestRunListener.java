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

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedContext;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedProvider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * A JUnit run listener that listens for test case execution life-cycle and
 * manages spring application context.
 *
 * @author saden
 */
public class SpringIntegrationTestRunListener extends RunListener {

    private final TestContext testContext;
    private final ServiceAnnotations serviceAnnotations;
    private final Logger logger;
    private SpringServiceLocator serviceLocator;
    private Set<NeedContext> needContexts;

    SpringIntegrationTestRunListener(TestContext testContext, ServiceAnnotations serviceAnnotations, Logger logger) {
        this.testContext = testContext;
        this.serviceAnnotations = serviceAnnotations;
        this.logger = logger;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        logger.info("Running {}", description.getMethodName());
        String testClassName = testContext.getTestClassName();
        Object testInstance = testContext.getTestInstance();

        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.setId(testClassName);
        appContext.setAllowBeanDefinitionOverriding(true);
        appContext.setAllowCircularReferences(false);
        appContext.register(SpringIntegrationPostProcessor.class);

        this.serviceLocator = new SpringServiceLocator(appContext, serviceAnnotations);

        this.needContexts = testContext.getAnnotations(Need.class).parallelStream().map(p -> {
            Class<? extends NeedProvider> providerClass = p.value();
            try {
                NeedProvider provider = providerClass.newInstance();
                NeedDescriptor descriptor
                        = new SpringIntegrationNeedDescriptor(p, testContext, serviceLocator);
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

                serviceLocator.addConstant(descriptor.getTestClassName(), context);

                provider.init(descriptor, context);

                return new NeedContext(provider, descriptor, serviceLocator, context);
            } catch (InstantiationException | IllegalAccessException ex) {
                checkState(false, "Need provider '%s' could not be instanticated.", providerClass.getSimpleName());
                return null;
            }
        }).collect(toSet());

        IntegrationTestReifier reifier
                = new IntegrationTestReifier(testContext, serviceLocator, testInstance);
        IntegrationTestCreator creator
                = new IntegrationTestCreator(testContext, reifier, serviceLocator);

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

        IntegrationTestVerifier verifier = new IntegrationTestVerifier(testContext, logger);
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
        needContexts.parallelStream().forEach(p -> {
            p.getProvider().destroy(p.getDescriptor(), p.getContext());
        });

        serviceLocator.destroy();

    }

}
