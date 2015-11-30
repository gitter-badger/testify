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

import com.fitbur.testify.di.ServiceLocator;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A descriptor class passed to {@link NeedProvider} to instantiate a need
 * instance.
 *
 * @author saden
 */
public interface NeedDescriptor {

    /**
     * Get the need annotation itself.
     *
     * @return the need annotation.
     */
    Need getNeed();

    /**
     * Get the test instance the need is for.
     *
     * @return an instance of the test class
     */
    Object getTestInstance();

    /**
     * Get the test class.
     *
     * @return the test class
     */
    Class<?> getTestClass();

    /**
     * Get the name of the test class the need is for.
     *
     * @return the test class name
     */
    String getTestClassName();

    /**
     * Get an optional service locator associated with the need.
     *
     * @return an optional containing the service locator, an empty otherwise
     */
    Optional<? extends ServiceLocator> getServiceLocator();

    /**
     * Get an optional configuration method on the test class with the given
     * parameter types used to configure the need.
     *
     * @param parameterTypes the configuration method parameter types
     * @return optional containing the configuration method, an empty otherwise
     *
     */
    Optional<Method> getConfigMethod(Class... parameterTypes);
}
