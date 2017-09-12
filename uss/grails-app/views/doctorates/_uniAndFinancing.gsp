<style type="text/css">
@import url("/assets/doctorates/main.css");
</style>
<form method="POST">
    <fieldset>
        <label class="mandatory" for="doctorate_uniName"><g:message code="doctorate.label.doctorateUniName"/>:</label>
        <input type="text" name="doctorate_uniName" id="doctorate_uniName" value="${data.doct_uniName?:'HTW Berlin'}"/>
        <label class="mandatory" for="doctorate_uniCategory"><g:message code="doctorate.label.doctorateUniCategory"/>:</label>
        <select name="doctorate_uniCategory" id="doctorate_uniCategory">
            <g:each in="${data.uni_categories}">
                <option value="${it}" ${it == data?.doct_uniCategory?'selected':''}><g:message code="doctorate.uniCategories.${it}"/></option>
            </g:each>
        </select>
        <label class="mandatory" for="doctorate_uniPlace"><g:message code="doctorate.label.doctorateUniPlace"/>:</label>
        <input type="text" name="doctorate_uniPlace" id="doctorate_uniPlace" value="${data.doct_uniPlace?:'Berlin'}"/>
        <br/>
        <label class="mandatory" for="doctorate_mainFinancing"><g:message code="doctorate.label.mainFinancing"/>:</label>
        <select name="doctorate_mainFinancing" id="doctorate_mainFinancing">
            <option value="0" ${!data?.doct_mainFinancing?'selected':''}><g:message code="doctorate.financing.default"/></option>
            <g:each in="${data.financings}">
                <option value="${it}" ${it == data?.doct_mainFinancing?'selected':''}><g:message code="doctorate.financing.${it}"/></option>
            </g:each>
        </select>
        <label for="doctorate_secondaryFinancing"><g:message code="doctorate.label.secondaryFinancing"/>:</label>
        <select class="" name="doctorate_secondaryFinancing" id="doctorate_secondaryFinancing">
            <option value="0" ${!data?.doct_secFinancing?'selected':''}><g:message code="doctorate.financing.default"/></option>
            <g:each in="${data.financings}">
                <option value="${it}" ${it == data?.doct_secFinancing?'selected':''}><g:message code="doctorate.financing.${it}"/></option>
            </g:each>
        </select>
    </fieldset>
    <input type="submit" value="${message(code:'portal.label.next')}" name="_next"/>
    <input type="submit" value="${message(code:'portal.label.prev')}" name="_prev" style="float: left;"/>
</form>