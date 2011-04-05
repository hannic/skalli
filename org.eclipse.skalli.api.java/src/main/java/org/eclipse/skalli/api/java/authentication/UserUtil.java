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
package org.eclipse.skalli.api.java.authentication;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.api.java.GroupService;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.UserService;
import org.eclipse.skalli.common.UserServiceUtil;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;

public class UserUtil {

  public static User getUser(String userId) {
    if (userId != null) {
      UserService userService = UserServiceUtil.getUserService();
      User user = userService.getUserById(userId.toString());
      if (user != null) {
        return user;
      }
    }
    return null;
  }


  /**
   * Returns <code>true</code> if the currently authenticated user belongs
   * to the group of administrators (see {@link GroupService}).
   * Certain administrators can be "banned" temporaily by defining the
   * system property <tt>"-DbannedAdmins=&lt;comma-separated list of user ids&gt;"</tt>.
   * @param userId  the unique identifier of a user.
   */
  public static boolean isAdministrator(String userId) {
    String bannedAdmins = System.getProperty("bannedAdmins"); //$NON-NLS-1$
    if (StringUtils.isNotBlank(bannedAdmins)) {
      String[] bannedAdminsList = StringUtils.split(bannedAdmins, ',');
      for (String bannedAdmin : bannedAdminsList) {
        if (bannedAdmin.equals(userId)) {
          return false;
        }
      }
    }
    GroupService groupService = Services.getRequiredService(GroupService.class);
    return groupService.isAdministrator(userId);
  }

  public static boolean isAdministrator(User user) {
    if (user != null) {
      return isAdministrator(user.getUserId());
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the currently authenticated user belongs
   * to the group of administrators of the given project. Currently this
   * method treats all project members as project administrators.
   * @param userId  the unique identifier of a user.
   * @param project  a project.
   */
  // TODO authorization with configurable project admin group
  public static boolean isProjectAdmin(String userId, Project project) {
    if (project != null) {
      for (ProjectMember member : Services.getRequiredService(ProjectService.class).getAllPeople(project)) {
        if (StringUtils.equalsIgnoreCase(member.getUserID(), userId)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isProjectAdmin(User user, Project project) {
    if (user != null) {
      return isProjectAdmin(user.getUserId(), project);
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the currently authenticated user belongs
   * to the group of administrators in the parent chain of the given project.
   * Currently this method treats all project members as project administrators.
   * @param userId  the unique identifier of a user.
   * @param project  a project.
   */
  public static boolean isProjectAdminInParentChain(String userId, Project project) {
    if (project == null || StringUtils.isBlank(userId)) {
      return false;
    }
    for (Project parent : Services.getRequiredService(ProjectService.class).getParentChain(project.getUuid())) {
      if (isProjectAdmin(userId, parent)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isProjectAdminInParentChain(User user, Project project) {
    if (user == null) {
      return false;
    }

    return isProjectAdminInParentChain(user.getUserId(), project);
  }
}

