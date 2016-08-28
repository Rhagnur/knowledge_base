<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.createFaq.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.createFaq.title"/></h1>
        <g:form controller="KnowledgeBase" action="createFaq" class="docForm">
            <label for="question"><g:message code="kb.view.createFaq.question"/></label><br/>
            <g:textField name="question"/>
            <br/><br/>
            <label for="answer"><g:message code="kb.view.createFaq.answer"/></label><br/>
            <g:textArea name="answer"/>
            <br/><br/>

            <label for="docTags"><g:message code="kb.view.createArticle.docTags"/>*</label><br/>
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