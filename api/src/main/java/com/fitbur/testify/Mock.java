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
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation used on test class fields to inject mocks of the class under
 * test's collaborators. Note that if the field is initialized the value of the
 * field will be injected into the class under test.
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface Mock {

    /**
     * <p>
     * This value represents the collaborator's class under test constructor
     * parameter index that will be mocked and injected into the test class and
     * the class under test.
     * </p>
     * <p>
     * By default this value is set to -1 to enable auto detection. If you wish
     * to not rely on auto detection you can explicitly specify the
     * collaborator's class under test constructor parameter index.
     * </p>
     *
     * @return the collaborator's class under test constructor parameter index.
     */
    int index() default -1;

    /**
     * <p>
     * This value represents the collaborator's class under test constructor
     * parameter name that will be mocked and injected into both the test class
     * and the class under test.
     * </p>
     * <p>
     * By default this value is set to "" to enable auto detection. If you wish
     * to not rely on auto detection you can explicitly specify the
     * collaborator's class under test constructor parameter name.
     * </p>
     * <p>
     * Please note that name based auto detection works if your code is compiled
     * with debug information (javac -parameters or javac -g:vars).
     * </p>
     *
     * @return the collaborator's class under test constructor parameter name.
     */
    String name() default "";

}
