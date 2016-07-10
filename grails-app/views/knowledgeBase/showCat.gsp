<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="page">
    <head>
        <title>Kategorien anzeigen</title>
    </head>

    <content tag="main">
        <g:each in="${cats}">
            <g:if test="${it.subCats.size() == 0}">
                <div id="${it.name}" class="catDiv">
                    <h2>${it.name}</h2>
                    ${it.docs ? 'Ich habe folgende Dokumente:' : ''}
                    <g:each in="${it.docs}">
                        <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                            <p>${it.docTitle}</p>
                        </g:link>
                    </g:each>
                </div>
            </g:if>
            <g:else test="${it.subCats.size() > 0}">
                <g:link controller="KnowledgeBase" action="showCat" params="[cat: it.name]" class="catLink">
                    <div id="${it.name}" class="catDiv">
                        <h2>${it.name}</h2>
                        <p>Ich habe ${it.subCats.size()} Kinder</p>
                        ${it.docs ? 'Ich habe folgende Dokumente:' : ''}
                        <g:each in="${it.docs}">
                            <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">
                                <p>${it.docTitle}</p>
                            </g:link>
                        </g:each>
                    </div>
                </g:link>
            </g:else>
        </g:each>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>