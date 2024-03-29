<%--
    Copyright (c) 2010, 2011 SAP AG and others.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        SAP AG - initial API and implementation
 --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.eclipse.skalli.model.ext.Issues" %>
<%@ page import="org.eclipse.skalli.common.Consts" %>

<c:if test="${editmode==false && issues!=null && issues.stale==false}">
    <div class="issuesarea">
        <div class="${maxSeverity}">
            <div class="message">
                This project has issues.
                <c:if test="${isProjectAdmin==true}">
                    &nbsp;Click <a href='<%=request.getRequestURI() + "?" + Consts.PARAM_ACTION + "=" + Consts.PARAM_VALUE_EDIT%>'>here</a> to
                correct them.
                </c:if>
            </div>
        </div>
    </div>
</c:if>
