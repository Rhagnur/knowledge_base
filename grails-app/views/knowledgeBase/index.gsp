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
            <g:form controller="knowledgeBase" action="search" method="POST">
                <g:textField name="searchBar" id="searchBar" placeholder="Suchbegriff eingeben, keine Eingabe um alle Dokumente aufzulisten"/>
                <g:submitButton name="search" id="searchButton" value="Suchen"></g:submitButton>
            </g:form>
        </section>
        <section id="document-of-interest">
            <h2>Dokumente von Interesse</h2>
            <g:if test="${otherDocs && otherDocs.size() == 0}">
                <p>Es wurden keine relevanten Dokumente gefunden!</p>
            </g:if>
            <g:elseif test="${otherDocs && otherDocs.size() == 1}">
                <p>Es wurden Dokumente in der folgenden Rubrik gefunden...</p>
            </g:elseif>
            <g:elseif test="${otherDocs && otherDocs.size() > 1}">
                <p>Es wurden Dokumente in den folgenden Rubriken gefunden...</p>
            </g:elseif>
            <g:else>
                <p>Es ist ein unerwarteter Fehler aufgetreten!</p>
            </g:else>

            <g:each in="${otherDocs}">
                <div class="docDiv">
                    <g:if test="${it.key =='popular'}">
                        <g:set var="popular" value="${true}"></g:set>
                    </g:if>
                    <g:else>
                        <g:set var="popular" value="${false}"></g:set>
                    </g:else>
                    <p>...'${it.key}'</p>
                    <g:each in="${it.value}">
                        <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                            <p>${it.docTitle} <g:if test="${popular}">(${it.viewCount})</g:if></p>
                        </g:link>
                    </g:each>
                </div>
            </g:each>
            <div class="clear"></div>
        </section>
        <section id="debug">
            <h2>Debug-/Test-Funktionen</h2>
            <!--sec:ifAllGranted roles="ROLE_GP-STAFF"-->
                <g:form controller="KnowledgeBase" action="createDoc" method="POST">
                    <g:submitButton name="createTut" value="Neue Anleitung erstellen"/>
                </g:form>
                <g:form controller="KnowledgeBase" action="createDoc" method="POST">
                    <g:submitButton name="createFaq" value="Neues FAQ erstellen"/>
                </g:form>
            <!--/sec:ifAllGranted-->

            <g:form controller="KnowledgeBase" action="showCat">
                <g:submitButton name="show" value="Kategorie ansehen"/>
            </g:form>

            <g:form controller="KnowledgeBase" action="navCat">
                <g:submitButton name="nav" value="Durch Kategorien navigieren"/>
            </g:form>

            <g:form controller="KnowledgeBase" action="findUnlinkedSubCats">
                <g:submitButton name="nav" value="Unverlinkte Subkategorien finden"/>
            </g:form>
        </section>
    </content>
</g:applyLayout>