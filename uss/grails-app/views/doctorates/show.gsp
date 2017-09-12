<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article>
            <section id="general">
                <uss:h1><g:message code="doctorate.general.title"/></uss:h1>
                <uss:kvTable>
                    <uss:kvItem key="${message(code:'doctorate.label.title')}">${doct?.title?:''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'typo3.step.summary.labelStatus')}">${message(code: "doctorate.status.${doct?.status}")}</uss:kvItem>
                    <uss:kvItem key="${message(code: 'doctorate.label.scienceCategories')}">
                        <g:each in="${cats}" status="i" var="category">${i>0?', ':''}${category.cat}</g:each>
                    </uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.expose')}">
                        <g:if test="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'ex' as String}}">
                            <g:set var="ex_attach" value="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'ex' as String}}"/>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: ex_attach.attachCat]">${ex_attach.fileName} (${ex_attach.prettySize})</g:link>
                        </g:if>
                    </uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.acceptDate')}">${doct?.accepted?.format("dd.MM.yyyy")?:''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.endDate')}">${doct?.finished?.format("dd.MM.yyyy")?:''}</uss:kvItem>
                </uss:kvTable>

                <g:if test="${canWrite}">
                    <g:if test="${ex_attach}">
                        <g:link style="float: left;" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}')" class="linkbutton" controller="doctorates" action="deleteAttachment" params="[id: doct?.id, attachCat: ex_attach.attachCat]"><uss:icon icon="sym-delete"/> <g:message code="doctorate.label.deleteAttachment"/></g:link>
                    </g:if>

                    <p><g:link style="float:right;" class="linkbutton" id="${doct?.id}" action="editGeneral"><uss:icon icon="sym-edit"/> <g:message code='doctorate.label.partialEdit'/></g:link></p>
                </g:if>
                <div style="clear: both; height: 1em;"></div>
            </section>

            <g:if test="${published}">
                <section id="published">
                    <uss:h1><g:message code="doctorate.published.title"/></uss:h1>
                    <uss:kvTable>
                        <uss:kvItem key="${message(code:'doctorate.label.title')}">${published?.title}</uss:kvItem>
                        <uss:kvItem key="${message(code:'doctorate.label.author')}">${published?.author}</uss:kvItem>
                        <uss:kvItem key="${message(code:'doctorate.label.place')}">${published?.place}</uss:kvItem>
                        <uss:kvItem key="${message(code:'doctorate.label.isbn')}">${published?.isbn}</uss:kvItem>
                        <uss:kvItem key="${message(code:'doctorate.label.publishedDate')}">${published?.published_date?.format('dd.MM.yyyy')?:''}</uss:kvItem>
                        <uss:kvItem key="${message(code:'doctorate.label.link')}">${published?.link?:''}</uss:kvItem>
                        <uss:kvItem key="${message(code:'doctorate.label.dissertation')}">
                            <g:if test="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'dis' as String}}">
                                <g:set var="dis_attach" value="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'dis' as String}}"/>
                                <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: dis_attach.attachCat]">${dis_attach.fileName} (${dis_attach.prettySize})</g:link>
                            </g:if>
                        </uss:kvItem>
                    </uss:kvTable>

                    <g:if test="${canWrite}">
                        <g:if test="${dis_attach}">
                            <g:link style="float: left;" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}')" class="linkbutton" controller="doctorates" action="deleteAttachment" params="[id: doct?.id, attachCat: dis_attach.attachCat]"><uss:icon icon="sym-delete"/> <g:message code="doctorate.label.deleteAttachment"/></g:link>
                        </g:if>

                        <g:link style="float:left;" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}')" class="linkbutton" params="[id: doct?.id]" action="deletePublished"><uss:icon icon="sym-delete"/> <g:message code='doctorate.label.partialDelete'/></g:link>
                        <p><g:link style="float:right;" class="linkbutton" id="${doct?.id}" action="editPublished"><uss:icon icon="sym-edit"/> <g:message code='doctorate.label.partialEdit'/></g:link></p>
                    </g:if>
                    <div style="clear: both; height: 1em;"></div>
                </section>
            </g:if>

            <section id="promovend">
                <uss:h1><g:message code="doctorate.promovend.title"/></uss:h1>
                <uss:kvTable>
                    <uss:kvItem key="${message(code:'portal.label.name')}">${doct?.doctorand_sn?:''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'portal.label.givenName')}">${doct?.doctorand_givenName?:''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.graduations')}">
                        <g:each in="${doct?.graduations}" status="i" var="grad">${i>0?', ':' '}<g:message code="doctorate.graduations.${grad}"/></g:each>
                    </uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.uniNameHightestGrad')}">${doct?.doctorand_university_name?:''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.dateHightestGrad')}">${doct?.doctorand_graduation_date?.format("dd.MM.yyyy")?:''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.supervisionAgreement')}">
                        <g:if test="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'sv_ag' as String}}">
                            <g:set var="sv_attach" value="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'sv_ag' as String}}"/>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: sv_attach.attachCat]">${sv_attach.fileName} (${sv_attach.prettySize})</g:link>
                        </g:if>
                    </uss:kvItem>
                </uss:kvTable>

                <g:if test="${canWrite}">
                    <g:if test="${sv_attach}">
                        <g:link style="float: left;" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}')" class="linkbutton" controller="doctorates" action="deleteAttachment" params="[id: doct?.id, attachCat: sv_attach.attachCat]"><uss:icon icon="sym-delete"/> <g:message code="doctorate.label.deleteAttachment"/></g:link>
                    </g:if>
                    <p><g:link style="float:right;" class="linkbutton" id="${doct?.id}" action="editPromovend"><uss:icon icon="sym-edit"/> <g:message code='doctorate.label.partialEdit'/></g:link></p>
                </g:if>
                <div style="clear: both; height: 1em;"></div>
            </section>

            <section id="uni_financing">
                <uss:h1><g:message code="doctorate.uniAndFinancing.title"/></uss:h1>
                <uss:kvTable>
                    <uss:kvItem key="${message(code:'doctorate.label.doctorateUniName')}">${doct?.university_name}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.doctorateUniCategory')}">${message(code: "doctorate.uniCategories.${doct?.university_category}")}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.doctorateUniPlace')}">${doct?.university_place}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.mainFinancing')}">${message(code: "doctorate.financing.${doct?.main_financing}")}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.secondaryFinancing')}">${doct?.second_financing?message(code: "doctorate.financing.${doct?.second_financing}"):''}</uss:kvItem>
                    <uss:kvItem key="${message(code:'doctorate.label.cooperationAgreement')}">
                        <g:if test="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'coop' as String}}">
                            <g:set var="coop_attach" value="${attachments.find{ it.'attachCat' == grailsApplication.config.'fo_doctorates'.'attach_for'.'coop' as String}}"/>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: coop_attach.attachCat]">${coop_attach.fileName} (${coop_attach.prettySize})</g:link>
                        </g:if>
                    </uss:kvItem>
                </uss:kvTable>

                <g:if test="${canWrite}">
                    <g:if test="${coop_attach}">
                        <g:link style="float: left;" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}')" class="linkbutton" controller="doctorates" action="deleteAttachment" params="[id: doct?.id, attachCat: coop_attach.attachCat]"><uss:icon icon="sym-delete"/> <g:message code="doctorate.label.deleteAttachment"/></g:link>
                    </g:if>

                    <p><g:link style="float:right;" class="linkbutton" id="${doct?.id}" action="editUniAndFinance"><uss:icon icon="sym-edit"/> <g:message code='doctorate.label.partialEdit'/></g:link></p>
                </g:if>
                <div style="clear: both; height: 1em;"></div>
            </section>

            <g:each in="${doct?.persons}">
                <section id="${it.'supervision_category'}">
                    <g:if test="${it.find { it.key == 'id'}}">
                        <uss:h1><g:message code="doctorate.${it.'supervision_category'}.title"/> (intern)</uss:h1>
                        <div id="intern_canvas">
                            <g:include controller="researchers" action="personInfo" params="[lsfid: it.lsfId, pvzid: it.id]"/>
                        </div>
                    </g:if>
                    <g:else>
                        <uss:h1><g:message code="doctorate.${it.'supervision_category'}.title"/> (extern)</uss:h1>
                        <uss:kvTable>
                            <uss:kvItem key="${message(code:'portal.label.name')}">${it.sn}</uss:kvItem>
                            <uss:kvItem key="${message(code:'portal.label.givenName')}">${it.givenname}</uss:kvItem>
                            <uss:kvItem key="${message(code:'page.label.title')}">${it.title?:''}</uss:kvItem>
                            <uss:kvItem key="${message(code:'doctorate.label.faculty')}">${it.faculty?:''}</uss:kvItem>
                            <uss:kvItem key="${message(code:'doctorate.label.field')}">${it.field?:''}</uss:kvItem>
                        </uss:kvTable>
                    </g:else>

                    <g:if test="${canWrite}">
                        <g:if test="${! it.'supervision_category'.endsWith('supervisor')}">
                            <g:link style="float:left;" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}')" class="linkbutton" params="[id: doct?.id, svCat: it.'supervision_category']" action="deletePerson"><uss:icon icon="sym-delete"/> <g:message code='doctorate.label.partialDelete'/></g:link>
                        </g:if>

                        <p><g:link style="float:right;" class="linkbutton" params="[id: doct?.id, svCat: it.'supervision_category']" action="editSupervisor"><uss:icon icon="sym-edit"/> <g:message code='doctorate.label.partialEdit'/></g:link></p>
                    </g:if>
                    <div style="clear: both; height: 1em;"></div>
                </section>

            </g:each>

            <g:if test="${(unusedSVTypes || !published) && canWrite}">
                <section id="options">
                    <h1><g:message code="typo3.index.labelOptions"/></h1>
                    <g:if test="${unusedSVTypes}">
                        <section id="new_person">
                            <g:form action="addPerson" id="${doct?.id}" style="margin-top: 4em;">
                                <label for="new_person_select"><g:message code="doctorate.label.addNewPerson"/>:</label>
                                <div style="clear: both;"></div><br/>
                                <select id="new_person_select" name="new_person_select" style="width: 55%; float: left; height: 2.6em !important;">
                                    <g:each in="${unusedSVTypes}">
                                        <option value="${it}"><g:message code="doctorate.${it}.title"/></option>
                                    </g:each>
                                </select>

                                <input style="float: right;" type="submit" id="new_person_submit" name="new_person_submit" value="${message(code:'action.add')}"/>
                                <div style="clear: both;"></div>
                            </g:form>
                        </section>
                    </g:if>
                    <g:if test="${!published}">
                        <br/>
                        <section id="new_published">
                            <label><g:message code="doctorate.label.addPublished"/>:</label>
                            <p><g:link style="float: left; margin-top: 1em; padding: 0.5em 0.65em; width:40.42553%; max-width: 33em; cursor: pointer; text-align: center; background: #646464;" class="linkbutton" action="addPublished" params="[id: doct?.id]"><g:message code="doctorate.label.newPublished"/></g:link></p>
                        </section>
                        <div style="clear: both;"></div>
                    </g:if>
                </section>
            </g:if>



            
        </article>
    </content>
</g:applyLayout>