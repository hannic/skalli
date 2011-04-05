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
package org.eclipse.skalli.log.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import org.eclipse.skalli.log.internal.listener.ConsoleLogListener;

/**
 * Binds the {@link JdkLogManager} to the {@link LogService} and {@link LogReaderService}.
 */
public class LoggingComponent {

  private final LogListener consoleLogListener = new ConsoleLogListener();

  protected void activate(ComponentContext context) {
  }

  protected void deactivate(ComponentContext context) {
  }

  protected void bindLogService(LogService logService) {
    JdkLogManager.getInstance().bindLogService(logService);
  }

  protected void unbindLogService(LogService logService) {
    JdkLogManager.getInstance().unbindLogService(logService);
  }

  protected void bindLogReader(LogReaderService logServiceReader) {
    logServiceReader.addLogListener(consoleLogListener);
  }

  protected void unbindLogReader(LogReaderService logServiceReader) {
    logServiceReader.removeLogListener(consoleLogListener);
  }
}

