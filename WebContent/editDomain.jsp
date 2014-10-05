<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="f" uri="/WEB-INF/tags/taglib.tld" %><%@ taglib prefix="dt" uri="http://jakarta.apache.org/taglibs/datetime-1.0" %><!doctype html>
<html>
<head>
<title><c:choose><c:when test="${!empty domain.name}">Edit Domain (${domain.name})</c:when><c:otherwise>Create Domain</c:otherwise></c:choose> - DNSManager v${version}</title>
<link rel="stylesheet" type="text/css" href="${root}/styles.css" />
<script src="${root}/jquery-1.4.2.min.js"></script>
<style>
</style>
<script type="text/javascript">

/* Create Record */
function cr(group)
{
	window.location.href="${self}/record/new/"+group;
}

/* Copy record */
function cpr(group, source)
{
	window.location.href="${self}/record/new/"+group+"?copyOf="+source;
}


/* Edit Record */
function er(id)
{
	window.location.href="${self}/record/"+id;
}

/* Delete Record */
function dr(id, name)
{
	if( confirm("Really delete this record ("+name+")?") )
	window.location.href="${self}/record/"+id+"/delete";
}

/* Edit Master */
function em(id)
{
	window.location.href="${self}/master/"+id;
}

/* Delete Master */
function dm(id, name)
{
	if( confirm("Really delete this master ("+name+")?") )
		window.location.href="${self}/master/"+id+"/delete";
}


/* Edit Alias */
function ea(name)
{
	window.location.href="${self}/alias/"+name;
}

/* Delete Alias */
function da(name)
{
	if( confirm("Really delete this alias ("+name+")?") )
	window.location.href="${self}/alias/"+name+"/delete";
}

/* Promote Alias */
function pa(name)
{
	window.location.href="${module}/new?copyOf=${domain.name}&promote="+name;
}
</script>

</head>
<body>
<jsp:include page="head.jsp" />
<div id="contents">
<h2>Domain<c:if test="${!empty domain.name}"> ${domain.name}</c:if></h2>
<form method="post" action="${self}">
<fieldset>
<table class="layout">
<c:if test="${not empty copyOf}"><input type="hidden" name="copyOf" value='<c:out value="${copyOf}" />' />
<tr><th>Copy of</th><td>${copyOf}</td></tr></c:if>
<c:if test="${not empty promote}"><input type="hidden" name="promote" value='<c:out value="${promote}" />' /></c:if>
<tr><th>Name</th><td><input type="text" name="domain_name" value="${domain.name}<c:if test="${not empty promote}"><c:out value="${promote}" /></c:if>" maxlength="255"></td></tr><c:if test="${!empty domain.name}">
<tr><th>Created</th><td><dt:format pattern="yyyy-MM-dd HH:mm">${domain.created.time}</dt:format></td></tr>
<tr><th>Updated</th><td><dt:format pattern="yyyy-MM-dd HH:mm">${domain.updated.time}</dt:format></td></tr>
<tr><th>Records Updated</th><td><dt:format pattern="yyyy-MM-dd HH:mm">${domain.newestRecord.time}</dt:format></td></tr>
<tr><th>Exported</th><td><c:choose><c:when test="${domain.exported != null}"><dt:format pattern="yyyy-MM-dd HH:mm">${domain.exported.time}</dt:format></c:when><c:otherwise>Never</c:otherwise></c:choose> <button onclick="window.location.href='${self}?clear_export=1'; return false;">Clear</button></td></tr>
</c:if><tr><th><label for="active">Active</label></th><td><input type="checkbox" name="active" id="active" value="1"<c:if test="${domain.active}"> checked="checked"</c:if>></td></tr>
<tr><th>Server</th><td><f:select values="${servers}" name="server_id" selected="${domain.serverId}" nullOption="---Server---" /><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.host}</c:if></td></tr>
<tr><th>Owner</th><td><f:select values="${owners}" name="owner_id" selected="${domain.ownerId}" nullOption="---Owner---" /><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.admin}</c:if></td></tr>
<tr><th><abbr title="Standard TTL values apply (range 0 to 2147483647 clarified by RFC 2181). The slave (Secondary) DNS does not use the the TTL value but various parameters defined within the SOA">TTL</abbr></th><td><input type="text" name="ttl" value="${domain.ttl}" size="8"><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.ttl}</c:if></td></tr>
<tr><th>Serial</th><td><input type="text" name="serial" value="${domain.serial}" size="14"> <input type="checkbox" id="bump_serial" name="bump_serial" title="Bump serial" value="1"><c:if test="${domain.newestRecord.time > domain.updated.time}"> <label for="bump_serial">Bump serial</label></c:if><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.serial}</c:if></td></tr>
<tr><th><abbr title="Signed 32 bit time value in seconds. Indicates the time when the slave will try to refresh the zone from the master (by reading the master DNS SOA RR). RFC 1912 recommends 1200 to 43200 seconds, low (1200) if the data is volatile or 43200 (12 hours) if it's not. If you are using NOTIFY you can set for much higher values, for instance, 1 or more days (> 86400 seconds).">Refresh</abbr></th><td><input type="text" name="refresh" value="${domain.refresh}" size="8"><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.refresh}</c:if></td></tr>
<tr><th><abbr title="Signed 32 bit value in seconds. Defines the time between retries if the slave (secondary) fails to contact the master when refresh (above) has expired. Typical values would be 180 (3 minutes) to 900 (15 minutes) or higher.">Retry</abbr></th><td><input type="text" name="retry" value="${domain.retry}" size="8"><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.retry}</c:if></td></tr>
<tr><th><abbr title="Signed 32 bit value in seconds. Indicates when the zone data is no longer authoritative. Used by Slave or (Secondary) servers only. BIND9 slaves stop responding to queries for the zone when this time has expired and no contact has been made with the master. Thus every time the refresh values expires the slave will attempt to read the SOA record from the zone master - and request a zone transfer AXFR/IXFR if sn is HIGHER. If contact is made the expiry and refresh values are reset and the cycle starts again. If the slave fails to contact the master it will retry every retry period but continue to supply authoritative data for the zone until the expiry value is reached at which point it will stop answering queries for the domain. RFC 1912 recommends 1209600 to 2419200 seconds (2-4 weeks) to allow for major outages of the zone master.">Expire</abbr></th><td><input type="text" name="expire" value="${domain.expire}" size="8"><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.expire}</c:if></td></tr>
<tr><th><abbr title="Signed 32 bit value in seconds. RFC 2308 (implemented by BIND 9) redefined this value to be the negative caching time - the time a NAME ERROR = NXDOMAIN result may be cached by any resolver. The maximum value allowed by RFC 2308 for this parameter is 3 hours (10800 seconds). This value was historically (in BIND 4 and 8) used to hold the default TTL value for any RR from the zone that did not specify an explicit TTL. RFC 2308 (and BIND 9) uses the $TTL directive as the zone default TTL (and which was also standardized in RFC 2308). You may find older documentation or zone file configurations which reflect the old usage (there there are still a lot of BIND 4/8 operational sites).">Minimum</abbr></th><td><input type="text" name="minimum" value="${domain.minimum}" size="8"><c:if test="${domain.soaRecord !=null}"> ${domain.soaRecord.minimum}</c:if></td></tr>
<tr><th>Comment</th><td><input type="text" name="comment" value="<c:out value="${domain.comment}" />" size="80"></td></tr>
</table>
</fieldset>

