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
package org.eclipse.skalli.model.ext;

import java.util.Set;

/**
 * Marker interface for model entities that support tagging.
 */
public interface Taggable {
    public Set<String> getTags();

    public void addTag(String tag);

    public void removeTag(String tag);

    public boolean hasTag(String tag);
}
