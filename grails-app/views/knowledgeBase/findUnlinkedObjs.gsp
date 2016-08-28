<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.findUnlinked.title"/></title>
    </head>

    <content tag="main">
        <section>
            <h1><g:message code="kb.view.findUnlinked.title"/></h1>
            <g:if test="${!subCats && !docs}">
                <p><g:message code="kb.view.findUnlinked.nothingFound"/></p>
            </g:if>
            <g:if test="${subCats}">
                <p><g:message code="kb.view.findUnlinked.foundSubs"/></p>
                <ul>
                    <g:each var="cat" in="${subCats}">
                        <li>${cat.name} <sec:link controller="KnowledgeBase" action="changeCat" params="[name:cat.name]"><g:message code="kb.view.edit"/></sec:link></li>
                    </g:each>
                </ul>
            </g:if>
            <g:if test="${docs}">
                <p><g:message code="kb.view.findUnlinked.foundDocs"/></p>
                <ul>
                    <g:each var="doc" in="${docs}">
                        <li>${doc.docTitle} <sec:link controller="KnowledgeBase" action="changeDoc" params="[docTitle:doc.docTitle]"><g:message code="kb.view.edit"/></sec:link></li>
                    </g:each>
                </ul>
            </g:if>

        </section>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>