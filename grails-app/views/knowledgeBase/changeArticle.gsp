<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.changeArticle.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.changeArticle.title"/></h1>
        <g:form controller="KnowledgeBase" action="changeArticle" class="docForm">
            <g:hiddenField name="docTitle" value="${doc.docTitle}"/>
            <label for="docTitleNew"><g:message code="kb.view.createArticle.docTitle"/></label><br/>
            <g:textField name="docTitleNew" value="${doc.docTitle}"/>
            <br/><br/>
            <label for="docContent"><g:message code="kb.view.createArticle.docContent"/></label><br/>
            <g:textArea name="docContent" value="${doc.docContent}"/>
            <br/><br/>

            <label for="docTags"><g:message code="kb.view.createArticle.docTags"/></label><br/>
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