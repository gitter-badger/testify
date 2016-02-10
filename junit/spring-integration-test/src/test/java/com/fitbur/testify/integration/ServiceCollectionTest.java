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
package com.fitbur.testify.integration;

import com.fitbur.testify.Module;
import com.fitbur.testify.fixture.SpringIntegrationConfig;
import com.fitbur.testify.fixture.service.collaborator.Greeting;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTest.class)
@Module(SpringIntegrationConfig.class)
public class ServiceCollectionTest {

    @Inject
    List<Greeting> greetingsList;

    @Inject
    Set<Greeting> greetingsSet;

    @Inject
    Map<String, Greeting> greetingsMap;

    @Test
    public void verifyInjection() {
        assertThat(greetingsList).isNotEmpty();
        assertThat(greetingsSet).isNotEmpty();
        assertThat(greetingsMap).isNotEmpty();

    }
}
