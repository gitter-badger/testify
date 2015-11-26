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
package com.fitbur.testify.hsql;

/**
 * Generic HSQL test exception.
 *
 * @author saden
 */
public class HSQLTestException extends RuntimeException {

    /**
     * Creates a new instance of <code>HSQLTestException</code> without detail
     * message.
     */
    public HSQLTestException() {
    }

    /**
     * Constructs an instance of <code>HSQLTestException</code> with the
     * specified detail message.
     *
     * @param message the detail message.
     */
    public HSQLTestException(String message) {
        super(message);
    }

    /**
     * Constructs an instance of <code>HSQLTestException</code> with the
     * specified cause.
     *
     * @param cause the root cause of the exception.
     */
    public HSQLTestException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>HSQLTestException</code> with the
     * specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the root cause of the exception.
     */
    public HSQLTestException(String message, Throwable cause) {
        super(message, cause);
    }

}
