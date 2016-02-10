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
package com.fitbur.testify.example.junit.spring.systemtest.need.resource;

import com.fitbur.testify.App;
import com.fitbur.testify.Module;
import com.fitbur.testify.Real;
import com.fitbur.testify.client.ClientInstance;
import com.fitbur.testify.example.junit.spring.systemtest.fixture.TestDatabaseConfig;
import com.fitbur.testify.example.junit.spring.systemtest.need.GreeterApplication;
import com.fitbur.testify.need.NeedContainer;
import com.fitbur.testify.system.SpringSystemTest;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringSystemTest.class)
@App(GreeterApplication.class)
@Module(TestDatabaseConfig.class)
@NeedContainer(value = "postgres", version = "9.4")
public class GreetingResourceNeedContainerTest {

    @Real
    ClientInstance<WebTarget> cut;

    @Test
    public void givenHelloGetShouldReturnHello() {
        //Arrange
        String phrase = "Hello";

        //Act
        Response result = cut.getTarget()
                .path("/")
                .queryParam("phrase", phrase)
                .request()
                .get();

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(result.readEntity(String.class)).isEqualTo(phrase);
    }

}
