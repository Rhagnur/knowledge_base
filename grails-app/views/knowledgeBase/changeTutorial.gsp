<%@ page import="berlin.htw.hrz.kb.Sidebox" %>
<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.changeTutorial.title"/></title>
    </head>

    <content tag="side">
        <g:each in="${doc.sideboxes?.sort{ it.number }}" var="sidebox" status="counter">
            <g:if test="${counter == 0}">
                <input class="button-newstep-edit" type="button" value="${message(code: 'kb.view.createTutorial.newSidebox')}" class=""/>
                <div class="clear"></div>
            </g:if>
            <section class="sidebox sidebox-holder-edit">
                <h2><g:message code="kb.view.editDoc.labelSidebox"/> ${sidebox.number}</h2>
                <g:if test="${counter > 0}">
                    <input type="button" value="&uarr;" class=""/>
                </g:if>
                <g:if test="${counter < (doc.sideboxes.size() - 1)}">
                    <input type="button" value="&darr;" class=""/>
                </g:if>
                <g:if test="${sidebox.stepTitle}">
                    <label for="sideboxTitle_${sidebox.number}"><g:message code="kb.view.createTutorial.stepTitle"/></label><br/>
                    <g:textField name="sideboxTitle_${sidebox.number}" value="${sidebox.stepTitle}"/>
                </g:if>

                <label for="sideboxText_${sidebox.number}"><g:message code="kb.view.createTutorial.stepText"/></label><br/>
                <g:textArea name="sideboxText_${sidebox.number}" value="${sidebox.stepText}"/>

                <g:each in="${sidebox.images.sort{ it.number }}">
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
            <input class="button-newstep-edit" type="button" value="${message(code: 'kb.view.createTutorial.newSidebox')}" class=""/>
            <div class="clear"></div>
        </g:each>
    </content>

    <content tag="main">
        <h1><g:message code="kb.view.changeTutorial.title"/></h1>
        <g:form controller="KnowledgeBase" action="changeTutorial" class="docForm">
            <g:hiddenField name="docTitle" value="${doc.docTitle}"/>
            <label for="docTitleNew"><g:message code="kb.view.createArticle.docTitle"/></label><br/>
            <g:textField name="docTitleNew" value="${doc.docTitle}"/>
            <br/><br/>

            <g:each in="${doc.steps.sort{ it.number }}" var="step" status="counter">
                <g:if test="${counter == 0}">
                    <input class="button-newstep-edit" type="button" value="${message(code: 'kb.view.createTutorial.newStep')}" class=""/>
                    <div class="clear"></div>
                </g:if>

                <div class="step-holder-edit">
                    <h2><g:message code="kb.view.showDoc.labelStep"/> ${step.number}</h2>
                    <g:if test="${counter > 0}">
                        <!--input type="button" value="&uarr;"/-->
                        <g:link class="toolbar-button" action="stepUp" params="[stepNumber: step.number, docTitle: doc.docTitle]"><i class="icon-sym-pfeil-hoch-gross"></i></g:link>
                    </g:if>
                    <g:if test="${counter < (doc.steps.size() - 1)}">
                        <!--input type="button" value="&darr;"/-->
                        <g:link class="toolbar-button" action="stepDown" params="[stepNumber: step.number, docTitle: doc.docTitle]"><i class="icon-sym-pfeil-runter-gross"></i></g:link>
                    </g:if>
                    <div class="clear"></div>

                    <label for="stepTitle_${step.number}"><g:message code="kb.view.createTutorial.stepTitle"/></label><br/>
                    <g:textField name="stepTitle_${step.number}" value="${step.stepTitle}"/>
                    <label for="stepText_${step.number}"><g:message code="kb.view.createTutorial.stepText"/></label><br/>
                    <g:textArea name="stepText_${step.number}" value="${step.stepText}"/>

                    <g:each in="${step.images.sort{ it.number }}" var="image" status="counter2">
                        <label for="stepImage_${step.number}_${counter2}"><g:message code="kb.view.createTutorial.stepImage"/> ${counter2}</label><br/>
                        <div class="step-media csc-textpic-imagewrap">
                            <a href="/knowledgeBase/showImage/${image.id}"><img src="/knowledgeBase/showImage/${image.preview?.id}" alt="${image.altText}" title="${image.altText}"/></a>
                        </div>
                    </g:each>

                </div>
                <input class="button-newstep-edit" type="button" value="${message(code: 'kb.view.createTutorial.newStep')}" class=""/>
                <div class="clear"></div>
            </g:each>
            <br/><br/>

            <label for="docTags"><g:message code="kb.view.createArticle.docTags"/>*</label><br/>
            <g:textArea name="docTags" value="${doc.tags?.toString()?.replaceAll('[\\[\\]]', '')}"/><br/>

            <p><i>*Trennen mit ','</i></p>
            <br/><br/>

            <p><g:message code="kb.view.showDoc.language"/></p>
            <g:select name="languageNew" from="${lang}"/>
            <br/><br/>
            <p><g:message code="kb.view.showDoc.author"/></p>
            <g:select name="authorNew" from="${author}"/>

            <br/><br/>
            <p><g:message code="kb.view.createArticle.docParents"/></p>
            <g:each in="${cats}">
                <g:if test="${it.value != null}">
                    <div class="cat-checkbox-holder">
                        <p>'${it.key}' - <g:message code="kb.view.category"/></p>
                        <g:each var="cat" in="${it.value.sort{ it }}">
                            <div class="cat-checkbox">
                                <g:checkBox name="checkbox" value="${cat}" checked="${doc.linker?.subcat?.name?.contains(cat)?'true':'false'}"/>
                                <label>${cat}</label>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </g:if>
            </g:each>
            <br/><br/>
            <g:submitButton value="change" name="submit"/>
        </g:form>
        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>