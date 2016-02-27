<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<div id="welcome">
	<script type="text/javascript">
		$(document).ready(function() {
			$( "button").button();
		});
	</script>
	<table id="login" class="ui-table">
		<colgroup>
			<col width="100">
			<col width="400">
		</colgroup>
		<tr><td colspan="2" class="ui-table header">Spring workshop</td></tr>
		<tr><td colspan="2">Workshop on using Spring and its Web MVC</td></tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<c:if test="${not empty loginBean}">
			<form:form modelAttribute="loginBean" action="login" method="post">
				<c:if test="${not empty msg}">
					<tr><td colspan="3" class="err"><c:out value="${msg}"></c:out></td></tr>
				</c:if>
				<tr><td colspan="2">Username:</td></tr>
				<tr><td colspan="2"><form:input path="username" size="10"/></td></tr>
				<tr><td colspan="2">Password:</td></tr>
				<tr><td colspan="2"><form:password path="password" size="10"/></td></tr>
				<tr><td><form:button class="big">Enter</form:button></td></tr>
			</form:form>
		</c:if>
	</table>	
</div>