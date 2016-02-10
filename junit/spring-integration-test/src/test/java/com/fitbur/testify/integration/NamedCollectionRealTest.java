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
import com.fitbur.testify.Real;
import com.fitbur.testify.fixture.SpringIntegrationConfig;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author saden
 */
@RunWith(SpringIntegrationTest.class)
@Module(SpringIntegrationConfig.class)
public class NamedCollectionRealTest {

    @Real
    @Named("listOfStrings")
    List<String> list;

    @Real
    @Named("setOfStrings")
    Set<String> set;

    @Real
    @Named("mapOfStrings")
    Map<String, String> map;

    @Test
    public void verifyInjection() {
        assertThat(list).containsExactly("list");
        assertThat(set).containsExactly("set");
        assertThat(map).containsExactly(entry("map", "map"));
    }
}
