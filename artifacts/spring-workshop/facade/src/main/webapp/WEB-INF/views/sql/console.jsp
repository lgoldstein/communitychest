<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div id="sqlConsole">
	<h2>SQL Console</h2>
	<div id="sqlScript">
		<form:form method="post" action="executeScript" modelAttribute="scriptSubmissionBean">
			<form:textarea path="script" rows="80" cols="200" />
			<br />
			<input type="submit" value="Execute" />
		</form:form>
	</div>
	<hr />
	<div id="sqlFile">
		<form:form method="POST" action="executeFile" enctype="multipart/form-data">
			<form:errors path="*" cssClass="errorblock" element="div" />
			<input type="submit" value="Execute" /><input type="file" name="file" size="60" />
			<span><form:errors path="file" cssClass="error" /></span>
		</form:form>
	</div>
	<div id="sqlResult">
		<textarea rows="40" cols="120" readonly="yes"><c:out value="${scriptSubmissionBean.result}" /></textarea>
	</div>
</div>
