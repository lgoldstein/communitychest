<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h2>Values</h2>
<div id="dataListResults">
<c:if test="${not empty dataList}">
	<table class="summary">
		<thead>
			<tr>
				<th>Id</th>
				<th>Balance</th>
				<th>Last Modified</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="value" items="${dataList}">
				<tr>
					<td>${value.id}</td>
					<td>${value.balance}</td>
					<td>${value.lastModified}</td>
				</tr>
			</c:forEach>
			<c:if test="${empty dataList}">
				<tr>
					<td colspan="5">No values found</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</c:if>
</div>	

