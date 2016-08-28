<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title>Nicht zugewiesene Kategorien anzeigen</title>
    </head>

    <content tag="main">
        <section>
            <h1>Unverlinkte Elemente</h1>
            <g:if test="${!subCats && !docs}">
                <p>Keine Elemente gefunden</p>
            </g:if>
            <g:if test="${subCats}">
                <p>Folgende Subkategorien haben keinen Elternknoten:</p>
                <ul>
                    <g:each var="cat" in="${subCats}">
                        <li>${cat.name} <sec:link controller="KnowledgeBase" action="changeCat" params="[name:cat.name]">bearbeiten</sec:link></li>
                    </g:each>
                </ul>
            </g:if>
            <g:if test="${docs}">
                <p>Folgende Dokumente besitzen keine Zuweisung:</p>
                <ul>
                    <g:each var="doc" in="${docs}">
                        <li>${doc.docTitle} <sec:link controller="KnowledgeBase" action="changeDoc" params="[docTitle:doc.docTitle]">bearbeiten</sec:link></li>
                    </g:each>
                </ul>
            </g:if>

        </section>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>