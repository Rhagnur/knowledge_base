<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>

<g:applyLayout name="main">
    <head>
        <title>Kategorie anlegen</title>
    </head>

    <content tag="main">
        <h1>Kategorie anlegen</h1>
        <g:form controller="KnowledgeBase" action="createCat">
            <h2>Kategoriedetails</h2>
            <label for="catName">Kategoriename:</label><br/>
            <g:textField name="catName"/><br/><br/><br/>

            <label for="parentCat">Elternknoten:</label><br/>
            <select id="parentCat" name="parentCat">
                <g:each in="${allCatsByMainCats.sort { it.key }}">
                    <option value="${it.key}" }>${it.key}</option>
                    <g:each in="${it.value}">
                        <option value="${it}" }>--- ${it}</option>
                    </g:each>
                </g:each>
            </select>

            <br/><br/><br/>
            <p>Kindsknoten:</p>
            <p><i>Es ist auch möglich, keinen Kindsknoten auszuwählen!</i></p>
            <g:each in="${allCatsByMainCats}">
                <g:if test="${it.value != null}">
                    <div class="cat-checkbox-holder">
                        <p>'${it.key}' - Subkategorien</p>
                        <g:each var="myCat" in="${it.value}">
                            <div class="cat-checkbox">
                                <g:checkBox name="checkbox" value="${myCat}" checked="false"/>
                                <label for="checkbox">${myCat}</label>
                            </div>
                        </g:each>
                        <div class="clear"></div>
                    </div>
                </g:if>
            </g:each>


            <div class="clear"></div>
            <br/><br/>
            <g:submitButton name="submit" value="Anlegen"/>

        </g:form>
        <div class="clear"></div>
        <g:link controller="KnowledgeBase" action="index">Zur Startseite</g:link>
    </content>
</g:applyLayout>