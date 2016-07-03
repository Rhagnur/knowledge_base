<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>
<g:if test="${flash.error}">
    <div class="alert alert-error" style="display: block">${flash.error}</div>
</g:if><br/><br/>
<g:if test="${flash.info}">
    <div class="alert alert-error" style="display: block">${flash.info}</div>
</g:if><br/><br/>

    <g:form controller="KnowledgeBase" action="testingThings">
        <g:submitButton name="submit" value="call testingThings()"/>
    </g:form>

    <g:form controller="KnowledgeBase" action="createDoc">
        <g:submitButton name="create" value="Neues Doc erstellen"/>
    </g:form>

</body>
</html>