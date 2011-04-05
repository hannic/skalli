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
package org.eclipse.skalli.view.internal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutServlet extends HttpServlet {

  private static final long serialVersionUID = -6622092406596651354L;

//  private static final String PARAM_RETURNURL = "returnUrl"; //$NON-NLS-1$

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // TODO solve problem: if return url points to a page that needs authentication,
    // logout is followed by login immediately. this would be very confusing to the
    // user, therefore redirect to the root page as a workaround.

//    // go back to calling page if provided
//    String returnUrl = (String) request.getParameter(PARAM_RETURNURL);
//    if (StringUtils.isBlank(returnUrl)) {
//      returnUrl = "/"; //$NON-NLS-1$
//    }
    String returnUrl = "/"; //$NON-NLS-1$


    response.sendRedirect(returnUrl);
    request.getSession().invalidate();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

}

