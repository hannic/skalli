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
package org.eclipse.skalli.storage.db.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.storage.db.PersistenceDB;
import org.eclipse.skalli.storage.db.entities.HistoryStorageItem;
import org.junit.Test;

public class PersistenceDBTest {
    static final String TEST_ID = "test_id";
    static final String TEST_NONEXISTING_ID = "some_non_existing_id";
    static final String TEST_NONEXISTING_CATEGORY = "some_non_existing_category";
    static final String TEST_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<entity-project lastModified=\"2011-05-10T08:16:25.137Z\" modifiedBy=\"d044774\" version=\"16\">" +
            "  <uuid>00c5cdb4-fb0f-4f11-913c-44a7f24531bd</uuid>" +
            "  <deleted>true</deleted>" +
            "  <parentEntityId>ccefcf51-6b8b-440b-ac32-3b8f94c66590</parentEntityId>" +
            "</entity-project>";
    static final String TEST_CONTENT_UPDATED = "updated_data";

    @Test
    public void testReadWrite() throws Exception {
        final String TEST_CATEGORY = "test_readwrite";

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
    public void testUpdate() throws Exception {
        final String TEST_CATEGORY = "test_update";

        PersistenceDB pdb = new PersistenceDB();

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        //update the same key with TEST_CONTENT_UPDATED
        is = new ByteArrayInputStream(TEST_CONTENT_UPDATED.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        InputStream stream = pdb.read(TEST_CATEGORY, TEST_ID);
        String outputText = IOUtils.toString(stream, "UTF-8");

        assertEquals(TEST_CONTENT_UPDATED, outputText);
        is.close();
        stream.close();
    }

    @Test
    public void testReadNonExistingId() throws Exception {
        PersistenceDB pdb = new PersistenceDB();

        InputStream stream = pdb.read(TEST_NONEXISTING_CATEGORY, TEST_NONEXISTING_ID);

        assertNull(stream);
    }

    @Test
    public void testKeys() throws Exception {
        final String TEST_CATEGORY = "test_keys";

        PersistenceDB pdb = new PersistenceDB();

        List<String> ids = pdb.keys(TEST_CATEGORY);
        assertTrue(ids.isEmpty());

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        ids = pdb.keys(TEST_CATEGORY);
        assertTrue(ids.size() == 1);
        assertEquals(TEST_ID, ids.get(0));
    }

    @Test
    public void testArchive() throws Exception {
        final String TEST_CATEGORY = "test_archive";

        PersistenceDB pdb = new PersistenceDB();

        // initially empty
        List<HistoryStorageItem> items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.isEmpty());

        // archive non existing element, should do nothing
        pdb.archive(TEST_CATEGORY, TEST_ID);
        items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.isEmpty());

        // create item
        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        // first archive step
        pdb.archive(TEST_CATEGORY, TEST_ID);
        items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.size() == 1);
        assertTrue(items.get(0).getDateCreated() != null);
        assertEquals(TEST_ID, items.get(0).getId());

        // second archive step
        pdb.archive(TEST_CATEGORY, TEST_ID);
        items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.size() == 2);
        assertTrue(items.get(0).getDateCreated() != null);
        assertEquals(TEST_ID, items.get(0).getId());
        assertTrue(items.get(1).getDateCreated() != null);
        assertEquals(TEST_ID, items.get(1).getId());
    }

    @Test
    public void testWriteBigData() throws Exception {
        final String TEST_CATEGORY = "test_writebigdata";

        PersistenceDB pdb = new PersistenceDB();

        char[] chars = new char[100000];
        Arrays.fill(chars, 'a');
        String bigString = String.valueOf(chars);

        ByteArrayInputStream is = new ByteArrayInputStream(bigString.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        InputStream stream = pdb.read(TEST_CATEGORY, TEST_ID);
        String outputText = IOUtils.toString(stream, "UTF-8");
        assertEquals(bigString, outputText);

        is.close();
        stream.close();
    }

}