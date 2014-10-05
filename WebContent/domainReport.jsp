<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>Domain Report</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Domain Report</h2>
<table class="data">
<c:forEach items="${domains}" var="domain">
<tr><td>${domain.name}</td><td><c:if test="${domain.soaRecord != null}">${domain.soaRecord.host}</c:if></td><td><c:if test="${domain.soaRecord != null}">${domain.soaRecord.admin}</c:if></td></tr>
</c:forEach>
</table>
</div>
</body>
</html>