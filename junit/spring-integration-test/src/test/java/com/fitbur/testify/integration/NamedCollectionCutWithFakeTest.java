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
import com.fitbur.testify.fixture.SpringIntegrationConfig;
import com.fitbur.testify.fixture.service.NamedCollectionsService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTest.class)
@Module(SpringIntegrationConfig.class)
public class NamedCollectionCutWithFakeTest {

    @Cut
    NamedCollectionsService cut;

    @Fake
    ArrayList<String> list;

    @Fake
    HashSet<String> set;

    @Fake
    HashMap<String, String> map;

    @Test
    public void verifyInjection() {
        assertThat(cut).isNotNull();
        assertThat(cut.getList()).isSameAs(list);
        assertThat(cut.getSet()).isSameAs(set);
        assertThat(cut.getMap()).isSameAs(map);

        MockUtil util = new MockUtil();
        assertThat(util.isMock(list)).isTrue();
        assertThat(util.isMock(set)).isTrue();
        assertThat(util.isMock(map)).isTrue();

    }
}
