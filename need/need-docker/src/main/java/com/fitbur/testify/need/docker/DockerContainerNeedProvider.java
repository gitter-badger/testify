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

import com.fitbur.guava.common.collect.ImmutableMap;
import com.fitbur.testify.need.NeedContainer;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedInstance;
import com.fitbur.testify.need.NeedProvider;
import com.fitbur.testify.need.docker.callback.PullCallback;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.NetworkSettings;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig.DockerClientConfigBuilder;
import static com.github.dockerjava.core.DockerClientConfig.createDefaultConfigBuilder;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
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
public class DockerContainerNeedProvider implements NeedProvider<DockerClientConfigBuilder> {

    public static final String DEFAULT_DAEMON_URI = "http://127.0.0.1:2375";

    private final static Logger LOGGER = LoggerFactory.getLogger("docker");
    private DockerClientConfig clientConfig;
    private DockerClient client;
    private CreateContainerResponse containerResponse;

    @Override
    public DockerClientConfigBuilder configuration(NeedDescriptor descriptor) {
        return createDefaultConfigBuilder().withUri(DEFAULT_DAEMON_URI);
    }

    @Override
    public Map<String, NeedInstance> init(NeedDescriptor descriptor, DockerClientConfigBuilder context) {
        try {
            clientConfig = context.build();
            client = DockerClientBuilder.getInstance(clientConfig).build();

            Set<NeedContainer> dockerContainers = descriptor.getAnnotations(NeedContainer.class);
            ImmutableMap.Builder<String, NeedInstance> needInstances = ImmutableMap.builder();

            for (NeedContainer needContainer : dockerContainers) {
                CountDownLatch latch = new CountDownLatch(1);
                if (needContainer.pull()) {
                    //TODO: check value first and only pull if it doesn't exist locally
                    PullCallback callback = new PullCallback(needContainer, latch, LOGGER);
                    client.pullImageCmd(needContainer.value())
                            .withTag(needContainer.version())
                            .exec(callback);
                } else {
                    latch.countDown();
                }

                latch.await();
                String image = needContainer.value() + ":" + needContainer.version();

                CreateContainerCmd cmd = client.createContainerCmd(image);
                cmd.withPublishAllPorts(true);

                if (!needContainer.cmd().isEmpty()) {
                    cmd.withCmd(needContainer.cmd());
                }

                if (!needContainer.name().isEmpty()) {
                    cmd.withName(needContainer.name());
                }

                containerResponse = cmd.exec();
                client.startContainerCmd(containerResponse.getId())
                        .exec();

                if (needContainer.await()) {
                    RetryPolicy retryPolicy = new RetryPolicy()
                            .retryOn(IllegalStateException.class)
                            .withBackoff(needContainer.delay(),
                                    needContainer.maxDelay(),
                                    needContainer.unit())
                            .withMaxRetries(needContainer.maxRetries())
                            .withMaxDuration(needContainer.maxDuration(), needContainer.unit());

                    InspectContainerResponse inspectResponse
                            = client.inspectContainerCmd(containerResponse.getId())
                            .exec();

                    NetworkSettings networkSettings = inspectResponse.getNetworkSettings();

                    String address = networkSettings.getIpAddress();
                    List<Integer> ports = networkSettings
                            .getPorts()
                            .getBindings()
                            .entrySet()
                            .parallelStream()
                            .map(p -> p.getKey().getPort())
                            .collect(toList());

                    DockerContainerInstance containerInstance = new DockerContainerInstance(inspectResponse);
                    needInstances.put(inspectResponse.getId(), containerInstance);

                    ports.parallelStream().forEach(p -> Recurrent.run(() -> {
                        LOGGER.info("Waiting for port '{}' to be reachable", p);
                        try (Socket socket = new Socket(address, p)) {
                            if (!socket.isConnected()) {
                                throw new IllegalStateException();
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }, retryPolicy));

                }

            }

            return needInstances.build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void destroy(NeedDescriptor descriptor, DockerClientConfigBuilder context) {
        String containerId = containerResponse.getId();
        client.stopContainerCmd(containerId).exec();
        client.removeContainerCmd(containerId).exec();
    }

}
