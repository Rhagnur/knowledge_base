<style type="text/css">
@import url("/assets/doctorates/main.css");
</style>
<p><g:message code="doctorate.summary.intro" encodeFor="none"/></p>
<form method="POST">
    <fieldset>
        <legend><g:message code="doctorate.general.title"/></legend>
        <label class="mandatory" for="doctorate_title"><g:message code="doctorate.label.title"/>:</label>
        <input disabled type="text" name="doctorate_title" id="doctorate_title" value="${data?.doct_title?:''}"/>

        <label class="mandatory" for="doctorate_status"><g:message code="typo3.step.summary.labelStatus"/>:</label>
        <input disabled type="text" id="doctorate_status" name="doctorate_status" value="${message(code:"doctorate.status.${data?.doct_status}")}"/>

        <label for="canvas_categories"><g:message code="doctorate.label.scienceCategories"/>:</label>
        <div id="canvas_categories">
            <g:each in="${data.doct_categories}" status="i" var="cat">
                <div id="${cat}" class="canvas_obj">
                    <input type="hidden" name="doctorate_categories" value="${cat}"/>
                    <span>${data.categories.find{ it.catID == cat }.cat}</span>
                </div>
            </g:each>
            <div class="clear" style="clear:both;"></div>
        </div>

        <label for="doctorate_acceptDate"><g:message code="doctorate.label.acceptDate"/>:</label>
        <input disabled type="text" name="doctorate_acceptDate" id="doctorate_acceptDate"  value="${data?.doct_acceptDate?.format("dd.MM.yyyy")?:''}"/>

        <label for="doctorate_endDate"><g:message code="doctorate.label.endDate"/>:</label>
        <input disabled type="text" name="doctorate_endDate" id="doctorate_endDate" value="${data?.doct_endDate?.format("dd.MM.yyyy")?:''}"/>
    </fieldset>
    <fieldset>
        <legend><g:message code="doctorate.promovend.title"/></legend>
        <label class="mandatory" for="doctorand_sn"><g:message code="portal.label.name"/>:</label>
        <input disabled type="text" name="doctorand_sn" id="doctorand_sn" value="${data.doctorand_sn?:''}"/>
        <label class="mandatory" for="doctorand_givenName"><g:message code="portal.label.givenName"/>:</label>
        <input disabled type="text" name="doctorand_givenName" id="doctorand_givenName" value="${data.doctorand_givenName?:''}"/>

        <label class="mandatory"><g:message code="doctorate.label.graduations"/>:</label>
        <div id="canvas_graduations">
            <g:each in="${data.doctorand_graduations}" status="i" var="grad">
                <div id="${grad + i}" class="canvas_obj">
                    <input disabled type="hidden" name="doctorand_graduations" value="${grad}"/>
                    <span><g:message code="doctorate.graduations.${grad}"/></span>
                </div>
            </g:each>
            <div class="clear" style="clear:both;"></div>
        </div>

        <label class="mandatory" for="doctorand_hightestGrad_uniName"><g:message code="doctorate.label.uniNameHightestGrad"/>:</label>
        <input disabled type="text" name="doctorand_hightestGrad_uniName" id="doctorand_hightestGrad_uniName" value="${data.doctorand_hightestGrad_uniName?:''}"/>
        <label class="mandatory" for="doctorand_hightestGrad_date"><g:message code="doctorate.label.dateHightestGrad"/>:</label>
        <input disabled type="text" name="doctorand_hightestGrad_date" id="doctorand_hightestGrad_date" value="${data.doctorand_hightestGrad_date?.format("dd.MM.yyyy")?:''}"/>
    </fieldset>
    <fieldset>
        <legend><g:message code="doctorate.uniAndFinancing.title"/></legend>
        <label class="mandatory" for="doctorate_uniName"><g:message code="doctorate.label.doctorateUniName"/>:</label>
        <input disabled type="text" name="doctorate_uniName" id="doctorate_uniName" value="${data.doct_uniName?:'HTW Berlin'}"/>

        <label class="mandatory" for="doctorate_uniCategory"><g:message code="doctorate.label.doctorateUniCategory"/>:</label>
        <input disabled type="text" name="doctorate_uniCategory" id="doctorate_uniCategory" value="${data.doct_uniCategory?message(code: "doctorate.uniCategories.${data.doct_uniCategory}"):''}"/>

        <label class="mandatory" for="doctorate_uniPlace"><g:message code="doctorate.label.doctorateUniPlace"/>:</label>
        <input disabled type="text" name="doctorate_uniPlace" id="doctorate_uniPlace" value="${data.doct_uniPlace?:'Berlin'}"/>
        <br/>
        <label class="mandatory" for="doctorate_mainFinancing"><g:message code="doctorate.label.mainFinancing"/>:</label>
        <input disabled type="text" name="doctorate_mainFinancing" id="doctorate_mainFinancing" value="${data.doct_mainFinancing?message(code: "doctorate.financing.${data.doct_mainFinancing}"):''}"/>

        <label for="doctorate_secondaryFinancing"><g:message code="doctorate.label.secondaryFinancing"/>:</label>
        <input disabled type="text" name="doctorate_secondaryFinancing" id="doctorate_secondaryFinancing" value="${data.doct_secFinancing?message(code: "doctorate.financing.${data.doct_secFinancing}"):''}"/>
    </fieldset>
    <fieldset>
        <legend><g:message code="doctorate.first_supervisor.title"/></legend>
        <label class="mandatory" for="doctorate_firstSupervisor_sn"><g:message code="portal.label.name"/></label>
        <input disabled type="text" id="doctorate_firstSupervisor_sn" name="doctorate_firstSupervisor_sn" value="${data.doct_firstSV_sn?:''}"/>

        <label class="mandatory" for="doctorate_firstSupervisor_givenName"><g:message code="portal.label.givenName"/></label>
        <input disabled type="text" id="doctorate_firstSupervisor_givenName" name="doctorate_firstSupervisor_givenName" value="${data.doct_firstSV_givenName?:''}"/>

        <label for="doctorate_firstSupervisor_title"><g:message code="page.label.title"/></label>
        <input disabled type="text" id="doctorate_firstSupervisor_title" name="doctorate_firstSupervisor_title" value="${data.doct_firstSV_title?:''}"/>

        <label for="doctorate_firstSupervisor_faculty"><g:message code="doctorate.label.faculty"/></label>
        <input disabled type="text" id="doctorate_firstSupervisor_faculty" name="doctorate_firstSupervisor_faculty" value="${data.doct_firstSV_faculty?:''}"/>

        <label for="doctorate_firstSupervisor_field"><g:message code="doctorate.label.field"/></label>
        <input disabled type="text" id="doctorate_firstSupervisor_field" name="doctorate_firstSupervisor_field" value="${data.doct_firstSV_field?:''}"/>
    </fieldset>
    <fieldset>
        <legend><g:message code="doctorate.second_supervisor.title"/></legend>
        <input disabled style="float: left;" type="checkbox" name="is_extern" id="is_extern" ${data?.doct_secSV_isExtern?'checked':''}/>
        <label for="is_extern"><g:message code="doctorate.label.externalPerson"/></label>
        <div style="clear: both;"></div>

        <div id="doct_secSV_intern_container">
            <div id="intern_canvas">
                <g:if test="${!data.doct_secSV_pvzID}">
                    <uss:infobox>
                        <p id="no_person_choosen" style="margin-bottom: 0;"><g:message code="doctorate.info.noInternalPersonChosen"/></p>
                    </uss:infobox>
                </g:if>
                <g:else>
                    <g:include controller="researchers" action="personInfo" params="[lsfid: data.doct_secSV_person?.lsfId, pvzid: data.doct_secSV_person?.id]"/>
                </g:else>
            </div>
            <g:if test="${data.doct_secSV_pvzID}">
                <g:set var="pvzID" value="${data.doct_secSV_pvzID}"/>
            </g:if>
            <input type="hidden" id="intern_pvz_id" name="intern_pvz_id" value="${pvzID?:''}"/>
        </div>
        <div id="doct_secSV_extern_container" hidden>
            <label class="mandatory" for="doctorate_secondSupervisor_sn"><g:message code="portal.label.name"/></label>
            <input disabled type="text" id="doctorate_secondSupervisor_sn" name="doctorate_secondSupervisor_sn" value="${data.doct_secSV_sn?:''}"/>

            <label class="mandatory" for="doctorate_secondSupervisor_givenName"><g:message code="portal.label.givenName"/></label>
            <input disabled type="text" id="doctorate_secondSupervisor_givenName" name="doctorate_secondSupervisor_givenName" value="${data.doct_secSV_givenName?:''}"/>

            <label for="doctorate_secondSupervisor_title"><g:message code="page.label.title"/></label>
            <input disabled type="text" id="doctorate_secondSupervisor_title" name="doctorate_secondSupervisor_title" value="${data.doct_secSV_title?:''}"/>

            <label for="doctorate_secondSupervisor_faculty"><g:message code="doctorate.label.faculty"/></label>
            <input disabled type="text" id="doctorate_secondSupervisor_faculty" name="doctorate_secondSupervisor_faculty" value="${data.doct_secSV_faculty?:''}"/>

            <label for="doctorate_secondSupervisor_field"><g:message code="doctorate.label.field"/></label>
            <input disabled type="text" id="doctorate_secondSupervisor_field" name="doctorate_secondSupervisor_field" value="${data.doct_secSV_field?:''}"/>
        </div>
    </fieldset>
    <input type="submit" value="${message(code:'portal.label.finish')}" name="_next"/>
    <input type="submit" value="${message(code:'portal.label.prev')}" name="_prev" style="float: left;"/>
</form>