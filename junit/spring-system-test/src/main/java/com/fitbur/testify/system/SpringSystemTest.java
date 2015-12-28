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

import com.fitbur.asm.ClassReader;
import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.Real;
import com.fitbur.testify.TestContext;
import com.fitbur.testify.analyzer.CutClassAnalyzer;
import com.fitbur.testify.analyzer.TestClassAnalyzer;
import com.fitbur.testify.descriptor.CutDescriptor;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import com.fitbur.testify.need.NeedProvider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
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
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import static org.slf4j.bridge.SLF4JBridgeHandler.install;
import static org.slf4j.bridge.SLF4JBridgeHandler.isInstalled;
import static org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger;
import static org.slf4j.bridge.SLF4JBridgeHandler.uninstall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * A JUnit spring integration test runner. This class is the main entry point
 * for running a unit test using {@link org.junit.runner.RunWith} and provides
 * means of creating your class under test and substituting mock instances or
 * real instances of its collaborators in a spring application context.
 *
 * @author saden
 */
public class SpringSystemTest extends BlockJUnit4ClassRunner {

    static final Logger LOGGER = getLogger("testify");
    protected Map<Class, TestContext> testClassContexts = new ConcurrentHashMap<>();
    public Map<Class, SpringServiceLocator> applicationContexts = new ConcurrentHashMap<>();
    public Map<Class, List<NeedProvider>> needProvider = new ConcurrentHashMap<>();

    public RunNotifier notifier;

    /**
     * Create a new test runner instance for the class under test.
     *
     * @param testClass the test class type
     *
     * @throws InitializationError thrown if the test class is malformed.
     */
    public SpringSystemTest(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();

        TestContext testContext = testClassContexts.get(testInstance.getClass());
        testContext.setTestInstance(testInstance);

        return testInstance;
    }

    public TestContext analyzeTest(Class<?> javaClass, String name) {
        TestContext testContext = testClassContexts.computeIfAbsent(javaClass, p -> {
            try {
                TestContext context = new TestContext(name, javaClass, LOGGER);

                ClassReader testReader = new ClassReader(javaClass.getName());
                testReader.accept(new TestClassAnalyzer(context), ClassReader.SKIP_DEBUG);

                CutDescriptor cutDescriptor = context.getCutDescriptor();

                if (cutDescriptor != null) {
                    ClassReader cutReader = new ClassReader(context.getCutDescriptor().getField().getType().getName());
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
        this.notifier = notifier;
        Description description = this.getDescription();
        TestClass testClass = this.getTestClass();
        Class<?> javaClass = testClass.getJavaClass();
        String name = javaClass.getSimpleName();

        TestContext testContext = analyzeTest(javaClass, name);
        SystemTestVerifier verifier = new SystemTestVerifier(testContext, LOGGER);
        verifier.dependency();
        verifier.configuration();

        //register slf4j bridge
        if (!isInstalled()) {
            removeHandlersForRootLogger();
            install();
        }

        ServiceAnnotations serviceAnnotations = new ServiceAnnotations();
        serviceAnnotations.addInjectors(Inject.class, Autowired.class, Real.class);
        serviceAnnotations.addNamedQualifier(Named.class, Qualifier.class);
        serviceAnnotations.addCustomQualfier(javax.inject.Qualifier.class, Qualifier.class);

        SpringSystemTestRunListener listener
                = new SpringSystemTestRunListener(testContext, serviceAnnotations, LOGGER);

        notifier.addListener(listener);
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, description);

        try {
            notifier.fireTestRunStarted(description);
            Statement statement = this.classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            LOGGER.warn(e.getMessage());
            testNotifier.fireTestIgnored();
        } catch (IllegalStateException e) {
            LOGGER.error("{}", e.getMessage());
            testNotifier.addFailure(e);
            notifier.pleaseStop();
        } catch (StoppedByUserException e) {
            LOGGER.error(e.getMessage());
            throw e;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
            testNotifier.addFailure(e);
            notifier.pleaseStop();
        } finally {
            notifier.fireTestRunFinished(new Result());
            //XXX: notifier is a singleton so we have to remove it or otherwise
            //the listener will keep getting added to it and will be called
            //multiple times
            notifier.removeListener(listener);

            if (isInstalled()) {
                uninstall();
            }
        }
    }

}
