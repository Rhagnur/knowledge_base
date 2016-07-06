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
    <h2 class="tutStepHeadline">Schritt ${it.id}: ${it.title}</h2>
    <div class="tutStep">
        <p>${it.text}</p>
        <a href="${it.link}"><g:img uri="${it.link}"/></a>
        <div class="clear"></div>
    </div>
</g:each>

<g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
</content>
</g:applyLayout>