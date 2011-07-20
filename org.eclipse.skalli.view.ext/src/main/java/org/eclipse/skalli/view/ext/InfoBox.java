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
package org.eclipse.skalli.view.ext;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;

public class InfoBox {

    protected static final String STYLE_LABEL = "infolabel";
    protected static final String STYLE_LINK = "infolink";
    protected static final String STYLE_TEAMLABEL = "teamlabel";
    protected static final String HSPACE = "&nbsp;&nbsp;&nbsp;&nbsp;"; //$NON-NLS-1$

    protected void createLabel(Layout layout, String caption) {
        Label label = new Label(caption, Label.CONTENT_XHTML);
        label.addStyleName(STYLE_LABEL);
        layout.addComponent(label);
    }

    protected void createLink(Layout layout, String caption, String url){
        Link link = new Link(caption, new ExternalResource(url));
        link.addStyleName(STYLE_LINK);
        layout.addComponent(link);
    }
}
