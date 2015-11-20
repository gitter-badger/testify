/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fitbur.testify.integration;

import com.fitbur.testify.Module;
import com.fitbur.testify.hsql.InMemoryHSQL;
import com.fitbur.testify.integration.fixture.NeedConfig;
import com.fitbur.testify.need.Need;
import com.zaxxer.hikari.HikariDataSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTestRunner.class)
@Module(NeedConfig.class)
@Need(InMemoryHSQL.class)
public class InjectAutowiredTest {

    @Autowired
    HikariDataSource dataSource;

    @Test
    public void testSomeMethod() {
        assertThat(this.dataSource).isNotNull();
    }

    @Test
    public void testSomeMethod2() {
        assertThat(this.dataSource).isNotNull();
    }

}
