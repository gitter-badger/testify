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
package com.fitbur.testify.hsql;

import com.fitbur.testify.Config;
import com.fitbur.testify.di.ServiceDescriptor;
import com.fitbur.testify.di.ServiceDescriptorBuilder;
import com.fitbur.testify.di.ServiceLocator;
import static com.fitbur.testify.di.ServiceScope.SINGLETON;
import com.fitbur.testify.need.TestNeed;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import static java.lang.String.format;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static java.security.AccessController.doPrivileged;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

/**
 * An in memory implementation of a HSQL test need.
 *
 * @author saden
 */
public class InMemoryHSQL implements TestNeed<HSQLContext> {

    @Override
    public HSQLContext init(Object testInstance, ServiceLocator serviceLocator) {
        String dataSourceName = serviceLocator.getName() + "DataSource";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(format("jdbc:hsqldb:mem:test-%s", serviceLocator.getName()));
        config.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        config.setUsername("sa");
        config.setPassword("");

//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ServiceDescriptor descriptor = new ServiceDescriptorBuilder()
                .name(dataSourceName)
                .type(HikariDataSource.class)
                .scope(SINGLETON)
                .arguments(config)
                .lazy(false)
                .injectable(true)
                .primary(true)
                .build();

        HSQLContext context = new HSQLContext(testInstance, descriptor, serviceLocator);

        config(context);

        serviceLocator.addService(descriptor);
        return context;
    }

    @Override
    public void config(HSQLContext context) {
        Object[] arguments = context.getDescriptor().getArguments();
        Object testInstance = context.getTestInstance();
        Class<? extends Object> testClass = testInstance.getClass();
        Set<Method> methods = of(testClass.getDeclaredMethods())
                .filter(p -> p.isAnnotationPresent(Config.class))
                .collect(toSet());

        methods.forEach(p -> {
            Class<?>[] paramTypes = p.getParameterTypes();
            Object[] methodArgs = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                for (Object argument : arguments) {
                    if (argument.getClass().equals(paramType)) {
                        methodArgs[i] = argument;
                    }
                }
            }

            doPrivileged((PrivilegedAction<Object>) () -> {
                try {
                    p.setAccessible(true);
                    p.invoke(testInstance, methodArgs);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        });

    }

    @Override

    public void destroy(HSQLContext context) {
        ServiceDescriptor descriptor = context.getDescriptor();
        try (HikariDataSource dataSource = context.getServiceLocator()
                .getServiceWith(descriptor.getType())) {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            try {
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new HSQLTestException(e);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new HSQLTestException(e);
        }
    }

    @Override
    public void before(HSQLContext context) {
        try {
            ServiceDescriptor descriptor = context.getDescriptor();
            HikariDataSource dataSource = context.getServiceLocator()
                    .getServiceWith(descriptor.getType());
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK");
            try {
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new HSQLTestException(e);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new HSQLTestException(e);
        }
    }

}
