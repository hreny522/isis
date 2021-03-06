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

package org.apache.isis.core.metamodel.facets.collections.layout;

import java.util.Properties;
import com.google.common.base.Strings;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstract;

public class HiddenFacetOnCollectionFromLayoutProperties extends HiddenFacetAbstract {

    public static HiddenFacet create(Properties properties, FacetHolder holder) {
        final Where where = hidden(properties);
        return where != null && where != Where.NOT_SPECIFIED ? new HiddenFacetOnCollectionFromLayoutProperties(where, holder): null;
    }

    private static Where hidden(Properties properties) {
        if(properties == null) {
            return null;
        }
        String hidden = Strings.emptyToNull(properties.getProperty("hidden"));
        if(hidden == null) {
            return null;
        }
        return Where.valueOf(hidden);
    }

    private HiddenFacetOnCollectionFromLayoutProperties(Where where, FacetHolder holder) {
        super(When.ALWAYS, where, holder);
    }

    @Override
    public String hiddenReason(final ObjectAdapter targetAdapter, Where whereContext) {
        if(!where().includes(whereContext)) {
            return null;
        }
        return "Hidden on " + where().getFriendlyName();
    }
}
