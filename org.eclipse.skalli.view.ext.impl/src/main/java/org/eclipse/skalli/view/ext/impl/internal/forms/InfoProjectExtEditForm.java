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
package org.eclipse.skalli.view.ext.impl.internal.forms;

import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;
import org.eclipse.skalli.view.component.MultiTextField;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class InfoProjectExtEditForm extends AbstractExtensionFormService<InfoProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/info.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 1.1f;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    private class FieldFactory extends DefaultProjectFieldFactory<InfoProjectExt> {

        private static final long serialVersionUID = 812653604052021444L;
        private InfoProjectExt extension;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, InfoProjectExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (InfoProjectExt.PROPERTY_MAILING_LIST.equals(propertyId)) {
                field = new MultiTextField(caption, extension.getMailingLists());
            }

            return field;
        }
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<InfoProjectExt>(getExtension(project));
    }

    @Override
    public Class<InfoProjectExt> getExtensionClass() {
        return InfoProjectExt.class;
    }

    @Override
    public InfoProjectExt newExtensionInstance() {
        return new InfoProjectExt();
    }

}
