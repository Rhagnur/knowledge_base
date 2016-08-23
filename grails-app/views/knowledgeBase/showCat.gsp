<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title>Kategorie anzeigen</title>
    </head>

    <content tag="main">
        <g:if test="${cat}">
            <h1>Kategorie anzeigen</h1>

            <h2>Eigenschaften</h2>
            <p>Name: ${cat.name}</p>
            <p>Typ: ${cat.getClass().simpleName}</p>
            <g:if test="${cat instanceof berlin.htw.hrz.kb.Subcategory}">
                <g:link controller="KnowledgeBase" action="changeCat" params="[name:cat.name]">Kategoriedetails ändern</g:link><br/><br/>
                <g:link controller="KnowledgeBase" action="deleteCat" params="[name:cat.name]">Kategorie löschen(ohne Bestätigung!)</g:link>
            </g:if>

            <g:if test="${cat.parentCat}">
                <h2>Eltern-Knoten</h2>
                <p>Knoten: <g:link controller="KnowledgeBase" action="showCat" params="[name:cat.parentCat.name]">${cat.parentCat.name}</g:link></p>
            </g:if>


            <g:if test="${cat.subCats}">
                <h2>Subkategorien</h2>
                <p>Anzahl: ${cat.subCats?.size()}</p>
                <p>Liste:
                <ul>
                    <g:each in="${cat.subCats}">
                        <li>
                            <g:link controller="KnowledgeBase" action="showCat" params="[name:it.name]">${it.name}</g:link>
                        </li>
                    </g:each>
                </ul>
                </p>
            </g:if>

            <g:if test="${cat.linker}">
                <h2>Dokumente</h2>
                <p>Anzahl: ${cat.linker?.size()}</p>
                <p>Liste:
                <ul>
                    <g:each in="${cat.linker.doc.sort{ it.docTitle }}">
                        <li>
                            <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">${it.docTitle}</g:link>
                        </li>
                    </g:each>
                </ul>
                </p>
            </g:if>
        </g:if>
        <g:else>
            <section>
                <h2>Hauptkategorien</h2>
                <ul>
                    <g:each in="${mainCats}">
                        <li>
                            <g:link controller="KnowledgeBase" action="showCat" params="[name:it.name]">${it.name}</g:link>
                        </li>
                    </g:each>
                </ul>
            </section>
        </g:else>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>