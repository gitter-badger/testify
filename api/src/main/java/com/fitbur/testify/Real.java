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
 * <p>
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface Real {

    /**
     * Indicates whether the real instance should be spied upon. This is
     * usefully if you wish to stub or verify calls to protected/private methods
     * of the real instance. Be very careful how you use this since stubbing a
     * spy object can lead to calls to the real object which often is not
     * desirable.
     *
     * <p>
     * By default spying on a real instance is set to false.
     *
     * <p>
     * @return true if real instance should be spied on.
     */
    boolean value() default false;
}
