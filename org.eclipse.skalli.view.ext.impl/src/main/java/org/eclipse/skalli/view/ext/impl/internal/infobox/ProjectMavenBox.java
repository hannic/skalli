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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.MapperUtil;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.devinf.ScmLocationMapper;
import org.eclipse.skalli.model.ext.devinf.ScmLocationMappingConfig;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.view.ext.AbstractInfoBox;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;

public class ProjectMavenBox extends AbstractInfoBox implements ProjectInfoBox {

    private static final String DEFAULT_POM_FILENAME = "pom.xml"; //$NON-NLS-1$

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public String getIconPath() {
        return "res/icons/maven.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Maven Project Information";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        boolean rendered = false;
        MavenProjectExt mavenExt = project.getExtension(MavenProjectExt.class);
        if (mavenExt != null) {
            if (StringUtils.isNotBlank(mavenExt.getGroupID())) {
                Label label = new Label("GroupId - <b>" + mavenExt.getGroupID() + "</b>", Label.CONTENT_XHTML);
                label.addStyleName(STYLE_LABEL);
                layout.addComponent(label);
                rendered = true;
            }
            DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
            if (devInf != null) {
                String reactorPomUrl = getReactorPomUrl(project, devInf, mavenExt);
                if (reactorPomUrl == null) {
                    String reactorPomPath = mavenExt.getReactorPOM();
                    Label label = new Label(MessageFormat.format(
                            "Reactor POM Path: {0} (relative to SCM root location)",
                            StringUtils.isNotBlank(reactorPomPath) ? reactorPomPath : "/"));
                    label.addStyleName(STYLE_LABEL);
                    layout.addComponent(label);
                } else {
                    Link link = new Link("Reactor POM", new ExternalResource(reactorPomUrl));
                    link.addStyleName(STYLE_LINK);
                    layout.addComponent(link);
                }
                rendered = true;
            }
            if (StringUtils.isNotBlank(mavenExt.getSiteUrl())) {
                Link link = new Link("Project Site", new ExternalResource(mavenExt.getSiteUrl()));
                link.addStyleName(STYLE_LINK);
                layout.addComponent(link);
                rendered = true;
            }

        }
        if (!rendered) {
            Label label = new Label("Maven extension added but no data maintained.");
            label.addStyleName(STYLE_LABEL);
            layout.addComponent(label);
        }
        return layout;
    }

    private String getReactorPomUrl(Project project, DevInfProjectExt devInf, MavenProjectExt mavenExt) {
        if (configService == null) {
            return null;
        }
        String scmLocation = devInf.getScmLocation();
        if (StringUtils.isBlank(scmLocation)) {
            return null;
        }
        String relativePath = mavenExt.getReactorPOM();
        if (!isValidNormalizedPath(relativePath)) {
            return null;
        }
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<ScmLocationMappingConfig> mappings = mapper.getMappings(configService,
                "git", ScmLocationMapper.PURPOSE_BROWSE); //$NON-NLS-1$
        if (mappings.isEmpty()) {
            return null;
        }
        String repositoryRoot = null;
        for (ScmLocationMappingConfig mapping : mappings) {
            repositoryRoot = MapperUtil.convert(project.getProjectId(), scmLocation, mapping);
            if (StringUtils.isNotBlank(repositoryRoot)) {
                break;
            }
        }
        if (StringUtils.isBlank(repositoryRoot)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(repositoryRoot);
        sb.append(";f="); //$NON-NLS-1$
        if (StringUtils.isBlank(relativePath) || ".".equals(relativePath)) { //$NON-NLS-1$
            sb.append(DEFAULT_POM_FILENAME);
        }
        else if (!relativePath.endsWith(DEFAULT_POM_FILENAME)) {
            appendPath(sb, relativePath);
            if (!relativePath.endsWith("/")) { //$NON-NLS-1$
                sb.append("/"); //$NON-NLS-1$
            }
            sb.append(DEFAULT_POM_FILENAME);
        }
        else {
            appendPath(sb, relativePath);
        }
        sb.append(";hb=HEAD"); //$NON-NLS-1$
        return sb.toString();
    }

    private void appendPath(StringBuilder sb, String relativePath) {
        if (relativePath.charAt(0) == '/') {
            sb.append(relativePath.substring(1));
        } else {
            sb.append(relativePath);
        }
    }

    @SuppressWarnings("nls")
    private boolean isValidNormalizedPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            if (path.indexOf('\\') >= 0) {
                return false;
            }
            if (path.indexOf("..") >= 0 ||
                    path.startsWith("./") ||
                    path.endsWith("/.") ||
                    path.indexOf("/./") >= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float getPositionWeight() {
        return 1.6f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        return project.getExtension(MavenProjectExt.class) != null;
    }

}
