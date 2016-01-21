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

import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedProvider;
import com.fitbur.testify.need.docker.callback.PullCallback;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig.DockerClientConfigBuilder;
import static com.github.dockerjava.core.DockerClientConfig.createDefaultConfigBuilder;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import static java.util.stream.Collectors.toList;
import net.jodah.recurrent.Recurrent;
import net.jodah.recurrent.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Docker need provider.
 *
 * @author saden
 */
public class DockerNeedProvider implements NeedProvider<DockerClientConfigBuilder> {

    private final static Logger LOGGER = LoggerFactory.getLogger("docker");
    private DockerClientConfig clientConfig;
    private DockerClient client;
    private CreateContainerResponse containerResponse;

    @Override
    public DockerClientConfigBuilder configuration(NeedDescriptor descriptor) {
        return createDefaultConfigBuilder().withUri("http://0.0.0.0:2375");
    }

    @Override
    public void init(NeedDescriptor descriptor, DockerClientConfigBuilder context) {
        try {
            clientConfig = context.build();
            client = DockerClientBuilder.getInstance(clientConfig).build();

            Set<DockerContainer> dockerContainers = descriptor.getAnnotations(DockerContainer.class);

            for (DockerContainer dockerContainer : dockerContainers) {
                CountDownLatch latch = new CountDownLatch(1);
                if (dockerContainer.pull()) {
                    //TODO: check image first and only pull if it doesn't exist locally
                    PullCallback callback = new PullCallback(dockerContainer, latch, LOGGER);
                    client.pullImageCmd(dockerContainer.value())
                            .withTag(dockerContainer.tag())
                            .exec(callback);
                } else {
                    latch.countDown();
                }

                latch.await();
                String image = dockerContainer.value() + ":" + dockerContainer.tag();

                CreateContainerCmd cmd = client.createContainerCmd(image);
                cmd.withPublishAllPorts(true);

                if (!dockerContainer.cmd().isEmpty()) {
                    cmd.withCmd(dockerContainer.cmd());
                }

                if (!dockerContainer.name().isEmpty()) {
                    cmd.withName(dockerContainer.name());
                }

                containerResponse = cmd.exec();
                client.startContainerCmd(containerResponse.getId())
                        .exec();

                if (dockerContainer.await()) {
                    RetryPolicy retryPolicy = new RetryPolicy()
                            .retryOn(DockerContainerException.class)
                            .withBackoff(dockerContainer.delay(),
                                    dockerContainer.maxDelay(),
                                    dockerContainer.unit())
                            .withMaxRetries(dockerContainer.maxRetries())
                            .withMaxDuration(dockerContainer.maxDuration(), dockerContainer.unit());

                    InspectContainerResponse inspectResponse
                            = client.inspectContainerCmd(containerResponse.getId())
                            .exec();

                    String address = inspectResponse.getNetworkSettings().getIpAddress();
                    List<Integer> ports = inspectResponse.getNetworkSettings()
                            .getPorts()
                            .getBindings()
                            .entrySet()
                            .parallelStream()
                            .map(p -> p.getKey().getPort())
                            .collect(toList());

                    ContainerInstance instance = new ContainerInstance(client, inspectResponse);
                    descriptor.getServiceLocator().addConstant(instance.getHostname(), instance);

                    ports.parallelStream().forEach(p -> Recurrent.run(() -> {
                        LOGGER.info("Waiting for port '{}' to be reachable", p);
                        try (Socket socket = new Socket(address, p)) {
                            if (!socket.isConnected()) {
                                throw new DockerContainerException();
                            }
                        } catch (IOException e) {
                            throw new DockerContainerException(e);
                        }
                    }, retryPolicy));

                }

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void destroy(NeedDescriptor descriptor, DockerClientConfigBuilder context) {
        String containerId = containerResponse.getId();
        client.stopContainerCmd(containerId).exec();
        client.waitContainerCmd(containerId).exec();
        client.removeContainerCmd(containerId).exec();
    }

}
