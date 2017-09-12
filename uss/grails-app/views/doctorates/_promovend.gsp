<p><g:message code="doctorate.promovend.intro" encodeFor="none"/></p>
<form method="POST">
    <fieldset>
        <label class="mandatory" for="doctorand_sn"><g:message code="portal.label.name"/>:</label>
        <input type="text" name="doctorand_sn" id="doctorand_sn" value="${data.doctorand_sn?:''}"/>
        <label class="mandatory" for="doctorand_givenName"><g:message code="portal.label.givenName"/>:</label>
        <input type="text" name="doctorand_givenName" id="doctorand_givenName" value="${data.doctorand_givenName?:''}"/>

        <label class="mandatory" for="doct_graduations_select"><g:message code="doctorate.label.graduations"/>:</label>
        <div id="canvas_graduations">
            <g:each in="${data.doctorand_graduations}" status="i" var="grad">
                <div id="${grad + i}" class="canvas_obj">
                    <input type="hidden" name="doctorand_graduations" value="${grad}"/>
                    <span><g:message code="doctorate.graduations.${grad}"/></span>
                    <a onclick="removeCanvasObj('${grad + i}')"><i class='icon-sym-false2'></i></a>
                </div>
            </g:each>
            <div class="clear" style="clear:both;"></div>
        </div>
        <select name="doct_graduations_select" id="doct_graduations_select">
            <option value="0"><g:message code="doctorate.graduations.default"/></option>
            <g:each in="${data.graduations}">
                <option value="${it}"><g:message code="doctorate.graduations.${it}"/> </option>
            </g:each>
        </select>

        <label class="mandatory" for="doctorand_hightestGrad_uniName"><g:message code="doctorate.label.uniNameHightestGrad"/>:</label>
        <input type="text" name="doctorand_hightestGrad_uniName" id="doctorand_hightestGrad_uniName" value="${data.doctorand_hightestGrad_uniName?:''}"/>
        <label class="mandatory" for="doctorand_hightestGrad_date"><g:message code="doctorate.label.dateHightestGrad"/>:</label>
        <input class="date_field" placeholder="dd.MM.yyyy" type="text" name="doctorand_hightestGrad_date" id="doctorand_hightestGrad_date" value="${data.doctorand_hightestGrad_date?.format("dd.MM.yyyy")?:''}"/>
    </fieldset>
    <input type="submit" value="${message(code:'portal.label.next')}" name="_next"/>
    <input type="submit" value="${message(code:'portal.label.prev')}" name="_prev" style="float: left;"/>
</form>