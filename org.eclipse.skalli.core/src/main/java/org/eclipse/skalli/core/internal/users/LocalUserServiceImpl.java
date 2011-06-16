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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.UserService;
import org.eclipse.skalli.core.internal.persistence.xstream.IgnoreUnknownElementsXStream;
import org.eclipse.skalli.core.internal.persistence.xstream.NoopConverter;
import org.eclipse.skalli.log.Log;
import org.osgi.service.component.ComponentContext;

import com.thoughtworks.xstream.XStream;

/**
 * Implementation of {@link UserService} based on XStream-serialized
 * {@link User} instances. The service expects to find XML files named
 * <tt>&lt;userId&gt.xml</tt> in the <tt>$workdir/storage/User</tt>
 * directory.<br>
 * Note, by default this service implementation is disabled (see component
 * descriptor <tt>LocalUserComponent</tt>). It must be enabled explicitly
 * in the OSGi shell (<tt>enable</tt> command, LDAP user service should
 * be disabled before that).
 */
public class LocalUserServiceImpl implements UserService {

    private static final Logger LOG = Log.getLogger(LocalUserServiceImpl.class);
    private static final String STORAGE_BASE = "storage" + IOUtils.DIR_SEPARATOR; //$NON-NLS-1$
    private File storageDirectory;

    private final Map<String, User> cache = new HashMap<String, User>();

    public LocalUserServiceImpl() {
        initializeStorage();
        loadUsers();
    }

    protected void activate(ComponentContext context) {
        LOG.info("Local User Service activated");
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("Local User Service deactivated");
    }

    private void initializeStorage() {
        if (storageDirectory == null) {
            String workdir = System.getProperty("workdir");
            if (workdir != null) {
                File workingDirectory = new File(workdir);
                if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                    storageDirectory = new File(workingDirectory, STORAGE_BASE + "User");
                } else {
                    LOG.warning("Working directory " + workingDirectory.getAbsolutePath()
                            + "not found - falling back to current directory");
                }
            }
            if (storageDirectory == null) {
                storageDirectory = new File(STORAGE_BASE + "User");
            }
            if (!storageDirectory.exists()) {
                storageDirectory.mkdirs();
            }
        }
    }

    private void loadUsers() {
        @SuppressWarnings("unchecked")
        Iterator<File> files = FileUtils.iterateFiles(storageDirectory, new String[] { "xml" }, true); //$NON-NLS-1$
        while (files.hasNext()) {
            File file = files.next();
            LOG.info("  Loading " + file.getAbsolutePath()); //$NON-NLS-1$
            User user = loadFromFile(file);
            cache.put(user.getUserId(), user);
        }
    }

    private XStream getXStreamInstance() {
        return IgnoreUnknownElementsXStream.getXStreamInstance(
                Collections.singleton(new NoopConverter()),
                Collections.singleton(User.class.getClassLoader()),
                null);
    }

    File saveToFile(File file, User user) {
        if (file == null) {
            file = new File(storageDirectory, user.getUserId() + ".xml");
        }
        XStream xstream = getXStreamInstance();
        String xml = xstream.toXML(user);
        try {
            FileUtils.writeStringToFile(file, xml, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    User loadFromFile(File file) {
        String xml = null;
        try {
            xml = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        XStream xstreamValidation = getXStreamInstance();
        User user = (User) xstreamValidation.fromXML(xml);
        if (user == null) {
            throw new RuntimeException("Could not load user from file " + file.getAbsolutePath());
        }
        LOG.info("Loaded user " + user.getUserId() + " from " + file.getAbsolutePath());
        return user;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<User>(cache.values());
    }

    @Override
    public User getUserById(String userId) {
        User user = cache.get(userId);
        return user != null ? user : User.createUserWithoutDetails(userId);
    }

    @Override
    public List<User> findUser(String search) {
        List<User> result = new ArrayList<User>();
        if (StringUtils.isNotBlank(search)) {
            String[] parts = StringUtils.split(NormalizeUtil.normalize(search), " ,");
            Pattern[] patterns = new Pattern[parts.length];
            for (int i = 0; i < parts.length; ++i) {
                patterns[i] = Pattern.compile(parts[i] + ".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }

            for (User user : cache.values()) {
                if (parts.length == 1) {
                    match(user, patterns[0], result);
                }
                else if (parts.length == 2) {
                    // givenname surname ('Michael Ochmann')
                    if (matches(patterns, user.getFirstname(), user.getLastname())) {
                        result.add(user);
                    }
                    // surname givenname('Ochmann, Michael')
                    if (matches(patterns, user.getLastname(), user.getFirstname())) {
                        result.add(user);
                    }
                }
                else if (parts.length == 3) {
                    // givenname initial surname, e.g. 'Michael R. Ochmann'
                    // or title givenname surname or given name surname title
                    if (matches(patterns, user.getFirstname(), null, user.getLastname())) {
                        result.add(user);
                    }
                    if (matches(patterns, user.getLastname(), null, user.getFirstname())) {
                        result.add(user);
                    }
                    if (matches(patterns, null, user.getFirstname(), user.getLastname())) {
                        result.add(user);
                    }
                    if (matches(patterns, user.getFirstname(), user.getLastname(), null)) {
                        result.add(user);
                    }
                }
            }
            if (result.isEmpty()) {
                for (User user : cache.values()) {
                    // try to match each part individually
                    for (int i = 0; i < parts.length; ++i) {
                        match(user, patterns[i], result);
                    }
                }
            }
        }
        return result;
    }

    private boolean matches(Pattern[] pattern, String... strings) {
        boolean matches = true;
        for (int i = 0; i < strings.length; ++i) {
            if (StringUtils.isNotBlank(strings[i])) {
                matches &= pattern[i].matcher(strings[i]).matches();
            }
        }
        return matches;
    }

    private void match(User user, Pattern pattern, List<User> result) {
        Pattern[] patterns = new Pattern[] { pattern };
        // try a match with surname*
        if (matches(patterns, user.getLastname())) {
            result.add(user);
        }
        // try a match with firstname*
        if (matches(patterns, user.getFirstname())) {
            result.add(user);
        }
        //try a match with the account name
        if (result.isEmpty() && matches(patterns, user.getUserId())) {
            result.add(user);
        }
        //try a match with the mail address
        if (result.isEmpty() && matches(patterns, user.getEmail())) {
            result.add(user);
        }
        //try a match with the department
        if (result.isEmpty() && matches(patterns, user.getDepartment())) {
            result.add(user);
        }
    }

    @Override
    public Set<User> getUsersById(Set<String> userIds) {
        Set<User> result = new HashSet<User>();
        if (userIds != null) {
            for (String userId : userIds) {
                User user = cache.get(userId);
                if (user != null) {
                    result.add(user);
                } else {
                    result.add(User.createUserWithoutDetails(userId));
                }
            }
        }
        return result;
    }

}
