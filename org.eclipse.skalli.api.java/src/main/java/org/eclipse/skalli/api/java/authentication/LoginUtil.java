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

import java.security.Principal;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.User;

/**
 * Helper class to manage logged in user.
 */
public class LoginUtil {

  private String userName;

  public LoginUtil(ServletRequest request) {
    if (request instanceof HttpServletRequest) {
      Principal userPrincipal = ((HttpServletRequest) request).getUserPrincipal();
      if (userPrincipal != null) {
        userName = userPrincipal.getName();
        if (StringUtils.isNotBlank(userName)) {
          userName = userName.toLowerCase(Locale.ENGLISH);
        }
      }
    }
  }

  /**
   * Returns the unique identifier of the user that is currently authenticated.
   *
   * @return the unique identifier of a user, or <code>null</code>, if no user
   *         is authenticated or no proper authentication mechanism is in place.
   */
  public String getLoggedInUserId() {
    return userName;
  }

  /**
   * Returns the user that is currently authenticated.
   *
   * <p>
   * Please note: Using this method may invoke a remote call to the user store (i.e. LDAP).
   * Hence if you only need the UserId of the currently authenticated user,
   * you should use {@link #getLoggedInUserId()} instead.
   * </p>
   *
   * @return a user, or <code>null</code>, if no user is authenticated or no
   *         proper authentication mechanism is in place.
   */
  public User getLoggedInUser() {
    return UserUtil.getUser(getLoggedInUserId());
  }
}
