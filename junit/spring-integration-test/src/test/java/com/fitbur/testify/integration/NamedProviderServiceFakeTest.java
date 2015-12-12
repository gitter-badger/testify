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
import com.fitbur.testify.Fake;
import com.fitbur.testify.Module;
import com.fitbur.testify.integration.fixture.SpringIntegrationConfig;
import com.fitbur.testify.integration.fixture.service.NamedProviderService;
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
public class NamedProviderServiceFakeTest {

    @Cut
    NamedProviderService cut;

    @Fake
    Provider<String> provider;

    @Test
    public void verifyInjection() {
        assertThat(cut).isNotNull();
        assertThat(provider).isNotNull();
        assertThat(provider).isSameAs(cut.getProvider());

        MockUtil util = new MockUtil();
        assertThat(util.isMock(provider)).isTrue();

    }
}