<input type="submit" name="submit" value="Save"> <input type="submit" name="cancel" value="Cancel">
</form>

<c:if test="${!empty domain.name}">

<p><button<c:if test="${domain.soaRecord !=null}"> disabled="disabled"</c:if> onclick="window.location.href='${self}?report=1';">Get SOA</button></p>

<h3>Views</h3>
<p><button onclick="em('new');">New</button></p>
<table class="data clickable">
<c:forEach items="${domain.masters}" var="view">
<tr><td onclick="em(${view.id});"><a title="slaves: ${view.slaves}" href="${self}/master/${view.id}">${view}</a></td><td><button onclick="dm(${view.id}, '${view}')">Delete</button></td></tr>
</c:forEach>
</table>
<h3>Aliases</h3>
<p><button onclick="ea('new');">New</button></p>
<table class="data clickable">
<c:forEach items="${domain.aliases}" var="alias">
<tr><td onclick="ea(${alias});"><a href="${self}/alias/${alias}">${alias}</a></td><td><button onclick="pa('${alias}');">Promote</button><button onclick="da('${alias}')">Delete</button></td></tr>
</c:forEach>
</table>

<h3>Records</h3>
<c:forEach items="${domain.records}" var="group">
<h4>${group.key}</h4>
<p><button onclick="cr('${group.key}')">New</button> <button onclick="window.location.href='${self}/record/paste/${group.key}';">Paste</button></p>
<table class="data clickable"><c:forEach items="${group.value}" var="record">
<tr<c:if test="${not record.active}"> class="inactive"</c:if>><td onclick="er(${record.id});"><a href="${self}/record/${record.id}">${record.builtName}</a></td><td onclick="er(${record.id});">${record.ttl}</td><td onclick="er(${record.id});">${record.type}</td><td onclick="er(${record.id});" title="${record.comment}"><c:if test="${record.type == 'MX' || record.type == 'SRV'}">${record.priority} </c:if><span<c:if test="${record.data!=record.shortData}"> title='<c:out value="${record.data}" />'</c:if>>${record.shortData}</span></td><td onclick="er(${record.id});"><small>${record.comment}</small></td><td><button onclick="cpr('${group.key}', ${record.id})">Copy</button> <button onclick="dr(${record.id}, '${record.name}');">Delete</button></td><td onclick="er(${record.id});"><dt:format pattern="yyyy-MM-dd HH:mm">${record.updated.time}</dt:format></td></tr>
</c:forEach></table>
</c:forEach>
</c:if>
</div>

</body>
</html>