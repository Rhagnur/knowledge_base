<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="page">
<head>
    <title>Dokument anzeigen</title>
</head>

<content tag="main">
<h1>${document.docTitle}</h1><br/>
<g:link controller="KnowledgeBase" action="exportDoc" params="[docTitle: document.docTitle, exportAs: 'json']">JSON 'Export'</g:link><br/>
<g:link controller="KnowledgeBase" action="exportDoc" params="[docTitle: document.docTitle, exportAs: 'xml']">XML 'Export'</g:link>
<g:each in="${document.docContent}">
    <h2>Schritt ${it.id}: ${it.title}</h2>
    <p>${it.text}</p>
    <g:img uri="${it.link}"/>
    <br/><br/>
</g:each>

<g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
</content>
</g:applyLayout>