/*
 * Copyright 2016 Sharmarke Aden.
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
package com.fitbur.testify.junit.system.internal;

import com.fitbur.testify.server.ServerInstance;
import java.net.URI;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;

/**
 *
 * @author saden
 */
public class SpringBootServerInstance implements ServerInstance {

    private final EmbeddedServletContainer servletContainer;
    private final URI baseURI;

    public SpringBootServerInstance(EmbeddedServletContainer servletContainer, URI baseURI) {
        this.servletContainer = servletContainer;
        this.baseURI = baseURI;
    }

    @Override
    public URI getURI() {
        return baseURI;
    }

    @Override
    public Object getServer() {
        return servletContainer;
    }

    @Override
    public void start() {
        servletContainer.start();
    }

    @Override
    public void stop() {
        servletContainer.stop();
    }

}
