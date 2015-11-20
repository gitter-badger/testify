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
package com.fitbur.testify.junit;

import com.fitbur.testify.Cut;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.unit.UnitTestCreator;
import com.fitbur.testify.unit.UnitTestReifier;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
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
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * A JUnit unit test runner. This class is the main entry point for running a
 * unit test using {@link org.junit.runner.RunWith} and provides means of
 * creating your class under test and substituting mock instances of its
 * collaborators.
 *
 * @author saden
 */
public class UnitTestRunner extends BlockJUnit4ClassRunner {

    Map<Class, TestContext> testClassContexts = new HashMap<>();
    private RunNotifier notifier;

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public UnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * Describe the test class.
     *
     * @return
     */
    @Override
    public Description getDescription() {
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        String name = javaClass.getSimpleName();

        return createTestDescription(javaClass, name, testClass.getAnnotations());
    }

    @Override
    protected Object createTest() throws Exception {
        try {
            TestClass testClass = getTestClass();
            Class<?> javaClass = testClass.getJavaClass();
            String name = javaClass.getSimpleName();

            TestContext testContext = testClassContexts.computeIfAbsent(javaClass, p -> {
                Object instance;
                try {
                    instance = super.createTest();
                    TestContext context = new TestContext(name, javaClass, instance);
                    Set<Field> candidatesFields = of(javaClass.getDeclaredFields())
                            .parallel()
                            .filter(f -> f.isAnnotationPresent(Cut.class))
                            .collect(toSet());

                    Field cutField = candidatesFields.iterator().next();
                    ClassReader testReader = new ClassReader(javaClass.getName());
                    ClassReader cutReader = new ClassReader(cutField.getType().getName());

                    testReader.accept(new TestClassAnalyzer(context), ClassReader.SKIP_DEBUG);
                    cutReader.accept(new CutClassAnalyzer(context), ClassReader.SKIP_DEBUG);

                    return context;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Object instance = testContext.getTestInstance();

            UnitTestReifier testReifier = new UnitTestReifier(instance);
            UnitTestCreator unitTestCreator = new UnitTestCreator(testContext, testReifier);
            unitTestCreator.create();

            return instance;
        } catch (IllegalStateException e) {
            notifier.pleaseStop();
            throw e;
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        if (!SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }

        this.notifier = notifier;
        Description description = getDescription();
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        String name = javaClass.getSimpleName();
        UnitTestRunListener listener = new UnitTestRunListener(name, testClass, testClassContexts);
        notifier.addListener(listener);

        EachTestNotifier testNotifier = new EachTestNotifier(notifier, description);
        try {
            Statement statement = classBlock(notifier);
            statement.evaluate();

            // invoke here the run started method
            notifier.fireTestRunStarted(description);
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        } finally {
            if (SLF4JBridgeHandler.isInstalled()) {
                SLF4JBridgeHandler.uninstall();
            }
        }
    }

}
