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
package org.eclipse.skalli.commands;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class CommandProviderImpl implements CommandProvider {

  private static final String COMMAND = "skalli"; //$NON-NLS-1$
  private static final String COMMAND_ADMIN = "admin"; //$NON-NLS-1$

  private static final String OPTION_LIST = "-list"; //$NON-NLS-1$
  private static final String OPTION_ADD = "-add"; //$NON-NLS-1$
  private static final String OPTION_REMOVE = "-remove"; //$NON-NLS-1$

  private static final String DESCRIPTION_ADMIN = "maintain set of user with administrative permissions";

  private static String HELP_ADMIN = "\t" + COMMAND + " " + COMMAND_ADMIN + //$NON-NLS-1$ //$NON-NLS-2$
      " [" + OPTION_LIST + "]" + //$NON-NLS-1$ //$NON-NLS-2$
      " [" + OPTION_ADD + " <admin_id>]" + //$NON-NLS-1$ //$NON-NLS-2$
      " [" + OPTION_REMOVE + " <admin_id>]" + //$NON-NLS-1$//$NON-NLS-2$
      " - " + DESCRIPTION_ADMIN + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
  private static String HELP = "\n---Skalli---\n" + HELP_ADMIN; //$NON-NLS-1$

  private static String INVALID = "Invalid command invocation.\n" + HELP; //$NON-NLS-1$

  @Override
  public String getHelp() {
    return HELP;
  }

  public void _skalli(CommandInterpreter intr) {
    try {
      String command = intr.nextArgument();
      if (StringUtils.equals(command, COMMAND_ADMIN)) {
        String option = intr.nextArgument();
        if (StringUtils.equals(option, OPTION_LIST)) {
          intr.println(AdminCommand.list());
        } else if (StringUtils.equals(option, OPTION_ADD)) {
          String adminId = intr.nextArgument();
          if (StringUtils.isNotBlank(adminId)) {
            intr.println(AdminCommand.add(adminId));
          } else {
            intr.println(INVALID);
          }
        } else if (StringUtils.equals(option, OPTION_REMOVE)) {
          String adminId = intr.nextArgument();
          if (StringUtils.isNotBlank(adminId)) {
            intr.println(AdminCommand.remove(adminId));
          } else {
            intr.println(INVALID);
          }
        } else {
          intr.println(INVALID);
        }
      } else {
        // command not valid or null, print help
        intr.println(HELP);
      }
    } catch (Exception e) {
      intr.printStackTrace(e);
    }
  }

}

