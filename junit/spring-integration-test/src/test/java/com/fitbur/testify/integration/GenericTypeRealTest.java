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
import com.fitbur.testify.integration.fixture.SpringIntegrationConfig;
import com.fitbur.testify.integration.fixture.service.GenericTypeService;
import com.fitbur.testify.integration.fixture.service.collaborator.Hello;
import javax.inject.Provider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTestRunner.class)
@Module(SpringIntegrationConfig.class)
public class GenericTypeRealTest {

    @Cut
    GenericTypeService cut;

    @Real
    Provider<Hello> hello;

    @Test
    public void verifyInjections() {
        assertThat(cut).isNotNull();
        assertThat(hello)
                .isNotNull()
                .isSameAs(cut.getHello());

        MockUtil util = new MockUtil();
        assertThat(util.isMock(hello)).isFalse();
    }

}