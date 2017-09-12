<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.published.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article>
            <section id="main_1">
                <g:uploadForm controller="doctorates" action="editPublished">
                    <fieldset>
                        <input type="hidden" name="id" id="id" value="${doct?.id}"/>

                        <label class="mandatory" for="published_title"><g:message code="doctorate.label.title"/>:</label>
                        <input required="required" type="text" name="published_title" id="published_title" value="${published?.title?:doct?.title?:''}"/>

                        <label class="mandatory" for="published_author"><g:message code="doctorate.label.author"/>:</label>
                        <input required="required" type="text" name="published_author" id="published_author" value="${published?.author?:(doct?.doctorand_sn && doct?.doctorand_givenname)?"${doct?.doctorand_givenname} ${doct?.doctorand_sn}":''}"/>

                        <label class="mandatory" for="published_place"><g:message code="doctorate.label.place"/>:</label>
                        <input required="required" type="text" name="published_place" id="published_place" value="${published?.place?:''}"/>

                        <label class="mandatory" for="published_isbn"><g:message code="doctorate.label.isbn"/>:</label>
                        <input required="required" type="text" name="published_isbn" id="published_isbn" value="${published?.isbn?:''}"/>

                        <label class="mandatory" for="published_date"><g:message code="doctorate.label.publishedDate"/>:</label>
                        <input required="required" class="date_field" placeholder="dd.MM.yyyy" type="text" name="published_date" id="published_date"  value="${published?.published_date?.format("dd.MM.yyyy")?:''}"/>

                        <label for="published_link"><g:message code="doctorate.label.link"/>:</label>
                        <input type="text" name="published_link" id="published_link" value="${published?.link?:''}"/>

                        <br/>
                        <g:if test="${attachment}">
                            <label><g:message code="doctorate.label.publishedFile"/>:</label>
                            <g:link controller="doctorates" action="downloadAttachment" params="[id: doct?.id, attachCat: attachment.attachCat]">${attachment.fileName} (${attachment.prettySize} / ${attachment.created.format('dd.MM.yyyy')})</g:link>
                            <!--g:link style="margin-top: 0.5em; margin-bottom: 0.5em;" class="linkbutton" controller="doctorates" action="deleteAttachment" params="[id: doct?.id, attachCat: attachment.attachCat]"--><!--g:message code="doctorate.label.deleteAttachment"/--><!--/g:link-->
                        </g:if>
                        <br/>
                        <label for="doctorate_dis_file"><g:message code="doctorate.label.newPublishedFile"/>:</label>
                        <input type="file" name="doctorate_dis_file" id="doctorate_dis_file"/>

                        <g:link style="margin-top: 2em; padding: 0.5em 0.65em; width:40.42553%; max-width: 33em; cursor: pointer; text-align: center; background: #646464;" class="linkbutton" name="cancel" action="show" params="[id: doct?.id]"><g:message code="portal.label.cancel"/></g:link>
                        <input style="margin-top: 2em;" type="submit" id="submit" name="submit" value="${message(code: 'portal.label.submit')}"/>
                    </fieldset>
                </g:uploadForm>
            </section>
        </article>
    </content>
</g:applyLayout>