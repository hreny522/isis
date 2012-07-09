/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.wicket.ui.pages.about;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.models.AboutModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing the about page.
 */
public class AboutPage extends PageAbstract {

    public AboutPage() {
        super(new PageParameters(), ComponentType.ACTION); // TODO: sort out!!!
        final IModel<?> model = buildModel();
        addChildComponents(model);
    }

    private IModel<?> buildModel() {
        return new AboutModel();
    }

}