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
package com.fitbur.testify.examples.junit.spring.need.database.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Greeting Entity.
 *
 * @author saden
 */
@Entity
@Table
public class GreetingEntity {

    private Long greetingId;
    private String phrase;

    public GreetingEntity() {
    }

    public GreetingEntity(Long greetingId, String phrase) {
        this.greetingId = greetingId;
        this.phrase = phrase;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getGreetingId() {
        return greetingId;
    }

    public void setGreetingId(Long greetingId) {
        this.greetingId = greetingId;
    }

    @Column(length = 128)
    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.greetingId);
        hash = 67 * hash + Objects.hashCode(this.phrase);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GreetingEntity other = (GreetingEntity) obj;
        if (!Objects.equals(this.phrase, other.phrase)) {
            return false;
        }
        return Objects.equals(this.greetingId, other.greetingId);
    }

    @Override
    public String toString() {
        return "GreetingEntity{" + "id=" + greetingId + ", phrase=" + phrase + '}';
    }

}
