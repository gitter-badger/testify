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
package com.fitbur.testify.server;

import com.fitbur.testify.di.ServiceLocator;
import java.util.Objects;

/**
 * A small context object that contains server contextual information. This
 * context is used in conjunction with {@link ServerProvider} to manage the
 * life-cycle of a server.
 *
 * @author saden
 */
public class ServerContext {

    private final ServerProvider provider;
    private final ServerDescriptor descriptor;
    private final ServerInstance instance;
    private final ServiceLocator locator;
    private final Object config;

    public ServerContext(ServerProvider provider,
            ServerDescriptor descriptor,
            ServerInstance serverInstance,
            ServiceLocator locator,
            Object config) {
        this.provider = provider;
        this.descriptor = descriptor;
        this.instance = serverInstance;
        this.locator = locator;
        this.config = config;
    }

    /**
     * Get the server provider.
     *
     * @return the server provider
     */
    public ServerProvider getProvider() {
        return provider;
    }

    /**
     * Get the server descriptor.
     *
     * @return the server descriptor.
     */
    public ServerDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get the server configuration object.
     *
     * @return the server configuration.
     */
    public Object getConfig() {
        return config;
    }

    /**
     * Get the server instance.
     *
     * @return the server instance.
     */
    public ServerInstance getInstance() {
        return instance;
    }

    /**
     * Get the server service locator.
     *
     * @return the server service locator.
     */
    public ServiceLocator getLocator() {
        return locator;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.provider);
        hash = 17 * hash + Objects.hashCode(this.descriptor);
        hash = 17 * hash + Objects.hashCode(this.instance);
        hash = 17 * hash + Objects.hashCode(this.locator);
        hash = 17 * hash + Objects.hashCode(this.config);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerContext other = (ServerContext) obj;
        if (!Objects.equals(this.provider, other.provider)) {
            return false;
        }
        if (!Objects.equals(this.descriptor, other.descriptor)) {
            return false;
        }
        if (!Objects.equals(this.instance, other.instance)) {
            return false;
        }
        if (!Objects.equals(this.locator, other.locator)) {
            return false;
        }
        return Objects.equals(this.config, other.config);
    }

    @Override
    public String toString() {
        return "ServerContext{"
                + "provider=" + provider
                + ", descriptor=" + descriptor
                + ", instance=" + instance
                + ", locator=" + locator
                + ", config=" + config
                + '}';
    }

}
