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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensibleEntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.eclipse.skalli.testutil.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class PersistenceServiceXStreamTest {

    private File tempdir;

    @Before
    public void setup() throws Exception {
        tempdir = TestUtils.createTempDir("test");
    }

    @After
    public void tearDown() throws Exception {
        if (tempdir != null) {
            FileUtils.forceDelete(tempdir);
        }
    }

    @Test
    public void testPersistAndLoad() throws Exception {
        File storageDirectory = new File(tempdir, "storage");

        // create a new instance for persistence of test entities
        PersistenceServiceXStreamMock p = new PersistenceServiceXStreamMock( new FileStorageService(storageDirectory), new ExtensibleEntityExtensionService());

        // create and persist test entities
        List<TestExtensibleEntityBase> expectedEntities = createTestEntityHierarchy();
        for (ExtensibleEntityBase entity : expectedEntities) {
            p.persist(entity, "anyonomous");
        }

        // read the persisted files as DOM and do some asserts
        for (TestExtensibleEntityBase entity : expectedEntities) {
            Assert.assertNotNull(entity);

            File f = new File(storageDirectory, "TestExtensibleEntityBase/" + entity.getUuid().toString() + ".xml");
            Assert.assertTrue(f.exists() && f.isFile());

            Document doc = XMLUtils.documentFromFile(f);
            Element root = (Element) doc.getElementsByTagName(TestExtensibleEntityBase.class.getName()).item(0);
            Assert.assertNotNull(root);
            Assert.assertNull(doc.getElementsByTagName(TestExtensibleEntityBase.class.getName()).item(1));

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                assertNode(nodes.item(i), entity);
            }
        }

        // create a new instance for loading of persisted entities
        p = new PersistenceServiceXStreamMock(new FileStorageService(storageDirectory),new ExtensibleEntityExtensionService());


        List<TestExtensibleEntityBase> actualEntities = p.getEntities(TestExtensibleEntityBase.class);
        List<TestExtensibleEntityBase> actualDeletedEntities = p.getDeletedEntities(TestExtensibleEntityBase.class);
        Assert.assertEquals(3, actualEntities.size());
        Assert.assertEquals(2, actualDeletedEntities.size());

        // assert that unmarshalled entities equal the original entities
        // see assertEquals() methods in TestExtensibleEntityBase and TestExtension
        for (TestExtensibleEntityBase actualEntity : actualEntities) {
            UUID uuid = actualEntity.getUuid();
            boolean found = false;
            for (TestExtensibleEntityBase expected : expectedEntities) {
                if (expected.getUuid().equals(uuid)) {
                    TestExtensibleEntityBase.assertEquals(expected, actualEntity);
                    found = true;
                }
            }
            if (!found) {
                Assert.fail("Should have been found");
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void testPersist_withUnknownParent() throws Exception {
        File storageDirectory = new File(tempdir, "storage");

        // create a new instance for persistence of test entities
        PersistenceServiceXStreamMock p = new PersistenceServiceXStreamMock(new FileStorageService(storageDirectory), new ExtensibleEntityExtensionService());


        // create and persist test entities
        List<TestExtensibleEntityBase> expectedEntities = createTestEntityHierarchy();

        p.persist(expectedEntities.get(1), "anonymous");
    }

    @Test
    public void testResolveParentEntities() throws Exception {
        PersistenceServiceXStream p = new PersistenceServiceXStream();

        EntityBase parent1 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[0]);
        EntityBase parent2 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[1]);
        parent2.setDeleted(true);

        // non-deleted entities referencing non-deleted parent entities
        // hierarchy: entity2 -> entity1 -> parent1
        EntityBase entity1 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[2], PropertyHelperUtils.TEST_UUIDS[0]);
        EntityBase entity2 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[3], PropertyHelperUtils.TEST_UUIDS[2]);

        // non-deleted entity referencing a deleted parent entity
        EntityBase entity3 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[4], PropertyHelperUtils.TEST_UUIDS[1]);

        // deleted entity referencing a deleted parent entity
        EntityBase entity4 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[5], PropertyHelperUtils.TEST_UUIDS[1]);
        entity4.setDeleted(true);

        // deleted entity referencing a non-deleted parent entity
        EntityBase entity5 = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[6], PropertyHelperUtils.TEST_UUIDS[0]);
        entity5.setDeleted(true);

        p.updateCache(parent1);
        p.updateCache(parent2);
        p.updateCache(entity1);
        p.updateCache(entity2);
        p.updateCache(entity3);
        p.updateCache(entity4);
        p.updateCache(entity5);

        p.resolveParentEntities(TestEntityBase1.class);

        Assert.assertNull(p.getEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0]).getParentEntity());
        Assert.assertNull(p.getDeletedEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[1])
                .getParentEntity());

        Assert.assertEquals(parent1, p.getEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[2])
                .getParentEntity());
        Assert.assertEquals(entity1, p.getEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[3])
                .getParentEntity());

        // non-deleted entity MUST NOT  reference a deleted parent entity:
        Assert.assertNull(p.getEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[4]).getParentEntity());

        // deleted entity MAY reference a deleted parent entity
        Assert.assertEquals(parent2, p.getDeletedEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[5])
                .getParentEntity());

        // deleted entity MAY reference a non-deleted parent entity
        Assert.assertEquals(parent1, p.getDeletedEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[6])
                .getParentEntity());
    }

    // helper stuff

    private List<TestExtensibleEntityBase> createTestEntityHierarchy()
            throws Exception {
        List<TestExtensibleEntityBase> entities = new LinkedList<TestExtensibleEntityBase>();

        TestExtensibleEntityBase parentParent = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[2]);
        entities.add(parentParent);
        ExtensionEntityBase exts2[] = { createExtension(TestExtension.class, true, "foo", "1", "2"),
                createExtension(TestExtension1.class, false, "bar", "3") };
        addExtensions(parentParent, exts2);

        TestExtensibleEntityBase parent = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[1]);
        entities.add(parent);
        ExtensionEntityBase exts1[] = { createExtension(TestExtension.class, true, "Z", "F", "G", "H", "I"),
                createExtension(TestExtension1.class, true, "Y", "D", "E") };
        addExtensions(parent, exts1);
        parent.setParentEntity(parentParent);

        TestExtensibleEntityBase base = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[0]);
        entities.add(base);
        ExtensionEntityBase exts0[] = { createExtension(TestExtension.class, false, "X", "A", "B", "C") };
        base.setInherited(TestExtension1.class, true);
        addExtensions(base, exts0);
        base.setParentEntity(parent);

        TestExtensibleEntityBase deletedParent = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[4]);
        deletedParent.setDeleted(true);
        entities.add(deletedParent);
        ExtensionEntityBase exts4[] = { createExtension(TestExtension1.class, false, "deleted parent", "d", "e") };
        addExtensions(deletedParent, exts4);

        TestExtensibleEntityBase deleted = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[3]);
        deleted.setDeleted(true);
        entities.add(deleted);
        ExtensionEntityBase exts3[] = { createExtension(TestExtension.class, false, "deleted", "a", "b", "c") };
        addExtensions(deleted, exts3);
        deleted.setParentEntity(deletedParent);

        return entities;
    }

    private void addExtensions(ExtensibleEntityBase base, ExtensionEntityBase... exts) {
        for (ExtensionEntityBase ext : exts) {
            base.addExtension(ext);
        }
    }

    private ExtensionEntityBase createExtension(Class<? extends ExtensionEntityBase> clazz, boolean bool, String str,
            String... items)
            throws Exception {
        TestExtension ext = (TestExtension) clazz.newInstance();
        ext.setBool(bool);
        ext.setStr(str);
        for (String item : items) {
            ext.addItem(item);
        }
        return ext;
    }

    private void assertNode(Node node, TestExtensibleEntityBase entity) {
        String nodeName = node.getNodeName();
        if ("uuid".equals(nodeName)) {
            Assert.assertEquals(entity.getUuid().toString(), node.getTextContent());
        }
        else if ("deleted".equals(nodeName)) {
            Assert.assertEquals(Boolean.toString(entity.isDeleted()), node.getTextContent());
        }
        else if ("parentEntityId".equals(nodeName)) {
            Assert.assertEquals(entity.getParentEntityId().toString(), node.getTextContent());
        }
        else if ("extensions".equals(nodeName)) {
            assertExtensions(node, entity);
        }
        else if ("inheritedExtensions".equals(nodeName)) {
            assertInheritedExtensions(node, entity);
        }
    }

    private void assertExtensions(Node node, TestExtensibleEntityBase entity) {
        NodeList extNodes = node.getChildNodes();
        for (int j = 0; j < extNodes.getLength(); ++j) {
            Node extNode = extNodes.item(j);
            String extNodeName = extNode.getNodeName();
            if (TestExtension.class.getName().equals(extNodeName)) {
                assertExtension(extNode, entity.getExtension(TestExtension.class));
            }
            else if (TestExtension1.class.getName().equals(extNodeName)) {
                assertExtension(extNode, entity.getExtension(TestExtension1.class));
            }
        }
    }

    private void assertExtension(Node extNode, TestExtension extension) {
        Assert.assertNotNull(extension);
        NodeList nodes = extNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            if ("bool".equals(nodeName)) {
                Assert.assertEquals(Boolean.toString(extension.isBool()), node.getTextContent());
            }
            else if ("str".equals(nodeName)) {
                Assert.assertEquals(extension.getStr(), node.getTextContent());
            }
            else if ("items".equals(nodeName)) {
                assertItems(node, extension.getItems());
            }
        }
    }

    private void assertItems(Node node, List<String> items) {
        NodeList itemNodes = node.getChildNodes();
        int n = 0;
        for (int j = 0; j < itemNodes.getLength(); ++j) {
            Node itemNode = itemNodes.item(j);
            if ("string".equals(itemNode.getNodeName())) {
                Assert.assertEquals(items.get(n++), itemNode.getTextContent());
            }
        }
    }

    private void assertInheritedExtensions(Node node, TestExtensibleEntityBase entity) {
        NodeList extNodes = node.getChildNodes();
        for (int j = 0; j < extNodes.getLength(); ++j) {
            Node extNode = extNodes.item(j);
            String extNodeName = extNode.getNodeName();
            if ("string".equals(extNodeName)) {
                String inheritedEntityName = extNode.getTextContent();
                if (TestExtension.class.getName().equals(inheritedEntityName)) {
                    Assert.assertTrue(entity.isInherited(TestExtension.class));
                }
                else if (TestExtension1.class.getName().equals(inheritedEntityName)) {
                    Assert.assertTrue(entity.isInherited(TestExtension1.class));
                }
            }
        }
    }
}
