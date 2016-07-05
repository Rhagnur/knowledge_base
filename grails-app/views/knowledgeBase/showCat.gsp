<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>
<g:if test="${flash.error}">
    <div class="alert alert-error" style="display: block">${flash.error}</div>
</g:if><br/><br/>
<g:if test="${flash.info}">
    <div class="alert alert-error" style="display: block">${flash.info}</div>
</g:if><br/><br/>


<g:each in="${cats}">
        <g:if test="${it.subCats.size() == 0}">
            <div id="${it.name}" style="border: 1px solid black;">
                <h2>${it.name}</h2>
                <p>Ich habe keine Kinder</p>
                ${it.docs?'Ich habe folgende Dokumente:':''}
                <g:each in="${it.docs}">
                    <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle : it.title]">
                        <p>${it.title}</p>
                    </g:link>
                </g:each>
            </div>
        </g:if>
        <g:else test="${it.subCats.size() > 0}">
            <g:link controller="KnowledgeBase" action="showCat" params="[cat: it.name]">
                <div id="${it.name}" style="border: 1px solid black;">
                    <h2>${it.name}</h2>
                    <p>Ich habe ${it.subCats.size()} Kinder</p>
                    ${it.docs?'Ich habe folgende Dokumente:':''}
                    <g:each in="${it.docs}">
                        <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle : it.title]">
                            <p>${it.title}</p>
                        </g:link>
                    </g:each>
                </div>
            </g:link>
        </g:else>
</g:each>

<g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
</body>
</html>