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

import org.springframework.context.ConfigurableApplicationContext;

/**
 * A descriptor class that contains servlet context information.
 *
 * @author saden
 */
public class SpringSystemDescriptor {

    private Class<?>[] servletConfigClasses;
    private ConfigurableApplicationContext servletAppContext;

    public Class<?>[] getServletConfigClasses() {
        return servletConfigClasses;
    }

    public void setServletConfigClasses(Class<?>[] servletConfigClasses) {
        this.servletConfigClasses = servletConfigClasses;
    }

    public ConfigurableApplicationContext getServletAppContext() {
        return servletAppContext;
    }

    public void setServletAppContext(ConfigurableApplicationContext servletAppContext) {
        this.servletAppContext = servletAppContext;
    }

}
