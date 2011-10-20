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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedProvider;
import org.eclipse.skalli.api.java.feeds.FeedService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.api.java.feeds.FeedUpdater;
import org.eclipse.skalli.common.util.HtmlBuilder;
import org.eclipse.skalli.model.core.Project;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;

public class ExtensionFeedBox extends InfoBox implements ProjectInfoBox {

    private static final String CAPTION = "Timeline"; //$NON-NLS-1$
    private static final String CAPTION_COLLAPSE = "collapse"; //$NON-NLS-1$
    private static final String CAPTION_EXPAND = "expand"; //$NON-NLS-1$

    private static final String ICON = "res/icons/feed.png"; //$NON-NLS-1$

    private final ThemeResource ICON_ARROW_DOWN = new ThemeResource("icons/feed/bullet_arrow_down.png"); //$NON-NLS-1$
    private final ThemeResource ICON_ARROW_UP = new ThemeResource("icons/feed/bullet_arrow_up.png"); //$NON-NLS-1$

    private static final String STYLE_TIMELINE_INFOBOX = "infobox-timeline"; //$NON-NLS-1$
    private static final String INFOBOX_HEIGHT = "200px"; //$NON-NLS-1$

    private static final String STYLE_TIMELINE_PANEL = "timeline-panel";  //$NON-NLS-1$
    private static final String STYLE_SOURCE_SELECT = "source-select"; //$NON-NLS-1$
    private static final String STYLE_TIMELINE_CONTENT = "timeline-content"; //$NON-NLS-1$
    private static final String STYLE_TIMELINE_ENTRY = "timeline-entry"; //$NON-NLS-1$
    private static final String STYLE_TOOGLE_BUTTON = "toggle-btn"; //$NON-NLS-1$
    private static final String STYLE_DETAILS = "timeline-details"; //$NON-NLS-1$

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
        return CAPTION;
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
        return ext != null && feedService != null;
    }

    @Override
    public String getIconPath() {
        return ICON;
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        List<String> sources = getSources(project);
        if (sources.isEmpty()) {
            return null; // nothing to render
        }

        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_TIMELINE_INFOBOX);
        layout.setSizeFull();

        Panel panel = new Panel();
        layout.addStyleName(STYLE_TIMELINE_PANEL);
        panel.setHeight(INFOBOX_HEIGHT);
        panel.setWidth("100%"); //$NON-NLS-1$

        Component sourceFilters = getSourceFilters(panel, project, sources);
        layout.addComponent(sourceFilters);
        renderPanelContent(panel, project, sources);
        layout.addComponent(panel);
        return layout;
    }

    private List<String> getSources(Project project) {
        try {
            return feedService.findSources(project.getUuid());
        } catch (FeedServiceException e) {
            LOG.error(MessageFormat.format("Failed to retrieve feed sources for project {0}", project.getProjectId()), e);
        }
        return Collections.emptyList();
    }

    private Component getSourceFilters(Panel panel, Project project, List<String> sources) {
        CssLayout layout = new CssLayout();
        layout.addStyleName(STYLE_SOURCE_SELECT);
        Map<String, String> captions = getCaptions(project, sources);
        for (String source : sources) {
            addSourceFilter(layout, captions.get(source), sources, source, panel, project);
        }
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

    private void renderPanelContent(Panel panel, Project project, List<String> sources) {
        panel.removeAllComponents();
        Component timelineLayout = getTimelineContent(project, sources);
        panel.getContent().addComponent(timelineLayout);
    }

    private Component getTimelineContent(Project project, List<String> sources) {
        CssLayout timelineLayout = new CssLayout();
        timelineLayout.addStyleName(STYLE_TIMELINE_CONTENT);
        timelineLayout.setSizeFull();
        timelineLayout.setMargin(false);
        try {
            if (!CollectionUtils.isEmpty(sources)) {
                List<Entry> entries = feedService.findEntries(project.getUuid(), sources, -1);
                for (Entry entry : entries) {
                    addEntry(timelineLayout, entry);
                }
            }
        } catch (FeedServiceException e) {
            LOG.error(MessageFormat.format("Failed to retrieve feed entries for project {0}", project.getProjectId()), e);
        }
        return timelineLayout;
    }

    private void addSourceFilter(Layout layout, String caption,
            final List<String> sources, final String source, final Panel panel, final Project project) {
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
                renderPanelContent(panel, project, sources);
            }
        });
        layout.addComponent(cb);
    }

    @SuppressWarnings("nls")
    private void addEntry(Layout layout, Entry entry) {
        HtmlBuilder html = new HtmlBuilder();
        html.append("<p class=\"").append(STYLE_TIMELINE_ENTRY).append("\">");
        String title = entry.getTitle();
        String link = null;
        if (entry.getLink() != null) {
            link = entry.getLink().getHref();
        }
        html.appendLink(title, link);
        html.appendLineBreak();

        String source = entry.getSource();
        if (StringUtils.isNotBlank(source)) {
            html.append(source);
        }
        String date = getDate(entry);
        if (StringUtils.isNotBlank(date)) {
            html.append(" - ").append(date);
        }
        String author = getAuthor(entry);
        if (StringUtils.isNotBlank(author)) {
            html.append(" - ").append(author);
        }
        html.append("</p>");
        createLabel(layout, html.toString());

        String details = null;
        if (entry.getContent() != null) {
            details = entry.getContent().getValue();
        }
        if (StringUtils.isNotBlank(details)) {
            addDetails(layout, entry, details);
        }
    }

    @SuppressWarnings("nls")
    private String getDate(Entry entry) {
        String date = null;
        Date published = entry.getPublished();
        if (entry.getPublished() != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("d.M.yyyy hh:mm (zzz)");
                date = formatter.format(published);
            } catch (Exception e) {
                date = published.toString();
            }
        }
        return date;
    }

    @SuppressWarnings("nls")
    private String getAuthor(Entry entry) {
        StringBuilder author = new StringBuilder();
        if (entry.getAuthor() != null) {
            if (entry.getAuthor().getName() != null) {
                author.append(entry.getAuthor().getName());
            }
            if (entry.getAuthor().getEmail() != null){
                if (author.length() > 0) {
                    author.append(" ");
                }
                author.append("(");
                author.append(entry.getAuthor().getEmail());
                author.append(")");
            }
        }
        return author.toString();
    }

    @SuppressWarnings("deprecation")
    private void addDetails(Layout layout, Entry entry, String details) {
        final Label label = new Label(details, Label.CONTENT_XHTML);
        label.setVisible(false);
        label.addStyleName(STYLE_DETAILS);

        final Button expandButton = new Button(CAPTION_EXPAND);
        expandButton.setIcon(ICON_ARROW_DOWN);
        expandButton.setStyle(Button.STYLE_LINK);
        expandButton.addStyleName(STYLE_TOOGLE_BUTTON);
        expandButton.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 303119182831057658L;
            @Override
            public void buttonClick(ClickEvent event) {
                if (label.isVisible()) {
                    expandButton.setCaption(CAPTION_EXPAND);
                    expandButton.setIcon(ICON_ARROW_DOWN);
                    label.setVisible(false);
                } else {
                    expandButton.setCaption(CAPTION_COLLAPSE);
                    expandButton.setIcon(ICON_ARROW_UP);
                    label.setVisible(true);
                }
            }
        });
        layout.addComponent(expandButton);
        layout.addComponent(label);
    }

}
