<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.createArticle.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.createArticle.title"/></h1>
        <g:form controller="KnowledgeBase" action="createArticle" class="docForm">
            <label for="docTitle"><g:message code="kb.view.createArticle.docTitle"/></label><br/>
            <g:textField name="docTitle"/>
            <br/><br/>
            <label for="docContent"><g:message code="kb.view.createArticle.docContent"/></label><br/>
            <g:textArea name="docContent"/>
            <br/><br/>

            <label for="docTags"><g:message code="kb.view.createArticle.docTags"/></label><br/>
            <g:textArea name="docTags"/><br/>

            <p><i>*Trennen mit ','</i></p>
            <br/><br/>

            <p><g:message code="kb.view.showDoc.language"/></p>
            <g:select name="language" from="${lang}"/>

            <br/><br/>
            <p><g:message code="kb.view.createArticle.docParents"/></p>
            <g:each in="${cats}">
                <g:if test="${it.value != null}">
                    <div class="cat-checkbox-holder">
                        <p>'${it.key}' - <g:message code="kb.view.category"/></p>
                        <g:each in="${it.value}">
                            <div class="cat-checkbox">
                                <g:checkBox name="checkbox" value="${it}" checked="false"/>
                                <label for="checkbox">${it}</label>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </g:if>
            </g:each>
            <br/><br/>
            <g:submitButton name="submit" value="create"/>
        </g:form>
        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>