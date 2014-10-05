<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!doctype html>
<html>
<head>
<title>Change password - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>User</h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<tr><th>Name</th><td>${currentUser.name}</td></tr>
<tr><th>Password</th><td><input type="password" name="password" maxlength="40"></td></tr>
</table>
</fieldset><input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>
</div>
</body>
</html>