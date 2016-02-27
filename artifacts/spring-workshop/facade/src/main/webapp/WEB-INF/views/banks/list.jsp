<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<h2>Banks</h2>
<div id="dataListResults">
	<script type="text/javascript">
		$(document).ready(function() {
			$('div#showBankLocation').hide();
			$('a[id|="bankid"]').click(function(e) {
				e.preventDefault();

				var location=$(this).attr('name');
				var options={
						title: location,
						resizable: true,
						height: 600,
						width: 800,
						modal: true,
						buttons: {
							Close : function() {
								$( this ).dialog( "close" );
							}
						}
					};
				$("div#showBankLocation").dialog(options);
				makeMap(location, "map_canvas", options.height, options.width);
			});
		});
	</script>

	<c:if test="${not empty banksList}">
		<table id="banksList" class="summary">
			<thead>
				<tr>
					<th>Code</th>
					<th>Name</th>
					<th>Location</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="value" items="${banksList}">
					<tr>
						<td>${value.bankCode}</td>
						<td>
							<a 	id="bankCode-<c:out value="${value.bankCode}" />"
								href="<c:url value="/branches/${value.bankCode}" />"
								name="${value.name}">${value.name}</a>
						</td>
						<td>
							<a  id="bankid-<c:out value="${value.id}" />"
								name="<c:out value="${value.location}" />"
								href="http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q='<c:out value="${value.location}" />'" target="_blank">${value.location}</a>
						</td>
					</tr>
				</c:forEach>
				<c:if test="${empty banksList}">
					<tr>
						<td colspan="5">No values found</td>
					</tr>
				</c:if>
			</tbody>
		</table>
	</c:if>
	<table id="userActions" class="ui-table">
		<colgroup>
			<col width="100">
			<col width="400">
		</colgroup>
		<tr>
			<td>
				<form:form method="GET" action="banks/export">
					<input type="submit" value="Export" />
				</form:form>
			</td>
			<td>
				<sec:authorize access="hasRole('ADMIN')">
					<form:form method="POST" action="banks/import" enctype="multipart/form-data">
						<form:errors path="*" cssClass="errorblock" element="div" />
						<input type="submit" value="Import" /><input type="file" name="file" size="40" />
						<span><form:errors path="file" cssClass="error" /></span>
					</form:form>
				</sec:authorize>
			</td>
		</tr>
	</table>

	<div id="showBankLocation" style="width: 800px; height: 600px;">
		<div id="map_canvas" class="locationsList" />
	</div>
</div>	
