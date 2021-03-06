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
package com.fitbur.testify.di.spring;

import com.fitbur.testify.TestNeedContainers;
import com.fitbur.testify.TestNeeds;
import com.fitbur.testify.di.ServiceLocator;
import static java.util.stream.Stream.of;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * A bean factory post processor that insures all Spring beans are lazy loaded.
 *
 * @author saden
 */
public class SpringServicePostProcessor implements BeanFactoryPostProcessor {

    private final ServiceLocator serviceLocator;
    private final TestNeeds methodTestNeeds;
    private final TestNeedContainers methodTestNeedContainers;
    private final TestNeeds classTestNeeds;
    private final TestNeedContainers classTestNeedContainers;

    public SpringServicePostProcessor(ServiceLocator serviceLocator,
            TestNeeds methodTestNeeds,
            TestNeedContainers methodTestNeedContainers,
            TestNeeds classTestNeeds,
            TestNeedContainers classTestNeedContainers) {
        this.serviceLocator = serviceLocator;
        this.methodTestNeeds = methodTestNeeds;
        this.methodTestNeedContainers = methodTestNeedContainers;
        this.classTestNeeds = classTestNeeds;
        this.classTestNeedContainers = classTestNeedContainers;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        of(beanFactory.getBeanDefinitionNames())
                .parallel()
                .map(beanFactory::getBeanDefinition)
                .forEach(p -> p.setLazyInit(true));

        methodTestNeeds.inject(serviceLocator);
        methodTestNeedContainers.inject(serviceLocator);

        if (classTestNeeds != null) {
            classTestNeeds.inject(serviceLocator);
        }

        if (classTestNeedContainers != null) {
            classTestNeedContainers.inject(serviceLocator);
        }

    }

}
