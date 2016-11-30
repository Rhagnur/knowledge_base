<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 16.11.16
  Time: 15:58
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.importDocs.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.importDocs.title"/></h1>
        <g:uploadForm controller="KnowledgeBase" action="importDocs" class="catForm">
            <label for="infoFile"><g:message code="kb.view.importDocs.label.urls"/></label><br/>
            <!--textarea name="docURLs" placeholder="URLs durch neue Zeile trennen"></textarea><br/><br/><br/-->
            <input type="file" id="infoFile" name="infoFile"/>
            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="import"/>

        </g:uploadForm>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>