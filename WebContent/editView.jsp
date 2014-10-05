<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty view.name}">Edit View (${view.name})</c:when><c:otherwise>Create View</c:otherwise></c:choose> - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>View</h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<tr><th>Name</th><td><input type="text" name="view_name" value="${view.name}" maxlength="50"></td></tr>
<tr><th>Description</th><td><input type="text" name="view_description" value="${view.description}"></td></tr>
<tr><th>Address</th><td><input type="text" name="view_address" value="${view.address}" maxlength="255"></td></tr>
<tr><th>Notify</th><td><input type="checkbox" name="notify" value="1"<c:if test="${view.notify}"> checked="checked"</c:if>></td></tr>
</table>
</fieldset>
<c:if test="${!empty view.name}">
<h3>Groups</h3>
<fieldset>
<table class="data">
<thead><tr><th>Group</th><th title="Order of importance. Lower numbers have presendance over higher.">Order</th></tr></thead>
<tbody><c:forEach items="${groups}" var="group">
<tr><td><input type="checkbox" name="groups" id="group_checkbox_${group.id}" value="${group.name}"<c:if test="${group.viewSet}"> checked="checked"</c:if>> <label for="group_checkbox_${group.id}">${group.name}</label></td><td><input type="text" name="group_priorities[${group.name}]" value="${group.viewPriority}" size="2"></td></tr>
</c:forEach></tbody>
</table>
</fieldset>
</c:if>
<input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>
</div>
</body>
</html>