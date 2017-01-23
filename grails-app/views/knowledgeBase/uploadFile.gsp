<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 16.11.16
  Time: 15:58
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.uploadFile.title"/></title>
    </head>

    <content tag="main">
        <h1><g:message code="kb.view.uploadFile.title"/></h1>
        <g:message code="kb.view.uploadFile.introText"/>
        <g:uploadForm controller="KnowledgeBase" action="uploadFile" class="catForm">
            <g:message code="kb.view.uploadFile.infoText"/><br/>

            <label for="parentPath"><g:message code="kb.view.uploadFile.label.parentpath"/></label>
            <input type="text" name="parentPath" id="username"/>
            <input type="file" id="uploadFile" name="uploadFile" multiple/>
            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="upload"/>

        </g:uploadForm>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>