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
package com.fitbur.testify.example.junit.spring.systemtest.need.resource;

import com.fitbur.testify.example.junit.spring.systemtest.need.database.entity.GreetingEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Greeting Resource.
 *
 * @author saden
 */
@RestController
public class GreetingResource {

    private final SessionFactory sessionFactory;

    @Autowired
    GreetingResource(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String greet(@RequestParam String phrase) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(new GreetingEntity(null, phrase));
            tx.commit();
        }
        return phrase;
    }

}
