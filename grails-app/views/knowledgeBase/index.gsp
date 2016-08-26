<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>
<g:applyLayout name="main">
    <head>
        <title>Knowledge Base</title>
    </head>

    <content tag="main">
        <section id="search">
            <h2><g:message code="kb.view.index.search.headline"/></h2>
            <p><g:message code="kb.view.index.search.introText"/></p>
            <g:form controller="knowledgeBase" action="search" method="POST">
                <g:textField name="searchBar" id="searchBar" placeholder="${message(code: 'kb.view.index.search.placeholder')}"/>
                <g:submitButton name="search" id="searchButton" value="${message(code: 'kb.view.index.search.buttonText')}"/>
            </g:form>
        </section>
        <section id="document-of-interest">
            <h2><g:message code="kb.view.index.doi.headline"/></h2>
            <g:if test="${otherDocs && otherDocs.size() == 0}">
                <p><g:message code="kb.view.index.doi.noDocsFound"/></p>
            </g:if>
            <g:elseif test="${otherDocs && otherDocs.size() == 1}">
                <p><g:message code="kb.view.index.doi.docFound"/></p>
            </g:elseif>
            <g:elseif test="${otherDocs && otherDocs.size() > 1}">
                <p><g:message code="kb.view.index.doi.docsFound"/></p>
            </g:elseif>
            <g:else>
                <p><g:message code="kb.error.somethingWentWrong"/></p>
            </g:else>

            <g:each in="${otherDocs}">
                <div class="docDiv">
                    <g:if test="${it.key =='popular'}">
                        <g:set var="popular" value="${true}"/>
                    </g:if>
                    <g:else>
                        <g:set var="popular" value="${false}"/>
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
            <sec:ifAnyGranted roles="ROLE_GP-STAFF,ROLE_GP-PROF">
                <g:form controller="KnowledgeBase" action="createTutorial" method="POST">
                    <g:submitButton name="createTut" value="Neue Anleitung erstellen"/>
                </g:form>
                <g:form controller="KnowledgeBase" action="createFaq" method="POST">
                    <g:submitButton name="createFaq" value="Neues Faq erstellen"/>
                </g:form>
                <g:form controller="KnowledgeBase" action="createArticle" method="POST">
                    <g:submitButton name="createFaq" value="Neuen Artikel erstellen"/>
                </g:form>
                <br/><br/><br/>
                <g:form controller="KnowledgeBase" action="createDoc" method="POST">
                    <g:submitButton name="createTut" value="Neue Anleitung erstellen(old)"/>
                </g:form>
                <g:form controller="KnowledgeBase" action="createDoc" method="POST">
                    <g:submitButton name="createFaq" value="Neues FAQ erstellen(old)"/>
                </g:form>
                <g:form controller="KnowledgeBase" action="createCat">
                    <g:submitButton name="create" value="Neue Subkategorie anlegen"/>
                </g:form>

                <g:form controller="KnowledgeBase" action="findUnlinkedObjs">
                    <g:submitButton name="find" value="Unverlinkte Elemente finden"/>
                </g:form>
                <br/>
            </sec:ifAnyGranted>
            <g:form controller="KnowledgeBase" action="showCat">
                <g:submitButton name="show" value="Kategorie ansehen"/>
            </g:form>


        </section>
    </content>
</g:applyLayout>