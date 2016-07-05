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


<h1>${document.docTitle}</h1><br/>
<g:link controller="KnowledgeBase" action="exportDoc" params="[docTitle: document.docTitle, exportAs: 'json']">JSON 'Export'</g:link><br/>
<g:link controller="KnowledgeBase" action="exportDoc" params="[docTitle: document.docTitle, exportAs: 'xml']">XML 'Export'</g:link>
<g:each in="${document.docContent}">
    <h2>Schritt ${it.id}</h2>
    <p>${it.text}</p>
    <g:img uri="${it.link}"/>
    <br/><br/>
</g:each>

<g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
</body>
</html>