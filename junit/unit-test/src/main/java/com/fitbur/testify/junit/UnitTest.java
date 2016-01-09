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
package com.fitbur.testify.junit;

import com.fitbur.asm.ClassReader;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.junit.core.JUnitTestNotifier;
import com.fitbur.testify.unit.UnitTestCreator;
import com.fitbur.testify.unit.UnitTestReifier;
import com.fitbur.testify.unit.UnitTestVerifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * A JUnit unit test runner. This class is the main entry point for running a
 * unit test using {@link org.junit.runner.RunWith} and provides means of
 * creating your class under test and substituting mock instances of its
 * collaborators.
 *
 * @author saden
 */
public class UnitTest extends BlockJUnit4ClassRunner {

    static final Logger LOGGER = LoggerFactory.getLogger("testify");
    Map<Class, TestContext> testClassContexts = new ConcurrentHashMap<>();

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public UnitTest(Class<?> testClass) throws InitializationError {
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
                    ClassReader cutReader = new ClassReader(cutDescriptor.getType().getName());
                    cutReader.accept(new CutClassAnalyzer(context), ClassReader.SKIP_DEBUG);
                }

                return context;
            } catch (Exception e) {
                checkState(false, "Analysis of test class '%s' failed.\n'%s'", name, e.getMessage());
                //not reachable
                throw new IllegalStateException(e);
            }
        });

        return testContext;
    }

    @Override
    public void run(RunNotifier notifier) {
        Description description = getDescription();
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        TestContext testContext = getTestContext(javaClass);
        //register slf4j bridge
        if (!SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }

        JUnitTestNotifier testNotifier
                = new JUnitTestNotifier(notifier, description, LOGGER, testContext);

        try {
            Statement statement = classBlock(testNotifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            throw e;
        } catch (IllegalStateException e) {
            testNotifier.addFailure(e);
            testNotifier.pleaseStop();
        } catch (Throwable e) {
            testNotifier.addFailure(e);
            testNotifier.pleaseStop();
        } finally {
            //XXX: notifier is a singleton so we have to remove it or otherwise
            //the listener will keep getting added to it and will be called
            //multiple times

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
        UnitTestVerifier verifier = new UnitTestVerifier(testContext, LOGGER);
        verifier.dependency();
        verifier.configuration();
        return super.classBlock(notifier);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        TestClass testClass = getTestClass();
        Class<?> javaClass = testClass.getJavaClass();

        try {
            Object testInstance = createTest();

            TestContext testContext = testClassContexts.get(javaClass);
            testContext.setTestInstance(testInstance);
            UnitTestReifier reifier = new UnitTestReifier(testInstance);
            UnitTestCreator creator = new UnitTestCreator(testContext, reifier);
            creator.create();
            UnitTestVerifier verifier = new UnitTestVerifier(testContext, LOGGER);
            verifier.wiring();

            Statement statement = methodInvoker(method, testInstance);
            statement = possiblyExpectingExceptions(method, testInstance, statement);
            statement = withBefores(method, testInstance, statement);
            statement = withAfters(method, testInstance, statement);
            statement = withRules(method, testInstance, statement);
            return statement;
        } catch (Exception e) {
            throw new IllegalStateException(e);
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
