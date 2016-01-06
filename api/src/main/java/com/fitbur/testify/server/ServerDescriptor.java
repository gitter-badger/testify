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
package com.fitbur.testify.server;

import com.fitbur.testify.App;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;

/**
 * A descriptor class that describes the instantiate a server instance.
 *
 * @author saden
 */
public interface ServerDescriptor {

    /**
     * Get the application annotation.
     *
     * @return the need annotation.
     */
    App getApp();

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
     * Get the name of the test class the need is for.
     *
     * @return the test class name
     */
    Set<Class<?>> getHandlesType();

    /**
     * The servlet container initializer used to detect and create the servlet.
     *
     * @param <T> the type of the servlet initializer
     * @return the servlet initializer class.
     */
    <T extends ServletContainerInitializer> Class<T> getServletContainerInitializer();
}
