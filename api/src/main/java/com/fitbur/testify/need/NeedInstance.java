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
 * @param <T> the need type
 */
public interface NeedInstance<T> {

    /**
     * The need ip address.
     *
     * @return the need host
     */
    String getIpAddress();

    /**
     * The need hostname.
     *
     * @return the need hostname
     */
    String getHostname();

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
    Optional<Integer> getFirstPort();

    /**
     * The need base URI.
     *
     * @return the base URI, empty optional otherwise.
     */
    Optional<URI> getURI();

    /**
     * The underlying need instance.
     *
     * @return the need instance
     */
    T getNeed();

    /**
     * Start the need.
     */
    void start();

    /**
     * Stop the need.
     */
    void stop();

    /**
     * Restart the need.
     */
    default void restart() {
    }

}
