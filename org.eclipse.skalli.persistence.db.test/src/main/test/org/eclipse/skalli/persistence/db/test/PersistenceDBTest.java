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
package org.eclipse.skalli.persistence.db.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.persistence.db.PersistenceDB;
import org.eclipse.skalli.persistence.db.entities.HistoryStorageItem;
import org.junit.Test;

public class PersistenceDBTest {
    static final String TEST_ID = "test_key";
    static final String TEST_CATEGORY = "test_category";
    static final String TEST_CATEGORY2 = "test_category2";
    static final String TEST_CATEGORY3 = "test_category3";
    static final String TEST_NONEXISTING_KEY = "some_non_existing_key";
    static final String TEST_NONEXISTING_CATEGORY = "some_non_existing_category";
    static final String TEST_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<entity-project lastModified=\"2011-05-10T08:16:25.137Z\" modifiedBy=\"d044774\" version=\"16\">" +
            "  <uuid>00c5cdb4-fb0f-4f11-913c-44a7f24531bd</uuid>" +
            "  <deleted>true</deleted>" +
            "  <parentEntityId>ccefcf51-6b8b-440b-ac32-3b8f94c66590</parentEntityId>" +
            "</entity-project>";

    @Test
    public void testReadWrite() throws Exception {
        PersistenceDB pdb = new PersistenceDB();

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        InputStream stream = pdb.read(TEST_CATEGORY, TEST_ID);
        String outputText = IOUtils.toString(stream, "UTF-8");

        assertEquals(TEST_CONTENT, outputText);
        is.close();

        stream.close();
    }

    @Test
    public void testReadNonExistingId() throws Exception {
        PersistenceDB pdb = new PersistenceDB();

        InputStream stream = pdb.read(TEST_NONEXISTING_CATEGORY, TEST_NONEXISTING_KEY);

        assertNull(stream);
    }

    @Test
    public void testKeys() throws Exception {
        PersistenceDB pdb = new PersistenceDB();

        List<String> ids = pdb.keys(TEST_CATEGORY2);
        assertTrue(ids.isEmpty());

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY2, TEST_ID, is);

        ids = pdb.keys(TEST_CATEGORY2);
        assertTrue(ids.size() == 1);
        assertEquals(TEST_ID, ids.get(0));
    }

    @Test
    public void testHistorize() throws Exception {
        PersistenceDB pdb = new PersistenceDB();

        List<HistoryStorageItem> rows = pdb.getHistory(TEST_CATEGORY3, TEST_ID);
        assertTrue(rows.isEmpty());

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY3, TEST_ID, is);

        // first archive step
        pdb.archive(TEST_CATEGORY3, TEST_ID);
        rows = pdb.getHistory(TEST_CATEGORY3, TEST_ID);
        assertTrue(rows.size() == 1);
        assertTrue(rows.get(0).getDateCreated() != null);
        assertEquals(TEST_ID, rows.get(0).getId());

        // second archive step
        pdb.archive(TEST_CATEGORY3, TEST_ID);
        rows = pdb.getHistory(TEST_CATEGORY3, TEST_ID);
        assertTrue(rows.size() == 2);
        assertTrue(rows.get(0).getDateCreated() != null);
        assertEquals(TEST_ID, rows.get(0).getId());
        assertTrue(rows.get(1).getDateCreated() != null);
        assertEquals(TEST_ID, rows.get(1).getId());
    }

}