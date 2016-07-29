<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
    <head>
        <title>Dokument erstellen</title>
    </head>

    <content tag="main">
        <g:form controller="KnowledgeBase" action="createDoc">
            <g:if test="${docType == 'tutorial'}">
                <g:hiddenField name="tutorial" value="create"/>
                <label for="docTitle">Dokumenttitel</label><br/>
                <g:textField name="docTitle"/>
                <br/><br/><br/>

                <label for="stepTitle_1">Schritt 1 Titel</label><br/>
                <g:textField name="stepTitle_1"/>
                <br/>
                <label for="stepText_1">Schritt 1 Text</label><br/>
                <g:textArea name="stepText_1"/>
                <br/>
                <label for="stepLink_1">Schritt 1 Link</label><br/>
                <g:textField name="stepLink_1"/>
                <br/><br/>
                <label for="stepTitle_2">Schritt 2 Titel</label><br/>
                <g:textField name="stepTitle_2"/>
                <br/>
                <label for="stepText_2">Schritt 2 Text</label><br/>
                <g:textArea name="stepText_2"/>
                <br/>
                <label for="stepLink_2">Schritt 2 Link</label><br/>
                <g:textField name="stepLink_2"/>
                <br/><br/>
            </g:if>
            <g:elseif test="${docType == 'faq'}">
                <g:hiddenField name="faq" value="create"/>
                <label for="question">Frage</label><br/>
                <g:textField name="question"/>
                <br/><br/>
                <label for="answer">Antwort</label><br/>
                <g:textArea name="answer"/>
                <br/><br/>
            </g:elseif>

            <label for="docTitle">Schlagworte*</label><br/>
            <g:textArea name="docTags"/><br/>

            <p><i>*Trennen mit ','</i></p>
            <br/><br/>

            <p>Welche Kategorie(n) sollen dem Dokument zugewiesen werden?</p>
            <g:each in="${cats}">
                <g:if test="${it.value != null}">
                    <div class="cat-checkbox-holder">
                        <p>'${it.key}' - Subkategorien</p>
                        <g:each in="${it.value}">
                            <div class="cat-checkbox">
                                <g:checkBox name="checkbox" value="${it}" checked="false"/>
                                <label for="checkbox">${it}</label>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </g:if>
            </g:each>
            <br/><br/>
            <g:submitButton name="submit"/>
        </g:form>
        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>