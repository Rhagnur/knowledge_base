<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.uniAndFinancing.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article>
            <section id="main_1">
                <g:uploadForm controller="doctorates" action="editUniAndFinance">
                    <fieldset>
                        <input type="hidden" name="id" id="id" value="${doct?.id}"/>

                        <label class="mandatory" for="uni_name"><g:message code="doctorate.label.doctorateUniName"/>:</label>
                        <input type="text" name="uni_name" id="uni_name" value="${doct.university_name?:''}"/>

                        <label class="mandatory" for="uni_category"><g:message code="doctorate.label.doctorateUniCategory"/>:</label>
                        <select name="uni_category" id="uni_category">
                            <g:each in="${uniCategories}">
                                <option value="${it}" ${it == doct.university_category?'selected':''}><g:message code="doctorate.uniCategories.${it}"/></option>
                            </g:each>
                        </select>

                        <label class="mandatory" for="uni_location"><g:message code="doctorate.label.doctorateUniPlace"/>:</label>
                        <input type="text" name="uni_location" id="uni_location" value="${doct.university_place?:''}"/>

                        <br/>
                        <label class="mandatory" for="main_financing"><g:message code="doctorate.label.mainFinancing"/>:</label>
                        <select name="main_financing" id="main_financing">
                            <option value="0" ${!doct.main_financing?'selected':''}><g:message code="doctorate.financing.default"/></option>
                            <g:each in="${financeTypes}">
                                <option value="${it}" ${it == doct.main_financing?'selected':''}><g:message code="doctorate.financing.${it}"/></option>
                            </g:each>
                        </select>
                        <label for="second_financing"><g:message code="doctorate.label.secondaryFinancing"/>:</label>
                        <select class="" name="second_financing" id="second_financing">
                            <option value="0" ${!doct.second_financing?'selected':''}><g:message code="doctorate.financing.default"/></option>
                            <g:each in="${financeTypes}">
                                <option value="${it}" ${it == doct.second_financing?'selected':''}><g:message code="doctorate.financing.${it}"/></option>
                            </g:each>
                        </select>

                        <br/>
                        <g:if test="${attachment}">
                            <label><g:message code="doctorate.label.cooperationAgreementFile"/>:</label>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: attachment.attachCat]">${attachment.fileName} (${attachment.prettySize} / ${attachment.created.format('dd.MM.yyyy')})</g:link>
                        </g:if>
                        <br/>
                        <label for="doctorate_coop_agreement_file"><g:message code="doctorate.label.newCoopFile"/>:</label>
                        <input type="file" name="doctorate_coop_agreement_file" id="doctorate_coop_agreement_file"/>

                        <g:link style="margin-top: 2em; padding: 0.5em 0.65em; width:40.42553%; max-width: 33em; cursor: pointer; text-align: center; background: #646464;" class="linkbutton" name="cancel" action="show" params="[id: doct.id]"><g:message code="portal.label.cancel"/></g:link>
                        <input style="margin-top: 2em;" type="submit" id="submit" name="submit" value="${message(code: 'portal.label.submit')}"/>
                    </fieldset>
                </g:uploadForm>
            </section>
        </article>
    </content>
</g:applyLayout>