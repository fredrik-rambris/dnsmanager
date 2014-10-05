<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="f" uri="/WEB-INF/tags/taglib.tld" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty master.name}">Edit Master (${master})</c:when><c:otherwise>Add Master</c:otherwise></c:choose> - DNSManager</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Master<c:if test="${!empty master.name}"> ${master}</c:if> for ${domain.name}</h2>
<form method="post" action="${self}">
<fieldset>
<c:choose><c:when test="${!empty master.name}">
<p>
<f:checkboxes values="${views}" selectedValues="${slaves}" name="slaves[]" id="slaves" />
</p>
</c:when><c:otherwise>
<f:select values="${views}" name="master_id" />
</c:otherwise></c:choose>
<input type="submit" value="Save"/>
</fieldset>
</form>
</div>
</body>
</html>