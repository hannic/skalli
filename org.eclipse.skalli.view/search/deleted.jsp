<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="java.util.List"%>
<%@page import="java.util.Comparator"%>
<%@page import="org.eclipse.skalli.common.User"%>
<%@page import="org.eclipse.skalli.model.core.Project"%>
<%@ page import="org.eclipse.skalli.common.Consts"%>
<%@page import="org.eclipse.skalli.api.java.authentication.LoginUtil"%>
<%@page import="org.eclipse.skalli.api.java.authentication.UserUtil"%>
<%@page import="org.eclipse.skalli.api.java.ProjectService"%>
<%@page import="org.eclipse.skalli.common.Services"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ProjectPortal</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>


</head>
<body>

<!-- header area -->

<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />

<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

<!-- navigation menu on left side -->

<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<!-- deleted projects -->

<%
  LoginUtil util = new LoginUtil(request);
  User user = util.getLoggedInUser();
  if (UserUtil.isAdministrator(user)) {
    ProjectService projectService = Services.getRequiredService(ProjectService.class);
    List<Project> deletedProjects = projectService.getDeletedProjects(new Comparator<Project>() {
        public int compare(Project p1, Project p2) {
            return p1.getProjectId().compareTo(p2.getProjectId());
        }
    });
%>
<h2>Deleted Projects</h2>
<div>
    <ul class="deletedlist">
    <% for (Project deletedProject: deletedProjects) { %>
        <li><a href="<%=Consts.URL_PROJECTS%>/<%=deletedProject.getUuid()%>"><%=deletedProject.getProjectId()%> (<%=deletedProject.getName()%>)</a></li>
    <% } %>
    </ul>
</div>
<% } %>

</body>
</html>
