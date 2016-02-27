<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>REST Web Application</title>
</head>
<body class="tundra">
<div id="page" class="container">
	<div id="header">
		<div id="logo">
			<p>
				<a href="http://www.vmware.com">
					<img src="<c:url value="/resources/images/Developer.jpg"/>" alt="Lyor Goldstein" />
				</a>
			</p>
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
		<a href="http://www.springsource.org">
			<img src="<c:url value="/resources/images/powered-by-spring.png"/>" alt="Powered by Spring" />
		</a>
	</div>
</div>
</body>
</html>