<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 16.11.16
  Time: 15:58
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.importFiles.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.importFiles.title"/></h1>
        <g:message code="kb.view.importFiles.introText"/>
        <g:uploadForm controller="KnowledgeBase" action="importFiles" class="catForm">
            <label for="username"><g:message code="kb.view.importDocs.label.username"/></label>
            <input type="text" name="username" id="username"/>
            <label for="password"><g:message code="kb.view.importDocs.label.password"/></label>
            <input type="password" name="password" id="password"/>
            <g:message code="kb.view.importFiles.infoText.allLinkFile"/><br/>
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