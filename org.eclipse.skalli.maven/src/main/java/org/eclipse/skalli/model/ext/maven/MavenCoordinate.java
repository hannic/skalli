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
package org.eclipse.skalli.model.ext.maven;

import org.eclipse.skalli.common.util.ComparatorUtils;

public class MavenCoordinate implements Comparable<MavenCoordinate> {

  private String groupId = ""; //$NON-NLS-1$
  private String artefactId = ""; //$NON-NLS-1$
  private String packaging = ""; //$NON-NLS-1$

  public MavenCoordinate() {
  }

  public MavenCoordinate(String groupId, String artefactId, String packaging) {
    this.groupId = groupId;
    this.artefactId = artefactId;
    this.packaging = packaging;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtefactId() {
    return artefactId;
  }

  public void setArtefactId(String artefactId) {
    this.artefactId = artefactId;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((artefactId == null) ? 0 : artefactId.hashCode());
    result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
    result = prime * result + ((packaging == null) ? 0 : packaging.hashCode());
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    MavenCoordinate other = (MavenCoordinate) obj;
    return compareTo(other) == 0;
  }

  @Override
  public int compareTo(MavenCoordinate o) {
    int result = ComparatorUtils.compare(groupId, o.groupId);
    if (result == 0) {
      result = ComparatorUtils.compare(artefactId, o.artefactId);
      if (result == 0) {
        result = ComparatorUtils.compare(packaging, o.packaging);
      }
    }
    return result;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return groupId + ":" + artefactId + ":" + packaging;
  }
}

