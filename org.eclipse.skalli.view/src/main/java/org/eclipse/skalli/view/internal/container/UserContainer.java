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
package org.eclipse.skalli.view.internal.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.UserService;
import org.eclipse.skalli.common.UserServiceUtil;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.PropertyName;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;

public class UserContainer extends IndexedContainer implements Serializable {

    private static final long serialVersionUID = 632503436661743735L;
    private static UserContainer container;

    @PropertyName(position = 0)
    public static final Object PROPERTY_USER = "user"; //$NON-NLS-1$

    private UserContainer(Collection<User> users) {
        super();
        addContainerProperty(User.PROPERTY_FIRSTNAME, String.class, null);
        addContainerProperty(User.PROPERTY_LASTNAME, String.class, null);
        addContainerProperty(User.PROPERTY_EMAIL, String.class, null);
        addContainerProperty(User.PROPERTY_DISPLAY_NAME, String.class, null);
        addContainerProperty(PROPERTY_USER, User.class, null);
        for (User user : users) {
            addItem(user);
        }
    }

    private Item addItem(User user) {
        String userId = user.getUserId();
        Item item = getItem(userId);
        if (item == null) {
            item = addItem(user.getUserId()); // IndexedContainer#addItem return null, if entry already exists!!!
        }
        if (item != null) {
            item.getItemProperty(User.PROPERTY_FIRSTNAME).setValue(user.getFirstname());
            item.getItemProperty(User.PROPERTY_LASTNAME).setValue(user.getLastname());
            item.getItemProperty(User.PROPERTY_EMAIL).setValue(user.getEmail());
            item.getItemProperty(User.PROPERTY_DISPLAY_NAME).setValue(user.getDisplayName());
            item.getItemProperty(PROPERTY_USER).setValue(user);
        }
        return item;
    }

    public static synchronized UserContainer createWithData() {
        if (container == null) {
            UserService userService = UserServiceUtil.getUserService();
            container = new UserContainer(userService.getUsers());
        }
        return container;
    }

    public static void refresh() {
        container = null;
    }

    public static User getUser(Object userId) {
        if (userId instanceof User) {
            return (User) userId;
        }
        Item item = null;
        if (userId instanceof Property) {
            Object value = ((Property) userId).getValue();
            if (value == null) {
                return null;
            }
            item = searchAndAddUser(value.toString());
        }
        else if (userId instanceof ProjectMember) {
            item = searchAndAddUser(((ProjectMember) userId).getUserID());
        }
        else {
            item = searchAndAddUser(userId.toString());
        }
        if (item != null) {
            User user = (User) item.getItemProperty(PROPERTY_USER).getValue();
            return user;
        }
        return null;
    }

    public static List<User> findUsers(String searchText) {
        User user = getUser(searchText);
        if (user != null) {
            Collections.singletonList(user);
        }
        List<User> users = new ArrayList<User>();
        List<Item> items = findAndAddUser(searchText);
        for (Item item : items) {
            users.add((User) item.getItemProperty(PROPERTY_USER).getValue());
        }
        return users;
    }

    private static Item searchAndAddUser(String userId) {
        UserContainer container = UserContainer.createWithData();
        Item item = container.getItem(userId);
        if (item == null) {
            User user = UserUtil.getUser(userId.toString());
            if (user != null) {
                item = container.addItem(user);
            }
        }
        return item;
    }

    private static List<Item> findAndAddUser(String searchText) {
        List<Item> items = new ArrayList<Item>();
        UserService userService = UserServiceUtil.getUserService();
        List<User> users = userService.findUser(searchText);
        if (users != null) {
            for (User user : users) {
                Item item = container.addItem(user);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    public static Set<User> getUsers(Set<ProjectMember> members) {
        UserContainer container = UserContainer.createWithData();
        Set<User> users = new TreeSet<User>();
        Set<String> userIds = new HashSet<String>();
        for (ProjectMember member : members) {
            Item item = container.getItem(member.getUserID());
            if (item != null) {
                User user = (User) item.getItemProperty(PROPERTY_USER).getValue();
                users.add(user);
            } else {
                userIds.add(member.getUserID());
            }
        }
        if (userIds.size() > 0) {
            UserService userService = UserServiceUtil.getUserService();
            Set<User> fetchedUsers = userService.getUsersById(userIds);
            for (User user : fetchedUsers) {
                container.addItem(user);
            }
            users.addAll(fetchedUsers);
        }
        return users;
    }

}
