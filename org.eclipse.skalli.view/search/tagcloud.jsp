<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="org.eclipse.skalli.view.component.TagCloud"%>
<%@ page import="org.eclipse.skalli.common.Consts"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>All Tags - ${pagetitle}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body>
<%
    TagCloud tagCloud = TagCloud.getInstance();
%>

<!-- header area -->

<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />

<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

<!-- navigation menu on left side -->

<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<!-- tag cloud -->

<div class="tagcloudarea">
    <center>
        <div class="tagcloud">
            <%= tagCloud.doLayout() %>
        </div>
    </center>
</div>


</body>
</html>
