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
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.render.RenderFacet;
import org.apache.isis.core.metamodel.facets.members.render.RenderFacetAbstract;

public class RenderFacetOnCollectionFromLayoutProperties extends RenderFacetAbstract {

    public static RenderFacet create(Properties properties, FacetHolder holder) {
        final CollectionLayout.RenderType renderType = render(properties);
        return renderType != null? new RenderFacetOnCollectionFromLayoutProperties(renderType, holder): null;
    }

    private RenderFacetOnCollectionFromLayoutProperties(CollectionLayout.RenderType renderType, FacetHolder holder) {
        super(CollectionLayout.RenderType.typeOf(renderType), holder);
    }

    private static CollectionLayout.RenderType render(Properties properties) {
        if(properties == null) {
            return null;
        }
        String renderType = Strings.emptyToNull(properties.getProperty("render"));
        if(renderType == null) {
            return null;
        }
        return CollectionLayout.RenderType.valueOf(renderType);
    }

}
