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
package org.eclipse.skalli.api.rest.internal.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Specialized XStream for our needs: MapperWrapperIgnoreUnknownElements to
 * ignore unknown fields.
 */
public class IgnoreUnknownElementsXStream extends XStream {

  public IgnoreUnknownElementsXStream() {
  }

  @Override
  protected MapperWrapper wrapMapper(MapperWrapper next) {
    return new MapperWrapperIgnoreUnknownElements(next);
  }

}

