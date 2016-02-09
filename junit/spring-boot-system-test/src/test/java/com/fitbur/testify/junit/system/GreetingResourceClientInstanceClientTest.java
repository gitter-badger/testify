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
package com.fitbur.testify.junit.system;

import com.fitbur.testify.App;
import com.fitbur.testify.Real;
import com.fitbur.testify.client.ClientInstance;
import com.fitbur.testify.junit.fixture.GreeterApplication;
import com.fitbur.testify.junit.fixture.web.service.GreetingService;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringBootSystemTest.class)
@App(GreeterApplication.class)
public class GreetingResourceClientInstanceClientTest {

    @Real
    GreetingService cut;

    @Real
    ClientInstance<WebTarget> instance;

    @Test
    public void verifyInjections() {
        assertThat(cut).isNotNull();
    }

    @Test
    public void verifyInjections2() {
        assertThat(cut).isNotNull();

        Response result = instance.getClient().path("/").request().get();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(result.readEntity(String.class)).isEqualTo("Hello");
    }

}
