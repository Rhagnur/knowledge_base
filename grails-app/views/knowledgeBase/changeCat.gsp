<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.changeCat.title"/></title>
    </head>

    <content tag="main">
        <g:form controller="KnowledgeBase" action="changeCat">
            <h1><g:message code="kb.view.changeCat.title"/></h1>
            <g:hiddenField name="name" value="${cat.name}"/>

            <label for="catName"><g:message code="kb.view.createCat.name"/></label><br/>
            <g:textField name="catName" value="${cat.name}"/><br/><br/><br/>
            <g:if test="${cat instanceof berlin.htw.hrz.kb.Subcategory}">
                <label for="parentCat"><g:message code="kb.view.showDoc.parents"/></label><br/>
                <select id="parentCat" name="parentCat">
                    <g:each in="${allCatsByMainCats.sort{ it.key }}">
                        <option value="${it.key}" ${cat.parentCat.name == it.key?'selected':''} >${it.key}</option>
                        <g:each in="${it.value}">
                            <option value="${it}" ${cat.parentCat.name == it?'selected':''}>--- ${it}</option>
                        </g:each>
                    </g:each>
                </select>
            </g:if>
            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="change"/>

        </g:form>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>