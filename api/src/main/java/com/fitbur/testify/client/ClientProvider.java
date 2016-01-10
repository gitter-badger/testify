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
package com.fitbur.testify.client;

/**
 * A contract that defines methods for creating and destroying a client.
 *
 * @author saden
 * @param <T> the client context type
 * @param <S> the client type
 */
public interface ClientProvider<T, S> {

    /**
     * <p>
     * Method that must be implemented to configure a client. Configuring a
     * client typically involves creating or using an existing a client
     * configuration object so that a test class method annotated with
     * {@link com.fitbur.testify.Config} can be called to further fine tune
     * client configuration.
     * </p>
     * <p>
     * Note that implementation of this method should not do any work beyond
     * returning a mutable configuration object. It should not perform
     * instantiation of the client as that should be handled in
     * {@link #init(ClientDescriptor, java.lang.Object)} method.
     * </p>
     *
     * @param descriptor the client descriptor
     * @return the client configuration object
     */
    T configuration(ClientDescriptor descriptor);

    /**
     * <p>
     * Instantiate the client.
     * </p>
     *
     * @param descriptor the client descriptor
     * @param context the client context
     * @return the client instance.
     */
    ClientInstance<S> init(ClientDescriptor descriptor, T context);

    /**
     * Destroy the client with the given descriptor.
     *
     * @param descriptor the client descriptor
     * @param instance the client instance
     * @param context the client context
     */
    default void destroy(ClientDescriptor descriptor, ClientInstance<S> instance, T context) {
    }
}
