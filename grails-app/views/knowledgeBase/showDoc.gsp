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


<h1>${docTitle}</h1><br/>
<g:link controller="KnowledgeBase" action="exportDoc" params="[docTitle: docTitle]">JSON 'Export'</g:link>
<g:each in="${docContent.steps}">
    <h2>Schritt ${it.id}</h2>
    <p>${it.text}</p>
    <g:img uri="${it.link}"/>
    <br/><br/>
</g:each>
<p>${dochTags}</p>

<g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
</body>
</html>