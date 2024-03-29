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
package org.eclipse.skalli.core.internal.users;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestUtils;

public class LocalUserServiceImplTest {

    private File tmpDir;

    private LocalUserServiceImpl userService;
    private List<User> users;

    @Before
    public void setup() throws Exception {
        new BundleManager(this.getClass()).startBundles();

        userService = new LocalUserServiceImpl();
        users = userService.getUsers();
        Assert.assertEquals(5, users.size());
    }

    @After
    public void tearDown() throws Exception {
        if (tmpDir != null) {
            FileUtils.forceDelete(tmpDir);
        }
    }

    @Test
    public void testGetUserById() {
        for (User user : users) {
            Assert.assertEquals(user, userService.getUserById(user.getUserId()));
        }
        Assert.assertEquals(User.createUserWithoutDetails("anonymous"), userService.getUserById("anonymous"));
    }

    @Test
    public void testGetUsersById() {
        Set<User> userSet = userService.getUsersById(CollectionUtils.asSet("gh", "lc", "jw"));
        Assert.assertTrue(userSet.contains(userService.getUserById("gh")));
        Assert.assertTrue(userSet.contains(userService.getUserById("lc")));
        Assert.assertTrue(userSet.contains(userService.getUserById("jw")));

        userSet = userService.getUsersById(CollectionUtils.asSet("gh", "unknown"));
        Assert.assertTrue(userSet.contains(userService.getUserById("gh")));
        Assert.assertTrue(userSet.contains(User.createUserWithoutDetails("unknown")));

        userSet = userService.getUsersById(null);
        Assert.assertEquals(0, userSet.size());
    }

    @Test
    public void testFindUser() {
        User userGregHouse = userService.getUserById("gh");

        List<User> findResult = userService.findUser("Gregory House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("grEg HoUse"); // case-insensitive!
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Gregory");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("House, Greg");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg House, M.D.");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Dr. Greg House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg 'MasterOfCuddy' House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("gh");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("greg.house@princeton-plainsborough.com");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("diagnost");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("");
        Assert.assertEquals(0, findResult.size());
        findResult = userService.findUser(null);
        Assert.assertEquals(0, findResult.size());
    }

    @Test
    public void testSaveToFile() throws Exception {
        tmpDir = TestUtils.createTempDir("LocalUserServiceImplTest");
        File file = new File(tmpDir, "gh.xml");
        User expected = userService.getUserById("gh");
        userService.saveToFile(file, expected);
        Assert.assertTrue(file.exists());
        User loaded = userService.loadFromFile(file);
        Assert.assertEquals(expected, loaded);
    }

}
