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

/**
 * An enumeration used to indicate the scope of a need.
 *
 * @author saden
 */
public enum NeedScope {
    /**
     * Indicates the need is global and should be started and stopped before and
     * after test suites.
     */
    SUITE,
    /**
     * Indicates the need is per test class and should started before and after
     * test classes.
     */
    CLASS,
    /**
     * Indicates the need is per test method and should started before and after
     * test methods.
     */
    METHOD
}
