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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectTemplate;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Select;

public abstract class DefaultProjectFieldFactory<T extends ExtensionEntityBase> extends DefaultFieldFactory {

  private static final Logger LOG = Log.getLogger(DefaultProjectFieldFactory.class);
  private static final long serialVersionUID = 162355838708348565L;
  protected static final int COMMON_TEXT_FIELD_COLUMNS = 30;

  protected final Project project;
  protected final ProjectEditContext context;
  protected final ProjectTemplate projectTemplate;
  protected final boolean isAdmin;
  protected final ProjectEditMode mode;
  protected final String extensionClassName;
  protected final ExtensionService<T> extensionService;

  public DefaultProjectFieldFactory(Project project, Class<T> extensionClass, ProjectEditContext context) {
    this.project = project;
    this.extensionClassName = extensionClass.getName();
    this.extensionService = Services.getExtensionService(extensionClass);
    this.projectTemplate = context.getProjectTemplate();
    this.isAdmin = context.isAdministrator();
    this.mode = context.getProjectEditMode();
    this.context = context;
  }

  @Override
  public Field createField(Item item, Object propertyId, final Component uiContext) {
    String propertyName = propertyId.toString();
    String caption = getCaption(propertyName);
    Field field = createField(propertyId, caption);
    if (field == null) {
      field = super.createField(item, propertyId, uiContext);
      if (field == null) {
        throw new IllegalStateException("Cannot create field for property " + propertyId);
      }
      if (StringUtils.isNotBlank(caption)) {
        field.setCaption(caption);
      }
    }
    initializeField(propertyId, field);

    setColumns(field, COMMON_TEXT_FIELD_COLUMNS);

    String description = getDescription(propertyName);
    if (StringUtils.isNotBlank(description)) {
      field.setDescription(description);
    }

    String inputPrompt = projectTemplate.getInputPrompt(extensionClassName, propertyId);
    if (StringUtils.isNotBlank(inputPrompt)) {
      setInputPrompt(field, inputPrompt);
    }

    if (Select.class.isAssignableFrom(field.getClass())) {
      Select selection = (Select)field;
      Collection<?> defaultValues = projectTemplate.getDefaultValues(extensionClassName, propertyId);
      if (defaultValues != null) {
        field.setValue(defaultValues);
      }
      Object defaultValue = projectTemplate.getDefaultValue(extensionClassName, propertyId);
      if (defaultValue != null) {
        selection.select(defaultValue);
      }
      if (projectTemplate.isNewItemsAllowed(extensionClassName, propertyId)) {
        selection.setNewItemsAllowed(true);
      }
    }
    else {
      Object defaultValue = projectTemplate.getDefaultValue(extensionClassName, propertyId);
      if (defaultValue != null) {
        field.setValue(defaultValue);
      }
    }

    if (projectTemplate.isReadOnly(extensionClassName, propertyId, isAdmin)) {
       field.setReadOnly(true);
    }

    if (mode.equals(ProjectEditMode.VIEW_PROJECT)) {
      field.setReadOnly(true);
    }

    if (!projectTemplate.isEnabled(extensionClassName, propertyId, isAdmin)) {
      field.setEnabled(false);
    }

    return field;
  }

  private void setColumns(Field field, int columns) {
    try {
      Method method = field.getClass().getMethod("setColumns", int.class); //$NON-NLS-1$
      method.invoke(field, columns);
    }
    catch (Exception e) {
      // not all Vaadin Field implementations allow to set the columns, so this is ok
      LOG.fine(MessageFormat.format("Field {0} does not allow to set columns", field.getCaption()));
    }
  }

  private void setInputPrompt(Field field, String inputPrompt) {
    try {
      Method method = field.getClass().getMethod("setInputPrompt", String.class); //$NON-NLS-1$
      method.invoke(field, inputPrompt);
    }
    catch (Exception e) {
      // not all Vaadin Field implementations have an input prompt, so this is ok
      LOG.fine(MessageFormat.format("Field {0} does not allow to set columns", field.getCaption()));
    }
  }

  protected String getCaption(String propertyName) {
    String caption = projectTemplate.getCaption(extensionClassName, propertyName);
    if (StringUtils.isBlank(caption)) {
      caption = extensionService.getCaption(propertyName);
    }
    return caption;
  }

  protected String getDescription(String propertyName) {
    String description = projectTemplate.getDescription(extensionClassName, propertyName);
    if (StringUtils.isBlank(description)) {
      description = extensionService.getDescription(propertyName);
    }
    return description;
  }

  protected abstract Field createField(Object propertyId, String caption);
  protected abstract void initializeField(Object propertyId, Field field);

}

