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

/**
 * A contract that defines methods for creating and destroying a server.
 *
 * @author saden
 * @param <T> the server context type
 */
public interface ServerProvider<T> {

    /**
     * <p>
     * Method that must be implemented to configure a server. Configuring a
     * server typically involves creating or using an existing a server
     * configuration object so that a test class method annotated with
     * {@link com.fitbur.testify.Config} can be called to further fine tune
     * server configuration.
     * </p>
     * <p>
     * Note that implementation of this method should not do any work beyond
     * returning a mutable configuration object. It should not perform
     * instantiation of the server as that should be handled in
     * {@link #init(com.fitbur.testify.server.ServerDescriptor, java.lang.Object)}
     * method.
     * </p>
     *
     * @param descriptor the server descriptor
     * @return the server configuration object
     */
    T configuration(ServerDescriptor descriptor);

    /**
     * <p>
     * Instantiate the server.
     * </p>
     *
     * @param descriptor the server descriptor
     * @param context the server context
     * @return the server instance.
     */
    ServerInstance init(ServerDescriptor descriptor, T context);

    /**
     * Destroy the server with the given descriptor.
     *
     * @param descriptor the server descriptor
     * @param instance the server instance
     * @param context the server context
     */
    default void destroy(ServerDescriptor descriptor, ServerInstance instance, T context) {
    }
}
