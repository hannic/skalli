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
package org.eclipse.skalli.view.component;

import java.util.IllegalFormatConversionException;
import java.util.Set;

import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.view.internal.config.UserDetailsConfig;
import org.eclipse.skalli.view.internal.config.UserDetailsResource;
import org.eclipse.skalli.view.internal.container.UserContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

@SuppressWarnings("serial")
public class PeopleComponent extends CustomComponent {

    private static final String STYLE = "peoplecomponent"; //$NON-NLS-1$

    protected PeopleComponent(final User user) {
        addStyleName(STYLE);

        Layout layout = new CssLayout();
        layout.setSizeFull();
        layout.setMargin(false);

        StringBuilder sb = new StringBuilder();
        sb.append("<span class=\"v-img-peoplecomponent\">"); //$NON-NLS-1$
        sb.append("<img src=\"/VAADIN/themes/simple/icons/people/team.png\" /> "); //$NON-NLS-1$
        sb.append("</span>"); //$NON-NLS-1$

        String userDetailsLink = getUserDetailsLink(user.getUserId());
        if (userDetailsLink != null) {
            // user details link configured, render a link to user details dialog
            sb.append("<a href=\""); //$NON-NLS-1$
            sb.append(userDetailsLink);
            sb.append("\" target=\"_blank\">"); //$NON-NLS-1$
            sb.append(user.getDisplayName());
            sb.append("</a> "); //$NON-NLS-1$
        } else {
            // not configured, just display the user name
            sb.append(user.getDisplayName());
            sb.append(" "); //$NON-NLS-1$
        }

        sb.append("<span class=\"v-link-peoplecomponent\">"); //$NON-NLS-1$

        sb.append("<a class=\"link\" href=\"mailto:"); //$NON-NLS-1$
        sb.append(user.getEmail());
        sb.append("\">"); //$NON-NLS-1$
        sb.append("mail");
        sb.append("</a> "); //$NON-NLS-1$

        sb.append("<a class=\"link\" href=\""); //$NON-NLS-1$
        sb.append(Consts.URL_PROJECTS_USER);
        sb.append(user.getUserId());
        sb.append("\">"); //$NON-NLS-1$
        sb.append("projects");
        sb.append("</a> "); //$NON-NLS-1$

        sb.append("</span>"); //$NON-NLS-1$

        Label lbl = new Label();
        lbl.setContentMode(Label.CONTENT_XHTML);
        lbl.setValue(sb.toString());
        layout.addComponent(lbl);

        setCompositionRoot(layout);
    }

    public static Component getPeopleListComponent(Set<User> users) {
        return new PeopleListComponent(users);
    }

    public static Component getPeopleListComponentForMember(Set<ProjectMember> member) {
        return new PeopleListComponent(UserContainer.getUsers(member));
    }

    static class PeopleListComponent extends CustomComponent {
        public PeopleListComponent(Set<User> users) {
            Layout layout = new CssLayout();
            layout.setSizeFull();
            for (User user : users) {
                layout.addComponent(new PeopleComponent(user));
            }
            setCompositionRoot(layout);
        }
    }

    /**
     * if user details base url is customized, return a link to
     * user details for the passed user id, return null otherwise
     */
    private static String getUserDetailsLink(String userId) {
        ConfigurationService confService = Services.getService(ConfigurationService.class);
        if (confService != null) {
            UserDetailsConfig userDetailsConfig = confService.readCustomization(UserDetailsResource.KEY,
                    UserDetailsConfig.class);
            if (userDetailsConfig != null) {
                try {
                    // the configured base url can have a placeholder for
                    // the user ID (e.g. [http://show.user.com/userId=%s]),
                    // try to format with passed user ID and return
                    return String.format(userDetailsConfig.getUrl(), userId);
                } catch (IllegalFormatConversionException e) {
                    // user details base url seems to not contain any placeholder for
                    // the user ID, return the base url in this case.
                    return userDetailsConfig.getUrl();
                }
            }
        }
        return null;
    }
}
