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
<h2>Paste records into ${domain.name}</h2>
<form method="post" action="${self}">
<fieldset>
<c:choose><c:when test="${parsedRecords !=null}">
<table>
<tr><th>Original record</th><th>Parsed record</th></tr>
<c:forEach items="${parsedRecords}" var="record">
<tr><td><input type="text" name="record[]" value='<c:out value="${record.originalRecord}" />' size="100" /></td><td style="font-family: monospace;">${record.parsedRecord}</td></tr>
</c:forEach>
</table>
</c:when><c:otherwise>
<textarea name="records" rows="20" cols="80"></textarea><br />
</c:otherwise></c:choose>
<input type="button" onclick="this.form.submit();" name="preview" value="Preview" /> <c:if test="${parsedRecords!=null}"><input type="submit" name="save" value="Save"/></c:if>
</fieldset>
</form>
</div>
</body>
</html>