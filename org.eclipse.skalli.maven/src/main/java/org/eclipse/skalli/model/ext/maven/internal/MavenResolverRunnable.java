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
package org.eclipse.skalli.model.ext.maven.internal;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.ComparatorUtils;
import org.eclipse.skalli.common.util.MapperUtil;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.ValidationException;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.devinf.ScmLocationMapper;
import org.eclipse.skalli.model.ext.devinf.ScmLocationMappingConfig;
import org.eclipse.skalli.model.ext.maven.MavenPathResolver;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.nexus.NexusClient;

public class MavenResolverRunnable implements Runnable {

    private static final Logger LOG = Log.getLogger(MavenResolverRunnable.class);

    private ConfigurationService configService;
    private NexusClient nexusClient;
    private String userId;

    /**
     * @param configService
     * @param nexusClient - if set versions are calculated via nexusClinet. If nexusClient is null the versions cant be calculated.
     * @param userId
     */
    public MavenResolverRunnable(ConfigurationService configService, NexusClient nexusClient, String userId) {
        this.configService = configService;
        this.userId = userId;
        this.nexusClient = nexusClient;
    }

    @Override
    public void run() {
        int countUpdated = 0;
        int countInvalid = 0;
        ProjectService projectService = getProjectService();
        List<Project> projects = projectService.getAll();
        NexusVersionsResolver versionsResolver = new NexusVersionsResolver(nexusClient);
        LOG.info(MessageFormat.format("MavenResolver: started ({0} projects to scan)", projects.size()));
        for (Project project : projects) {
            MavenReactor newReactor = null;
            try {
                newReactor = resolveProject(project);
            } catch (ValidationException e) {
                ++countInvalid;
                LOG.log(Level.INFO, MessageFormat.format(
                        "Invalid Maven reactor information for project {0}:\n {1}",
                        project.getProjectId(), e.getMessage()));
                continue;
            } catch (Throwable t) {
                ++countInvalid;
                LOG.log(Level.SEVERE, MessageFormat.format(
                        "Failed to resolve Maven reactor information for project {0}",
                        project.getProjectId()), t);
                continue;
            }

            try {
                versionsResolver.addNexusVersions(newReactor);
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, MessageFormat.format(
                        "Can''t calculate Versions for project {0} . Unexpected Exception cought: {1}",
                        project.getProjectId(), e.getMessage()));
            }

            MavenReactor oldReactor = getMavenReactorProperty(project);
            if (!ComparatorUtils.equals(newReactor, oldReactor)) {
                if (updateMavenReactorExtension(project, newReactor)) {
                    try {
                        projectService.persist(project, userId);
                        ++countUpdated;
                    } catch (ValidationException e) {
                        ++countInvalid;
                        LOG.log(Level.WARNING, MessageFormat.format(
                                "Failed to persist Maven reactor information for project {0}",
                                project.getProjectId()), e);
                        continue;
                    }
                }
            }

            LOG.finest(MessageFormat.format(
                    "MavenResolver: ({0} projects scanned: {1} updated, {2} invalid, {3} remaining)",
                    projects.size(), countUpdated, countInvalid, projects.size() - countUpdated - countInvalid));

            // delay the execution for 10 seconds, otherwise we may
            // overcharge gitweb/Nexus with out requests
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
        LOG.info(MessageFormat.format(
                "MavenResolver: finished ({0} projects scanned: {1} updated, {2} invalid)",
                projects.size(), countUpdated, countInvalid));
    }

