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

import com.fitbur.guava.common.reflect.ClassPath;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author saden
 */
public class BadSetupTest {

    @Test
    public void runBadTestCases() throws IOException {
        Logger logger = LoggerFactory.getLogger("testify");
        String testPackage = "com.fitbur.testify.junit.bad";

        logger.warn("Running Bad Test Cases in package '{}'", testPackage);

        JUnitCore core = new JUnitCore();

        ClassPath classPath = ClassPath.from(BadSetupTest.class.getClassLoader());
        Class<?>[] classes = classPath.getTopLevelClassesRecursive(testPackage)
                .stream()
                .map(p -> p.load())
                .toArray(Class<?>[]::new);

        Result result = core.run(classes);
        assertThat(result).isNotNull();
        assertThat(result.getFailureCount()).isEqualTo(classes.length);

        result.getFailures().stream().forEach((failure) -> {
            logger.warn("{} Failed due to: {}",
                    failure.getDescription().getTestClass().getSimpleName(),
                    failure.getMessage());
        });
    }

}
