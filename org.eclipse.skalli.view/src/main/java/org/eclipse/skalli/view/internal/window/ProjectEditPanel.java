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
package org.eclipse.skalli.view.internal.window;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.api.java.IssuesService;
import org.eclipse.skalli.api.java.ProjectNode;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.ProjectTemplateService;
import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectTemplate;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issues;
import org.eclipse.skalli.model.ext.ValidationException;
import org.eclipse.skalli.view.ext.ExtensionFormService;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.ext.ProjectEditContext;
import org.eclipse.skalli.view.ext.ProjectEditMode;
import org.eclipse.skalli.view.internal.application.ProjectApplication;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ProjectEditPanel extends Panel {


  private static final long serialVersionUID = 2377962084815410728L;

  private static final Logger LOG = Log.getLogger(ProjectEditPanel.class);

  private static final String STYLE_EDIT_PROJECT = "prjedt"; //$NON-NLS-1$
  private static final String STYLE_EDIT_PROJECT_LAYOUT = "prjedt-layout"; //$NON-NLS-1$
  private static final String STYLE_EDIT_PROJECT_BUTTONS = "prjedt-buttons"; //$NON-NLS-1$
  private static final String STYLE_EDIT_PROJECT_ERROR = "prjedt-errorLabel"; //$NON-NLS-1$
  static final String STYLE_ISSUES = "prjedt-issues"; //$NON-NLS-1$

  static final String W600PX = "600px"; //$NON-NLS-1$
  private static final String W350PX = "350px"; //$NON-NLS-1$

  private final ThemeResource ICON_BUTTON_OK = new ThemeResource("icons/button/ok.png"); //$NON-NLS-1$
  private final ThemeResource ICON_BUTTON_CANCEL = new ThemeResource("icons/button/cancel.png"); //$NON-NLS-1$
  private final ThemeResource ICON_BUTTON_EXPAND_ALL = new ThemeResource("icons/button/openall.png"); //$NON-NLS-1$
  private final ThemeResource ICON_BUTTON_COLLAPSE_ALL = new ThemeResource("icons/button/closeall.png"); //$NON-NLS-1$

  private static final String WARN_EXTENSION_DISABLED = "Extension <i>{0}</i> has been disabled";
  private static final String WARN_EXTENSION_INHERITED = "Extension <i>{0}</i> is inherited from parent project <i>{1}</i>";


  private Project project;
  private ProjectTemplate projectTemplate;
  private ProjectEditMode mode;

  private Project modifiedProject;

  private TreeSet<ProjectEditPanelEntry> entries;
  private Set<String> selectableExtensions;
  private Map<String,String> displayNames;

  private ProjectApplication application;
  private Navigator navigator;

  private Label headerLabel;

  private SortedSet<Issue> persistedIssuesWithoutExtension;
  private Map<String, SortedSet<Issue>> persistedIssuesPerExtension;

  /**
   * Creates a project edit view for browsing a project in readonly mode
   * ({@link ProjectEditMode#VIEW_PROJECT}), for editing a project
   * ({@see ProjectEditMode#EDIT_PROJECT}), or for creating a new project
   * based on a given project template.
   * @param application
   * @param project
   * @param mode
   * @param projectTemplate
   */
  public ProjectEditPanel(ProjectApplication application, Navigator navigator, Project project, ProjectEditMode mode)
  {
    this.application = application;
    this.navigator = navigator;
    this.mode = mode;

    if (project == null) {
      project = new Project();
    }
    this.project = project;

    // get a new project instance as the edit dialog could make changes
    // to the cached instance while editing and that would have visible
    // side effects to other users
    if (project.getUuid() == null) {
      modifiedProject = project;
    } else {
      ProjectService service = Services.getRequiredService(ProjectService.class);
      modifiedProject = service.loadEntity(Project.class, project.getUuid());
    }

    ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
    projectTemplate = templateService.getProjectTemplateById(project.getProjectTemplateId());

    initializeSelectableExtensions();
    initializePersistedIssues();
    initializePanelEntries();

    setSizeFull();
    setStyleName(STYLE_EDIT_PROJECT);
    renderContent((VerticalLayout)getContent());
  }

  /**
   * Sorts the issues (per extension / global issues) for further processing (e.g. setErrorMessage()).
   * Guarantees that the set (global issues) and the map (issues per extension) are not null.
   */
  private void initializePersistedIssues() {
    persistedIssuesWithoutExtension = new TreeSet<Issue>();
    persistedIssuesPerExtension = new HashMap<String, SortedSet<Issue>>();
    if (modifiedProject.getUuid() == null) {
      LOG.info("New project, no issues available. Skipping initialization of issues.");
      return;
    }

    IssuesService issuesService = Services.getService(IssuesService.class);
    if (issuesService == null) {
      LOG.warning("No issue service available. Skipping initialization of issues.");
      return;
    }

    Issues issues = issuesService.loadEntity(Issues.class, modifiedProject.getUuid());
    if (issues == null || issues.getIssues().isEmpty()) {
      LOG.info("No issues available. Skipping initialization of issues.");
      return;
    }

    for (Issue issue : issues.getIssues()) {
      Class<? extends ExtensionEntityBase> extension = issue.getExtension();
      if (extension == null) {
        persistedIssuesWithoutExtension.add(issue);
      } else {
        if (persistedIssuesPerExtension.containsKey(extension.getName())) {
          persistedIssuesPerExtension.get(extension.getName()).add(issue);
        }
        else {
          SortedSet<Issue> extensionIssues = new TreeSet<Issue>();
          extensionIssues.add(issue);
          persistedIssuesPerExtension.put(extension.getName(), extensionIssues);
        }
      }
    }
  }

  /**
   * Initializes {@link #selectableExtensions}:
   * Iterates over all {@link ExtensionService} implementations, i.e. over
   * all extensions, and
   * <ol>
   * <li>checks that the extension can work with the given template</li>
   * <li>checks that the extensions is not in the template's exclude list (if an exclude list is specified)</li>
   * <li>checks that the extensions is in the templates include list (if an include list is specified)</li>
   * </ol>
   * If all checks succeed, the extension's class name is added to <code>selectableExtensions</code>.
   * Note, <code>selectableExtensions</code> is pre-initialized with the project's extensions.
   * This ensures, that extensions do not accidentially vansish, when the template of a project changes.
   */
  private void initializeSelectableExtensions() {
    ProjectTemplateService projectTemplateService = Services.getRequiredService(ProjectTemplateService.class);

    selectableExtensions = new HashSet<String>();
    for (Class<? extends ExtensionEntityBase> extension : projectTemplateService.getSelectableExtensions(projectTemplate, modifiedProject)) {
      selectableExtensions.add(extension.getName());
    }
  }

  /**
   * Creates and initializes the trays.
   * Iterates over all available {@link ExtensionFormService} and creates a {@link ProjectEditPanelEntry}
   * for each form service that is supported by the project template of the project to edit.
   */
  @SuppressWarnings("rawtypes")
  private void initializePanelEntries() {
    entries = new TreeSet<ProjectEditPanelEntry>(new ProjectEditPanelComparator(projectTemplate));
    displayNames = new HashMap<String,String>();
    Context context = new Context(application, projectTemplate, mode);
    Iterator<ExtensionFormService> extensionFormFactories = Services.getServiceIterator(ExtensionFormService.class);
    while(extensionFormFactories.hasNext()) {
      ExtensionFormService extensionFormFactory = extensionFormFactories.next();
      String extensionClassName = extensionFormFactory.getExtensionClass().getName();
      if (selectableExtensions.contains(extensionClassName)) {
        ProjectEditPanelEntry entry = new ProjectEditPanelEntry(modifiedProject, extensionFormFactory, context, application);
        entry.setWidth(W600PX);
        entries.add(entry);
        displayNames.put(extensionClassName, entry.getDisplayName());

        entry.setPersistedIssues(persistedIssuesPerExtension.get(extensionClassName));
      }
    }
  }

  /**
   * Renders the content of the panel.
   */
  private void renderContent(VerticalLayout content) {
    content.setStyleName(STYLE_EDIT_PROJECT_LAYOUT);
    renderButtons(content);
    renderHeaderMessage(content);
    renderProjectEntries(content);
    renderButtons(content);

    setHeaderMessage(persistedIssuesWithoutExtension);
  }

  /**
   * Renders the header message between the upper button bar and the first tray.
   */
  private void renderHeaderMessage(VerticalLayout layout) {
    CssLayout headerLine = new CssLayout();
    headerLine.setMargin(true);
    headerLine.setWidth(W600PX);
    headerLabel = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
    headerLabel.setSizeUndefined();
    headerLabel.addStyleName(STYLE_ISSUES);
    headerLabel.setVisible(false);
    headerLine.addComponent(headerLabel);
    layout.addComponent(headerLine);
    layout.setComponentAlignment(headerLine, Alignment.MIDDLE_CENTER);
  }

  /**
   * Sets or resets the header message and repaints the label.
   */
  private void setHeaderMessage(String message) {
    headerLabel.setValue(message);
    headerLabel.setVisible(StringUtils.isNotEmpty(message));
    headerLabel.requestRepaint();
  }

  /**
   * Sets or resets the header message and repaints the label.
   */
  private void setHeaderMessage(SortedSet<Issue> issues) {
    setHeaderMessage(Issues.asHTMLList(null , issues));
  }

  /**
   * Renders the OK/Cancel/Expand All/Collapse All button bar.
   */
  @SuppressWarnings("serial")
  private void renderButtons(VerticalLayout layout) {
    CssLayout buttons = new CssLayout();
    buttons.addStyleName(STYLE_EDIT_PROJECT_BUTTONS);

    Button okButton = new Button("OK");
    okButton.setIcon(ICON_BUTTON_OK);
    okButton.setDescription("Commit changes");
    okButton.addListener(new OKButtonListener());
    buttons.addComponent(okButton);

    Button cancelButton = new Button("Cancel");
    cancelButton.setIcon(ICON_BUTTON_CANCEL);
    cancelButton.setDescription("Discard changes");
    cancelButton.addListener(new CancelButtonListener());
    buttons.addComponent(cancelButton);

    Button expandAllButton = new Button("Expand All");
    expandAllButton.setIcon(ICON_BUTTON_EXPAND_ALL);
    expandAllButton.setDescription("Expand All");
    expandAllButton.addListener(new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        expandAllTrays();
      }
    });
    buttons.addComponent(expandAllButton);

    Button collapseAllButton = new Button("Collapse All");
    collapseAllButton.setIcon(ICON_BUTTON_COLLAPSE_ALL);
    collapseAllButton.setDescription("Collapse All ");
    collapseAllButton.addListener(new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        collapseAllTrays();
      }
    });
    buttons.addComponent(collapseAllButton);

    layout.addComponent(buttons);
    layout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
  }

  /**
   * Renders the trays in the order defined by the comparator of {@link #entries}.
   */
  private void renderProjectEntries(VerticalLayout layout) {
    for (ProjectEditPanelEntry entry: entries) {
      layout.addComponent(entry);
      layout.setComponentAlignment(entry, Alignment.MIDDLE_CENTER);
    }
  }

  /**
   * Collapses all trays.
   */
  private void collapseAllTrays() {
    for (ProjectEditPanelEntry entry : entries) {
      entry.collapse();
    }
  }

  /**
   * Expands all trays.
   */
  private void expandAllTrays() {
    for (ProjectEditPanelEntry entry : entries) {
      entry.expand();
    }
  }

  /**
   * Expands all invalid trays and collapses the valid trays.
   */
  private void expandAllInvalidTrays() {
    for (ProjectEditPanelEntry entry : entries) {
      if (entry.isEnabled()) {
        if (entry.isValid()) {
          entry.markAsInvalid(false);
          entry.collapse();
        } else {
          entry.markAsInvalid(true);
          entry.expand();
        }
      }
    }
  }

  /**
   * Shows validation issues given by a <code>ValidationException</code>.
   * Expands all trays with validation issues and marks them as invalid.
   * Displays issues that are not related to any extension in the error line above the trays.
   * Sets the error marker on invalid fields and displays error messages related to an extension
   * by setting the error text of the corresponding form.
   *
   * @param e  the validation exception to evaluate.
   */
  private void showValidationIssues(ValidationException e) {
    SortedSet<Issue> globalIssues = new TreeSet<Issue>(persistedIssuesWithoutExtension);
    Map<String,SortedSet<Issue>> invalidExtensions = new HashMap<String, SortedSet<Issue>>();

    // sort the issues of the exception by extension
    for (Issue issue: e.getIssues()) {
      Class<? extends ExtensionEntityBase> extension = issue.getExtension();
      if (extension != null) {
        String extensionName = extension.getName();
        SortedSet<Issue> extensionIssues = invalidExtensions.get(extensionName);
        if (extensionIssues == null) {
          extensionIssues = new TreeSet<Issue>();
        }
        extensionIssues.add(issue);
        invalidExtensions.put(extensionName, extensionIssues);
      } else {
        globalIssues.add(issue);
      }
    }
    // render issues related to certain extensions by setting error texts
    // of the invalid fields and the form of the corresponding tray
    for (ProjectEditPanelEntry entry : entries) {
      String extensionName = entry.getExtensionClassName();
      SortedSet<Issue> extensionIssues = invalidExtensions.get(extensionName);
      if (extensionIssues != null) {
        entry.markAsInvalid(true, extensionIssues);
        entry.expand();
      } else {
        entry.markAsInvalid(false, null);
      }
    }
    // render issues that are not related to an extension in the error view
    // above the trays
    if (globalIssues.size() > 0) {
      setHeaderMessage(globalIssues);
    }
  }

  /**
   * Commits all enabled trays and persists the current {@link #modifiedProject}.
   * Sets the current system time as {@link Project#setRegistered(long) registration time},
   * if a new project is safed for the first time.
   *
   * @throws ValidationException
   */
  private void commitAll() throws ValidationException {
    for (ProjectEditPanelEntry entry : entries) {
      if (entry.isEnabled()) {
        entry.commit();
      }
    }

    // set the current system time as date of registration
    if (ProjectEditMode.NEW_PROJECT.equals(mode)) {
      modifiedProject.setRegistered(System.currentTimeMillis());
    }

    ProjectService projectService = Services.getRequiredService(ProjectService.class);
    projectService.persist(modifiedProject, application.getLoggedInUser());
  }

  private class OKButtonListener implements Button.ClickListener {
    private static final long serialVersionUID = 6531396291087032954L;

    @Override
    public void buttonClick(ClickEvent event) {
      List<String> warnings = getWarnings();
      if (warnings.isEmpty()) {
        doCommit();
      } else {
        ConfirmPopup popup = new ConfirmPopup(warnings, new ConfirmPopup.OnConfirmation() {
          @Override
          public void onConfirmation(boolean confirmed) {
            if (confirmed) {
              doCommit();
            }
          }
        });
        getWindow().addWindow(popup);
      }
    }

    private void doCommit() {
      try {
        setHeaderMessage(StringUtils.EMPTY);
        commitAll();
        application.refresh(modifiedProject);
        application.refresh((Project)project.getParentEntity());
        application.refresh((Project)modifiedProject.getParentEntity());
        if (!modifiedProject.isDeleted()) {
          refreshSubProjects();
          navigator.navigateProjectView(modifiedProject);
        }
        else {
          navigator.navigateWelcomeView();
        }
      }
      catch (InvalidValueException e) { // thrown by Vaadin validators
        expandAllInvalidTrays();
        LOG.log(Level.FINE, e.getMessage(), e);
      }
      catch (ValidationException e) { // thrown by persistency
        showValidationIssues(e);
        LOG.log(Level.FINE, e.getMessage(), e);
      }
      catch (RuntimeException e) {
        expandAllInvalidTrays();
        String errorMessage = e.getMessage();
        StringBuilder sb = new StringBuilder();
        sb.append("<strong class=\"").append(STYLE_EDIT_PROJECT_ERROR).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$
        if (StringUtils.isBlank(errorMessage)) {
          sb.append("Failed to commit changes. An internal error occured. See log for details.");
        }
        else {
          sb.append(errorMessage);
        }
        sb.append("</strong>"); //$NON-NLS-1$
        setHeaderMessage(sb.toString());
        LOG.log(Level.SEVERE, errorMessage, e);
      }
    }

    private void refreshSubProjects() {
      ProjectService projectService = Services.getRequiredService(ProjectService.class);
      ProjectNode node = projectService.getProjectNode(modifiedProject.getUuid(), null);
      refreshNode(projectService, node);
    }

    private void refreshNode(ProjectService projectService, ProjectNode node) {
      for (ProjectNode child: node.getSubProjects()) {
        application.refresh(child.getProject());
        refreshNode(projectService, child);
      }
    }

    private List<String> getWarnings() {
      List<String> warnings = new ArrayList<String>();
      for (ExtensionEntityBase extension: project.getAllExtensions()) {
        Class<? extends ExtensionEntityBase> extensionClass = extension.getClass();
        if (modifiedProject.isInherited(extensionClass) && !project.isInherited(extensionClass)) {
          warnings.add(asWarning(WARN_EXTENSION_INHERITED, extension));
        }
        else if (modifiedProject.getExtension(extensionClass) == null) {
          warnings.add(asWarning(WARN_EXTENSION_DISABLED, extension));
        }
      }
      return warnings;
    }

    private String asWarning(String pattern, ExtensionEntityBase extension) {
      String displayName = displayNames.get(extension.getClass().getName());
      if (pattern == WARN_EXTENSION_DISABLED) {
        return MessageFormat.format(pattern, displayName);
      }
      return MessageFormat.format(pattern, displayName,
          ((Project)extension.getExtensibleEntity()).getName());
    }
  }


  private static class ConfirmPopup extends Window implements Button.ClickListener {
    private static final long serialVersionUID = 6695547216798144892L;

    public interface OnConfirmation {
      public void onConfirmation(boolean confirmed);
    }

    private OnConfirmation callback;
    private Button yes = new Button("Yes", this);
    private Button no = new Button("No", this);

    public ConfirmPopup(List<String> warnings, OnConfirmation callback) {
      super("Confirmation of Changes");
      setModal(true);
      setWidth(W350PX);
      this.callback = callback;

      StringBuilder sb = new StringBuilder();
      sb.append("<p>").append("The following changes will <strong>remove data permanently</strong> from the project:").append("</p>"); //$NON-NLS-1$ //$NON-NLS-3$
      sb.append("<p><ul>"); //$NON-NLS-1$
      for (String warning: warnings) {
        sb.append("<li>").append(warning).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      sb.append("</ul></p>"); //$NON-NLS-1$
      sb.append("<p>").append("Commit anyway?").append("</p>"); //$NON-NLS-1$ //$NON-NLS-3$
      Label content = new Label(sb.toString(), Label.CONTENT_XHTML);
      addComponent(content);

      HorizontalLayout hl = new HorizontalLayout();
      hl.setSpacing(true);
      hl.addComponent(yes);
      hl.addComponent(no);
      addComponent(hl);
    }

    @Override
    public void buttonClick(ClickEvent event) {
      if (getParent() != null) {
        ((Window)getParent()).removeWindow(this);
      }
      callback.onConfirmation(event.getSource() == yes);
    }
  }

  private class CancelButtonListener implements Button.ClickListener {
    private static final long serialVersionUID = 4567366927195161150L;

    @Override
    public void buttonClick(ClickEvent event) {
      if (mode.equals(ProjectEditMode.EDIT_PROJECT)) {
        navigator.navigateProjectView(project);
      }
      else if (mode.equals(ProjectEditMode.NEW_PROJECT)) {
        navigator.navigateWelcomeView();
      }
    }
  }

  private class Context implements ProjectEditContext {
    private final ProjectApplication application;
    private final ProjectTemplate projectTemplate;
    private final ProjectEditMode mode;

    public Context(ProjectApplication application, ProjectTemplate projectTemplate, ProjectEditMode mode) {
      this.projectTemplate = projectTemplate;
      this.application = application;
      this.mode = mode;
    }

    @Override
    public ProjectTemplate getProjectTemplate() {
      return projectTemplate;
    }

    @Override
    public boolean isAdministrator() {
      return UserUtil.isAdministrator(application.getLoggedInUser());
    }

    @Override
    public ProjectEditMode getProjectEditMode() {
      return mode;
    }

    @Override
    public void onPropertyChanged(String propertyId, Object newValue) {
      for (ProjectEditPanelEntry entry : entries) {
        entry.onPropertyChanged(propertyId, newValue);
      }
    }
  }

  private static class ProjectEditPanelComparator implements Comparator<ProjectEditPanelEntry> {
    private ProjectTemplate projectTemplate;

    public ProjectEditPanelComparator(ProjectTemplate projectTemplate) {
     this.projectTemplate = projectTemplate;
    }

    @Override
    public int compare(ProjectEditPanelEntry o1, ProjectEditPanelEntry o2) {
      int result = Float.compare(getRank(o1), getRank(o2));
      if (result == 0) {
        // same rank: compare states (required < visible < enabled
        result = compareStates(o1, o2);
        if (result == 0) {
          // same rank and state: order captions alphabetically
          result = o1.getDisplayName().compareTo(o2.getDisplayName());
        }
      }
      return result;
    }

    private float getRank(ProjectEditPanelEntry o) {
      float rank = projectTemplate.getRank(o.getExtensionClassName());
      if (rank < 0) {
        rank = o.getRank();
        if (rank < 0) {
          rank = Float.MAX_VALUE;
        }
      }
      return rank;
    }

    private int compareStates(ProjectEditPanelEntry o1, ProjectEditPanelEntry o2) {
      String ext1 = o1.getExtensionClassName();
      String ext2 = o2.getExtensionClassName();
      int result = compareStates(projectTemplate.isVisible(ext1), projectTemplate.isVisible(ext2));
      if (result == 0) {
        result = compareStates(projectTemplate.isEnabled(ext1), projectTemplate.isEnabled(ext2));
      }
      return result;
    }

    private int compareStates(boolean state1, boolean state2) {
      if (state1) {
        return state2? 0 : -1;
      } else {
        return state2? 1 : 0;
      }
    }
  }
}

