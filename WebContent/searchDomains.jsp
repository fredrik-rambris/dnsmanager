<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="dt" uri="http://jakarta.apache.org/taglibs/datetime-1.0" %><!doctype html>
<html>
<head>
<title>Domains - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
<script type="text/javascript">
/* Delete Domain */
function dd(name)
{
	if( confirm("Really delete this domain ("+name+")?") )
	window.location.href="${module}/"+name+"/delete";
}
/* Copy Domain */
function cd(name)
{
	window.location.href="${module}/new?copyOf="+name;
}
</script>
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Domains</h2>
<form method="get" action="${module}/search"><input type="text" name="q" value='<c:out value="${query}" />' /><input type="submit" value="Search" /></form>
<table class="data">
<tr><th>Name</th></tr><c:forEach items="${result}" var="domain">
<c:choose><c:when test="${domain.parentDomain == null}">
<tr><td><a href="${module}/${domain.domainName}">${domain.domainName}</a></td></tr>
</c:when><c:otherwise>
<tr><td><a href="${module}/${domain.parentDomain}/alias/${domain.domainName}">${domain.domainName}</a> <small>(<a href="${module}/${domain.parentDomain}">${domain.parentDomain}</a>)</small></td></tr>
</c:otherwise></c:choose>
</c:forEach></table>
</div>
</body>
</html>