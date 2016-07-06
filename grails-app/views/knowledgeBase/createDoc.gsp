<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="page">
<head>
    <title>Dokument erstellen</title>
</head>

<content tag="main">
<g:form controller="KnowledgeBase" action="createDoc">
    <label for="docTitle">Doc Title</label><br/>
    <g:textField name="docTitle"/>
    <br/><br/>
    <label for="docTitle">Doc Content</label><br/>
    <g:textField name="docContent"/>
    <br/><br/>
    <label for="docTitle">Doc hiddenTags*</label><br/>
    <g:textArea name="docTags"/><br/>
    <p><i>*Trennen mit ','</i></p>
    <br/><br/>
    <p>Welche Kategorie(n) sollen dem Dokument zugewiesen werden?</p>
    <g:each in="${cats}">
        <label for="checkbox">${it}</label>
        <g:checkBox name="checkbox" value="${it}" checked="false"/>
        <br/>
    </g:each>
    <br/><br/>
    <g:submitButton name="submit"/>
</g:form>
<g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
</content>
</g:applyLayout>