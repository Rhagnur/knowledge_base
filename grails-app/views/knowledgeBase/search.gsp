<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title>Suche</title>
    </head>

    <content tag="main">
        <h2>Suche</h2>
        <p>Die Suche nach '${searchTerm}' brachte folgende Ergebnisse:</p>
        <p>Hinweis: Klicken Sie auf den Tabellenkopf um die Zeilen zu sortieren.</p>
        <g:if test="${foundDocs}">
            <table id="search-results-table">
                <thead>
                    <tr>
                        <th data-sort="string">Titel</th>
                        <th data-sort="string">Typ</th>
                        <th data-sort="int">Views</th>
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
            <p>Es wurden leider keine Dokumente gefunden!</p>
        </g:else>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>