<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="dt" uri="http://jakarta.apache.org/taglibs/datetime-1.0" %><%@ taglib prefix="f" uri="/WEB-INF/tags/taglib.tld" %><!doctype html>
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
<form method="get" action="${module}/search"><input type="text" name="q" /><input type="submit" value="Search" /></form>
<button onclick="window.location.href='${module}/new'">New domain</button>
<table class="data">
<tr><th>Name</th><th>Updated</th><th>Aliases</th><th>TTL</th><th>Serial</th><th>Owner</th><th>&nbsp;</th></tr><c:forEach items="${domains}" var="domain"><tr class="<c:if test="${not domain.active}">inactive</c:if><c:if test="${domain.shouldExport}"> strong</c:if><c:if test="${domain.ttlSeconds < 900}"> warn</c:if>"><td><a href="${module}/${domain.name}"><c:choose><c:when test="${domain.reverse}"><span title="${domain.name}">${domain.reverseName}</span></c:when><c:otherwise>${domain.name}</c:otherwise></c:choose></a></td><td><dt:format pattern="yyyy-MM-dd HH:mm">${domain.updated.time}</dt:format></td><td>${domain.aliasCount}</td><td>${domain.ttl}</td><td>${domain.serial}</td><td>${domain.owner.name}</td><td><button onclick="cd('${domain.name}');">Copy</button> <button onclick="dd('${domain.name}');">Delete</button></td></tr>
</c:forEach></table>
</div>
</body>
</html>