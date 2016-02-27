<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h2><c:if test="${not empty bank}"><c:out value="${ bank.name } "/></c:if>Branches</h2>
<div id="dataListResults">
	<script type="text/javascript">
		$(document).ready(function() {
			$('div#showBranchLocation').hide();
			$('a[id|="branchid"]').click(function(e) {
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
				$("div#showBranchLocation").dialog(options);
				makeMap(location, "map_canvas", options.height, options.width);
			});
		});
	</script>

	<c:if test="${not empty branchesList}">
		<table id="branchesList" class="summary">
			<thead>
				<tr>
					<c:if test="${empty bank}"><th>Bank</th></c:if>
					<th>Code</th>
					<th>Name</th>
					<th>Location</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="value" items="${branchesList}">
					<c:set var="branch" value="${value.getDTOValue() }" />
					<c:set var="ownerBank" value="${value.getBankValue() }" />
					<tr>
						<c:if test="${empty bank}">
							<td>
								<a 	id="bankCode-<c:out value="${ownerBank.bankCode}" />"
									href="<c:url value="/branches/${ownerBank.bankCode}" />"
									name="${ownerBank.name}">${ownerBank.name}</a>
							</td>
						</c:if>
						<td>${branch.branchCode}</td>
						<td>${branch.name}</td>
						<td>
							<a  id="branchid-<c:out value="${branch.id}" />"
								name="<c:out value="${branch.location}" />"
								href="http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q='<c:out value="${branch.location}" />'" target="_blank">${branch.location}</a>
						</td>
					</tr>
				</c:forEach>
				<c:if test="${empty branchesList}">
					<tr>
						<td colspan="5">No branches found</td>
					</tr>
				</c:if>
				<tr>
					<c:choose>
					 	<c:when test="${empty bank}">
							<td colspan="4"><a href="<c:url value="/branches/export" />">Export values</a></td>
						</c:when>
						
						<c:otherwise>
							<td colspan="3"><a href="<c:url value="/branches/export/${ bank.bankCode }" />">Export values</a></td>
						</c:otherwise>
					</c:choose>
			</tbody>
		</table>
	</c:if>

	<div id="showBranchLocation" style="width: 800px; height: 600px;">
		<div id="map_canvas" class="locationsList" />
	</div>
</div>	
