<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>Owners - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Owners</h2>
<a href="${module}/new">New owner</a>
<table class="data">
<tr><th>Name</th></tr><c:forEach items="${owners}" var="owner">
<tr><td><a href="${module}/${owner.name}">${owner.name}</a></td><td><a href="${module}/${owner.name}/delete" onclick="return confirm('Do you really want to delete the owner ${owner.name} and all its dependencies?');">Delete</a></td></tr>
</c:forEach></table>
</div>
</body>
</html>