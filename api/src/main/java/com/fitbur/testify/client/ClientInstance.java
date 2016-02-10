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

import java.net.URI;

/**
 * A contract that defines methods for getting information a client.
 *
 * @author saden
 * @param <T> the client type
 */
public interface ClientInstance<T> {

    /**
     * The base server URI used by the client.
     *
     * @return the base URI.
     */
    URI getURI();

    /**
     * Get a configured client instance.
     * <p>
     * Note that the instance returned could be the same as the instance
     * returned by {@link #getClient()}
     * </p>
     *
     * @return a configured client instance
     */
    T getTarget();

    /**
     * Get the underlying client instance.
     *
     * @param <S> the underlying client
     * @return a client instance
     */
    <S> S getClient();

    /**
     * Close the client instance.
     */
    void close();

}
