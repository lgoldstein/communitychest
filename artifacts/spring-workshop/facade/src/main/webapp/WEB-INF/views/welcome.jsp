<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<div>
	<h2>Welcome to Spring Workshop facade</h2>
	<hr/>
	<p>
		<table id="welcome-options" class="ui-table">
			<colgroup>
				<col width="400">
				<col width="100">
			</colgroup>
			<tr>
				<td>
					<a href='<c:out value="${downloadLocation}" />' target="_blank" >
						<img src='<c:url value="/resources/images/browsers/${browserType}-logo.jpg" />'
							 alt='<c:out value="${agentInfo}" />'
							 title='<c:out value="${agentInfo}" />'
						 	/>
					</a>
				</td>
				<td><a href="login">Login</a></td>
			</tr>
		</table>
	</p>
</div>
