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

import static org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil.TEST_COORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.eclipse.skalli.model.ext.maven.MavenCoordinate;

@SuppressWarnings("nls")
public class MavenCoordinateTest {

    @Test
    public void testConstructor() throws Exception {
        assertEquals(MavenCoordinateUtil.GROUPID, TEST_COORD.getGroupId());
        assertEquals(MavenCoordinateUtil.ARTIFACT, TEST_COORD.getArtefactId());
        assertEquals(MavenCoordinateUtil.PACKAGING, TEST_COORD.getPackaging());
    }

    @Test
    public void testCompare() throws Exception {
        MavenCoordinate left = TEST_COORD;
        MavenCoordinate right = new MavenCoordinate("com.example.test", MavenCoordinateUtil.ARTIFACT,
                MavenCoordinateUtil.PACKAGING);
        assertTrue(left.compareTo(right) > 0);
        assertTrue(right.compareTo(left) < 0);

        right = new MavenCoordinate(MavenCoordinateUtil.GROUPID, "b", MavenCoordinateUtil.PACKAGING);
        assertTrue(left.compareTo(right) < 0);
        assertTrue(right.compareTo(left) > 0);

        right = new MavenCoordinate(MavenCoordinateUtil.GROUPID, MavenCoordinateUtil.ARTIFACT, "eclipse-plugin");
        assertTrue(left.compareTo(right) > 0);
        assertTrue(right.compareTo(left) < 0);

        right = new MavenCoordinate(null, MavenCoordinateUtil.ARTIFACT, MavenCoordinateUtil.PACKAGING);
        assertTrue(left.compareTo(right) > 0);
        assertTrue(right.compareTo(left) < 0);

        right = new MavenCoordinate(MavenCoordinateUtil.GROUPID, null, MavenCoordinateUtil.PACKAGING);
        assertTrue(left.compareTo(right) > 0);
        assertTrue(right.compareTo(left) < 0);

        right = new MavenCoordinate(MavenCoordinateUtil.GROUPID, MavenCoordinateUtil.ARTIFACT, null);
        assertTrue(left.compareTo(right) > 0);
        assertTrue(right.compareTo(left) < 0);

        right = new MavenCoordinate(MavenCoordinateUtil.GROUPID.toUpperCase(), MavenCoordinateUtil.ARTIFACT,
                MavenCoordinateUtil.PACKAGING);
        assertTrue(left.compareTo(right) != 0);

        right = left;
        assertTrue(left.compareTo(right) == 0);

    }
}
