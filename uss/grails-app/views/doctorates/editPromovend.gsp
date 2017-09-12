<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.promovend.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article>
            <section id="main_1">
                <g:uploadForm controller="doctorates" action="editPromovend">
                    <fieldset>
                        <input type="hidden" name="id" id="id" value="${doct?.id}"/>

                        <label class="mandatory" for="doctorand_sn"><g:message code="portal.label.name"/>:</label>
                        <input type="text" name="doctorand_sn" id="doctorand_sn" value="${doct.doctorand_sn?:''}"/>

                        <label class="mandatory" for="doctorand_givenName"><g:message code="portal.label.givenName"/>:</label>
                        <input type="text" name="doctorand_givenName" id="doctorand_givenName" value="${doct.doctorand_givenname?:''}"/>

                        <label class="mandatory" for="doct_graduations_select"><g:message code="doctorate.label.graduations"/>:</label>
                        <div id="canvas_graduations">
                            <g:each in="${grads}" status="i" var="grad">
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
                            <g:each in="${gradTypes}">
                                <option value="${it}"><g:message code="doctorate.graduations.${it}"/> </option>
                            </g:each>
                        </select>

                        <label class="mandatory" for="doctorand_hightestGrad_uniName"><g:message code="doctorate.label.uniNameHightestGrad"/>:</label>
                        <input type="text" name="doctorand_hightestGrad_uniName" id="doctorand_hightestGrad_uniName" value="${doct?.doctorand_university_name?:''}"/>

                        <label class="mandatory" for="doctorand_hightestGrad_date"><g:message code="doctorate.label.dateHightestGrad"/>:</label>
                        <input class="date_field" placeholder="dd.MM.yyyy" type="text" name="doctorand_hightestGrad_date" id="doctorand_hightestGrad_date" value="${doct?.doctorand_graduation_date?.format("dd.MM.yyyy")?:''}"/>

                        <br/>
                        <g:if test="${attachment}">
                            <label><g:message code="doctorate.label.supervisionAgreementFile"/>:</label>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: attachment.attachCat]">${attachment.fileName} (${attachment.prettySize} / ${attachment.created.format('dd.MM.yyyy')})</g:link>
                        </g:if>
                        <br/>
                        <label for="doctorate_sv_agreement_file"><g:message code="doctorate.label.newSupervisionAgreementFile"/>:</label>
                        <input type="file" name="doctorate_sv_agreement_file" id="doctorate_sv_agreement_file"/>

                        <g:link style="margin-top: 2em; padding: 0.5em 0.65em; width:40.42553%; max-width: 33em; cursor: pointer; text-align: center; background: #646464;" class="linkbutton" name="cancel" action="show" params="[id: doct.id]"><g:message code="portal.label.cancel"/></g:link>
                        <input style="margin-top: 2em;" type="submit" id="submit" name="submit" value="${message(code: 'portal.label.submit')}"/>
                    </fieldset>
                </g:uploadForm>
            </section>
        </article>
    </content>
</g:applyLayout>