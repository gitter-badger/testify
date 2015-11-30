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
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;

/**
 * A junit listener that listens for test case execution life-cycle.
 *
 * @author saden
 */
public class UnitTestRunListener extends RunListener {

    private final Logger logger;
    private final TestContext testContext;

    UnitTestRunListener(TestContext testContext, Logger logger) {
        this.testContext = testContext;
        this.logger = logger;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void testStarted(Description description) throws Exception {
        logger.info("Running {}", description.getMethodName());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        String methodName = failure.getDescription().getMethodName();
        String traceMessage = failure.getTrace();
        logger.error("Failed: {}\n{}", methodName, traceMessage);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        logger.debug("Finished {}", description.getMethodName());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        String methodName = description.getMethodName();
        logger.warn("Ignored: {}", methodName);
    }

}
