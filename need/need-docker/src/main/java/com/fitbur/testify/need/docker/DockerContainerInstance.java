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
package com.fitbur.testify.need.docker;

import com.fitbur.guava.common.collect.ImmutableSet;
import com.fitbur.testify.need.NeedInstance;
import com.github.dockerjava.api.command.InspectContainerResponse;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toList;

/**
 * A NeedInstance implementation to hold docker container need getInstance
 * information.
 *
 * @author saden
 */
public class DockerContainerInstance implements NeedInstance<InspectContainerResponse> {

    private final InspectContainerResponse inspectResponse;

    DockerContainerInstance(InspectContainerResponse inspectResponse) {
        this.inspectResponse = inspectResponse;
    }

    @Override
    public String getHost() {
        return inspectResponse.getNetworkSettings().getIpAddress();
    }

    @Override
    public List<Integer> getPorts() {
        return inspectResponse.getNetworkSettings()
                .getPorts()
                .getBindings()
                .entrySet()
                .stream()
                .map(p -> p.getKey().getPort())
                .collect(toList());
    }

    @Override
    public Optional<Integer> findFirstPort() {
        return getPorts().stream().findFirst();
    }

    @Override
    public List<URI> getURIs() {
        InspectContainerResponse.NetworkSettings networkSettings = inspectResponse.getNetworkSettings();

        return networkSettings
                .getPorts()
                .getBindings()
                .entrySet()
                .stream()
                .map(p -> {
                    String uri = String.format(
                            "%s://%s:%d",
                            p.getKey().getProtocol(),
                            networkSettings.getIpAddress(),
                            p.getKey().getPort()
                    );

                    return URI.create(uri);
                })
                .collect(toList());
    }

    @Override
    public Optional<URI> findFirstURI() {
        InspectContainerResponse.NetworkSettings networkSettings = inspectResponse.getNetworkSettings();

        return networkSettings
                .getPorts()
                .getBindings()
                .entrySet()
                .stream()
                .findFirst()
                .map(p -> {
                    String uri = String.format(
                            "%s://%s:%d",
                            p.getKey().getProtocol(),
                            networkSettings.getIpAddress(),
                            p.getKey().getPort()
                    );

                    return URI.create(uri);
                });
    }

    @Override
    public InspectContainerResponse getInstance() {
        return inspectResponse;
    }

    @Override
    public Set<Class<InspectContainerResponse>> getContracts() {
        return ImmutableSet.of(InspectContainerResponse.class);
    }

}
