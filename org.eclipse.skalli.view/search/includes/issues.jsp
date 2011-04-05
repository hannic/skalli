<%@page import="org.eclipse.skalli.model.ext.Issues"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.eclipse.skalli.common.Consts"%>  

<c:if test="${editmode==false && issues!=null}">
  <div class="issuesarea">
    <div class="${maxSeverity}">
      <div class="message">
        <% request.setAttribute("issuesSize", ((Issues) request.getAttribute(Consts.ATTRIBUTE_ISSUES)).getIssues().size()); %>
        ${issuesSize}
        <c:choose>
          <c:when test="${ issuesSize==1 }">issue was</c:when>
          <c:when test="${ issuesSize>1 }">issues were</c:when>
        </c:choose>
        detected when asynchronously validating this 
        project<c:if test="${ issues.stale}"> (project is scheduled for revalidation)</c:if>.
        <c:if test="${ isProjectAdmin}">
          Click
          <a href='<%=request.getRequestURI() + "?" + Consts.PARAM_ACTION + "=" + Consts.PARAM_VALUE_EDIT%>'>here</a>
          to correct
          <c:choose>
           <c:when test="${ issuesSize==1 }">it</c:when>
            <c:when test="${ issuesSize>1 }">them</c:when>
          </c:choose>        
          .
        </c:if>
      </div>
    </div>
  </div>
</c:if>