    /**
     * Returns the Maven reactor information for a given project.
     *
     * @param project  the project to resolve.
     * @return  the Maven reactor for the project, or <code>null</code> if
     * <ul>
     * <li>the reactor POM path ({@link MavenProjectExt#PROPERTY_REACTOR_POM}) is not specified,</li>
     * <li>the SCM location ({@link DevInfProjectExt#PROPERTY_SCM_LOCATIONS}) is not defined,</li>
     * <li>the mapping from the SCM location to the source repository is not configured,</li>
     * <li>or the mapping is not applicable for the given SCM location.</li>
     * </ul>
     *
     * @throws IOException  if an i/o error occured, e.g. the connection to the source repository
     * providing POM files cannot be established or is lost.
     * @throws MavenValidationException  if any of the relevant POMs is invalid or cannot be parsed.
     */
    public MavenReactor resolveProject(Project project)
            throws IOException, MavenValidationException {
        String reactorPomPath = getReactorPomPathProperty(project);
        if (reactorPomPath == null) {
            return null;
        }
        String scmLocation = getScmLocationProperty(project);
        if (scmLocation == null) {
            return null;
        }
        MavenPathResolver pathResolver = getMavenPathResolver(configService, scmLocation);
        if (pathResolver == null) {
            return null;
        }
        if (!pathResolver.canResolve(scmLocation)) {
            return null;
        }
        MavenResolver resolver = getMavenResolver(project.getUuid(), pathResolver);
        return resolver.resolve(scmLocation, reactorPomPath);
    }

    private boolean updateMavenReactorExtension(Project project, MavenReactor mavenReactor) {
        MavenReactorProjectExt ext = project.getExtension(MavenReactorProjectExt.class);
        if (ext == null) {
            if (mavenReactor == null) {
                return false;
            }
            ext = new MavenReactorProjectExt();
            project.addExtension(ext);
        }
        ext.setMavenReactor(mavenReactor);
        return true;
    }

    @SuppressWarnings("nls")
    private String getReactorPomPathProperty(Project project) {
        MavenProjectExt ext = project.getExtension(MavenProjectExt.class);
        if (ext == null) {
            return null;
        }
        String reactorPomPath = ext.getReactorPOM();
        if (reactorPomPath == null) {
            reactorPomPath = "";
        }
        if (reactorPomPath.endsWith("/pom.xml")) {
            reactorPomPath = reactorPomPath.substring(0, reactorPomPath.length() - 8);
        }
        if (reactorPomPath.startsWith("/")) {
            reactorPomPath = reactorPomPath.substring(1);
        }
        if (reactorPomPath.endsWith("/")) {
            reactorPomPath = reactorPomPath.substring(0, reactorPomPath.length() - 1);
        }
        return reactorPomPath;
    }

    private String getScmLocationProperty(Project project) {
        DevInfProjectExt devExtension = project.getExtension(DevInfProjectExt.class);
        if (devExtension == null) {
            return null;
        }
        String scmLocation = devExtension.getScmLocation();
        if (StringUtils.isEmpty(scmLocation)) {
            return null;
        }
        return scmLocation;
    }

    private MavenReactor getMavenReactorProperty(Project project) {
        MavenReactorProjectExt ext = project.getExtension(MavenReactorProjectExt.class);
        return ext != null ? ext.getMavenReactor() : null;
    }

    // package protected for testing purposes
    MavenPathResolver getMavenPathResolver(ConfigurationService configService, String scmLocation) {
        if (configService == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("no scm mappings available: configService==null");
            }
            return null;
        }
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<ScmLocationMappingConfig> mappings = mapper.getMappings(configService,
                "git", ScmLocationMapper.PURPOSE_BROWSE); //$NON-NLS-1$
        if (mappings.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "no suitable scm mapping found matching filter provider=''git'' && purpose=''{0}''",
                        ScmLocationMapper.PURPOSE_BROWSE));
            }
            return null;
        }
        for (ScmLocationMappingConfig mapping : mappings) {
            if (MapperUtil.matches(scmLocation, mapping)) {
                return new GitWebPathResolver(mapping.getPattern(), mapping.getTemplate());
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(MessageFormat.format(
                    "no suitable scm mapping found matching scmLocation=''{0}'' && purpose=''{1}''",
                    scmLocation, ScmLocationMapper.PURPOSE_BROWSE));
        }
        return null;
    }

    // package protected for testing purposes
    MavenResolver getMavenResolver(UUID entityId, MavenPathResolver pathResolver) {
        return new MavenResolver(entityId, pathResolver);
    }

    // package protected for testing purposes
    ProjectService getProjectService() {
        return Services.getRequiredService(ProjectService.class);
    }
}
