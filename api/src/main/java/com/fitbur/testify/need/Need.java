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
package com.fitbur.testify.need;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation for specifying a need class that should be loaded for a test
 * class. This is useful for integration and system tests which require an
 * external resource to be loaded (i.e. an in-memory database).
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Repeatable(Needs.class)
public @interface Need {

    /**
     * Specifies a need implementation class that should be loaded.
     *
     * @return a need class.
     */
    Class<? extends NeedProvider> value();

}
