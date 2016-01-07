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
package com.fitbur.testify.server.undertow;

import com.fitbur.testify.server.ServerInstance;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;

/**
 * An Undertow based implementation of a server instance.
 *
 * @author saden
 */
public class UndertowServerInstance implements ServerInstance {

    private final Undertow undertow;
    private final DeploymentInfo deploymentInfo;
    private URI baseURI;

    UndertowServerInstance(Undertow undertow, DeploymentInfo deploymentInfo) {
        this.undertow = undertow;
        this.deploymentInfo = deploymentInfo;
    }

    @Override
    public void start() {
        try {
            undertow.start();
            baseURI = new URI("http",
                    null,
                    deploymentInfo.getHostName(),
                    getPorts().get(0),
                    deploymentInfo.getContextPath(),
                    null,
                    null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void stop() {
        undertow.stop();
    }

    @Override
    public URI getBaseURI() {
        return baseURI;
    }

    public List<Integer> getPorts() {
        List<Integer> ports = new ArrayList<>();
        Field channelsField = findField(Undertow.class, "channels").get();
        channelsField.setAccessible(true);
        List<AcceptingChannel<? extends StreamConnection>> channels = (List) getFieldValue(channelsField, undertow);
        channels.stream()
                .map(p -> getPortFromChannel(p)).
                filter(Objects::nonNull)
                .forEach(p -> ports.add(p));

        return ports;

    }

    Integer getPortFromChannel(Object channel) {
        Object tcpServer = channel;
        Optional<Field> sslContext = findField(channel.getClass(), "sslContext");
        if (sslContext.isPresent()) {
            tcpServer = getTcpServer(channel);
        }

        ServerSocket socket = getSocket(tcpServer);

        return socket.getLocalPort();

    }

    Object getTcpServer(Object channel) {
        Field field = findField(channel.getClass(), "tcpServer").get();
        return getFieldValue(field, channel);
    }

    ServerSocket getSocket(Object tcpServer) {
        Optional<Field> socketField = findField(tcpServer.getClass(), "socket");
        if (!socketField.isPresent()) {
            return null;
        }

        return getFieldValue(socketField.get(), tcpServer);
    }

    public Optional<Field> findField(Class<?> type, String name) {
        try {
            return of(type.getDeclaredField(name));
        } catch (Exception e) {
            return empty();
        }
    }

    public <T> T getFieldValue(Field field, Object instance) {
        try {
            field.setAccessible(true);

            return (T) field.get(instance);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
