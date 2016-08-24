<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title>Kategorie ändern</title>
    </head>

    <content tag="main">
        <h1>Kategorie ändern</h1>
        <g:form controller="KnowledgeBase" action="changeCat">
            <g:hiddenField name="name" value="${cat.name}"/>

            <h2>Kategoriedetails</h2>
            <label for="catName">Kategoriename:</label><br/>
            <g:textField name="catName" value="${cat.name}"/><br/><br/><br/>
            <g:if test="${cat instanceof berlin.htw.hrz.kb.Subcategory}">
                <label for="parentCat">Elternknoten:</label><br/>
                <select id="parentCat" name="parentCat">
                    <g:each in="${allCatsByMainCats.sort{ it.key }}">
                        <option value="${it.key}" ${cat.parentCat.name == it.key?'selected':''} >${it.key}</option>
                        <g:each in="${it.value}">
                            <option value="${it}" ${cat.parentCat.name == it?'selected':''}>--- ${it}</option>
                        </g:each>
                    </g:each>
                </select>
            </g:if>
            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="submit">Speichern</g:submitButton>

        </g:form>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>