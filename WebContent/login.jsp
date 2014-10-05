<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Login - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
<style type="text/css">
body { background: #555; }
div#contents
{
background-color: #eee;
border: outset 2px #eee;
width: 30em;
margin: 10% auto;
padding: 2em;
}
</style>
</head>
<body onload="document.forms[0].user_name.focus();">
<jsp:include page="head.jsp" />
<div id="contents">
<form method="post" action="${self}" accept-charset="UTF-8">
<fieldset>
<table class="layout">
<tr><th><label for="user_name"><span>Name</span></label></th><td><input type="text" name="user_name" id="user_name" size="40" /></td></tr>
<tr><th><label for="password"><span>Password</span></label></th><td><input type="password" name="password" id="password" size="40" /></td></tr>
</fieldset>
</table>
<input value="Login" name="submit" type="submit" /></form>
</html>