<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title>Artikel erstellen</title>
    </head>

    <content tag="main">
        <g:form controller="KnowledgeBase" action="createArticle" class="docForm">
            <label for="docTitle">Dokumenttitel</label><br/>
            <g:textField name="docTitle"/>
            <br/><br/>
            <label for="docContent">Dokumentinhalt</label><br/>
            <g:textArea name="docContent"/>
            <br/><br/>

            <label for="docTags">Schlagworte*</label><br/>
            <g:textArea name="docTags"/><br/>

            <p><i>*Trennen mit ','</i></p>
            <br/><br/>

            <p>Sprache:</p>
            <g:select name="language" from="${lang}"/>

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