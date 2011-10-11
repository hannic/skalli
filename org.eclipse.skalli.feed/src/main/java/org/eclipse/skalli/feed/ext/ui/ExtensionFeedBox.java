/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/

package org.eclipse.skalli.feed.ext.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedProvider;
import org.eclipse.skalli.api.java.feeds.FeedService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.api.java.feeds.FeedUpdater;
import org.eclipse.skalli.common.util.HtmlBuilder;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Link;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;

public class ExtensionFeedBox extends InfoBox implements ProjectInfoBox {

    private final ThemeResource ICON_ARROW_DOWN = new ThemeResource("icons/feed/bullet_arrow_down.png");//$NON-NLS-1$
    private final ThemeResource ICON_ARROW_UP = new ThemeResource("icons/feed/bullet_arrow_up.png");//$NON-NLS-1$

    private static final String STYLE_LAYOUT = "toolbar-layout"; //$NON-NLS-1$
    private static final String STYLE_TOOLBAR_BUTTON = "toolbar-button"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionFeedBox.class);

    private FeedService feedService;
    private Set<FeedProvider> feedProviders = new HashSet<FeedProvider>();

    protected void bindFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    protected void unbindFeedService(FeedService feedService) {
        this.feedService = null;
    }

    protected void bindFeedProvider(FeedProvider feedProvider) {
        feedProviders.add(feedProvider);
    }

    protected void unbindFeedProvider(FeedProvider feedProvider) {
        feedProviders.remove(feedProvider);
    }

    @Override
    public String getCaption() {
        return "Timeline";
    }

    @Override
    public float getPositionWeight() {
        // some high value to have it displayed as one of the last extensions
        return 100;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String userId) {
        DevInfProjectExt ext = project.getExtension(DevInfProjectExt.class);
        if (ext == null || feedService == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getIconPath() {
        return "res/icons/feed.png"; //$NON-NLS-1$
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setStyleName(STYLE_LAYOUT);
        layout.setSizeFull();

        //Toolbar
        CssLayout buttons = new CssLayout();
        buttons.addStyleName(STYLE_TOOLBAR_BUTTON);
        layout.addComponent(buttons);

        //create panel
        Panel panel = new Panel();
        panel.setHeight("200px");
        panel.setWidth("100%");

        List<String> sources = null;
        try {
            sources = feedService.findSources(project.getUuid());
            Map<String, String> captions = getCaptions(project, sources);
            for (String source : sources) {
                addToolbarButton(buttons, captions.get(source), sources, source, panel, project);
            }
        } catch (FeedServiceException ex) {
            LOG.error("Can't find sources for project " + project.getName(), ex);
        }

        addPanelContent(project, panel, sources);
        layout.addComponent(panel);
        return layout;
    }

    private Map<String, String> getCaptions(Project project, List<String> sources) {
        Map<String, String> captions = new HashMap<String, String>();
        for (FeedProvider feedProvider : feedProviders) {
            List<FeedUpdater> updaters = feedProvider.getFeedUpdaters(project);
            for (FeedUpdater updater : updaters) {
                if (sources.contains(updater.getSource())) {
                    captions.put(updater.getSource(), updater.getCaption());
                }
            }
        }
        return captions;
    }

    protected void addPanelContent(Project project, Panel panel, List<String> sources) {
        panel.removeAllComponents();
        GridLayout timelineLayout = getTimelineContent(project, sources);
        panel.getContent().addComponent(timelineLayout);
    }

    protected GridLayout getTimelineContent(Project project, List<String> sources) {
        try {
            List<Entry> entries = feedService.findEntries(project.getUuid(), -1);
            int rows = Math.max(entries.size(), 1) * 3;
            GridLayout timelineLayout = new GridLayout(2, rows);
            timelineLayout.setMargin(false);
            int row = 0;
            for (Entry entry : entries) {
                if (sources != null && sources.contains(entry.getSource())) {
                    addEntry(timelineLayout, entry, row);
                    addDetails(timelineLayout, entry, row);
                    row = row + 2; //jump over a row, as it is taken by the details
                }
            }
            return timelineLayout;

        } catch (FeedServiceException e) {
            LOG.error("Can't find feeds: " + e.getMessage(), e);
        }
        return new GridLayout();
    }

    private void addToolbarButton(Layout filters, String caption, final List<String> sources,
            final String source, final Panel panel, final Project project) {
        CheckBox cb = new CheckBox(caption, true);
        cb.setImmediate(true);
        cb.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 7364120771141334914L;

            @Override
            public void buttonClick(ClickEvent event) {
                boolean checked = event.getButton().booleanValue();
                if (checked && !sources.contains(source)) {
                    sources.add(source);
                } else {
                    sources.remove(source);
                }
                addPanelContent(project, panel, sources);
            }

        });
        filters.addComponent(cb);
    }

    private void addEntry(GridLayout layout, Entry entry, int row) {
        //date
        String date = "";
        if (entry.getPublished() != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("d.M.yyyy hh:mm (zzz)");
                date = formatter.format(entry.getPublished());
            } catch (Exception e) {
                date = entry.getPublished().toString();
            }
        }

        StringBuffer author = new StringBuffer();
        if (entry.getAuthor() != null) {
            if (entry.getAuthor().getName() != null) {
                author.append(entry.getAuthor().getName());
            }
            if (entry.getAuthor().getEmail() != null)
            {
                if (author.length() > 0) {
                    author.append(" ");
                }
                author.append("(").append(entry.getAuthor().getEmail()).append(")");
            }
        }
        HtmlBuilder html = new HtmlBuilder();
        html.append("<hr/>");
        String title = entry.getTitle();
        String link = null;
        if (entry.getLink() != null) {
            link = entry.getLink().getHref();
        }
        if (link != null) {
            html.append("");
            List<Link> links = new ArrayList<Link>(1);
            links.add(new Link(link, title));
            html.appendLinks(links);
        } else {
            html.append(title);
        }
        html.append("<br/>");
        String authorString = "";
        if (author.length() > 0) {
            authorString = " - " + author.toString() + " ";
        }
        html.append(entry.getSource() + " - " + date + authorString);
        Label label = new Label(html.toString(), Label.CONTENT_XHTML);
        label.addStyleName(STYLE_LABEL);
        layout.addComponent(label, 0, row);
    }

    protected void addDetails(GridLayout layout, Entry entry, int row) {
        HtmlBuilder html = new HtmlBuilder();
        String details = null;
        if (entry.getContent() != null) {
            details = entry.getContent().getValue();
        }
        if (StringUtils.isBlank(details)) {
            return;
        }
        html.append(details);
        html.append("<hr/>");
        final Label label = new Label(html.toString(), Label.CONTENT_XHTML);
        label.setVisible(false);
        label.addStyleName(STYLE_LABEL);

        //append clickable icon
        final Button expandButton = new Button("expand");
        expandButton.setIcon(ICON_ARROW_DOWN);
        expandButton.setStyle(Button.STYLE_LINK);
        expandButton.addListener(new Button.ClickListener() {

            private static final long serialVersionUID = 303119182831057658L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (label.isVisible()) {
                    expandButton.setCaption("expand");
                    expandButton.setIcon(ICON_ARROW_DOWN);
                    label.setVisible(false);
                } else {
                    expandButton.setCaption("collapse");
                    expandButton.setIcon(ICON_ARROW_UP);
                    label.setVisible(true);
                }

            }
        });
        layout.addComponent(expandButton, 1, row);
        layout.addComponent(label, 0, row + 1, 1, row + 1);
    }

}
