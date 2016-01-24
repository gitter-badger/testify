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

import com.fitbur.guava.common.collect.ImmutableMap;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedInstance;
import com.fitbur.testify.need.NeedProvider;
import static java.lang.String.format;
import java.net.URI;
import java.util.Map;
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
        dataSource.setUrl(format("jdbc:hsqldb:mem:%s_%s",
                descriptor.getTestClassName(), descriptor.getTestMethodName()));
        dataSource.setUser("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Override
    public Map<String, NeedInstance> init(NeedDescriptor descriptor, JDBCDataSource configuration) {
        HSQLInstance instnace = new HSQLInstance(configuration, URI.create(configuration.getUrl()));

        return ImmutableMap.of(configuration.getDatabaseName(), instnace);
    }

}
