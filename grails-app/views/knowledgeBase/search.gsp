<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.index.search.headline"/></title>
    </head>
    <content tag="navigation" style="width: 60%!important;">
        <div id="nav-context">
            <section id="subnav">
                <p><g:message code="kb.view.index.search.filterText"/></p>
                <g:form controller="knowledgeBase" action="search">
                    <g:hiddenField name="searchBar" id="searchBar" value="${searchBar}"/>
                    <g:each in="${allCatsByMainCats}">
                        <b>${it.key}</b><br/>
                        <g:each in="${it.value}">
                            <g:checkBox name="checkbox" value="${it}" checked="${!filter?'false':filter.contains(it)?'true':'false'}"/><label>${it}</label><br/>
                        </g:each>
                    </g:each>
                    <g:submitButton name="filter" value="Filtern"/>
                </g:form>
            </section>
        </div>
    </content>

    <content tag="main">
        <h2><g:message code="kb.view.index.search.headline"/></h2>
        <p><g:message code="kb.view.search.term"/> '${searchBar}'</p>
        <p><g:message code="kb.view.search.count"/> ${foundDocs.size()}</p>
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