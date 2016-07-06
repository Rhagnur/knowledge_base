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
        <div id="os">

        </div>
        <div id="theme">

        </div>
        <div id="gruppe">

        </div>
        <div id="lang">

        </div>
    </content>
</g:applyLayout>