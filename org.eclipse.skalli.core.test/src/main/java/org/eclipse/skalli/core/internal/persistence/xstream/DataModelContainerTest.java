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

import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.junit.Assert;
import org.junit.Test;

public class DataModelContainerTest {

    private class TestEntity extends EntityBase {
    }

    private class TestEntityDerived extends TestEntity {
    }

    @Test
    public void testPutEntity() {
    }

    @Test
    public void testGetAllEntities_empty() {
        DataModelContainer cont = new DataModelContainer();
        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testGetAllEntities_filledWithOne() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();
        e1.setUuid(UUID.randomUUID());
        e2.setUuid(UUID.randomUUID());
        Assert.assertFalse(e1.equals(e2));
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);

        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertSame(e1, res.get(0));
        Assert.assertNotSame(e2, res.get(0));
    }

    @Test
    public void testGetAllEntities_filledWithTwo() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();
        e1.setUuid(UUID.randomUUID());
        e2.setUuid(UUID.randomUUID());
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);
        cont.putEntity(e2);

        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
        Assert.assertNotSame(res.get(1), res.get(0));
    }

    @Test
    public void testPutEntity_double() {
        TestEntity e1 = new TestEntity();
        e1.setUuid(UUID.randomUUID());
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);
        cont.putEntity(e1);

        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertSame(e1, res.get(0));
    }

    @Test
    public void testPutEntity_identicalId() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();
        e1.setUuid(UUID.randomUUID());
        e2.setUuid(e1.getUuid());
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);
        cont.putEntity(e2);

        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(e1, res.get(0));
        Assert.assertEquals(e2, res.get(0));
        Assert.assertSame(e2, res.get(0));
    }

    @Test
    public void testAddRemove() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();
        e1.setUuid(UUID.randomUUID());
        e2.setUuid(UUID.randomUUID());
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);
        cont.putEntity(e2);

        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
        Assert.assertNotSame(res.get(1), res.get(0));

        cont.removeEntity(e1);
        res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(e2, res.get(0));
    }

    @Test
    public void testRemoveUnknown() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();
        e1.setUuid(UUID.randomUUID());
        e2.setUuid(UUID.randomUUID());
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);

        cont.removeEntity(e2);
        List<TestEntity> res = cont.getEntities(TestEntity.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(e1, res.get(0));
    }

    @Test
    public void testClearAllEntitiesOfGivenClass() throws Exception {
        TestEntity e1 = new TestEntity();
        TestEntityDerived e2 = new TestEntityDerived();
        TestEntityBase1 e3 = new TestEntityBase1();
        e1.setUuid(UUID.randomUUID());
        e2.setUuid(UUID.randomUUID());
        e3.setUuid(UUID.randomUUID());
        DataModelContainer cont = new DataModelContainer();
        cont.putEntity(e1);
        cont.putEntity(e2);
        cont.putEntity(e3);
        cont.clearAll(TestEntity.class);
        Assert.assertEquals(0, cont.getEntities(TestEntity.class).size());
        Assert.assertEquals(1, cont.getEntities(TestEntityBase1.class).size());
    }
}
