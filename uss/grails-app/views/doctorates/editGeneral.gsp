<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.general.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
            @import url("/assets/doctorates/main.css");
        </style>
        <article>
            <section id="main_1">
                <g:uploadForm controller="doctorates" action="editGeneral">
                    <fieldset>
                        <input type="hidden" name="id" id="id" value="${doct?.id}"/>

                        <label class="mandatory" for="doctorate_title"><g:message code="doctorate.label.title"/>:</label>
                        <input required style="margin-bottom: 1em;" type="text" name="doctorate_title" id="doctorate_title" value="${doct?.title?:''}"/>

                        <label class="mandatory" for="doctorate_status"><g:message code="typo3.step.summary.labelStatus"/>:</label>
                        <select required id="doctorate_status" name="doctorate_status">
                            <g:each in="${stati}">
                                <option value="${it}" ${it==doct.status?'selected':''}><g:message code="doctorate.status.${it}"/></option>
                            </g:each>
                        </select>

                        <label for="doctorate_categories_select"><g:message code="doctorate.label.scienceCategories"/></label>
                        <div id="canvas_categories">
                            <g:each in="${cats}" status="i" var="cat">
                                <div id="${cat.catID}" class="canvas_obj">
                                    <input type="hidden" name="doctorate_categories" value="${cat.catID}"/>
                                    <span>${cat.cat}</span>
                                    <a onclick="removeCanvasObj('${cat.catID}')"><i class='icon-sym-false2'></i></a>
                                </div>
                            </g:each>
                            <div class="clear" style="clear:both;"></div>
                        </div>
                        <select id="doctorate_categories_select" name="doctorate_categories_select">
                            <option value="0"><g:message code="doctorate.category.default"/></option>
                            <g:each in="${catsAll}" var="cat">
                                <option value="${cat.catID}" class="${cats.find{ it.catID == cat.catID }?'usedCat':''}">${cat.cat}</option>
                            </g:each>
                        </select>

                        <label for="doctorate_acceptDate"><g:message code="doctorate.label.acceptDate"/>:</label>
                        <input class="date_field" placeholder="dd.MM.yyyy" type="text" name="doctorate_acceptDate" id="doctorate_acceptDate"  value="${doct?.accepted?.format("dd.MM.yyyy")?:''}"/>

                        <label for="doctorate_endDate"><g:message code="doctorate.label.endDate"/>:</label>
                        <input class="date_field" placeholder="dd.MM.yyyy" type="text" name="doctorate_endDate" id="doctorate_endDate" value="${doct?.finished?.format("dd.MM.yyyy")?:''}"/>

                        <br/>
                        <g:if test="${attachment}">
                            <label><g:message code="doctorate.label.exposeFile"/>:</label>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: attachment.attachCat]">${attachment.fileName} (${attachment.prettySize} / ${attachment.created.format('dd.MM.yyyy')})</g:link>
                            <!--g:link style="margin-top: 0.5em; margin-bottom: 0.5em;" class="linkbutton" controller="doctorates" action="deleteAttachment" params="[id: doct?.id, attachCat: attachment.attachCat]"--><!--g:message code="doctorate.label.deleteAttachment"/--><!--/g:link-->
                        </g:if>
                        <br/>
                        <label for="doctorate_ex_file"><g:message code="doctorate.label.newExposeFile"/>:</label>
                        <input type="file" name="doctorate_ex_file" id="doctorate_ex_file"/>

                        <g:link style="margin-top: 2em; padding: 0.5em 0.65em; width:40.42553%; max-width: 33em; cursor: pointer; text-align: center; background: #646464;" class="linkbutton" name="cancel" action="show" params="[id: doct.id]"><g:message code="portal.label.cancel"/></g:link>
                        <input style="margin-top: 2em;" type="submit" id="submit" name="submit" value="${message(code: 'portal.label.submit')}"/>
                    </fieldset>
                </g:uploadForm>
            </section>
        </article>
    </content>
</g:applyLayout>