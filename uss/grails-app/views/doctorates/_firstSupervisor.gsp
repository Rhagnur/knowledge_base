<style type="text/css">
@import url("/assets/doctorates/main.css");
</style>
<form method="POST">
    <fieldset>
        <label class="mandatory" for="doctorate_firstSupervisor_sn"><g:message code="portal.label.name"/></label>
        <input type="text" id="doctorate_firstSupervisor_sn" name="doctorate_firstSupervisor_sn" value="${data.doct_firstSV_sn?:''}"/>

        <label class="mandatory" for="doctorate_firstSupervisor_givenName"><g:message code="portal.label.givenName"/></label>
        <input type="text" id="doctorate_firstSupervisor_givenName" name="doctorate_firstSupervisor_givenName" value="${data.doct_firstSV_givenName?:''}"/>

        <label for="doctorate_firstSupervisor_title"><g:message code="page.label.title"/></label>
        <input type="text" id="doctorate_firstSupervisor_title" name="doctorate_firstSupervisor_title" value="${data.doct_firstSV_title?:''}"/>

        <label for="doctorate_firstSupervisor_faculty"><g:message code="doctorate.label.faculty"/></label>
        <input type="text" id="doctorate_firstSupervisor_faculty" name="doctorate_firstSupervisor_faculty" value="${data.doct_firstSV_faculty?:''}"/>

        <label for="doctorate_firstSupervisor_field"><g:message code="doctorate.label.field"/></label>
        <input type="text" id="doctorate_firstSupervisor_field" name="doctorate_firstSupervisor_field" value="${data.doct_firstSV_field?:''}"/>
    </fieldset>
    <input type="submit" value="${message(code:'portal.label.next')}" name="_next"/>
    <input type="submit" value="${message(code:'portal.label.prev')}" name="_prev" style="float: left;"/>
</form>