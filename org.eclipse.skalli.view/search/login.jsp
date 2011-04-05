<%@page import="org.eclipse.skalli.common.Consts"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ProjectPortal - Login</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body>
<%
	// if user is already authenticated, go to welcome page
	if (request.getUserPrincipal()!=null) {
		response.sendRedirect("/");
	}
%>

<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />

<div class="searcharea">
    <center>
    <img src="/VAADIN/themes/simple/images/logo350x89.png" alt="Logo" width=350px>
    <div class="search-section">
	<form method="POST" action="j_security_check">
		Name: <input type="text" name="j_username" />
		Password: <input type="password" name="j_password" />
		<input type="submit" name="ok" value="OK" />
		<input type="button" onclick="javascript:history.back()" name="back" value="Cancel" />
	</form>
	</div>
	</center>
</div>
</body>
</html>
