<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>
<g:applyLayout name="page">
    <head>
        <title>Knowledge Base</title>
    </head>

    <content tag="main">
        <div id="functions">
            <h2>Debug-/Test-Funktionen</h2>
            <g:form controller="KnowledgeBase" action="testingThings">
                <g:submitButton name="submit" value="call testingThings()"/>
            </g:form>

            <g:form controller="KnowledgeBase" action="createDoc">
                <g:submitButton name="create" value="Neues Doc erstellen"/>
            </g:form>

            <g:form controller="KnowledgeBase" action="showDoc">
                <g:submitButton name="show" value="Doc ansehen"/>
            </g:form>

            <g:form controller="KnowledgeBase" action="showCat">
                <g:submitButton name="show" value="Cat ansehen"/>
            </g:form>
        </div>
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
    </content>
</g:applyLayout>