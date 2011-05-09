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

import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Link;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.devinf.ScmLocationMapper;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ProjectGitActivityBox implements ProjectInfoBox {

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public String getIconPath() {
        return "res/icons/activity.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Project Activity";
    }

    @Override
    public float getPositionWeight() {
        return 1.55f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        List<Link> links = getActivityGraphicUrl(project);
        return (links != null) && (links.size() > 0);
    }

    private List<Link> getActivityGraphicUrl(Project project) {
        if (configService == null) {
            return null;
        }
        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
        if (devInf == null) {
            return null;
        }
        String scmLocation = devInf.getScmLocation();
        if (StringUtils.isBlank(scmLocation)) {
            return null;
        }
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<Link> links = mapper.getMappedLinks(configService, project.getProjectId(), scmLocation,
                ScmLocationMapper.PURPOSE_ACTIVITY);
        return links;
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        Label label;
        label = new Label("Recent Git Commit Activity:");
        layout.addComponent(label);

        List<Link> links = getActivityGraphicUrl(project);
        if (links != null) {
            for (Link link : links) {
                ExternalResource externalResource = new ExternalResource(link.getUrl());
                Embedded embedded = new Embedded("", externalResource); //$NON-NLS-1$
                embedded.setDescription(link.getLabel());
                layout.addComponent(embedded);
            }
        }
        return layout;
    }
}
