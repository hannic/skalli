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

import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.TaggingService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class ProjectAboutBox extends InfoBox implements ProjectInfoBox {

    private static final String DEBUG_ID = "projectAboutInfoBoxContent"; //$NON-NLS-1$

    private static final String STYLE_ABOUT_INFOBOX = "infobox-about"; //$NON-NLS-1$

    private static final String STYLE_ABOUT = "about"; //$NON-NLS-1$
    private static final String STYLE_HOMEPAGE = "homepage"; //$NON-NLS-1$
    private static final String STYLE_PHASE = "phase"; //$NON-NLS-1$

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
        layout.addStyleName(STYLE_ABOUT_INFOBOX);
        layout.setSizeFull();

        String description = "No description available";
        if (StringUtils.isNotBlank(project.getDescription())) {
            description = project.getDescription();
            description = StringEscapeUtils.escapeHtml(description);
            description = StringUtils.replace(description, "\n", "<br />");  //$NON-NLS-1$//$NON-NLS-2$
        }
        createLabel(layout, description, STYLE_ABOUT);

        InfoProjectExt ext = project.getExtension(InfoProjectExt.class);
        if (ext != null && StringUtils.isNotBlank(ext.getPageUrl())) {
            createLink(layout, "Project Homepage", ext.getPageUrl(), DEFAULT_TARGET, STYLE_HOMEPAGE);
        }

        TaggingService taggingService = Services.getService(TaggingService.class);
        if (taggingService != null) {
            TagComponent tagComponent = new TagComponent(project, taggingService, util);
            layout.addComponent(tagComponent);
        }

        if (util.getProjectTemplate().isVisible(Project.class.getName(), Project.PROPERTY_PHASE,
                util.isUserProjectAdmin(project))) {
            createLabel(layout,
                    MessageFormat.format("This project is in the <b>{0}</b> phase.", project.getPhase()),
                    STYLE_PHASE);
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
