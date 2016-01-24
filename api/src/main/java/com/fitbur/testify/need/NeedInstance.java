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

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * A contract that defines methods for getting information about the running
 * need as well as managing its lifecycle.
 *
 * @author saden
 * @param <T> the underlying need getInstance type
 */
public interface NeedInstance<T> {

    /**
     * The need ip address or hostname.
     *
     * @return the need ip address or hostname.
     */
    String getHost();

    /**
     * The need ports.
     *
     * @return the need ports
     */
    List<Integer> getPorts();

    /**
     * The need first port.
     *
     * @return the need ports
     */
    Optional<Integer> findFirstPort();

    /**
     * Get all the URIs.
     *
     * @return a list of URIs, empty list otherwise.
     */
    List<URI> getURIs();

    /**
     * Find the first need base URI.
     *
     * @return the first URI, empty optional otherwise.
     */
    Optional<URI> findFirstURI();

    /**
     * Get the underlying need getInstance.
     *
     * @return the underlying need getInstance
     */
    T getInstance();

}
