<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>
<g:applyLayout name="subPage">
    <head>
        <title>Knowledge Base</title>
    </head>

    <content tag="main">
        <section id="search">
            <h2>Knowledge Base durchsuchen</h2>
            <p>Bla bla bla, ich bin ein kleiner Begrüßgungs- und Hilfetext</p>
            <g:form controller="knowledgeBase" action="search">
                <g:textField name="searchBar" id="searchBar" placeholder="Suchbegriff eingeben"/>
                <g:submitButton name="search" id="searchButton" value="Suchen"></g:submitButton>
            </g:form>
        </section>
        <section id="functions">
            <h2>Debug-/Test-Funktionen</h2>
            <g:form controller="KnowledgeBase" action="testingThings">
                <g:submitButton name="submit" value="call testingThings()"/>
            </g:form>

            <sec:ifAllGranted roles="ROLE_GP-STAFF">
                <g:form controller="KnowledgeBase" action="createDoc" method="POST">
                    <g:submitButton name="createTut" value="Neue Anleitung erstellen"/>
                </g:form>
                <g:form controller="KnowledgeBase" action="createDoc" method="POST">
                    <g:submitButton name="createFaq" value="Neues FAQ erstellen"/>
                </g:form>
            </sec:ifAllGranted>

            <g:form controller="KnowledgeBase" action="showDoc">
                <g:submitButton name="show" value="Doc ansehen"/>
            </g:form>

            <g:form controller="KnowledgeBase" action="showCat">
                <g:submitButton name="show" value="Cat ansehen"/>
            </g:form>
            <br/>

            <h2>Test für weitere Dokumente</h2>

            <p>Gefundene Dokumente in...</p>

            <g:each in="${otherDocs}">
                <div class="docDiv">
                    <p>...'${it.key}'</p>
                    <g:each in="${it.value}">
                        <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                            <p>${it.docTitle}</p>
                        </g:link>
                    </g:each>
                </div>

            </g:each>
        </section>
    </content>
</g:applyLayout>