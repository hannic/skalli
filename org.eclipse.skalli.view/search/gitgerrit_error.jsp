<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@page import="org.eclipse.skalli.common.Consts"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ProjectPortal - Failed creating Git/Gerrit repository</title>
<style type="text/css">
  @import "<%=Consts.JSP_STYLE%>";
  .errormessage {
  	font-style: italic;
  	color: #9E0000;
  }
</style>
</head>
<body>

<!-- header area -->
<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />
<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />
<!-- navigation menu on left side -->
<jsp:include page="<%= Consts.JSP_NAVIGATIONBAR %>" flush="true" />

<!-- git/gerrit error -->
<div class="rightarea" >
<h2>
Creation of Git/Gerrit repository not possible!
</h2>
Creation of a Git/Gerrit repository is not possible because of the following reason:
<br/><br/>
<c:choose>
  <c:when test="${errormessage == 'noConfiguration'}" >
  <div class='errormessage'>
  The Gerrit client is not configured. Please contact the system administrator.
  </div>
  <br/>
  <c:if test="${feedbackConfig != null }">
    <a title="${feedbackConfig.displayName}" href="${feedbackConfig.url}">Send Bug Report</a>
  </c:if>
  </c:when>
  <c:when test="${errormessage == 'noParent'}" >
  <div id="errorMessageNoParent" class='errormessage'>
  Root projects are not allowed to create a Git/Gerrit repository. Please set
  a parent project first.
  </div>
  <br/>
  <a id="errorEditProjectLink" href="<%= Consts.URL_PROJECTS + "/" + request.getAttribute(Consts.ATTRIBUTE_PROJECTID) + "?" + Consts.PARAM_ACTION + "=" + Consts.PARAM_VALUE_EDIT%>" target="_top">
  Edit this project</a>
  </c:when>
  <c:otherwise>
    Unknown.
  </c:otherwise>
</c:choose>
</div>
</body>
</html>
