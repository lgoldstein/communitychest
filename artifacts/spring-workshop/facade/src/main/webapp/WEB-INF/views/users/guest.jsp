<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="guestOptions">
	<h2>Welcome to Spring Workshop facade</h2>
	<hr/>
	<p>
		<table id="guest-options" class="ui-table">
			<tr><td><a href="<c:url value="/banks" />">View banks</a></td></tr>
			<tr><td><a href="<c:url value="/branches" />">View branches</a></td></tr>
		</table>
	</p>
</div>