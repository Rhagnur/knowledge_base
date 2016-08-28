<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title><g:message code="kb.view.showCat.title"/></title>
    </head>

    <content tag="main">
        <g:if test="${cat}">
            <h1><g:message code="kb.view.showCat.title"/></h1>

            <h2><g:message code="kb.view.details"/></h2>
            <p><g:message code="kb.view.createCat.name"/> ${cat.name}</p>
            <p><g:message code="kb.view.type"/> ${cat.getClass().simpleName}</p>
            <g:if test="${cat instanceof berlin.htw.hrz.kb.Subcategory}">
                <sec:link controller="KnowledgeBase" action="changeCat" params="[name:cat.name]"><g:message code="kb.view.edit"/></sec:link><br/>
                <sec:link controller="KnowledgeBase" action="deleteCat" params="[name:cat.name]"><g:message code="kb.view.delete"/></sec:link><br/>
            </g:if>
            <sec:link controller="KnowledgeBase" action="createCat" params="[originName:cat.name]"><g:message code="kb.view.showCat.newCat"/></sec:link><br/>

            <g:if test="${cat.parentCat}">
                <h2><g:message code="kb.view.createArticle.docParents"/></h2>
                <p><g:message code="kb.view.node"/> <g:link controller="KnowledgeBase" action="showCat" params="[name:cat.parentCat.name]">${cat.parentCat.name}</g:link></p>
            </g:if>


            <g:if test="${cat.subCats}">
                <h2><g:message code="kb.view.showCat.subCats"/></h2>
                <p><g:message code="kb.view.amount"/> ${cat.subCats?.size()}</p>
                <p><g:message code="kb.view.list"/>
                <ul>
                    <g:each in="${cat.subCats}">
                        <li>
                            <g:link controller="KnowledgeBase" action="showCat" params="[name:it.name]">${it.name}</g:link>
                        </li>
                    </g:each>
                </ul>
                </p>
            </g:if>

            <g:if test="${cat.linker}">
                <h2><g:message code="kb.view.showCat.documents"/></h2>
                <p><g:message code="kb.view.amount"/> ${cat.linker?.size()}</p>
                <p><g:message code="kb.view.list"/>
                <ul>
                    <g:each in="${cat.linker?.doc?.findAll{ it }?.sort{ it?.docTitle }}">
                        <li>
                            <g:link controller="KnowledgeBase" action="showDoc" params="[docTitle: it.docTitle]">${it.docTitle}</g:link>
                        </li>
                    </g:each>
                </ul>
                </p>
            </g:if>
        </g:if>
        <g:else>
            <section>
                <h2><g:message code="kb.view.showCat.mainCats"/></h2>
                <ul>
                    <g:each in="${mainCats}">
                        <li>
                            <g:link controller="KnowledgeBase" action="showCat" params="[name:it.name]">${it.name}</g:link>
                        </li>
                    </g:each>
                </ul>
            </section>
        </g:else>
        <div class="clear"></div>

        <g:link controller="KnowledgeBase" action="index"><g:message code="kb.view.backToHome"/></g:link>
    </content>
</g:applyLayout>