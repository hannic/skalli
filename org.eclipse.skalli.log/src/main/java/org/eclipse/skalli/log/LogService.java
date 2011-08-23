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

import java.util.logging.LogRecord;

/**
 * Adapter interface for the Skalli logging framework.
 */
public interface LogService {

    /**
     * Logs the given {@link LogRecord}.
     * @see {@link java.util.logging.Logger#log(LogRecord)}
     */
    public void log(LogRecord record);

    /**
     * Closes the log service.
     */
    public void close();

    /**
     * Flushes the log stream.
     */
    public void flush();
}
