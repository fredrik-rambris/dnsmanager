<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>Servers - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Servers</h2>
<a href="${module}/new">New server</a>
<table class="data">
<tr><th>Hostname</th></tr><c:forEach items="${servers}" var="server">
<tr><td><a href="${module}/${server.hostname}">${server.hostname}</a></td><td><a href="${module}/${server.hostname}/delete" onclick="return confirm('Do you really want to delete the server ${server.hostname} and all its dependencies?');">Delete</a></td></tr>
</c:forEach></table>
</div>
</body>
</html>