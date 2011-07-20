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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Link;
import org.eclipse.skalli.model.ext.LinkMapper;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;
import org.eclipse.skalli.model.ext.info.MailingListMapper;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ProjectMailingListBox extends InfoBox implements ProjectInfoBox {

    private static final Logger LOG = Log.getLogger(ProjectMailingListBox.class);
    private static final String STYLE_MAILING = "mailingList"; //$NON-NLS-1$
    private static final URLCodec URL_CODEC = new URLCodec();

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
        return "Mailing List";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        StringBuilder sb = new StringBuilder();
        InfoProjectExt ext = project.getExtension(InfoProjectExt.class);
        if (ext != null) {
            Set<String> mailingLists = ext.getMailingLists();
            if (mailingLists.size() > 0) {
                MailingListMapper mapper = new MailingListMapper();

                sb.append("<ul>"); //$NON-NLS-1$
                for (String mailingList : ext.getMailingLists()) {
                    try {
                        String urlEncoded = URL_CODEC.encode(mailingList);
                        String htmlEncoded = StringEscapeUtils.escapeHtml(mailingList);
                        sb.append("<li>"); //$NON-NLS-1$
                        sb.append("<a href=\"mailto:").append(urlEncoded).append("\">").append(htmlEncoded).append("</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        // Mapped / Generated Links
                        boolean isFirst = true;
                        List<Link> mappedLinks = mapper.getMappedLinks(configService, project.getProjectId(),
                                mailingList, LinkMapper.ALL_PURPOSES);
                        if (!mappedLinks.isEmpty()) {
                            sb.append("<br/>"); //$NON-NLS-1$
                            for (Link mappedLink : mappedLinks) {
                                sb.append(" <a href=\""); //$NON-NLS-1$
                                sb.append(mappedLink.getUrl());
                                sb.append("\" target=\"_blank\""); //$NON-NLS-1$
                                if (isFirst) {
                                    sb.append(">"); //$NON-NLS-1$
                                } else {
                                    sb.append(" class=\"leftMargin\">"); //$NON-NLS-1$
                                }
                                sb.append(mappedLink.getLabel());
                                sb.append("</a>"); //$NON-NLS-1$
                                isFirst = false;
                            }
                        }
                        sb.append("</li>"); //$NON-NLS-1$
                    } catch (EncoderException e) {
                        LOG.log(Level.WARNING, "Error while url-encoding mailing list: " + mailingList, e); //$NON-NLS-1$
                    }
                }
                sb.append("</ul>"); //$NON-NLS-1$
            }
        }

        if (sb.length() == 0) {
            sb.append("<div>").append("This project has no mailing lists.").append("</div>"); //$NON-NLS-1$ //$NON-NLS-3$
        }

        Label mailingListLabel = new Label(sb.toString(), Label.CONTENT_XHTML);
        mailingListLabel.addStyleName(STYLE_MAILING);
        layout.addComponent(mailingListLabel);

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
        if (ext != null && ext.getMailingLists() != null && !ext.getMailingLists().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
