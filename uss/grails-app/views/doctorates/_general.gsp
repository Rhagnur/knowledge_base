<p><g:message code="doctorate.general.intro" encodeFor="none"/></p>
<form method="POST">
    <fieldset>
        <label class="mandatory" for="doctorate_title"><g:message code="doctorate.label.title"/>:</label>
        <input style="margin-bottom: 1em;" type="text" name="doctorate_title" id="doctorate_title" value="${data?.doct_title?:''}"/>

        <label class="mandatory" for="doctorate_status"><g:message code="typo3.step.summary.labelStatus"/>:</label>
        <select required id="doctorate_status" name="doctorate_status">
            <g:each in="${data.stati}">
                <option value="${it}" ${it==data?.doct_status?'selected':''}><g:message code="doctorate.status.${it}"/></option>
            </g:each>
        </select>

        <label for="doctorate_categories_select"><g:message code="doctorate.label.scienceCategories"/>:</label>
        <div id="canvas_categories">
            <g:each in="${data.doct_categories}" status="i" var="cat">
                <div id="${cat}" class="canvas_obj">
                    <input type="hidden" name="doctorate_categories" value="${cat}"/>
                    <span>${data.categories.find{ it.catID == cat }.cat}</span>
                    <a onclick="removeCanvasObj('${cat}')"><i class='icon-sym-false2'></i></a>
                </div>
            </g:each>
            <div class="clear" style="clear:both;"></div>
        </div>
        <select id="doctorate_categories_select" name="doctorate_categories_select">
            <option value="0"><g:message code="doctorate.category.default"/></option>
            <g:each in="${data.categories}" var="cat">
                <option value="${cat.catID}" class="${data.doct_categories.find{ it == cat.catID }?'usedCat':''}">${cat.cat}</option>
            </g:each>
        </select>

        <label for="doctorate_acceptDate"><g:message code="doctorate.label.acceptDate"/>:</label>
        <input class="date_field" placeholder="dd.MM.yyyy" type="text" name="doctorate_acceptDate" id="doctorate_acceptDate"  value="${data?.doct_acceptDate?.format("dd.MM.yyyy")?:''}"/>

        <label for="doctorate_endDate"><g:message code="doctorate.label.endDate"/>:</label>
        <input class="date_field" placeholder="dd.MM.yyyy" type="text" name="doctorate_endDate" id="doctorate_endDate" value="${data?.doct_endDate?.format("dd.MM.yyyy")?:''}"/>
    </fieldset>
    <input type="submit" value="${message(code:'portal.label.next')}" name="_next"/>
</form>