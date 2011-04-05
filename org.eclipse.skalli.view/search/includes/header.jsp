<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@page import="org.eclipse.skalli.view.component.TagCloud"%>
<%@page import="org.eclipse.skalli.common.User"%>
<%@page import="org.osgi.framework.Version"%>
<%@page import="org.eclipse.skalli.view.internal.ViewBundleUtil"%>
<%@page import="org.eclipse.skalli.common.Consts"%>
<%@page import="org.eclipse.skalli.api.java.authentication.LoginUtil"%>

<div class="mainheader">
<%
  	LoginUtil util = new LoginUtil(request);
	User user = util.getLoggedInUser();
	TagCloud tagCloud = TagCloud.getInstance(25);
	Version version = ViewBundleUtil.getVersion();
	if (version!=null) {
		request.setAttribute("version", version);
	}
%>

    <div class="mainheader-left">
       <c:forEach var="toplinkConfig" items="${toplinksConfig.topLinks}" >
         <a href ="${toplinkConfig.url}">${toplinkConfig.displayName}</a>        
       </c:forEach>
    </div>
    <div class="mainheader-right">
      <div align="right">
        <c:choose>
          <c:when test="${user!=null}">
            Welcome
            <a href="<%=Consts.URL_MYPROJECTS%>"><%=user.getFullName()%></a>
            <a href="#" onclick="javascript:document.getElementById('logoutinput').value=document.location.href;document.forms['logout'].submit()">(Logout)</a>
            <c:if test="${feedbackConfig != null }">
              <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
              <a title="${feedbackConfig.displayName}" href="${feedbackConfig.url}">${feedbackConfig.displayName}</a>
            </c:if>
          </c:when>
          <c:otherwise>
            Anonymous User <a href="#" onclick="javascript:document.getElementById('logininput').value=document.location.href;document.forms['login'].submit()">(Login)</a>
          </c:otherwise>
        </c:choose>
        <c:choose>
          <c:when test="${version!=null}">
            <c:choose>
              <c:when test="${newsConfig != null}">
                <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
                <a href="${newsConfig.url}">Version <%= version.getQualifier() %></a>
              </c:when>
              <c:otherwise>
                <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
                Version <%= version.getQualifier() %>
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            <c:if test="${newsConfig != null}">
              <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
              <a href="${newsConfig.url}">Latest News</a>
            </c:if>
          </c:otherwise>
        </c:choose>
	  </div>
    </div>
</div>

<form id="login" method="get" action="/authenticate">
<input id="logininput" type="hidden" name="returnUrl" />
</form>
<form id="logout" method="get" action="/logout">
<input id="logoutinput" type="hidden" name="returnUrl" />
</form>

