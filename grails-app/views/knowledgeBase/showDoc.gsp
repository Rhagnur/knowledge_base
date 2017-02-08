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
            <section class="sidebox ${it.style?:''}">
                <g:if test="${it.stepTitle}">
                    <h2>${it.showNumber?"Schritt ${it.number}: ":""}${it.stepTitle}</h2>
                </g:if>
                ${raw(it.stepText)}

                <g:each in="${it.images.sort{ it.number }}">
                    <div class="step-media">
                        <g:if test="${it.link}">
                            <a href="${it.link}"><img src="/knowledgeBase/showImage/${it.id}" alt="${it.altText}" title="${it.altText}"/></a>
                        </g:if>
                        <g:else>
                            <img src="/knowledgeBase/showImage/${it.id}" alt="${it.altText}" title="${it.altText}"/>
                        </g:else>

                    </div>
                </g:each>
                <div class="clear"></div>
            </section>

        </g:each>
    </content>

    <content tag="main">
        <article>
            <div id="debug-functions">
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
            </div>

            <div id="doc-content">
                <g:if test="${!document.question}">
                    <h1>${document.docTitle}</h1>
                </g:if>

                <g:if test="${document.videoLink}">
                    <section id="video" class="video">
                        <video controls="true">
                            <source type="video/mp4" src="${document.videoLink}.mp4">
                            <source type="video/m4v" src="${document.videoLink}.m4v">
                            <source type="video/webm" src="${document.videoLink}.webm">
                            <source type="video/ogg" src="${document.videoLink}.ogg">
                            Ihr Browser unterstützt das HTML 5 Videoelement leider nicht.
                        </video>
                    </section>
                </g:if>

                <g:if test="${document.intro}">
                    <section id="intro">
                        ${raw(document.intro)}
                    </section>
                </g:if>

                <g:if test="${document.steps}">

                    <g:each in="${document.steps.sort{ it.number }}">
                        <section id="step_${it.number}" class="${it.style?:''}">
                            <g:if test="it.stepTitle">
                                <div class="csc-header csc-header-n2">
                                    <h1>${it.showNumber?"${message(code: 'kb.view.showDoc.labelStep')} ${it.number}: ":""}${it.stepTitle}</h1>
                                </div>
                            </g:if>

                            <div class="step-content csc-textpic">
                                <div class="step-text csc-textpic-text">
                                    <p>${raw(it.stepText)}</p>
                                </div>

                                <g:each in="${it.images.sort{ it.number }}">
                                    <div class="step-media csc-textpic-imagewrap">
                                        <a href="/knowledgeBase/showImage/${it.id}"><img src="/knowledgeBase/showImage/${it.preview?.id}" alt="${it.altText}" title="${it.altText}"/></a>
                                    </div>
                                </g:each>
                                <div class="clear"></div>
                            </div>
                        </section>
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
            </div>

            <br/><hr/>

            <div id="doc-meta">
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
            </div>

            <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
        </article>
        <div id="clear"></div>
    </content>
</g:applyLayout>