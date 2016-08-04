<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title>Nicht zugewiesene Kategorien anzeigen</title>
    </head>

    <content tag="main">
        <section>
            <h1>Unverlinkte Subkategorien</h1>
            <g:if test="${subCats}">
                <p>Folgende Subkategorien, haben keinen Elternknoten:</p>
                <ul>
                    <g:each var="cat" in="${subCats}">
                        <li>${cat.name} <g:link controller="KnowledgeBase" action="changeCat" params="[name:cat.name]">Kategoriedetails ändern</g:link></li>
                    </g:each>
                </ul>
            </g:if>
            <g:else>
                <p>Keine Subkategorien gefunden</p>
            </g:else>
        </section>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>