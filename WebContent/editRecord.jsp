<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="f" uri="/WEB-INF/tags/taglib.tld" %><%@ taglib prefix="dt" uri="http://jakarta.apache.org/taglibs/datetime-1.0" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty record.builtName}">Edit Record (${record.builtName})</c:when><c:otherwise>Create Record</c:otherwise></c:choose> - ${domain.name} - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
<script src="${root}/jquery-1.4.2.min.js"></script>
<script type="text/javascript">
function layoutForm()
{
	var typeElement=document.forms[0].type;
	var type=typeElement.options[typeElement.selectedIndex].value;
	if(type=='LOC')
	{
		$(".locRows").show();
		$(".srvRows").hide();
		$("#dataRow").hide();
	}
	else if(type=='SRV')
	{
		$(".locRows").hide();
		$("#dataRow").hide();
		$("#mxRow").show();
		$(".srvRows").show();
	}
	else
	{
		$(".locRows").hide();
		$(".srvRows").hide();
		$("#dataRow").show();
		if(type=='MX')
		{
			$("#mxRow").show();
		}
		else
		{
			$("#mxRow").hide();
		}
	}
}
</script>
</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Record<c:if test="${!empty record.builtName}"> ${record.builtName}<span class="domain">.${domain.name}</span></c:if></h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<tr><th>Type</th><td><select name="type" onchange="layoutForm();"<c:if test="${!empty record.name}"> disabled="disabled"</c:if>><c:forEach items="${types}" var="type">
<option<c:if test="${type==record.type}"> selected="selected"</c:if>>${type}</option></c:forEach>
</select></td></tr>
<tr id="nameRow"><th>Name</th><td><input type="text" name="name" value="${record.name}" maxlength="255"></td></tr>
<tr class="srvRows"><th><abbr title="Service name without underscore">Service</abbr></th><td><input type="text" name="service" value="<c:if test="${record.type=='SRV'}">${record.service}</c:if>" maxlength="255"></td></tr>
<tr class="srvRows"><th>Protocol</th><td><f:select name="protocol" values="${protocols}" selected="${record.type=='SRV'?record.protocol:''}" /></td></tr>
<tr><th><label for="active">Active</label></th><td><input type="checkbox" name="active" id="active" value="1"<c:if test="${record.active}"> checked="checked"</c:if>></td></tr><c:if test="${!empty record.name}">
<tr><th>Created</th><td><dt:format pattern="yyyy-MM-dd HH:mm">${record.created.time}</dt:format></td></tr>
<tr><th>Updated</th><td><dt:format pattern="yyyy-MM-dd HH:mm">${record.updated.time}</dt:format></td></tr>
</c:if><tr><th>Group</th><td><select name="group_id"><c:forEach items="${groups}" var="group">
<option value="${group.key}"<c:if test="${group.key==record.groupId || group.key==group_id}"> selected="selected"</c:if>>${group.value}</option></c:forEach>
</select></td></tr>
<tr><th>TTL</th><td><input type="text" name="ttl" value="${record.ttl}" size="8"></td></tr>
<tr id="mxRow"><th><abbr title="the priority of the target host, lower value means more preferred">Priority</abbr></th><td><input type="text" name="priority" value="<c:if test="${record.type=='MX' || record.type=='SRV'}">${record.priority}</c:if>" size="5"></td></tr>
<tr id="dataRow"><th>Data</th><td><input type="text" name="data" value="${record.data}" size="80" maxlength="255">
<c:if test="${record.type=='PTR'}"><c:if test="${record.reverse != null}"><a href="${module}/${record.reverse.domain.name}/record/${record.reverse.id}">A record</a></c:if></c:if>
<c:if test="${record.type=='A'}"> <label for="update_reverse"><input type="checkbox" name="update_reverse" id="update_reverse" value="1" /> Update reverse</label></c:if>
</td></tr>
<tr class="locRows"><th>Latitude</th><td><input type="text" name="lat_d" value="<c:if test="${record.type=='LOC'}">${record.latD}</c:if>" size="6"> <input type="text" name="lat_m" value="<c:if test="${record.type=='LOC'}">${record.latM}</c:if>" size="6"> <input type="text" name="lat_s" value="<c:if test="${record.type=='LOC'}">${record.latS}</c:if>" size="6"> <input type="text" name="lat_cardinal" value="<c:if test="${record.type=='LOC'}">${record.latCardinal}</c:if>" maxlength="1" size="2"></td></tr>
<tr class="locRows"><th>Longitude</th><td><input type="text" name="lon_d" value="<c:if test="${record.type=='LOC'}">${record.lonD}</c:if>" size="6"> <input type="text" name="lon_m" value="<c:if test="${record.type=='LOC'}">${record.lonM}</c:if>" size="6"> <input type="text" name="lon_s" value="<c:if test="${record.type=='LOC'}">${record.lonS}</c:if>" size="6"> <input type="text" name="lon_cardinal" value="<c:if test="${record.type=='LOC'}">${record.lonCardinal}</c:if>" maxlength="1" size="2"></td></tr>
<tr class="locRows"><th>Altitude</th><td><input type="text" name="alt" value="<c:if test="${record.type=='LOC'}">${record.alt}</c:if>" size="6"></td></tr>
<tr class="locRows"><th>Precision</th><td><input type="text" name="hp" value="<c:if test="${record.type=='LOC'}">${record.hp}</c:if>" size="6"> <input type="text" name="vp" value="<c:if test="${record.type=='LOC'}">${record.vp}</c:if>" size="6"></td></tr>
<tr class="srvRows"><th><abbr title="A relative weight for records with the same priority">Weight</abbr></th><td><input type="text" name="weight" value="<c:if test="${record.type=='SRV'}">${record.weight}</c:if>" size="6"></td></tr>
<tr class="srvRows"><th><abbr title="the TCP or UDP port on which the service is to be found">Port</abbr></th><td><input type="text" name="port" value="<c:if test="${record.type=='SRV'}">${record.port}</c:if>" size="6"></td></tr>
<tr class="srvRows"><th><abbr title="the canonical hostname of the machine providing the service">Target</abbr></th><td><input type="text" name="target" value="<c:if test="${record.type=='SRV'}">${record.target}</c:if>" maxlength="255"></td></tr>
<tr><th>Comment</th><td><input type="text" name="comment" value="<c:out value="${record.comment}" />" size="80"></td></tr>
</table>
</fieldset>
<input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>
</div>
<script type="text/javascript">
$(layoutForm);
</script>
</body>
</html>