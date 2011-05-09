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
package org.eclipse.skalli.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.skalli.log.internal.JdkLogManager;

public class Log {

    public static void setDefaultLogLevel(Level level) {
        JdkLogManager.getInstance().setDefaultLogLevel(level);
    }

    public static Logger getLogger(String name) {
        return JdkLogManager.getInstance().getLogger(name);
    }

    public static Logger getLogger(Class<?> c) {
        return JdkLogManager.getInstance().getLogger(c.getName());
    }
}
