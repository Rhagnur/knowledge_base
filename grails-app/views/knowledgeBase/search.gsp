<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="kb.view.index.search.headline"/></title>
    </head>

    <content tag="main">
        <h2><g:message code="kb.view.index.search.headline"/></h2>
        <p><g:message code="kb.view.search.infoText"/></p>
        <g:if test="${foundDocs}">
            <table id="search-results-table">
                <thead>
                    <tr>
                        <th data-sort="string"><g:message code="kb.view.search.thTitle"/></th>
                        <th data-sort="string"><g:message code="kb.view.search.thType"/></th>
                        <th data-sort="int"><g:message code="kb.view.search.thCount"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${foundDocs}">
                        <tr>
                            <td><g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">${it.docTitle}</g:link></td>
                            <td>
                                <p>${it.getClass().simpleName}</p>
                            </td>
                            <td>${it.viewCount}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>


        </g:if>
        <g:else>
            <p><g:message code="kb.view.index.doi.noDocsFound"/></p>
        </g:else>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>