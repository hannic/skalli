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
package org.eclipse.skalli.api.java;

import java.util.UUID;

import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.Severity;

/**
 * Helper class describing entities scheduled for validation.
 * A <code>Validation</code> must specify the unique identifier and
 * class of an entity, the identifier of the user that initiated the validation,
 * and may optionally specify the minimal {@link Severity} to apply to the validation.
 * If no severity is specified, {@link Severity#INFO} is assumed.
 */
public class Validation<T extends EntityBase> {
  private Class<T> entityClass;
  private UUID entityId;
  private Severity minSeverity;
  private String userId;

  /**
   * Creates a <code>Validation</code> instance.
   *
   * @param entityClass  the class of the entities to validate.
   * @param entityId  the unique identifier of the entity to validate.
   * @param minSeverity  the minimal severity of issues to report.
   * @param userId  unique identifier of the user initiating the validation.
   */
  public Validation(Class<T> entityClass, UUID entityId, Severity minSeverity, String userId) {
    if (entityClass == null) {
      throw new IllegalArgumentException("argument 'entityClass' must not be null");
    }
    if (entityId == null) {
      throw new IllegalArgumentException("argument 'entityId' must not be null");
    }
    this.entityClass = entityClass;
    this.entityId = entityId;
    setUserId(userId);
    setMinSeverity(minSeverity);
  }

  public Class<T> getEntityClass() {
    return entityClass;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public Severity getMinSeverity() {
    return minSeverity;
  }

  public void setMinSeverity(Severity minSeverity) {
    if (minSeverity == null) {
      minSeverity = Severity.INFO;
    }
    this.minSeverity = minSeverity;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    if (userId == null) {
      throw new IllegalArgumentException("argument 'userId' must not be null");
    }
    this.userId = userId;
  }

  @Override
  public int hashCode() {
    int result = 31 + entityClass.getName().hashCode();
    result = 31 * result + entityId.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj instanceof Validation) {
      Validation<?> v = (Validation<?>)obj;
      return entityClass.equals(v.entityClass)
        && entityId.equals(v.entityId);
    }
    return false;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "Validation [entityClass=" + entityClass + ", entityId=" + entityId + ", minSeverity=" + minSeverity
        + ", userId=" + userId + "]";
  }


}
