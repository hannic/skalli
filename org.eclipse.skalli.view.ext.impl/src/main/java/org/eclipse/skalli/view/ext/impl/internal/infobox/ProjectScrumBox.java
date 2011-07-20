/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.view.component.InformationBox;
import org.eclipse.skalli.view.component.PeopleComponent;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;

public class ProjectScrumBox extends InfoBox implements ProjectInfoBox {

    @Override
    public String getIconPath() {
        return "res/icons/scrum.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Scrum";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        boolean rendered = false;
        ScrumProjectExt scrumExt = project.getExtension(ScrumProjectExt.class);
        if (scrumExt != null) {
            if (CollectionUtils.isNotBlank(scrumExt.getProductOwners())) {
                Label projectProductOwnersLabel = new Label("Product Owner");
                projectProductOwnersLabel.addStyleName(STYLE_TEAMLABEL);
                layout.addComponent(projectProductOwnersLabel);
                Component peopleComponent = PeopleComponent
                        .getPeopleListComponentForMember(scrumExt.getProductOwners());
                peopleComponent.addStyleName(InformationBox.STYLE);
                layout.addComponent(peopleComponent);
                rendered = true;
            }

            if (CollectionUtils.isNotBlank(scrumExt.getScrumMasters())) {
                Label projectScrumMasterLabel = new Label("Scrum Master");
                projectScrumMasterLabel.addStyleName(STYLE_TEAMLABEL);
                layout.addComponent(projectScrumMasterLabel);
                Component peopleComponent = PeopleComponent.getPeopleListComponentForMember(scrumExt.getScrumMasters());
                peopleComponent.addStyleName(InformationBox.STYLE);
                layout.addComponent(peopleComponent);
                rendered = true;
            }

            if (StringUtils.isNotBlank(scrumExt.getBacklogUrl())) {
                Link backlogLink = new Link("Scrum Backlog", new ExternalResource(scrumExt.getBacklogUrl()));
                backlogLink.addStyleName(STYLE_LINK);
                layout.addComponent(backlogLink);
                rendered = true;
            }
        }
        if (!rendered) {
            Label label = new Label("SCRUM extension added but no data maintained.");
            label.addStyleName(STYLE_LABEL);
            layout.addComponent(label);
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.6f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        if (project.getExtension(ScrumProjectExt.class) != null) {
            return true;
        } else {
            return false;
        }
    }

}
