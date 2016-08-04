<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="subPage">
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
                    <g:each in="${allCats}">
                        <option value="${it.name}" ${(it==cat.parentCat)?'selected=selected':''}>${it.name}</option>
                    </g:each>
                </select>
            </g:if>
            <br/><br/><br/>
            <p>Kindsknoten:</p>
            <g:each in="${allCatsByMainCats}">
                <g:if test="${it.value != null}">
                    <div class="cat-checkbox-holder">
                        <p>'${it.key}' - Subkategorien</p>
                        <g:each var="myCat" in="${it.value}">
                            <div class="cat-checkbox">
                                <g:checkBox name="checkbox" value="${myCat}" checked="${(cat.subCats?.find{ it.name == myCat})?'true':'false'}"/>
                                <label for="checkbox">${myCat}</label>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </g:if>
            </g:each>


            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="submit">Speichern</g:submitButton>

        </g:form>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>