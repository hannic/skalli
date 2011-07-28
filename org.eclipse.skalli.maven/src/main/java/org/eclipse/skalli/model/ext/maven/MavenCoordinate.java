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

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.util.ComparatorUtils;

public class MavenCoordinate implements Comparable<MavenCoordinate> {

    private String groupId = ""; //$NON-NLS-1$
    private String artefactId = ""; //$NON-NLS-1$
    private String packaging = ""; //$NON-NLS-1$
    private TreeSet<String> versions = new TreeSet<String>();

    public MavenCoordinate() {
    }

    public MavenCoordinate(MavenCoordinate coordinate) {
        this(coordinate.getGroupId(), coordinate.getArtefactId(), coordinate.getPackaging());
        getVersions().addAll(coordinate.getVersions());
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

    public synchronized SortedSet<String> getVersions() {
        if (versions == null) {
            versions = new TreeSet<String>();
        }
        return versions;
    }

    public void addVersion(String version) {
        getVersions().add(version);
    }

    public boolean hasVersion(String version) {
        return getVersions().contains(version);
    }

    public void removeVersion(String version) {
        getVersions().remove(version);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artefactId == null) ? 0 : artefactId.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((packaging == null) ? 0 : packaging.hashCode());
        for (String version: getVersions()) {
            result = prime * result + ((version == null) ? 0 : version.hashCode());
        }
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
                if (result == 0) {
                    result = ComparatorUtils.compare(versions, o.versions);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupId);
        sb.append(':');
        sb.append(artefactId);
        if (StringUtils.isNotBlank(packaging)) {
            sb.append(':');
            sb.append(packaging);
        }
        if (getVersions().size() > 0) {
            sb.append(':');
            sb.append(StringUtils.join(getVersions(), ','));
        }
        return sb.toString();
    }
}
