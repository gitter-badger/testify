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
 * An annotation for specifying a module or application class that should be
 * loaded by the test class. This is useful for integration and system tests to
 * load a module or an entire application for testing purpose.
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Repeatable(Modules.class)
public @interface Module {

    /**
     * <p>
     * A value that represents a module class that will be loaded.
     * </p>
     * <p>
     * Please note that to encourage simplicity and modular design loading of
     * modules is limited to a single module class. If you absolutely need to
     * load multiple modules this annotation is repeatable and you may add
     * additional @Module annotations to your test class.
     * </p>
     *
     * @return a module class.
     */
    Class<?> value();

}
