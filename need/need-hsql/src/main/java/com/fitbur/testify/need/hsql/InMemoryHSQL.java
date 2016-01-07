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

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedProvider;
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.hsqldb.jdbc.JDBCDataSource;

/**
 * An in memory implementation of a HSQL test need provider.
 *
 * @author saden
 */
public class InMemoryHSQL implements NeedProvider<JDBCDataSource> {

    @Override
    public JDBCDataSource configuration(NeedDescriptor descriptor) {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(format("jdbc:hsqldb:mem:%s", descriptor.getTestClassName()));
        dataSource.setUser("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Override
    public void init(NeedDescriptor descriptor, JDBCDataSource dataSource) {
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
    public void destroy(NeedDescriptor descriptor, JDBCDataSource dataSource) {
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
    }

}
