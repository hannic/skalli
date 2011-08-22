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
package org.eclipse.skalli.core.internal.validation;

import org.eclipse.skalli.api.java.Validation;
import org.eclipse.skalli.model.ext.EntityBase;

class QueuedEntity<T extends EntityBase> extends Validation<T> {
    private long queuedAt = -1L;
    private long startedAt = -1L;

    public QueuedEntity(Validation<T> validation) {
        super(validation.getEntityClass(), validation.getEntityId(),
                validation.getMinSeverity(), validation.getUserId());
        queuedAt = System.currentTimeMillis();
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getQueuedAt() {
        return queuedAt;
    }
}