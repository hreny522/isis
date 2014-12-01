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

package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptHeaderPanel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.AjaxDeferredBehaviour.OpenUrlStrategy;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public abstract class ActionLinkFactoryAbstract implements ActionLinkFactory {

    private static final long serialVersionUID = 1L;

    protected AbstractLink newLink(
            final String linkId,
            final ObjectAdapter objectAdapter,
            final ObjectAction action) {
        
        final ActionModel actionModel = ActionModel.create(objectAdapter, action);

        final AjaxDeferredBehaviour ajaxDeferredBehaviour = determineDeferredBehaviour(action, actionModel);

        final AbstractLink link = new AjaxLink<Object>(linkId) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                if(ajaxDeferredBehaviour != null) {
                    ajaxDeferredBehaviour.initiate(target);
                } else {
                    final ActionPromptProvider promptProvider = ActionPromptProvider.Util.getFrom(getPage());
                    final ActionPrompt actionPrompt = promptProvider.getActionPrompt();
                    ActionPromptHeaderPanel titlePanel = new ActionPromptHeaderPanel(actionPrompt.getTitleId(), actionModel);
                    final ActionPanel actionPanel =
                            (ActionPanel) getComponentFactoryRegistry().createComponent(
                                    ComponentType.ACTION_PROMPT, actionPrompt.getContentId(), actionModel);

                    actionPanel.setShowHeader(false);

                    actionPrompt.setTitle(titlePanel, target);
                    actionPrompt.setPanel(actionPanel, target);
                    actionPanel.setActionPrompt(actionPrompt);
                    actionPrompt.showPrompt(target);

                    focusOnFirstParameter(target, actionPanel);
                }
            }

            private void focusOnFirstParameter(AjaxRequestTarget target, ActionPanel actionPanel) {

                // first, force all parameters to build themselves...
                actionPanel.visitChildren(new IVisitor<Component, Component>() {
                    @Override
                    public void component(Component object, IVisit<Component> visit) {
                        if (object instanceof ScalarPanelAbstract) {
                            ScalarPanelAbstract spa = (ScalarPanelAbstract) object;
                            spa.forceBuildGui();
                            visit.dontGoDeeper();
                        }
                    }
                });

                // second, go searching for the first <input> in the action panel.
                final Component actionPanelFirstParam = actionPanel.visitChildren(new IVisitor<Component, Component>() {
                    @Override
                    public void component(Component object, IVisit<Component> visit) {
                        if (object instanceof FormComponent &&
                            !"scalarIfCompact".equals(object.getId()) &&
                            object.getOutputMarkupId()) {
                            // there are components for 'compact' and 'regular'; we want the 'regular' one
                            // also double check that has outputMarkupId enabled (prereq for setting focus)
                            visit.stop(object);
                        }
                    }
                });

                // third, if found then use Wicket API to focus on this component
                if(actionPanelFirstParam != null) {
                    target.focusComponent(actionPanelFirstParam);
                }
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);

                // allow the event to bubble so the menu is hidden after click on an item
                attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.BUBBLE);
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                Buttons.fixDisabledState(this, tag);
            }
        };

        if(ajaxDeferredBehaviour != null) {
            link.add(ajaxDeferredBehaviour);
        }

        link.add(new CssClassAppender("noVeil"));

        return link;
    }

    private static AjaxDeferredBehaviour determineDeferredBehaviour(final ObjectAction action, final ActionModel actionModel) {
        // TODO: should unify with ActionResultResponseType (as used in ActionPanel)
        if(isNoArgReturnTypeRedirect(action)) {
            /**
             * adapted from:
             * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
             */
            return new AjaxDeferredBehaviour(OpenUrlStrategy.NEW_WINDOW) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected IRequestHandler getRequestHandler() {
                    ObjectAdapter resultAdapter = executeActionHandlingApplicationExceptions(actionModel);
                    final Object value = resultAdapter.getObject();
                    return ActionModel.redirectHandler(value);
                }
            };
        } 
        if(isNoArgReturnTypeDownload(action)) {

            /**
             * adapted from:
             * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
             */
            return new AjaxDeferredBehaviour(OpenUrlStrategy.SAME_WINDOW) {
                
                private static final long serialVersionUID = 1L;
   
                @Override
                protected IRequestHandler getRequestHandler() {
                    ObjectAdapter resultAdapter = executeActionHandlingApplicationExceptions(actionModel);
                    final Object value = resultAdapter.getObject();
                    return ActionModel.downloadHandler(value);
                }
            };
        } 
        return null;
    }

    // TODO: should unify with ActionResultResponseType (as used in ActionPanel)
    private static boolean isNoArgReturnTypeRedirect(final ObjectAction action) {
        return action.getParameterCount() == 0 &&
               action.getReturnType() != null && 
               action.getReturnType().getCorrespondingClass() == java.net.URL.class;
    }

    // TODO: should unify with ActionResultResponseType (as used in ActionPanel)
    private static boolean isNoArgReturnTypeDownload(final ObjectAction action) {
        return action.getParameterCount() == 0 && action.getReturnType() != null && 
                (action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Blob.class ||
                action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Clob.class);
    }
    
    // adapted from similar code in ActionPanel :-(
    private static ObjectAdapter executeActionHandlingApplicationExceptions(final ActionModel actionModel) {
        try {
            return actionModel.getObject();

        } catch (RuntimeException ex) {

            // TODO: some duplication between this code and ActionPanel

            // see if is an application-defined exception
            // if so, is converted to an application error,
            // equivalent to calling DomainObjectContainer#raiseError(...)
            final RecoverableException appEx = ActionModel.getApplicationExceptionIfAny(ex);
            if (appEx != null) {
                IsisContext.getMessageBroker().setApplicationError(appEx.getMessage());

                // there's no need to set the abort cause on the transaction, it will have already been done
                // (in IsisTransactionManager#executeWithinTransaction(...)).

                return null;
            } 

            // not handled, so propagate
            throw ex;
        }
    }

    protected LinkAndLabel newLinkAndLabel(final ObjectAction objectAction, final AbstractLink link, final String disabledReasonIfAny) {

        final String label = ObjectAction.Utils.nameFor(objectAction);
        final boolean blobOrClob = ObjectAction.Utils.returnsBlobOrClob(objectAction);
        final boolean prototype = ObjectAction.Utils.isExplorationOrPrototype(objectAction);
        final String actionIdentifier = ObjectAction.Utils.actionIdentifierFor(objectAction);
        final String description = ObjectAction.Utils.descriptionOf(objectAction);
        final ActionLayout.Position position = ObjectAction.Utils.actionLayoutPositionOf(objectAction);
        final String cssClass = ObjectAction.Utils.cssClassFor(objectAction);
        final String cssClassFa = ObjectAction.Utils.cssClassFaFor(objectAction);

        return new LinkAndLabel(link, label, disabledReasonIfAny, description, blobOrClob, prototype, actionIdentifier, cssClass, cssClassFa, position);
    }


    // ////////////////////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////////////////////
    
    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor)Application.get()).getComponentFactoryRegistry();
    }
    
    protected PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) Application.get()).getPageClassRegistry();
    }

}
