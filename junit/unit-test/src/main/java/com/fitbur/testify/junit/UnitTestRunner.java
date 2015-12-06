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

import com.fitbur.testify.TestContext;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.unit.UnitTestCreator;
import com.fitbur.testify.unit.UnitTestReifier;
import com.fitbur.testify.unit.UnitTestVerifier;
import static com.google.common.base.Preconditions.checkState;
import java.util.HashMap;
import java.util.Map;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.objectweb.asm.ClassReader;
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
public class UnitTestRunner extends BlockJUnit4ClassRunner {

    static final Logger LOGGER = LoggerFactory.getLogger("testify");
    Map<Class, TestContext> testClassContexts = new HashMap<>();

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

    @Override
    protected Object createTest() throws Exception {
        try {
            Object testInstace = super.createTest();

            TestClass testClass = getTestClass();
            Class<?> javaClass = testClass.getJavaClass();

            TestContext testContext = testClassContexts.get(javaClass);
            testContext.setTestInstance(testContext);
            UnitTestVerifier verifier = new UnitTestVerifier(testContext, LOGGER);

            UnitTestReifier reifier = new UnitTestReifier(testInstace);
            UnitTestCreator creator = new UnitTestCreator(testContext, reifier);
            creator.create();
            verifier.wiring();

            return testInstace;
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    public TestContext analyzeClass(Class<?> javaClass, String name) {
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
        String name = javaClass.getSimpleName();

        TestContext testContext = analyzeClass(javaClass, name);
        UnitTestVerifier verifier = new UnitTestVerifier(testContext, LOGGER);
        verifier.dependency();
        verifier.configuration();

        //register slf4j bridge
        if (!SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }

        UnitTestRunListener listener = new UnitTestRunListener(testContext, LOGGER);
        notifier.addFirstListener(listener);
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, description);
        try {
            notifier.fireTestRunStarted(description);
            Statement statement = classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            LOGGER.error("{}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            LOGGER.error("{}", e.getMessage());
            notifier.pleaseStop();
        } catch (Throwable e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.addFailure(e);
        } finally {
            notifier.fireTestRunFinished(new Result());
            //XXX: notifier is a singleton so we have to remove it or otherwise
            //the listener will keep getting added to it and will be called
            //multiple times
            notifier.removeListener(listener);

            if (SLF4JBridgeHandler.isInstalled()) {
                SLF4JBridgeHandler.uninstall();
            }
        }
    }

}
