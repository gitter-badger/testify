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
import com.fitbur.testify.integration.fixture.service.NamedCollectionsService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTestRunner.class)
@Module(SpringIntegrationConfig.class)
public class NamedCollectionCutWithRealTest {

    @Cut
    NamedCollectionsService cut;

    @Real
    @Named("list")
    List<String> list;

    @Real
    @Named("set")
    Set<String> set;

    @Real
    @Named("map")
    Map<String, String> map;

    @Test
    public void verifyInjection() {
        assertThat(cut).isNotNull();
        assertThat(cut.getList()).isSameAs(list);
        assertThat(cut.getSet()).isSameAs(set);
        assertThat(cut.getMap()).isSameAs(map);
        assertThat(list).containsExactly("test");
        assertThat(set).containsExactly("test");
        assertThat(map).containsOnly(MapEntry.entry("test", "test"));

    }
}
