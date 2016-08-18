<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="kb.view.showDoc.headline"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.showDoc.headline"/></h1>
        <p> Export:
            <g:link controller="KnowledgeBase" action="exportDoc"
                    params="[docTitle: document.docTitle, exportAs: 'json']">JSON</g:link>
            <g:link controller="KnowledgeBase" action="exportDoc"
                    params="[docTitle: document.docTitle, exportAs: 'xml']">XML</g:link>
        </p>
        <sec:ifAnyGranted roles="ROLE_GP-STAFF,ROLE_GP-PROF">
            <p>
                Edit:
                <g:link controller="KnowledgeBase" action="deleteDoc"
                        params="[docTitle: document.docTitle]">delete</g:link>
                <g:link controller="KnowledgeBase" action="editDoc"
                        params="[docTitle: document.docTitle]">edit</g:link>
            </p>
        </sec:ifAnyGranted>

        <g:if test="${!document.question}">
            <h1>${document.docTitle}</h1>
        </g:if>
        <g:if test="${document.steps}">

            <g:each in="${document.steps.sort { it.number }}">
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
        <g:elseif test="${document.docContent}">
            <div class="article-container">
                ${raw(document.docContent)}
            </div>
        </g:elseif>

        <br/>

        <hr/>
        <p><g:message code="kb.view.showDoc.author"/> ${author}</p>
        <p><g:message code="kb.view.showDoc.createDate"/> ${document.createDate}</p>
        <p><g:message code="kb.view.showDoc.clickCount"/> ${document.viewCount}</p>
        <g:if test="${document.linker}">
            <p>
                <g:message code="kb.view.showDoc.parents"/>
                <g:each in="${document.linker}">
                    '${it.subcat.name}'
                </g:each>
            </p>

        </g:if>

        <g:if test="${document.tags}">
            <p>
                <g:message code="kb.view.showDoc.tags"/>
                <g:each in="${document.tags?.toList()}">'${it}' </g:each>
            </p>
        </g:if>
        <br/><br/>

        <g:if test="${similarDocs.tutorial}">
            <p><g:message code="kb.view.showDoc.similarTuts"/></p>
            <ul>
                <g:each in="${similarDocs.tutorial}">
                    <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                        <li>${it.docTitle}</li>
                    </g:link>
                </g:each>
            </ul>
        </g:if>

        <g:if test="${similarDocs.faq}">
            <p><g:message code="kb.view.showDoc.relatedFaqs"/></p>
            <ul>
                <g:each in="${similarDocs.faq}">
                    <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                        <li>${it.docTitle}</li>
                    </g:link>
                </g:each>
            </ul>
        </g:if>

        <g:if test="${similarDocs.article}">
            <p><g:message code="kb.view.showDoc.relatedArticles"/></p>
            <ul>
                <g:each in="${similarDocs.article}">
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