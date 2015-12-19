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
package com.fitbur.testify.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * A contract that defines methods for working with various dependency injection
 * frameworks to add services and modules as well as retrieve services.
 *
 * @author saden
 */
public interface ServiceLocator {

    /**
     * Initialize the service locator.
     */
    void init();

    /**
     * Destroy the service locator.
     */
    void destroy();

    /**
     * Determine if the service locator is running.
     *
     * @return true if the service locator is running, false otherwise
     */
    boolean isActive();

    /**
     * Reload the service locator.
     */
    default void reload() {
    }

    /**
     * Get the context object associated with the service locator.
     *
     * @param <T> the type of the context object
     * @return the service locator context object.
     */
    <T> T getContext();

    /**
     * The name of the service locator.
     *
     * @return the name of the service
     */
    String getName();

    /**
     * Get a service with the given name.
     *
     * @param <T> the service type
     * @param name the service name
     * @return an instance of the service
     */
    <T> T getService(String name);

    /**
     * Get a service with the given type.
     *
     * @param <T> the service type
     * @param type the service class
     * @return an instance of the service
     */
    <T> T getService(Type type);

    /**
     * Get a service with the given type and name.
     *
     * @param <T> the service type
     * @param type the service class
     * @param name the service name
     * @return an instance of the service
     */
    <T> T getService(Type type, String name);

    /**
     * Get a service with the given name.
     *
     * @param <T> the service type
     * @param type the service type
     * @param annotations the service annotations
     * @return an instance of the service
     */
    <T> T getService(Type type, Set<? extends Annotation> annotations);

    /**
     * Get a service with the given type and arguments.
     *
     * @param <T> the service type
     * @param type the service class
     * @param arguments the service arguments
     * @return an instance of the service
     */
    <T> T getServiceWith(Class<T> type, Object... arguments);

    /**
     * Get a service with the given name and arguments.
     *
     * @param <T> the service type
     * @param name the service name
     * @param arguments the service arguments
     * @return an instance of the service
     */
    <T> T getServiceWith(String name, Object... arguments);

    /**
     * Add a service with the given type.
     *
     * @param type the service type
     */
    void addService(Class<?> type);

    /**
     * Add a service with the given descriptor.
     *
     * @param descriptor the service descriptor
     */
    void addService(ServiceDescriptor descriptor);

    /**
     * Remove a service with the given descriptor.
     *
     * @param descriptor the service descriptor
     */
    default void removeService(ServiceDescriptor descriptor) {
    }

    /**
     * Add a service with the given name.
     *
     * @param name the service name
     */
    default void removeService(String name) {
    }

    /**
     * Add the given module.
     *
     * @param type the class of the module
     */
    void addModule(Class<?> type);

    /**
     * Remove the given module.
     *
     * @param type the class of the module
     */
    default void removeModule(Class<?> type) {
    }

    /**
     * Scan the given package.
     *
     * @param packageName the package name
     */
    void scanPackage(String packageName);

    /**
     * Get the annotations supported by the service locator.
     *
     * @return service annotations instance.
     */
    ServiceAnnotations getServiceAnnotations();
}
