<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="f" uri="/WEB-INF/tags/taglib.tld" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty alias}">Edit Alias (${alias})</c:when><c:otherwise>Add Alias</c:otherwise></c:choose> - DNSManager</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Alias<c:if test="${!empty alias}"> ${alias}</c:if> for ${domain.name}</h2>
<form method="post" action="${self}">
<fieldset>
<c:choose><c:when test="${!empty alias}"><input type="text" name="alias_name" value="${alias}" /></c:when><c:otherwise><textarea name="alias_name" cols="30" rows="10"></textarea></c:otherwise></c:choose>
<input type="submit" value="Save"/>
</fieldset>
</form>
</div>
</body>
</html>