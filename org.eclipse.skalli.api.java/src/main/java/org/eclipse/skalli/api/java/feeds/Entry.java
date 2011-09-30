/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.api.java.feeds;

import java.util.Date;
import java.util.UUID;

public interface Entry {

    /** Returns the SHA-1 key of the entry */
    public String getId();

    public void setId(String id);

    public UUID getProjectId();

    public void setProjectId(UUID projectId);

    public String getSource();

    public void setSource(String source);

    public String getTitle();

    public void setTitle(String title);

    public Date getPublished();

    public void setPublished(Date published);

    public Link getLink();

    public Content getContent();

    public Person getAuthor();

}
