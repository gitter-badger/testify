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
package com.fitbur.testify.system;

import com.fitbur.testify.App;
import com.fitbur.testify.Cut;
import com.fitbur.testify.Real;
import com.fitbur.testify.fixture.servlet.GreeterServletApplication;
import com.fitbur.testify.fixture.web.resource.GreetingResource;
import com.fitbur.testify.fixture.web.service.GreetingService;
import com.fitbur.testify.server.ServerInstance;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;

/**
 *
 * @author saden
 */
@RunWith(SpringSystemTest.class)
@App(GreeterServletApplication.class)
public class ServerInstanceInjectionTest {

    @Cut
    GreetingResource cut;

    @Real
    GreetingService greetingService;

    @Inject
    ServerInstance serverInstance;

    @Test
    public void verifyInjections() {
        assertThat(cut).isNotNull();
        assertThat(greetingService)
                .isNotNull()
                .isSameAs(cut.getGreetingService());
        assertThat(serverInstance).isNotNull();

        MockUtil util = new MockUtil();
        assertThat(util.isMock(greetingService)).isFalse();
    }

}
