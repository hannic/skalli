package org.eclipse.skalli.model.core.internal;

import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractDataMigration;
import org.eclipse.skalli.model.ext.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DataMigration16 extends AbstractDataMigration {

    public DataMigration16() {
        super(Project.class, 16);
    }

    @Override
    public void migrate(Document doc) throws ValidationException {
        String extensionClassName = "entity-relatedProjects"; //$NON-NLS-1$
        Element relatedProjectsNode = XMLUtils.getOrCreateExtensionNode(doc, extensionClassName);

        addSection(doc, relatedProjectsNode, "relatedProjects", "");//$NON-NLS-1$//$NON-NLS-2$
        addSection(doc, relatedProjectsNode, "calculated", "true");//$NON-NLS-1$//$NON-NLS-2$
    }

    private void addSection(Document doc, Node parentNode, String name, String value) {
        Element childNode = doc.createElement(name);
        childNode.setTextContent(value);
        parentNode.appendChild(childNode);
    }

}
