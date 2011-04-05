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
package org.eclipse.skalli.model.ext.maven.internal;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.common.util.ComparatorUtils;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;

public class MavenPom {

  private MavenCoordinate self;
  private MavenCoordinate parent;
  private String parentRelativePath;
  private TreeSet<String> modules = new TreeSet<String>();


  public MavenCoordinate getSelf() {
    return self;
  }

  void setSelf(MavenCoordinate self) {
    this.self = self;
  }

  public MavenCoordinate getParent() {
    return parent;
  }

  void setParent(MavenCoordinate parent) {
    this.parent = parent;
  }

  public String getParentRelativePath() {
    return parentRelativePath;
  }

  void setParentRelativePath(String parentRelativePath) {
    this.parentRelativePath = parentRelativePath;
  }

  public Set<String> getModuleTags() {
    return modules;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + modules.hashCode();
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((parentRelativePath == null) ? 0 : parentRelativePath.hashCode());
    result = prime * result + ((self == null) ? 0 : self.hashCode());
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
    if (obj instanceof MavenPom) {
      MavenPom other = (MavenPom) obj;
      if (modules.equals(other.modules)
          && ComparatorUtils.equals(parent, other.parent)
          && ComparatorUtils.equals(parentRelativePath, other.parentRelativePath)
          && ComparatorUtils.equals(self, other.self)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "MavenPom [self=" + self + ", parent=" + parent + ", parentRelativePath=" + parentRelativePath
        + ", modules=" + modules + "]";
  }

}

