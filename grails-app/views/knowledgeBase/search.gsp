<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title>Suche</title>
    </head>

    <content tag="main">
        <g:if test="${foundDocs}">
            <table id="search-results">
                <tr><td>Titel</td><td>Typ</td><td>Views</td></tr>
                <g:each in="${foundDocs}">
                    <tr>
                        <td><g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">${it.docTitle}</g:link></td>
                        <td>
                            <g:if test="${it.steps}">
                                <p>Anleitung</p>
                            </g:if>
                            <g:else>
                                <p>FAQ</p>
                            </g:else>
                        </td>
                        <td>${it.viewCount}</td>

                    </tr>


            </g:each>
            </table>


        </g:if>
        <g:else>
            <p>Es wurden leider keine Dokumente gefunden!</p>
        </g:else>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>