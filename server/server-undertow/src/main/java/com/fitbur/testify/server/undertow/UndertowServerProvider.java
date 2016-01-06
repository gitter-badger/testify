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

import com.fitbur.testify.App;
import com.fitbur.testify.server.ServerDescriptor;
import com.fitbur.testify.server.ServerInstance;
import com.fitbur.testify.server.ServerProvider;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import java.net.URI;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;

/**
 * An Undertow server provider implementation.
 * @author saden
 */
public class UndertowServerProvider implements ServerProvider<DeploymentInfo> {

    @Override
    public DeploymentInfo configuration(ServerDescriptor descriptor) {
        try {
            App app = descriptor.getApp();
            Set<Class<?>> handles = descriptor.getHandlesType();
            String name = descriptor.getTestClassName();
            Class<? extends ServletContainerInitializer> servletType = descriptor.getServletContainerInitializer();
            ServletContainerInitializer servlet = servletType.newInstance();
            ImmediateInstanceFactory<ServletContainerInitializer> factory = new ImmediateInstanceFactory<>(servlet);
            URI uri = URI.create("http://0.0.0.0:0/");
            ServletContainerInitializerInfo initInfo
                    = new ServletContainerInitializerInfo(servletType, factory, handles);

            DeploymentInfo deploymentInfo = Servlets.deployment()
                    .addServletContainerInitalizer(initInfo)
                    .setClassLoader(descriptor.getTestClass().getClassLoader())
                    .setHostName(uri.getHost())
                    .setContextPath(uri.getPath())
                    .setDeploymentName(name)
                    .addServlet(Servlets.servlet(name, DefaultServlet.class));

            return deploymentInfo;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ServerInstance init(ServerDescriptor descriptor, DeploymentInfo deploymentInfo) {
        try {
            DeploymentManager manager = Servlets.defaultContainer()
                    .addDeployment(deploymentInfo);

            manager.deploy();
            HttpHandler httpHandler = manager.start();

            RedirectHandler defaultHandler = Handlers.redirect(deploymentInfo.getContextPath());
            PathHandler pathHandler = Handlers.path(defaultHandler);
            pathHandler.addPrefixPath(deploymentInfo.getContextPath(), httpHandler);

            Undertow undertow = Undertow.builder()
                    .addHttpListener(0, deploymentInfo.getHostName(), pathHandler)
                    .build();

            return new UndertowServerInstance(undertow, deploymentInfo);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void destroy(ServerDescriptor descriptor, ServerInstance instance, DeploymentInfo context) {
        instance.stop();
    }

}
