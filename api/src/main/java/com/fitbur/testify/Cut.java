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
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation used on single field of a test class to denote the field as the
 * class under test and to instruct the framework to wire up the class under
 * test with its collaborators.
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface Cut {

    /**
     * <p>
     * Indicates whether a mock that delegates to the class under test instance
     * is created. This is useful if you wish to stub or verify c package
     * private methods of the class under test instance.
     * </p>
     * <p>
     * By default a delegating mock is not created.
     * </p>
     *
     * @return true if a delegating mock is create, false otherwise.
     */
    boolean value() default false;
}
