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
import org.mockito.Answers;

/**
 * An annotation used on test class fields to inject mocks or spies of the class
 * under test's collaborators services. Note that that if the field is
 * initialized the value of the field will be used as a basis for creating a
 * spy.
 *
 * <p>
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface Mock {

    /**
     * This value represents index of the collaborator parameter on the class
     * under test constructor that will be mocked and injected into the test
     * class. By default this value is set to -1 to enable type based auto
     * detection. If you wish to not rely on auto detection you can explicitly
     * specify the index of the collaborator on the class under test
     * constructor.
     *
     * <p>
     * @return the index of the collaborator parameter on the class under test
     *         constructor.
     */
    int index() default -1;

    /**
     * This value represents name of the collaborator parameter on the class
     * under test constructor that will be mocked and injected into the test
     * class. By default this value is set to "" to enable type and name based
     * auto detection feature. If you do not wish to not rely on auto detection
     * you can explicitly specify the name of the collaborator on the class
     * under test constructor.
     *
     * <p>
     * Please note that name based auto detection will only work if your code is
     * compiled with debug information (javac -parameters).
     *
     * <p>
     * @return the name of the collaborator parameter on the class under test
     *         constructor.
     */
    String name() default "";

    /**
     * Specifies default answers for interacting with the mock.
     *
     * <p>
     * @return default answer to be used by mock when not stubbed.
     */
    Answers answer() default Answers.RETURNS_DEFAULTS;

    /**
     * Specifies extra interfaces the mock should implement. Might be useful for
     * legacy code or some corner cases. For background, see issue 51 <a
     * href="http://code.google.com/p/mockito/issues/detail?id=51">here</a>
     *
     * <p>
     * @return extra interfaces that should be implemented.
     */
    Class<?>[] extraInterfaces() default {};

}
