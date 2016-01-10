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

import com.fitbur.testify.client.ClientProvider;
import com.fitbur.testify.server.ServerProvider;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An annotation for specifying an application to load along with the
 * application server used to load it.
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
public @interface App {

    /**
     * The class of the application that will be deployed.
     *
     * @return the application class.
     */
    Class<?> value();

    /**
     * The server provider implementation used to deploy the application to. If
     * not specified the default server will be used.
     *
     * @return a server provider implementation class.
     */
    Class<? extends ServerProvider> server() default ServerProvider.class;

    /**
     * The client provider implementation used to communicate with server the
     * application is deployed to. If not specified the default client provider
     * will be used.
     *
     * @return a server provider implementation class.
     */
    Class<? extends ClientProvider> client() default ClientProvider.class;

}
