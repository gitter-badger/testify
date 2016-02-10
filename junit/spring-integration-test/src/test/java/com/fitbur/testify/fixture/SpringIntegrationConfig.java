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
package com.fitbur.testify.fixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author saden
 */
@Configuration
@ComponentScan
public class SpringIntegrationConfig {

    @Bean
    public ArrayList<String> listOfStrings() {
        ArrayList list = new ArrayList(1);
        list.add("list");

        return list;
    }

    @Bean
    public HashMap<String, String> mapOfStrings() {
        HashMap<String, String> map = new HashMap<>(1);
        map.put("map", "map");

        return map;
    }

    @Bean
    public HashSet<String> setOfStrings() {
        HashSet set = new HashSet(1);
        set.add("set");

        return set;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public String stringProvider() {
        return "test";
    }

}
