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

import com.fitbur.testify.Cut;
import com.fitbur.testify.Module;
import com.fitbur.testify.Real;
import com.fitbur.testify.fixture.SpringIntegrationConfig;
import com.fitbur.testify.fixture.service.QualifiedGreetingService;
import com.fitbur.testify.fixture.service.collaborator.Greeting;
import com.fitbur.testify.fixture.service.collaborator.qualfied.annotation.JSR330Qualifier;
import com.fitbur.testify.fixture.service.collaborator.qualfied.annotation.SpringQualifier;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTest.class)
@Module(SpringIntegrationConfig.class)
public class CustomQualfierTest {

    @Cut
    QualifiedGreetingService cut;

    @Real
    @JSR330Qualifier
    Greeting jsr330Qualfied;

    @Real
    @SpringQualifier
    Greeting springQualified;

    @Test
    public void verifyInjections() {
        assertThat(cut).isNotNull();
        assertThat(jsr330Qualfied)
                .isNotNull()
                .isSameAs(cut.getJSRQualified());
        assertThat(springQualified)
                .isNotNull()
                .isSameAs(cut.getSpringQualified());

    }

}
