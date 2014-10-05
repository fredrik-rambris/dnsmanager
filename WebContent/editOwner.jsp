<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty owner.name}">Edit Owner (${owner.name})</c:when><c:otherwise>Create Owner</c:otherwise></c:choose> - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Owner</h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<tr><th>Name</th><td><input type="text" name="owner_name" value="${owner.name}" maxlength="40"></td></tr>
<tr><th>Email</th><td><input type="text" name="owner_email" value="${owner.email}" maxlength="40"></td></tr>
</table>
</fieldset>
<input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>
</div>
</body>
</html>