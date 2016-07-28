<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title>Dokument anzeigen</title>
    </head>

    <content tag="main">
        <p>Das Dokumente wurde schon ${document.viewCount} mal angeklickt</p>
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
        <g:elseif test="${document.question && document.answer}">
            <h2>${document.question}</h2>
            <p>${raw(document.answer)}</p>
        </g:elseif>

        <br/><p>Geschrieben von: ${author}</p><br/><br/>

        <g:if test="${similarDocs.tutorials}">
            <p>Anleitungen, die Sie auch interessieren könnten...</p>
            <ul>
                <g:each in="${similarDocs.tutorials}">
                    <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                        <li>${it.docTitle}</li>
                    </g:link>
                </g:each>
            </ul>
        </g:if>

        <g:if test="${similarDocs.faq}">
            <p>Nützliche Fragen und Antworten zum Thema...</p>
            <ul>
                <g:each in="${similarDocs.faq}">
                    <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                        <li>${it.docTitle}</li>
                    </g:link>
                </g:each>
            </ul>
        </g:if>
        <br/><br/>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>