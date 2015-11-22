/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fitbur.testify.integration;

import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestException;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.descriptor.FieldDescriptor;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.di.spring.SpringTestServiceLocator;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.TestNeed;
import static com.google.common.base.Preconditions.checkState;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import javax.inject.Inject;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import static org.junit.runner.Description.createTestDescription;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.objectweb.asm.ClassReader;
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * A JUnit spring integration test runner. This class is the main entry point
 * for running a unit test using {@link org.junit.runner.RunWith} and provides
 * means of creating your class under test and substituting mock instances or
 * real instances of its collaborators in a spring application context.
 *
 * @author saden
 */
public class SpringIntegrationTestRunner extends BlockJUnit4ClassRunner {

    protected Map<Class, TestContext> testClassContexts = new ConcurrentHashMap<>();
    public Map<Class, ServiceLocator> applicationContexts = new ConcurrentHashMap<>();
    public Map<Class, List<NeedDescriptor>> testNeedDescriptors = new ConcurrentHashMap<>();

    public RunNotifier notifier;

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public SpringIntegrationTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * Describe the test class.
     *
     * @return
     */
    @Override
    public Description getDescription() {
        TestClass testClass = this.getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        String name = javaClass.getSimpleName();

        return createTestDescription(javaClass, name, testClass.getAnnotations());
    }

    @Override
    public Object createTest() throws Exception {
        try {
            TestClass testClass = this.getTestClass();
            Class<?> javaClass = testClass.getJavaClass();
            String name = javaClass.getSimpleName();

            TestContext testContext = this.testClassContexts.computeIfAbsent(javaClass, p -> {
                Object instance;
                try {
                    instance = super.createTest();
                    TestContext context = new TestContext(name, javaClass, instance);

                    ClassReader testReader = new ClassReader(javaClass.getName());
                    testReader.accept(new TestClassAnalyzer(context), EXPAND_FRAMES);

                    int cutCount = context.getCutCount();

                    if (cutCount == 1) {
                        ClassReader cutReader = new ClassReader(context.getCutDescriptor().getField().getType().getName());
                        cutReader.accept(new CutClassAnalyzer(context), EXPAND_FRAMES);
                    } else if (cutCount > 1) {
                        checkState(false,
                                "Found more than one class under test in %s. "
                                + "Please annotated only a single field with @Cut.", name);
                    }

                    return context;
                } catch (Exception e) {
                    throw new TestException(e);
                }
            });

            AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
            appContext.setAllowBeanDefinitionOverriding(true);
            appContext.setId(name);

            ServiceLocator serviceLocator
                    = new SpringTestServiceLocator(appContext);
            Object testInstance = testContext.getTestInstance();
            Need[] needs = testInstance.getClass().getDeclaredAnnotationsByType(Need.class);

            @SuppressWarnings("UseSpecificCatch")
            List<NeedDescriptor> needDescriptors = of(needs)
                    .map(p -> {
                        try {
                            TestNeed testNeed = p.value().newInstance();
                            Object needContext = testNeed.init(testInstance, serviceLocator);
                            return new NeedDescriptor(p, needContext, testNeed);
                        } catch (Exception e) {
                            throw new TestException(e);
                        }
                    })
                    .collect(toList());
            this.testNeedDescriptors.put(javaClass, needDescriptors);

            IntegrationTestReifier reifier = new IntegrationTestReifier(serviceLocator, testInstance);
            IntegrationTestCreator integrationTestCreator = new IntegrationTestCreator(testContext, reifier, serviceLocator);
            if (testContext.getCutDescriptor() == null) {
                Set<FieldDescriptor> real = testContext.getFieldDescriptors()
                        .values()
                        .parallelStream()
                        .filter(p -> p.hasAnyAnnotation(Inject.class, Autowired.class, Real.class))
                        .collect(toSet());
                integrationTestCreator.real(real);
            } else {
                integrationTestCreator.cut();
            }

            this.applicationContexts.put(javaClass, serviceLocator);
            return testInstance;
        } catch (IllegalStateException e) {
            this.notifier.pleaseStop();
            throw e;
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        this.notifier = notifier;
        Description description = this.getDescription();
        TestClass testClass = this.getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        String name = javaClass.getSimpleName();
        SpringIntegrationTestRunListener listener = new SpringIntegrationTestRunListener(name,
                testClassContexts,
                applicationContexts,
                testNeedDescriptors);
        notifier.addListener(listener);
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, description);
        try {
            Statement statement = this.classBlock(notifier);
            statement.evaluate();

            // invoke here the run started method
            notifier.fireTestRunStarted(description);
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }

}
