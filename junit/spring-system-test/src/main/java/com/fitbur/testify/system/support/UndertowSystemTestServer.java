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
package com.fitbur.testify.system.support;

import io.undertow.Undertow;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.util.ReflectionUtils;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;

/**
 *
 * @author saden
 */
public class UndertowSystemTestServer {

    private final URI uri;
    private final Undertow undertow;

    public UndertowSystemTestServer(URI uri, Undertow undertow) {
        this.uri = uri;
        this.undertow = undertow;
    }

    public List<Integer> getPorts() {
        List<Integer> ports = new ArrayList<>();
        try {
            Field channelsField = ReflectionUtils.findField(Undertow.class, "channels");
            ReflectionUtils.makeAccessible(channelsField);
            List<AcceptingChannel<? extends StreamConnection>> channels = (List) ReflectionUtils.getField(channelsField, server);
            channels.stream()
                    .map(p -> getPortFromChannel(p)).
                    filter(Objects::nonNull)
                    .forEach(p -> ports.add(p));
        } catch (Exception ex) {
            // Continue
        }

        return ports;

        //URI contextURI = new URI(uri.getScheme(), null, uri.getHost(), ports.get(0), uri.getPath(), null, null);
    }

    public void stop() {

    }

    Integer getPortFromChannel(Object channel) {
        Object tcpServer = channel;
        Field sslContext = ReflectionUtils.findField(channel.getClass(), "sslContext");
        if (sslContext != null) {
            tcpServer = getTcpServer(channel);
        }

        ServerSocket socket = getSocket(tcpServer);

        return socket.getLocalPort();
    }

    Object getTcpServer(Object channel) {
        Field field = ReflectionUtils.findField(channel.getClass(), "tcpServer");
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, channel);
    }

    ServerSocket getSocket(Object tcpServer) {
        Field socketField = ReflectionUtils.findField(tcpServer.getClass(), "socket");
        if (socketField == null) {
            return null;
        }
        ReflectionUtils.makeAccessible(socketField);
        return (ServerSocket) ReflectionUtils.getField(socketField, tcpServer);
    }

}
