<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.createCat.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.createCat.title"/></h1>
        <g:form controller="KnowledgeBase" action="createCat">
            <label for="catName"><g:message code="kb.view.createCat.name"/></label><br/>
            <g:textField name="catName"/><br/><br/><br/>

            <label for="parentCat"><g:message code="kb.view.createArticle.docParents"/></label><br/>
            <select id="parentCat" name="parentCat">
                <g:each in="${allCatsByMainCats.sort { it.key }}">
                    <option value="${it.key}" ${origin == it.key?'selected':''}>${it.key}</option>
                    <g:each in="${it.value}">
                        <option value="${it}" ${origin == it?'selected':''}>--- ${it}</option>
                    </g:each>
                </g:each>
            </select>
            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="create"/>

        </g:form>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>