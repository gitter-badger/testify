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
 * An annotation used on test class fields to inject real instance of the class
 * under test's collaborators. Note that you can optionally enable spying on the
 * real instance by setting {@link  #value()} to true.
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface Real {

    /**
     * <p>
     * Indicates whether mock that delegates to the real instance should be
     * created. This is useful if you wish to stub or verify c package private
     * methods of the class under test instance. Be very careful how you use
     * this since delegating to the real instance can have undesirable side
     * effect.
     * </p>
     * <p>
     * By default a delegating mock is not created.
     * </p>
     *
     * @return true if a mock delegating mock is created, false otherwise.
     */
    boolean value() default false;
}
