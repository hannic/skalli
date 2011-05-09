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

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Link;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.devinf.ScmLocationMapper;
import org.eclipse.skalli.view.ext.AbstractInfoBox;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ProjectDevInfBox extends AbstractInfoBox implements ProjectInfoBox {

    private static final String STYLE_DEFINF = "devInf"; //$NON-NLS-1$

    private static final String FLASH_CLIPPY = "/VAADIN/themes/simple//flash/clippy.swf"; //$NON-NLS-1$

    // TODO: solve the icon madness
    private static final String ICON_SOURCES = "/VAADIN/themes/simple/icons/devinf/code.png"; //$NON-NLS-1$
    private static final String ICON_BUGTRACKER = "/VAADIN/themes/simple/icons/devinf/bug.png"; //$NON-NLS-1$
    private static final String ICON_METRICS = "/VAADIN/themes/simple/icons/devinf/metrics.png"; //$NON-NLS-1$
    private static final String ICON_CI_SERVER = "/VAADIN/themes/simple/icons/devinf/ci_server.png"; //$NON-NLS-1$
    private static final String ICON_REVIEW = "/VAADIN/themes/simple/icons/devinf/review.png"; //$NON-NLS-1$
    private static final String ICON_JAVADOC = "/VAADIN/themes/simple/icons/devinf/javadoc.png"; //$NON-NLS-1$

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public String getIconPath() {
        return "res/icons/devInfInfo.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Development Information";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        boolean rendered = false;
        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);

        StringBuffer sb = new StringBuffer();

        if (devInf != null) {
            // Project Sources
            if (StringUtils.isNotBlank(devInf.getScmUrl())) {
                iconizedLink(sb, ICON_SOURCES, "Project Sources", devInf.getScmUrl());
                rendered = true;
            }
            // Bug Tracker
            if (StringUtils.isNotBlank(devInf.getBugtrackerUrl())) {
                iconizedLink(sb, ICON_BUGTRACKER, "Bug Tracker", devInf.getBugtrackerUrl());
                rendered = true;
            }
            // Code Metrics
            if (StringUtils.isNotBlank(devInf.getMetricsUrl())) {
                iconizedLink(sb, ICON_METRICS, "Code Metrics", devInf.getMetricsUrl());
                rendered = true;
            }
            // CI / Build Server
            if (StringUtils.isNotBlank(devInf.getCiUrl())) {
                iconizedLink(sb, ICON_CI_SERVER, "Continuous Integration / Build Server", devInf.getCiUrl());
                rendered = true;
            }
            // Code Review
            if (StringUtils.isNotBlank(devInf.getReviewUrl())) {
                iconizedLink(sb, ICON_REVIEW, "Code Review", devInf.getReviewUrl());
                rendered = true;
            }
            // Javadoc
            if (CollectionUtils.isNotBlank(devInf.getJavadocs())) {
                iconizedLinks(sb, ICON_JAVADOC, "Javadoc", "more Javadoc", devInf.getJavadocs());
                rendered = true;
            }

            // SCM Locations
            if (CollectionUtils.isNotBlank(devInf.getScmLocations())) {
                sb.append("<h4>").append("Source Locations").append("</h4>\n"); //$NON-NLS-1$ //$NON-NLS-3$
                sb.append("<ul>\n"); //$NON-NLS-1$

                ScmLocationMapper mapper = new ScmLocationMapper();
                for (String scmUrl : devInf.getScmLocations()) {
                    sb.append("<li>"); //$NON-NLS-1$
                    clippyLink(sb, scmUrl, scmUrl);

                    // Mapped / Generated Links
                    boolean isFirst = true;
                    List<Link> mappedScmLinks = mapper.getMappedLinks(configService, project.getProjectId(), scmUrl,
                            ScmLocationMapper.PURPOSE_BROWSE, ScmLocationMapper.PURPOSE_REVIEW);
                    for (Link mappedScmLink : mappedScmLinks) {
                        sb.append("<a href=\""); //$NON-NLS-1$
                        sb.append(mappedScmLink.getUrl());
                        sb.append("\" target=\"_blank\""); //$NON-NLS-1$
                        if (isFirst) {
                            sb.append(">"); //$NON-NLS-1$
                        } else {
                            sb.append(" class=\"leftMargin\">"); //$NON-NLS-1$
                        }
                        sb.append(mappedScmLink.getLabel());
                        sb.append("</a>"); //$NON-NLS-1$
                        isFirst = false;
                    }
                    sb.append("</li>"); //$NON-NLS-1$
                }

                sb.append("</ul>\n"); //$NON-NLS-1$
                rendered = true;
            }
        }

        Label label;
        if (rendered) {
            label = new Label(sb.toString(), Label.CONTENT_XHTML);
            label.addStyleName(STYLE_DEFINF);
        } else {
            label = new Label("Development Infrastructure extension added but no data maintained.");
            label.addStyleName(STYLE_LABEL);
        }
        layout.addComponent(label);

        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.5f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        if (project.getExtension(DevInfProjectExt.class) != null) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("nls")
    private void iconizedLink(final StringBuffer sb, final String icon, final String label, final String url) {
        sb.append("<img src =\"").append(icon).append("\" alt=\"\" />\n");
        sb.append("<a href=\"").append(url).append("\" target=\"_blank\">").append(label).append("</a>\n");
        sb.append("<br/>");
    }

    @SuppressWarnings("nls")
    private void iconizedLinks(final StringBuffer sb, final String icon, final String firstLabel,
            final String otherLabel, final Set<String> links) {
        if (links.size() == 1) {
            iconizedLink(sb, icon, firstLabel, links.iterator().next());
        }
        else {
            sb.append("<img src =\"").append(icon).append("\" alt=\"\" />\n");

            boolean isFirst = true;
            for (String url : links) {
                String label = (isFirst) ? firstLabel : otherLabel;
                sb.append("<a href=\"").append(url).append("\" target=\"_blank\"")
                        .append(isFirst ? ">" : " class=\"leftMargin\">").append(label).append("</a>");

                isFirst = false;
            }
            sb.append("<br/>");
        }
    }

    @SuppressWarnings("nls")
    private void clippyLink(final StringBuffer sb, final String label, String textToClipboard) {
        sb.append("<div>").append(label);

        /*
        sb.append("<object style=\"margin-left:5px;\"");
        sb.append(" classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\"");
        sb.append(" width=\"110\"");
        sb.append(" height=\"14\"");
        sb.append(" id=\"clippy_").append(System.currentTimeMillis()).append("\">\n");

        sb.append("<param name=\"movie\" value=\"").append(FLASH_CLIPPY).append("\" />\n");
        sb.append("<param name=\"allowScriptAccess\" value=\"always\" />\n");
        sb.append("<param name=\"quality\" value=\"high\" />\n");
        sb.append("<param name=\"scale\" value=\"noscale\" />\n");
        sb.append("<param name=\"FlashVars\" value=\"text=").append(truncate(textToClipboard)).append("\" />\n");
        sb.append("<param name=\"bgcolor\" value=\"#FFFFFF\" />\n");

        sb.append("<embed src=\"").append(FLASH_CLIPPY).append("\"");
        sb.append(" width=\"110\"");
        sb.append(" height=\"14\"");
        sb.append(" name=\"clippy\"");
        sb.append(" quality=\"high\"");
        sb.append(" allowScriptAccess=\"always\"");
        sb.append(" type=\"application/x-shockwave-flash\"");
        sb.append(" pluginspage=\"http://www.macromedia.com/go/getflashplayer\"");
        sb.append(" FlashVars=\"text=").append(truncate(textToClipboard)).append("\"");
        sb.append(" bgcolor=\"#FFFFFF\"");
        sb.append(" />\n");

        sb.append("</object>\n");
        */

        sb.append("</div>\n");
    }

    // truncate to have a useful SCM URL in the clipboard
    @SuppressWarnings("nls")
    private String truncate(String url) {
        return url.replaceFirst("^scm:.+?:", "");
    }
}
