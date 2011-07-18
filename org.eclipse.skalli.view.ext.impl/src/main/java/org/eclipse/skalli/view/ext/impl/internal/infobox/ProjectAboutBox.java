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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.api.java.TaggingService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;
import org.eclipse.skalli.view.ext.AbstractInfoBox;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;

public class ProjectAboutBox extends AbstractInfoBox implements ProjectInfoBox {

    private static final String DEBUG_ID = "projectAboutInfoBoxContent";
    private static final String STYLE_ABOUT = "about";
    private static final String STYLE_PHASE = "phase";

    @Override
    public String getIconPath() {
        return "res/icons/info.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "About";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        CssLayout layout = new CssLayout();
        layout.setSizeFull();

        String description = "No description available";
        if (StringUtils.isNotBlank(project.getDescription())) {
            description = project.getDescription();
            description = StringEscapeUtils.escapeHtml(description);
            description = StringUtils.replace(description, "\n", "<br />");
        }
        Label aboutLabel = new Label(description, Label.CONTENT_XHTML);
        aboutLabel.addStyleName(STYLE_ABOUT);
        layout.addComponent(aboutLabel);

        InfoProjectExt ext = project.getExtension(InfoProjectExt.class);
        if (ext != null && StringUtils.isNotBlank(ext.getPageUrl())) {
            Link wikiLink = new Link("Project Homepage", new ExternalResource(ext.getPageUrl()));
            wikiLink.addStyleName(STYLE_LINK);
            layout.addComponent(wikiLink);
        }

        TaggingService taggingService = Services.getService(TaggingService.class);
        if (taggingService != null) {
            TagComponent tagComponent = new TagComponent(project, taggingService, util);
            layout.addComponent(tagComponent);
        }

        if (util.getProjectTemplate().isVisible(Project.class.getName(), Project.PROPERTY_PHASE,
                util.isUserProjectAdmin(project))) {
            Label maturityLabel = new Label("This project is in the <b>" +
                    project.getPhase().toString() + "</b> phase.", Label.CONTENT_XHTML);
            maturityLabel.addStyleName(STYLE_PHASE);
            layout.addComponent(maturityLabel);
        }

        // for ui testing
        // TODO need to understand why vaadin does not accept the layout to have the id
        // (it then cannot render a second project, throws ISE)
        layout.addComponent(new Label("<div id=" + DEBUG_ID + "></div>", Label.CONTENT_XHTML)); //$NON-NLS-1$ //$NON-NLS-2$

        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.01f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        return true;
    }

}
