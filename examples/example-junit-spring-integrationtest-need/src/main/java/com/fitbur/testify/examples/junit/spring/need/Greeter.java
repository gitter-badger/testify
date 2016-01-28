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
package com.fitbur.testify.examples.junit.spring.need;

import com.fitbur.testify.examples.junit.spring.need.database.entity.GreetingEntity;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Simple greeter class.
 *
 * @author saden
 */
@Named
public class Greeter {

    private final SessionFactory sessionFactor;

    @Inject
    Greeter(SessionFactory sessionFactor) {
        this.sessionFactor = sessionFactor;
    }

    public void greet(String phrase) {
        try (Session session = sessionFactor.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(new GreetingEntity(null, phrase));
            tx.commit();
        }
    }

    public SessionFactory getSessionFactor() {
        return sessionFactor;
    }

}
