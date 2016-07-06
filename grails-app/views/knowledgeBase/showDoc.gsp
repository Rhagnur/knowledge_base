<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="page">
    <head>
        <title>Dokument anzeigen</title>
    </head>

    <content tag="main">
        <g:if test="${document.docType != 'faq'}">
            <h1>${document.docTitle}</h1>
        </g:if>
        <g:link controller="KnowledgeBase" action="exportDoc"
                params="[docTitle: document.docTitle, exportAs: 'json']">JSON 'Export'</g:link><br/>
        <g:link controller="KnowledgeBase" action="exportDoc"
                params="[docTitle: document.docTitle, exportAs: 'xml']">XML 'Export'</g:link>
        <g:if test="${document.docType == 'tutorial'}">
            <g:each in="${document.docContent}">
                <h2 class="tutStepHeadline">Schritt ${it.id}: ${it.title}</h2>

                <div class="tutStep">
                    <p>${it.text}</p>
                    <a href="${it.link}"><g:img uri="${it.link}"/></a>

                    <div class="clear"></div>
                </div>
            </g:each>
        </g:if>
        <g:elseif test="${document.docType == 'faq'}">
            <h2>${document.docTitle}</h2>
            <p>${document.docContent.answer}</p>
        </g:elseif>


        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>