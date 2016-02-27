<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div id="createNewUser" style="width: 800px; height: 400px;">
	<c:choose>
		<c:when test="${not empty userData.id}">
			<c:set var="formAction" value="${userData.id}" />
		</c:when>
		<c:otherwise>
			<c:set var="formAction" value="${actionName}" />
		</c:otherwise>
	</c:choose>

	<form:form modelAttribute="userData" action="${formAction}" method="post">
		<form:hidden path="id" />
		<table id="userDetails" class="ui-table">
			<tr>
				<td>Name:</td>
				<td><form:input path="name" size="15"/></td>
			</tr>
			<tr>
				<td>Login:</td>
				<td><form:input path="loginName" size="10"/></td>
				<td/>
			</tr>
			<tr>
				<td>Password:</td>
				<td><form:password path="password" size="10"/></td>
				<td/>
			</tr>
			<tr>
				<td>Home Address:</td>
				<td colspan="2"><form:input path="homeAddress" size="30"/></td>
			</tr>
			<tr>
				<td>Role:</td>
				<td><form:select path="role" items="${rolesList}" /></td>
				<td/>
			</tr>
			<tr>
				<td><form:button class="big"><c:out value="${fn:toUpperCase(actionName)}"/></form:button></td>
			</tr>
		</table>
	</form:form>
</div>