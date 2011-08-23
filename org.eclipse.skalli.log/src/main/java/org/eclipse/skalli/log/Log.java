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

import java.util.logging.Logger;

import org.eclipse.skalli.log.internal.DefaultLoggerManager;

/**
 * Utility class to retrieve {@link java.util.logging.Logger loggers}.
 */
public class Log {

    /**
     * Returns a {@link java.util.logging.Logger logger} for the given class.
     *
     * @param c  the class for which to retrieve a logger.
     * @return  a logger instance.
     */
    public static Logger getLogger(Class<?> c) {
        DefaultLoggerManager instance = DefaultLoggerManager.getInstance();
        if (instance != null) {
            return instance.getLogger(c);
        }
        return Logger.getLogger(c.getName());
    }
}
