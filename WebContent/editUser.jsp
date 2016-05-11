<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty user.name}">Edit User (${user.name})</c:when><c:otherwise>Create User</c:otherwise></c:choose> - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>User</h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<tr><th>Name</th><td><input type="text" name="user_name" value="${user.name}" maxlength="64" autocomplete="off"></td></tr>
<tr><th>Password</th><td><input type="text" name="password" maxlength="40" autocomplete="off"></td></tr>
<tr><th>Superadmin</th><td><input type="checkbox" name="superadmin" value="1"<c:if test="${user.superAdmin}"> checked="checked"</c:if>></td></tr>
</table>
</fieldset>
<c:if test="${!empty user.name}">
<fieldset>
<c:forEach items="${domains}" var="domain">
<label for="domain_${domain.id}"><input type="checkbox" name="domains[]" id="domain_${domain.id}" value="${domain.id}"<c:if test="${domain.selected}"> checked=checked</c:if> /> ${domain.name}</label><br /></c:forEach>
</fieldset></c:if>
<input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>
</div>
</body>
</html>