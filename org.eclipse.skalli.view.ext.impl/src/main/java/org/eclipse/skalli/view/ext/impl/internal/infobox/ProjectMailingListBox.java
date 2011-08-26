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
import java.util.Set;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Link;
import org.eclipse.skalli.model.ext.LinkMapper;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;
import org.eclipse.skalli.model.ext.info.MailingListMapper;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.HtmlBuilder;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;

public class ProjectMailingListBox extends InfoBox implements ProjectInfoBox {

    private static final String STYLE_MAILING = "mailingList"; //$NON-NLS-1$

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public String getIconPath() {
        return "res/icons/mailinglist.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Mailing Lists";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        HtmlBuilder html = new HtmlBuilder();
        InfoProjectExt ext = project.getExtension(InfoProjectExt.class);
        if (ext != null) {
            Set<String> mailingLists = ext.getMailingLists();
            if (mailingLists.size() > 0) {
                MailingListMapper mapper = new MailingListMapper();
                html.append("<ul>"); //$NON-NLS-1$
                for (String mailingList : ext.getMailingLists()) {
                    html.append("<li>"); //$NON-NLS-1$
                    html.appendMailToLink(mailingList);
                    List<Link> mappedLinks = mapper.getMappedLinks(configService, project.getProjectId(),
                            mailingList, LinkMapper.ALL_PURPOSES);
                    if (!mappedLinks.isEmpty()) {
                        html.append("<br/>"); //$NON-NLS-1$
                        html.appendLinks(mappedLinks);
                    }
                    html.append("</li>"); //$NON-NLS-1$
                }
                html.append("</ul>"); //$NON-NLS-1$
            }
        }

        if (html.length() > 0) {
            createLabel(layout, html.toString(), STYLE_MAILING);
        } else {
            createLabel(layout, "This project has no mailing lists."); //$NON-NLS-1$
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.2f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        InfoProjectExt ext = project.getExtension(InfoProjectExt.class);
        return ext != null && !ext.getMailingLists().isEmpty();
    }

}
