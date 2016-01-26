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
package com.fitbur.testify.example.junit.spring.systemtest.need;

import com.fitbur.testify.App;
import com.fitbur.testify.Real;
import com.fitbur.testify.need.Need;
import com.fitbur.testify.need.hsql.InMemoryHSQL;
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
@Need(InMemoryHSQL.class)
public class GreetingApplicationNeetTest {

    @Real
    WebTarget target;

    @Test
    public void givenPhraseGetShouldReturnPhrase() {
        String phrase = "Hello";
        Response result = target.path("/")
                .queryParam("phrase", phrase)
                .request()
                .get();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(result.readEntity(String.class)).isEqualTo(phrase);
    }

}
