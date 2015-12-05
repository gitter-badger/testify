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

import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceDescriptorBuilder;
import com.fitbur.testify.di.ServiceLocator;
import static com.fitbur.testify.di.ServiceScope.SINGLETON;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedProvider;
import static com.google.common.base.Preconditions.checkState;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import static java.lang.String.format;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * An in memory implementation of a HSQL test need provider.
 *
 * @author saden
 */
public class InMemoryHSQL implements NeedProvider<HikariConfig> {

    @Override
    public HikariConfig configure(NeedDescriptor descriptor) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(format("jdbc:hsqldb:mem:%s", descriptor.getTestClassName()));
        hikariConfig.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("");

        Optional<Method> configMethod = descriptor.getConfigMethod(HikariConfig.class);
        if (configMethod.isPresent()) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                Method method = configMethod.get();

                try {
                    method.setAccessible(true);
                    method.invoke(descriptor.getTestInstance(), hikariConfig);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    checkState(false, "Call to config method '%s' in test class '%s' failed.",
                            method.getName(), descriptor.getTestClassName());
                }
                return null;
            });
        }

        Optional<? extends ServiceLocator> locator = descriptor.getServiceLocator();
        if (locator.isPresent()) {
            ServiceLocator serviceLocator = locator.get();

            ServiceDescriptor serviceDescriptor = new ServiceDescriptorBuilder()
                    .name(descriptor.getTestClassName())
                    .type(HikariDataSource.class)
                    .scope(SINGLETON)
                    .arguments(hikariConfig)
                    .lazy(true)
                    .injectable(true)
                    .primary(true)
                    .build();

            serviceLocator.addService(serviceDescriptor);
        }

        return hikariConfig;
    }

    @Override
    public void init(NeedDescriptor descriptor, HikariConfig config) {
        HikariDataSource dataSource = new HikariDataSource(config);

        try {
            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK");
            } catch (SQLException e) {
                throw e;
            }

        } catch (SQLException e) {
            checkState(false, "Need provider '%s' 'init' failed.\n%s",
                    this.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void destroy(NeedDescriptor descriptor, HikariConfig config) {
        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("SHUTDOWN");
            } catch (SQLException e) {
                throw e;
            }

        } catch (SQLException e) {
            checkState(false, "Need provider '%' failed shutdown the database.\n%s",
                    this.getClass().getSimpleName(), e.getMessage());
        }

        Optional<? extends ServiceLocator> serviceLocator = descriptor.getServiceLocator();

        if (serviceLocator.isPresent()) {
            ServiceLocator locator = serviceLocator.get();
            if (locator.isActive()) {
                locator.removeService(descriptor.getTestClassName());
            }
        }
    }

}
