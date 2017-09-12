<style type="text/css">
@import url("/assets/doctorates/main.css");
</style>
<p><g:message code="doctorate.secondSupervisor.intro" encodeFor="none"/></p>
<form method="POST">
    <fieldset>
        <input style="float: left;" type="checkbox" name="is_extern" id="is_extern" ${data?.doct_secSV_isExtern?'checked':''}/>
        <label for="is_extern"><g:message code="doctorate.label.externalPerson"/></label>
        <div style="clear: both;"></div>
        <div id="intern">
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
            <input type="text" id="intern_type_ahead" name="intern_type_ahead" class="typeahead" placeholder="${message(code:'doctorate.label.findPersonTypeAhead')}"/>
            <g:if test="${data.doct_secSV_pvzID}">
                <g:set var="pvzID" value="${data.doct_secSV_pvzID}"/>
            </g:if>
            <input type="hidden" id="intern_pvz_id" name="intern_pvz_id" value="${pvzID?:''}"/>
        </div>
        <div id="extern" hidden>
            <label class="mandatory" for="doctorate_secondSupervisor_sn"><g:message code="portal.label.name"/></label>
            <input type="text" id="doctorate_secondSupervisor_sn" name="doctorate_secondSupervisor_sn" value="${data.doct_secSV_sn?:''}"/>

            <label class="mandatory" for="doctorate_secondSupervisor_givenName"><g:message code="portal.label.givenName"/></label>
            <input type="text" id="doctorate_secondSupervisor_givenName" name="doctorate_secondSupervisor_givenName" value="${data.doct_secSV_givenName?:''}"/>

            <label for="doctorate_secondSupervisor_title"><g:message code="page.label.title"/></label>
            <input type="text" id="doctorate_secondSupervisor_title" name="doctorate_secondSupervisor_title" value="${data.doct_secSV_title?:''}"/>

            <label for="doctorate_secondSupervisor_faculty"><g:message code="doctorate.label.faculty"/></label>
            <input type="text" id="doctorate_secondSupervisor_faculty" name="doctorate_secondSupervisor_faculty" value="${data.doct_secSV_faculty?:''}"/>

            <label for="doctorate_secondSupervisor_field"><g:message code="doctorate.label.field"/></label>
            <input type="text" id="doctorate_secondSupervisor_field" name="doctorate_secondSupervisor_field" value="${data.doct_secSV_field?:''}"/>
        </div>        
    </fieldset>
    <input type="submit" value="${message(code:'portal.label.next')}" name="_next"/>
    <input type="submit" value="${message(code:'portal.label.prev')}" name="_prev" style="float: left;"/>
</form>
<div style="height: 12em;"></div>