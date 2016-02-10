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
package com.fitbur.testify;

import static com.fitbur.guava.common.base.Preconditions.checkState;
import com.fitbur.guava.common.collect.Lists;
import com.fitbur.testify.di.ServiceLocator;
import com.fitbur.testify.need.NeedContainer;
import com.fitbur.testify.need.NeedContainerProvider;
import com.fitbur.testify.need.NeedContext;
import com.fitbur.testify.need.NeedDescriptor;
import com.fitbur.testify.need.NeedInstance;
import com.fitbur.testify.need.NeedScope;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;
import static java.util.stream.Collectors.toCollection;

/**
 * A class for managing container based needs.
 *
 * @author saden
 */
public class TestNeedContainers {

    private final TestContext testContext;
    private final String name;
    private final NeedScope scope;
    private Set<NeedContext> needContexts;

    public TestNeedContainers(TestContext testContext,
            String name,
            NeedScope scope) {
        this.testContext = testContext;
        this.name = name;
        this.scope = scope;
    }

    public <T extends Annotation> void init() {
        needContexts = new HashSet<>();
        ServiceLoader<NeedContainerProvider> serviceLoader = ServiceLoader.load(NeedContainerProvider.class);
        ArrayList<NeedContainerProvider> providers = Lists.newArrayList(serviceLoader);

        providers.parallelStream().forEach(provider -> {
            testContext.getAnnotations(NeedContainer.class).parallelStream().filter(p -> p.scope() == scope).map(p -> {
                NeedDescriptor descriptor = new TestNeedDescriptor(testContext, name);
                Object configuration = provider.configuration(descriptor);

                testContext.getConfigMethod(configuration.getClass())
                        .map(m -> m.getMethod())
                        .ifPresent(m -> {
                            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                                try {
                                    m.setAccessible(true);
                                    m.invoke(descriptor.getTestInstance(), configuration);
                                } catch (Exception e) {
                                    checkState(false, "Call to config method '%s' in test class '%s' failed.",
                                            m.getName(), descriptor.getTestClassName());
                                }

                                return null;
                            });
                        });

                Map<String, NeedInstance> instances = provider.init(descriptor, configuration);
                NeedContext needContext = new NeedContext(provider, descriptor, instances, configuration);

                return needContext;

            }).collect(toCollection(() -> needContexts));

        });

    }

    public void inject(ServiceLocator serviceLocator) {
        needContexts.parallelStream().forEach(p -> {
            p.getProvider().clean(p.getDescriptor(), p.getConfiguration());
            serviceLocator.addConstant(UUID.randomUUID().toString(), p);
            p.getInstances().forEach((k, v) -> {
                serviceLocator.addConstant(
                        UUID.randomUUID().toString(),
                        v.getInstance());
                serviceLocator.addConstant(k, v);
            });
        });
    }

    public void destory() {
        needContexts.parallelStream().forEach(p -> {
            p.getProvider().destroy(p.getDescriptor(), p.getConfiguration());
        });
    }

}
