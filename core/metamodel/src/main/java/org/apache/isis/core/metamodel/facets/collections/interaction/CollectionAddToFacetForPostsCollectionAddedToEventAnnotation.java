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

package org.apache.isis.core.metamodel.facets.collections.interaction;

import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

public class CollectionAddToFacetForPostsCollectionAddedToEventAnnotation
        extends CollectionAddToFacetForInteractionAbstract {

	public CollectionAddToFacetForPostsCollectionAddedToEventAnnotation(
            final Class<? extends CollectionAddedToEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final CollectionAddToFacet collectionAddToFacet,
            final CollectionInteractionFacetAbstract collectionInteractionFacet,
            final FacetHolder holder, final ServicesInjector servicesInjector) {
		super(eventType, getterFacet, collectionAddToFacet, collectionInteractionFacet, servicesInjector, holder);
	}

    @Override
    protected CollectionInteractionEvent<?, ?> verify(final CollectionInteractionEvent<?, ?> event) {
        // will discard event if different type to that specified in the PostsCollectionAddedToEvent annotation.
        return event != null && value() == event.getClass() ? event : null;
    }

}
