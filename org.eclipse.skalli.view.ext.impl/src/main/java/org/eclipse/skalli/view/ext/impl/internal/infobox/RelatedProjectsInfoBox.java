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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import org.eclipse.skalli.api.java.SearchHit;
import org.eclipse.skalli.api.java.SearchResult;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.view.ext.AbstractInfoBox;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;

public class RelatedProjectsInfoBox extends AbstractInfoBox implements ProjectInfoBox {

  @Override
  public String getIconPath() {
    return "res/icons/relProjects.png"; //$NON-NLS-1$
  }

  @Override
  public String getCaption() {
    return "Related Projects";
  }

  @Override
  public Component getContent(Project project, ExtensionUtil util) {
    Layout layout = new CssLayout();
    layout.setSizeFull();

    SearchService searchService = Services.getService(SearchService.class);
    if (searchService != null) {
      SearchResult<Project> relatedProjects = searchService.getRelatedProjects(project, 5);
      for (SearchHit<Project> hit : relatedProjects.getResult()) {
        Link link = new Link();
        link.setCaption(hit.getEntity().getName());
        link.setResource(new ExternalResource("/projects/" + hit.getEntity().getProjectId())); //$NON-NLS-1$
        layout.addComponent(link);
      }
    }

    return layout;
  }

  @Override
  public float getPositionWeight() {
    return 1.8f;
  }

  @Override
  public int getPreferredColumn() {
    return COLUMN_EAST;
  }

  @Override
  public boolean isVisible(Project project, String loggedInUserId) {
    if (project.isDeleted()) {
      return false;
    } else {
      return true;
    }
  }

}

