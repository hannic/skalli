package org.eclipse.skalli.view.ext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ext.Link;

public class HtmlBuilder {

    public static final String DEFAULT_TARGET = "_blank"; //$NON-NLS-1$

    private StringBuilder sb;

    public HtmlBuilder() {
        this.sb = new StringBuilder();
    }

    public HtmlBuilder(int capacity) {
        this.sb = new StringBuilder(capacity);
    }

    public HtmlBuilder(String s) {
        this.sb = new StringBuilder(s);
    }

    public HtmlBuilder(StringBuilder sb) {
        this.sb = new StringBuilder(sb.toString());
    }

    public HtmlBuilder(CharSequence seq) {
        this.sb = new StringBuilder(seq);
    }

    public HtmlBuilder append(boolean b) {
        sb.append(b);
        return this;
    }

    public HtmlBuilder append(char c) {
        sb.append(c);
        return this;
    }

    public HtmlBuilder append(char[] str) {
        sb.append(str);
        return this;
    }

    public HtmlBuilder append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    public HtmlBuilder append(CharSequence s) {
        sb.append(s);
        return this;
    }

    public HtmlBuilder append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    public HtmlBuilder append(double d) {
        sb.append(d);
        return this;
    }

    public HtmlBuilder append(float f) {
        sb.append(f);
        return this;
    }

    public HtmlBuilder append(int i) {
        sb.append(i);
        return this;
    }

    public HtmlBuilder append(long lng) {
        sb.append(lng);
        return this;
    }

    public HtmlBuilder append(Object obj) {
        sb.append(obj);
        return this;
    }

    public HtmlBuilder append(String str) {
        sb.append(str);
        return this;
    }

    public HtmlBuilder appendIconizedLink(String icon, String label, String url) {
        return appendIconizedLink(icon, label, url, DEFAULT_TARGET, null);
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendIconizedLink(String icon, String caption, String url, String targetName, String styleName) {
        sb.append("<img src =\"").append(icon).append("\" alt=\"\" />\n");
        sb.append("<a href=\"").append(url).append("\"");
        sb.append(" target=\"").append(StringUtils.isNotBlank(targetName)? targetName : DEFAULT_TARGET).append("\"");
        if (StringUtils.isNotBlank(styleName)) {
            sb.append(" class=\"").append(styleName).append("\"");
        }
        sb.append(">");
        sb.append(caption).append("</a>\n");
        sb.append("<br/>");
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendIconizedLinks(String icon,
            String firstCaption, String alternativeCaption, Collection<String> urls) {
        if (urls.size() == 1) {
            appendIconizedLink(icon, firstCaption, urls.iterator().next());
        }
        else {
            sb.append("<img src =\"").append(icon).append("\" alt=\"\" />\n");
            appendLinks(firstCaption, alternativeCaption, urls);
            sb.append("<br/>");
        }
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendLinks(String firstCaption, String alternativeCaption, Collection<String> urls) {
        if (urls != null) {
            boolean isFirst = true;
            for (String url : urls) {
                String caption = (isFirst) ? firstCaption : alternativeCaption;
                sb.append("<a href=\"").append(url).append("\"");
                sb.append(" target=\"").append(DEFAULT_TARGET).append("\"");
                sb.append(isFirst? ">" : " class=\"leftMargin\">").append(caption).append("</a>");
                isFirst = false;
            }
        }
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendLinks(Collection<Link> links) {
        if (links != null) {
            boolean isFirst = true;
            for (Link link : links) {
                sb.append("<a href=\"").append(link.getUrl()).append("\"");
                sb.append(" target=\"").append(DEFAULT_TARGET).append("\"");
                sb.append(isFirst? ">" : " class=\"leftMargin\">").append(link.getLabel()).append("</a>");
                isFirst = false;
            }
        }
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendMailToLink(String link) {
        if (StringUtils.isNotBlank(link)) {
            String urlEncoded = null;
            try {
                urlEncoded = URLEncoder.encode(link, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // should never happen, UTF-8 is available on all platforms
                throw new RuntimeException(e);
            }
            String htmlEncoded = StringEscapeUtils.escapeHtml(link);
            sb.append("<a href=\"mailto:").append(urlEncoded).append("\">").append(htmlEncoded).append("</a>");
        }
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendHeader(String caption, int level) {
        level = Math.max(1, Math.min(6, level));
        sb.append("<h").append(level).append(">").append(caption).append("</h").append(level).append(">\n");
        return this;
    }

    public int length() {
        return sb.length();
    }

    public StringBuilder asStringBuilder() {
        return sb;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
