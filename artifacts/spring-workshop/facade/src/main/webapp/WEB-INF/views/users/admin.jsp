<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="adminOptions">
	<h2>Welcome to Spring Workshop facade</h2>
	<hr/>
	<p>
		<table id="guest-options" class="ui-table">
			<tr><td><a href="<c:url value="/users" />">Manage users</a></td></tr>
			<tr><td><a href="<c:url value="/banks" />">Manage banks</a></td></tr>
			<tr><td><a href="<c:url value="/branches" />">Manage branches</a></td></tr>
			<tr><td><a href="<c:url value="/groovy" />">Groovy console</a></td></tr>
			<tr><td><a href="<c:url value="/sql" />">SQL console</a></td></tr>
		</table>
	</p>
</div>