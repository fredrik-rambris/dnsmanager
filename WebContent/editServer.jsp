<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty server.hostname}">Edit Server (${server.hostname})</c:when><c:otherwise>Create Server</c:otherwise></c:choose> - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Server<c:if test="${!empty server.hostname}"> ${server.hostname}</c:if></h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<tr><th>Hostname</th><td><input type="text" name="hostname" value="${server.hostname}" maxlength="255"></td></tr>
<tr><th>Master Prefix</th><td><input type="text" name="master_prefix" value="${server.masterPrefix}" maxlength="45"></td></tr>
<tr><th>Slave Prefix</th><td><input type="text" name="slave_prefix" value="${server.slavePrefix}" maxlength="45"></td></tr>
<tr><th>Scp Address</th><td><input type="text" name="scp_address" value="${server.scpAddress}" maxlength="255"></td></tr>
<tr><th>Zone Path</th><td><input type="text" name="zone_path" value="${server.zonePath}" maxlength="255"></td></tr>
<tr><th>Config Path</th><td><input type="text" name="config_path" value="${server.configPath}" maxlength="255"></td></tr>
<tr><th>Reload Command</th><td><input type="text" name="reload_command" value="${server.reloadCommand}" maxlength="255"></td></tr>
</table>
</fieldset>
<input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>
<c:if test="${!empty server.hostname}">
<h3>Views</h3>
<a href="${self}/view/new">New view</a>
<table class="data">
<tr><th>Name</th><th>Description</th><th></th></tr><c:forEach items="${server.views}" var="view">
<tr><td><a href="${self}/view/${view.name}">${view.name}</a></td><td>${view.description}</td><td><a href="${self}/view/${view.name}/delete" onclick="return confirm('Do you really want to delete the view ${view.name} and all its dependencies?');">Delete</a></td></tr>
</c:forEach></table>
</c:if>
</div>
</body>
</html>