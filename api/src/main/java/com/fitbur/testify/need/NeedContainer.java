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
package com.fitbur.testify.need;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation for specifying a need class that should be loaded for a test
 * class. This is useful for integration and system tests which require an
 * external resource to be loaded (i.e. an in-memory database).
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE, TYPE})
@Repeatable(NeedContainers.class)
public @interface NeedContainer {

    /**
     * Specifies a need implementation class that should be loaded.
     *
     * @return a need class.
     */
    Class<? extends NeedProvider> provider() default NeedProvider.class;

    /**
     * The lifecycle scope of the need.
     *
     * @return the lifecycle scope of the need.
     */
    NeedScope scope() default NeedScope.METHOD;

    /**
     * The value name.
     *
     * @return value name
     */
    String value();

    /**
     * The value version.
     *
     * @return value version
     */
    String version() default "latest";

    /**
     * The command that will be executed.
     *
     * @return the command.
     */
    String cmd() default "";

    /**
     * Pull an value or a repository from a registry.
     *
     * @return true if the value will be pulled, false otherwise
     */
    boolean pull() default true;

    /**
     * The container name.
     *
     * @return container name.
     */
    String name() default "";

    /**
     * A flag to indicate whether to wait for all container ports to be
     * reachable.
     *
     * @return health check URI.
     */
    boolean await() default true;

    /**
     * Sets the delay between retries.
     *
     * @return delay
     */
    long delay() default 1000;

    /**
     * Max delay for exponentially backoff.
     *
     * @return max delay
     */
    long maxDelay() default 8000;

    /**
     * Maximum number of retries before giving up.
     *
     * @return max retries.
     */
    int maxRetries() default 3;

    /**
     * Maximum retry duration before giving up.
     *
     * @return max retries.
     */
    long maxDuration() default 8000;

    /**
     * Time unit for delay, max delay, and duration.
     *
     * @return time unit
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
