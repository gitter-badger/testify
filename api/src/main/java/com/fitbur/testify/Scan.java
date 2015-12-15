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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation for specifying a package that should be scanned and loaded by
 * the test class. This is useful for integration and system tests which utilize
 * a dependency injection framework to scan packages (i.e. Spring, HK2, Jersey).
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Repeatable(Scans.class)
public @interface Scan {

    /**
     * <p>
     * A value that represents a package name that will be scanned.
     * </p>
     * <p>
     * Please note that to encourage simplicity and modular design loading of
     * modules is limited to a single package. If you absolutely need to scan
     * multiple packages {
     *
     * @Scan} annotation is repeatable and you may additional {
     * @Scan} annotation to your test class.
     * </p>
     *
     * @return a package name.
     */
    String value();

}
