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
package com.fitbur.testify;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation for specifying a list of packages that will be scanned and
 * loaded by the test class. This is useful for integration and system tests
 * which utilize a dependency injection framework to scan for services (i.e.
 * Spring HK2, Jersey).
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
public @interface Scans {

    /**
     * Specifies a list of packages that will be scanned and loaded.
     *
     * @return an array of packages to scan.
     */
    Scan[] value();

}
