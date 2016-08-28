<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title>Artikel verändern</title>
    </head>

    <content tag="main">
        <g:form controller="KnowledgeBase" action="changeArticle" class="docForm">
            <g:hiddenField name="docTitle" value="${doc.docTitle}"/>
            <label for="docTitleNew">Dokumenttitel</label><br/>
            <g:textField name="docTitleNew" value="${doc.docTitle}"/>
            <br/><br/>
            <label for="docContent">Dokumentinhalt</label><br/>
            <g:textArea name="docContent" value="${doc.docContent}"/>
            <br/><br/>

            <label for="docTags">Schlagworte*</label><br/>
            <g:textArea name="docTags" value="${doc.tags?.toString()?.replaceAll('[\\[\\]]', '')}"/><br/>

            <p><i>*Trennen mit ','</i></p>
            <br/><br/>

            <p>Sprache:</p>
            <g:select name="languageNew" from="${lang}"/>
            <br/><br/>
            <p>Autor:</p>
            <g:select name="authorNew" from="${author}"/>

            <br/><br/>
            <p>Welche Kategorie(n) sollen dem Dokument zugewiesen werden?</p>
            <g:each in="${cats}">
                <g:if test="${it.value != null}">
                    <div class="cat-checkbox-holder">
                        <p>'${it.key}' - Subkategorien</p>
                        <g:each var="cat" in="${it.value.sort{ it }}">
                            <div class="cat-checkbox">
                                <g:checkBox name="checkbox" value="${cat}" checked="${doc.linker?.subcat?.name?.contains(cat)?'true':'false'}"/>
                                <label>${cat}</label>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </g:if>
            </g:each>
            <br/><br/>
            <g:submitButton value="change" name="submit"/>
        </g:form>
        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>