<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>Users - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Users</h2>
<a href="${module}/new">New user</a>
<table class="data">
<tr><th>Name</th><th></th><th></th></tr><c:forEach items="${users}" var="user">
<tr><td><a href="${module}/${user.name}">${user.name}</a></td><td><c:if test="${user.superAdmin}">Superadmin</c:if></td><td><a href="${module}/${user.name}/delete" onclick="return confirm('Do you really want to delete the user ${user.name} and all its dependencies?');">Delete</a></td></tr>
</c:forEach></table>
</div>
</body>
</html>