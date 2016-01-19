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

import com.fitbur.testify.need.NeedInstance;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Need instance.
 *
 * @author saden
 */
public class ContainerInstance implements NeedInstance<InspectContainerResponse> {

    private final DockerClient client;
    private final InspectContainerResponse inspectResponse;

    public ContainerInstance(DockerClient client,
            InspectContainerResponse inspectResponse) {
        this.client = client;
        this.inspectResponse = inspectResponse;
    }

    @Override
    public String getHostname() {
        return inspectResponse.getConfig().getHostName();
    }

    @Override
    public String getIpAddress() {
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
    public Optional<Integer> getFirstPort() {
        return getPorts().stream().findFirst();
    }

    @Override
    public Optional<URI> getURI() {
        InspectContainerResponse.NetworkSettings networkSettings = inspectResponse.getNetworkSettings();
        Optional<Map.Entry<ExposedPort, Ports.Binding[]>> result = networkSettings
                .getPorts()
                .getBindings()
                .entrySet()
                .stream()
                .findFirst();

        if (result.isPresent()) {
            Map.Entry<ExposedPort, Ports.Binding[]> entry = result.get();
            String uri = String.format(
                    "%s://%s:%d",
                    entry.getKey().getProtocol(),
                    networkSettings.getIpAddress(),
                    entry.getKey().getPort()
            );

            return of(URI.create(uri));
        }
        return empty();
    }

    @Override
    public InspectContainerResponse getNeed() {
        return inspectResponse;
    }

    @Override
    public void start() {
        client.startContainerCmd(inspectResponse.getId()).exec();
    }

    @Override
    public void stop() {
        client.stopContainerCmd(inspectResponse.getId()).exec();
    }

}
