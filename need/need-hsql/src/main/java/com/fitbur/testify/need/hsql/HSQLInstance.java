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
package com.fitbur.testify.need.hsql;

import com.fitbur.testify.need.NeedInstance;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

/**
 * A NeedInstance implementation to hold HSQL need instance information.
 *
 * @author saden
 */
class HSQLInstance implements NeedInstance {

    private final URI jdbcURI;

    HSQLInstance(URI jdbcURI) {
        this.jdbcURI = jdbcURI;
    }

    @Override
    public String getHost() {
        return jdbcURI.getHost();
    }

    @Override
    public List<Integer> getPorts() {
        return Stream.of(jdbcURI.getPort()).collect(toList());
    }

    @Override
    public Optional<Integer> findFirstPort() {
        return of(jdbcURI.getPort());
    }

    @Override
    public List<URI> getURIs() {
        return Stream.of(jdbcURI).collect(toList());
    }

    @Override
    public Optional<URI> findFirstURI() {
        return of(jdbcURI);
    }

}
