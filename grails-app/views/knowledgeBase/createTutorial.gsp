<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.createTutorial.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.createTutorial.title"/></h1>
        <g:form controller="KnowledgeBase" action="createTutorial" class="docForm">
            <label for="docTitle"><g:message code="kb.view.createArticle.docTitle"/></label><br/>
            <g:textField name="docTitle"/>
            <br/><br/><br/>

            <div class="stepHolder">
                <input type="button" value="-" onclick="deleteStep()"/>
                <input type="button" value="+" onclick="addOneStep()"/>
                <label for="stepTitle_1"><g:message code="kb.view.createTutorial.stepTitle"/> 1</label><br/>
                <g:textField name="stepTitle_1"/>
                <br/>
                <label for="stepText_1"><g:message code="kb.view.createTutorial.stepText"/> 1</label><br/>
                <g:textArea name="stepText_1"/>
                <br/>
                <label for="stepLink_1"><g:message code="kb.view.createTutorial.stepLink"/> 1</label><br/>
                <g:textField name="stepLink_1"/>
                <div class="clear"></div>
            </div>

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