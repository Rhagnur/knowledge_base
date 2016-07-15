<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title>Dokument anzeigen</title>
    </head>

    <content tag="main">
        <g:if test="${!document.faq}">
            <h1>${document.docTitle}</h1>
        </g:if>
        <g:link controller="KnowledgeBase" action="exportDoc"
                params="[docTitle: document.docTitle, exportAs: 'json']">JSON 'Export'</g:link><br/>
        <g:link controller="KnowledgeBase" action="exportDoc"
                params="[docTitle: document.docTitle, exportAs: 'xml']">XML 'Export'</g:link>
        <g:if test="${document.steps}">
            <g:each in="${document.steps.sort{it.number}}">
                <div class="step-header">
                    <h2>Schritt ${it.number}: ${it.stepTitle}</h2>
                </div>

                <div class="step-content">
                    <div class="step-text">
                        <p>${raw(it.stepText)}</p>
                    </div>
                    <div class="step-media">
                        <a href="${it.mediaLink}"><g:img uri="${it.mediaLink}"/></a>
                    </div>
                    <div class="clear"></div>
                </div>
            </g:each>
        </g:if>
        <g:elseif test="${document.faq}">
            <h2>${document.faq?.question}</h2>
            <p>${raw(document.faq?.answer)}</p>
        </g:elseif>


        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>