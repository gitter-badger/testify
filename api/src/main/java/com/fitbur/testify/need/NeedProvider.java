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

/**
 * A contract that defines methods for creating and destroying a need.
 *
 * @author saden
 * @param <T> the need context type
 */
public interface NeedProvider<T> {

    /**
     * <p>
     * Method that must be implemented to configure a need. Configuring a need
     * typically involves creating a need configuration object so that a test
     * class method annotated with {@link com.fitbur.testify.Config} can be
     * called to further fine tune need.
     * </p>
     * <p>
     * Note that implementation of this method should not do any work beyond
     * returning a mutable configuration object. It should not perform
     * instantiation of the need as that should be handled in
     * {@link #init(com.fitbur.testify.need.NeedDescriptor, java.lang.Object)}
     * method.
     * </p>
     *
     * @param descriptor the need descriptor
     * @return the need configuration object
     */
    T configuration(NeedDescriptor descriptor);

    /**
     * <p>
     * Instantiate the need based on the given descriptor and configuration.
     * Implementations of the need should
     * </p>
     *
     * @param descriptor the need descriptor
     * @param context the configuration context object
     */
    void init(NeedDescriptor descriptor, T context);

    /**
     * Destroy the need with the given descriptor and configuration.
     *
     * @param descriptor the need descriptor
     * @param context the configuration context object
     */
    default void destroy(NeedDescriptor descriptor, T context) {
    }

}
