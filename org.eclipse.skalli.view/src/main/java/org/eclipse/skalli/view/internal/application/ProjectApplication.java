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
package org.eclipse.skalli.view.internal.application;

import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.authentication.LoginUtil;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.view.internal.window.ProjectWindow;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

/**
 * project portal (vaadin) application. implements the layout and screenflow of
 * this application.
 */
@SuppressWarnings("serial")
public class ProjectApplication extends com.vaadin.Application implements HttpServletRequestListener {

    public static final String WINDOW_TITLE = "ProjectPortal";

    private String userId;

    public ProjectApplication(User user) {
        this.userId = user != null ? StringUtils.lowerCase(user.getUserId()) : null;
        this.setUser(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        setTheme("simple"); //$NON-NLS-1$
        setMainWindow(new Window(WINDOW_TITLE));
    }

    public void refresh(Project project) {
        if (project != null) {
            ProjectWindow projectWindow = (ProjectWindow) getWindow(project.getProjectId());
            if (projectWindow != null) {
                projectWindow.refreshProject(project);
                projectWindow.requestRepaint();
            }
        }
    }

    @Override
    public Window getWindow(String name) {
        Window window = super.getWindow(name);
        if (window == null) {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            Project project = projectService.getProjectByProjectId(name);
            if (project == null) {
                UUID uuid = UUIDUtils.asUUID(name);
                if (uuid != null) {
                    project = projectService.getByUUID(uuid);
                    if (project == null) {
                        project = projectService.getDeletedProject(uuid);
                    }
                }
            }
            // make sure that we have no "dangling" template select views
            if (project == null) {
                ArrayList<Window> allWindows = new ArrayList<Window>(getWindows());
                for (Window w : allWindows) {
                    removeWindow(w);
                }
            }
            window = new ProjectWindow(this, project); // project==null opens create dialog
            window.setName(name);
            addWindow(window);
        }
        return window;
    }

    /***********************
     * some helper methods
     ***********************/

    public String getLoggedInUser() {
        return userId;
    }

    /**************************************
     * Interface HttpServletRequestListener
     *
     * implementing the edit link of projects is a hack...
     * replace edit link implementation as soon as possible
     */

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        LoginUtil util = new LoginUtil(request);
        userId = util.getLoggedInUserId();

        if (request.getParameter("windowName") != null) { //$NON-NLS-1$
            String requestUri = request.getRequestURI();
            String windowName = request.getParameter("windowName"); //$NON-NLS-1$
            String uidlUri = "UIDL/" + windowName; //$NON-NLS-1$

            if (requestUri.contains(uidlUri)) {
                String[] uriparts = requestUri.split(uidlUri);
                String relativeUri = ""; //$NON-NLS-1$
                if (uriparts.length > 1) {
                    relativeUri = uriparts[uriparts.length - 1];
                    if (relativeUri.startsWith("/")) { //$NON-NLS-1$
                        relativeUri = relativeUri.replaceFirst("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                ((ProjectWindow) getWindow(windowName)).handleRelativeURI(relativeUri);
            }
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
    }
}
