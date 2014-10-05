<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<table class="layout"><tr><td><h2>Statistics</h2>
<table class="data"><c:forEach items="${stats}" var="s">
<tr><th>${s.key}</th><td>${s.value}</td></tr>
</c:forEach></table>
</td><td><h2>Tasks</h2>
<ol><c:forEach items="${tasks}" var="task">
<li>${task}</li>
</c:forEach></ol>
</td></tr></table>
</div>
<c:if test="${currentUser.superAdmin}">
<button onclick="window.location.href='${self}?generateConfigs=1';">Generate Configs</button>
<button onclick="window.location.href='${self}?generateZones=1';">Generate Zones</button>
<button onclick="window.location.href='${self}?clearExported=1';">Clear exported</button>
</c:if>
</body>
</html>