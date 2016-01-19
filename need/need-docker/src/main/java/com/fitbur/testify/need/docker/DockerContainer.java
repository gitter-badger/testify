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
package com.fitbur.testify.need.docker;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotations that specifies a docker container that should be started.
 *
 * @author saden
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Repeatable(DockerContainers.class)
public @interface DockerContainer {

    /**
     * The image name.
     *
     * @return image name
     */
    String value();

    /**
     * The image tag.
     *
     * @return image tag
     */
    String tag() default "latest";

    /**
     * The command that will be executed.
     *
     * @return the command.
     */
    String cmd() default "";

    /**
     * Pull an image or a repository from a registry.
     *
     * @return true if the image will be pulled, false otherwise
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
