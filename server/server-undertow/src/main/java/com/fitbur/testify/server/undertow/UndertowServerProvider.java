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
package com.fitbur.testify.server.undertow;

import com.fitbur.testify.app.ServerDescriptor;
import com.fitbur.testify.app.ServerProvider;

/**
 *
 * @author saden
 */
public class UndertowServerProvider implements ServerProvider {

    @Override
    public ServerDescriptor init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy(ServerDescriptor descriptor) {
        ServerProvider.super.destroy(descriptor); //To change body of generated methods, choose Tools | Templates.
    }

}
