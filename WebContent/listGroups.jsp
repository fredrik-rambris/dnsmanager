<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>Groups - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Groups</h2>
<a href="${module}/new">New group</a>
<table class="data">
<tr><th>Name</th></tr><c:forEach items="${groups}" var="group">
<tr><td><a href="${module}/${group.name}">${group.name}</a></td><td><a href="${module}/${group.name}/delete" onclick="return confirm('Do you really want to delete the group ${group.name} and all its dependencies?');">Delete</a></td></tr>
</c:forEach></table>
</div>
</body>
</html>