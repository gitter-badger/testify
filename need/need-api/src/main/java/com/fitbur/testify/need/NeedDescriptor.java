/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fitbur.testify.need;

import java.util.Objects;

/**
 * A descriptor class that contains the need annotation, context and the test
 * need instance.
 *
 * @author saden
 */
public class NeedDescriptor {

    private final Need need;
    private final Object context;
    private final TestNeed testNeed;

    public NeedDescriptor(Need need, Object context, TestNeed testNeed) {
        this.need = need;
        this.context = context;
        this.testNeed = testNeed;
    }

    public Need getNeed() {
        return need;
    }

    public Object getContext() {
        return context;
    }

    public TestNeed getTestNeed() {
        return testNeed;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.need);
        hash = 89 * hash + Objects.hashCode(this.context);
        hash = 89 * hash + Objects.hashCode(this.testNeed);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NeedDescriptor other = (NeedDescriptor) obj;
        if (!Objects.equals(this.need, other.need)) {
            return false;
        }
        if (!Objects.equals(this.context, other.context)) {
            return false;
        }
        if (!Objects.equals(this.testNeed, other.testNeed)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NeedDescriptor{" + "need=" + need + ", context=" + context + ", testNeed=" + testNeed + '}';
    }

}
