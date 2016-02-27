<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Spring workshop</title>
	<link rel="stylesheet" href="<c:url value="/resources/styles/stylesheet.css" />" type="text/css" />
	<link rel="stylesheet" href="<c:url value="/resources/styles/jquery-ui-1.8.16.custom.css"/>" type="text/css" >
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js"></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>
	<script type="text/javascript" src="<c:url value="/resources/js/googleMapsHelpers.js" />"></script>
</head>
<body class="tundra">
<div id="page" class="container">
	<div id="header">
		<div id="topbar">
			<table id="topbarTable">
				<colgroup>
					<col width="20%"/>
					<col width="80%"/>
				</colgroup>
				<tr>
					<td>
						<a href="http://www.vmware.com" target="_blank">
							<img src="<c:url value="/resources/images/vmware-logo.jpg"/>" alt="Vmware" />
						</a>
					</td>
					<c:choose>
						<c:when test="${empty user}">
							<td>&nbsp;</td>
						</c:when>
						<c:otherwise>
							<td align="right">
								<table class="table">
									<tr>
										<td align="right">Hello&nbsp;<c:out value="${user.getName()}" />,&nbsp;&nbsp;&nbsp;<a href='<c:url value="/logout"/>'><img src='<c:url value="/resources/images/logout.jpg" />' width="32" height="32" title="Logout" /></a></td>
									</tr>
								</table>
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
			</table>
		</div>
	</div>
	<hr />
	<div id="content">
		<div id="main" class="span-18 last">
			<tiles:insertAttribute name="body" />
		</div>
	</div>
	<hr />
	<div id="footer">
		Powered by (<c:out value="${activeProfile}" />) <a href="http://www.springsource.org" target="_blank"><img src="<c:url value="/resources/images/springsource-logo.jpg"/>" alt="SpringSource" /></a>
	</div>
</div>
</body>
</html>