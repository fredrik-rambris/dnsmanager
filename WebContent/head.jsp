<h1>DNSManager <span>v${version} by <a href="mailto:fredrik@rambris.com">Fredrik Rambris</a></span></h1>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${currentUser!=null}">
<ul id="menu">
<li<c:if test="${pageName == 'overview'}"> class="selected"</c:if>><a href="${root}/overview">Overview</a></li>
<li<c:if test="${pageName == 'domain'}"> class="selected"</c:if>><a href="${root}/domain">Domain</a></li>
<c:if test="${currentUser.superAdmin}"><li<c:if test="${pageName == 'report'}"> class="selected"</c:if>><a href="${root}/report">Report</a></li>
<li<c:if test="${pageName == 'group'}"> class="selected"</c:if>><a href="${root}/group">Group</a></li>
<li<c:if test="${pageName == 'server'}"> class="selected"</c:if>><a href="${root}/server">Server</a></li>
<li<c:if test="${pageName == 'owner'}"> class="selected"</c:if>><a href="${root}/owner">Owner</a></li>
<li<c:if test="${pageName == 'user'}"> class="selected"</c:if>><a href="${root}/user">User</a></li>
</c:if><li<c:if test="${pageName == 'password'}"> class="selected"</c:if>><a href="${root}/password">Password</a></li>
<li><a href="${root}/logout">Log out</a></li>
</ul>
</c:if>
<c:if test="${not empty flash}"><div id="flash"<c:if test="${flash_type=='error'}"> class="error"</c:if>>
${flash}
</div><% 
pageContext.removeAttribute("flash");
pageContext.removeAttribute("flash_type");
%></c:if>
