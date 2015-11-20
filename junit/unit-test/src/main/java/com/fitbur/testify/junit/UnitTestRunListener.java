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

import com.fitbur.testify.TestContext;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A junit listener that listens for test case execution life-cycle.
 *
 * @author saden
 */
public class UnitTestRunListener extends RunListener {

    private final Logger log = getLogger("test");
    private final String name;
    private final TestClass testClass;
    private final Map<Class, TestContext> contexts;

    UnitTestRunListener(String name, TestClass testClass, Map<Class, TestContext> contexts) {
        this.name = name;
        this.testClass = testClass;
        this.contexts = contexts;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        log.info("Test Case Started: {}", description);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        log.info("Test Case Failed: {}", failure);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        log.info("Test Case Finished: {}", description);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        log.info("Test Case Ignored: {}", description);
    }

}
