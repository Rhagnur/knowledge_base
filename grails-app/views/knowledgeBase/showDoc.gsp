<%@ page import="berlin.htw.hrz.kb.Sidebox" %>
<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.showDoc.headline"/></title>
    </head>

    <content tag="side">
        <g:each in="${document.sideboxes?.sort{ it.number }}">
            <div class="sidebox">
                <div class="${it.style?:''}">
                    <div class="step-header">
                        <h2>${it.showNumber?"Schritt ${it.number}: ":""}${it.stepTitle}</h2>
                    </div>

                    <div class="step-content">
                        <div class="step-text">
                            <p>${raw(it.stepText)}</p>
                        </div>

                        <g:if test="${it.stepLink}">
                            <div class="step-media">
                                <a href="${it.stepLink}"><g:img uri="${it.stepLink}"/></a>
                            </div>
                        </g:if>
                        <g:each in="${it.images.sort{ it.number }}">
                            <div class="step-media2">
                                <a href="/knowledgeBase/showImage/${it.id}"><img src="/knowledgeBase/showImage/${it.preview?.id}" alt="${it.altText}" title="${it.altText}"/></a>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </div>
            </div>

        </g:each>
    </content>

    <content tag="main">
        <h1><g:message code="kb.view.showDoc.headline"/></h1>
        <p> <g:message code="kb.view.export"/>
            <g:link controller="KnowledgeBase" action="exportDoc"
                    params="[docTitle: document.docTitle, format: 'json']"><g:message code="kb.view.json"/></g:link>
            <g:link controller="KnowledgeBase" action="exportDoc"
                    params="[docTitle: document.docTitle, format: 'xml']"><g:message code="kb.view.xml"/></g:link>
        </p>
        <sec:ifAnyGranted roles="ROLE_GP-STAFF,ROLE_GP-PROF">
            <p>
                <g:message code="kb.view.options"/>
                <g:link controller="KnowledgeBase" action="deleteDoc"
                        params="[docTitle: document.docTitle]"><g:message code="kb.view.delete"/></g:link>
                <g:if test="${document instanceof berlin.htw.hrz.kb.Article}">
                    <g:link controller="KnowledgeBase" action="changeArticle"
                            params="[docTitle: document.docTitle]"><g:message code="kb.view.edit"/></g:link>
                </g:if>
                <g:elseif test="${document instanceof berlin.htw.hrz.kb.Faq}">
                    <g:link controller="KnowledgeBase" action="changeFaq"
                            params="[docTitle: document.docTitle]"><g:message code="kb.view.edit"/></g:link>
                </g:elseif>
                <g:elseif test="${document instanceof berlin.htw.hrz.kb.Tutorial}">
                    <g:link controller="KnowledgeBase" action="changeTutorial"
                            params="[docTitle: document.docTitle]"><g:message code="kb.view.edit"/></g:link>
                </g:elseif>
            </p>
        </sec:ifAnyGranted>

        <g:if test="${!document.question}">
            <h1>${document.docTitle}</h1>
        </g:if>

        <g:if test="${document.videoLink}">
            <div class="video">
                <video controls="true">
                    <source type="video/mp4" src="${document.videoLink}.mp4">
                    <source type="video/m4v" src="${document.videoLink}.m4v">
                    <source type="video/webm" src="${document.videoLink}.webm">
                    <source type="video/ogg" src="${document.videoLink}.ogg">
                    Ihr Browser unterstützt das HTML 5 Videoelement leider nicht.
                </video>
            </div>
        </g:if>

        <g:if test="${document.intro}">
            ${raw(document.intro)}
        </g:if>

        <g:if test="${document.steps}">

            <g:each in="${document.steps.sort { it.number }}">
                <g:if test="${!(it instanceof berlin.htw.hrz.kb.Sidebox)}">
                    <div class="${it.style?:''}">
                        <div class="step-header">
                            <h2>${it.showNumber?"Schritt ${it.number}: ":""}${it.stepTitle}</h2>
                        </div>

                        <div class="step-content">
                            <div class="step-text">
                                <p>${raw(it.stepText)}</p>
                            </div>

                            <g:if test="${it.stepLink}">
                                <div class="step-media">
                                    <a href="${it.stepLink}"><g:img uri="${it.stepLink}"/></a>
                                </div>
                            </g:if>
                            <g:each in="${it.images.sort{ it.number }}">
                                <div class="step-media2">
                                    <a href="/knowledgeBase/showImage/${it.id}"><img src="/knowledgeBase/showImage/${it.preview?.id}" alt="${it.altText}" title="${it.altText}"/></a>
                                </div>
                            </g:each>
                            <div class="clear"></div>
                        </div>
                    </div>
                </g:if>


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
        <p><g:message code="kb.view.showDoc.author"/> ${author?message(code:'kb.author.'+author):message(code:'kb.error.notFound')}</p>
        <p><g:message code="kb.view.showDoc.language"/> ${lang?message(code:'kb.lang.'+lang):message(code:'kb.error.notFound')}</p>
        <p><g:message code="kb.view.showDoc.createDate"/> ${document.createDate}</p>
        <p><g:message code="kb.view.showDoc.clickCount"/> ${document.viewCount}</p>
        <g:if test="${document.linker}">
            <p>
                <g:message code="kb.view.showDoc.parents"/>
                <g:each in="${document.linker.sort { it.subcat?.name }}">
                    '${it.subcat?.name}'
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

        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>