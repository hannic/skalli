<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<div class="searchheader">
  <div class="searchheader-left">
    <a href="/"> <img
      src="/VAADIN/themes/simple/images/logo_medium.png" alt="Logo" />
    </a>
  </div>

  <div class="searchheader-right">

    <form method="get" id="searchform" action="/projects">
      <c:choose>
        <c:when test="${query != null }">
          <input type="text" value="${query}" name="query" id="query"
            class="searchfield" />
        </c:when>
        <c:when test="${tagquery != null }">
          <input type="text" value="${tagquery}" name="query" id="query"
            class="searchfield" />
        </c:when>
        <c:when test="${userquery != null }">
          <input type="text" value="${userquery}" name="query"
            id="query" class="searchfield" />
        </c:when>
        <c:otherwise>
          <input type="text" value="" name="query" id="query"
            class="searchfield" />
        </c:otherwise>
      </c:choose>
      <input type="submit" value="Search" id="searchsubmit"
        class="searchsubmit" />
    </form>
    <c:choose>
      <c:when test="${query != null }">${resultSize} projects found for '${query}' in ${duration} ms</c:when>
      <c:when test="${tagquery != null }">${resultSize} projects found for '${tagquery}' in ${duration} ms</c:when>
      <c:when test="${userquery != null }">${resultSize} projects found for '${userquery}' in ${duration} ms</c:when>
      <c:otherwise />
    </c:choose>

  </div>
</div>

