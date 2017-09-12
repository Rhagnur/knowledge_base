<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.${person.'supervision_category'}.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article>
            <section id="main_1">
                <form id="supervisor">
                    <fieldset>
                        <input type="hidden" name="id" id="id" value="${doctID}"/>
                        <input type="hidden" name="svCat" id="svCat" value="${person.'supervision_category'}"/>

                        <g:if test="${!person.'supervision_category'.startsWith('first')}">
                            <input style="float: left;" type="checkbox" name="is_extern" id="is_extern" ${(person.sn || person.pvz_id) && !person.find{it.key == 'id'}?'checked':''}/>
                            <label for="is_extern"><g:message code="doctorate.label.externalPerson"/></label>
                            <div style="clear: both;"/>
                        </g:if>

                        <div id="extern">
                            <label class="mandatory" for="extern_sn"><g:message code="portal.label.name"/></label>
                            <input type="text" id="extern_sn" name="extern_sn" value="${person?.sn}"/>

                            <label class="mandatory" for="extern_givenName"><g:message code="portal.label.givenName"/></label>
                            <input type="text" id="extern_givenName" name="extern_givenName" value="${person?.givenName}"/>

                            <label for="extern_title"><g:message code="page.label.title"/></label>
                            <input type="text" id="extern_title" name="extern_title" value="${person?.title?:''}"/>

                            <label for="extern_faculty"><g:message code="doctorate.label.faculty"/></label>
                            <input type="text" id="extern_faculty" name="extern_faculty" value="${person?.faculty?:''}"/>

                            <label for="extern_field"><g:message code="doctorate.label.field"/></label>
                            <input type="text" id="extern_field" name="extern_field" value="${person?.field?:''}"/>
                        </div>

                        <g:if test="${!person.'supervision_category'.startsWith('first')}">
                            <div id="intern">
                                <div id="intern_canvas">
                                    <g:if test="${person.find{it.key == 'id'} && person.id}">
                                        <g:include controller="researchers" action="personInfo" params="[lsfid: person.lsfId, pvzid: person.id]"/>
                                    </g:if>
                                    <g:else>
                                        <uss:infobox>
                                            <p id="no_person_choosen" style="margin-bottom: 0;"><g:message code="doctorate.info.noInternalPersonChosen"/></p>
                                        </uss:infobox>
                                    </g:else>

                                </div>
                                <input type="text" id="intern_type_ahead" name="intern_type_ahead" class="typeahead" placeholder="${message(code:'doctorate.label.findPersonTypeAhead')}"/>

                                <input type="hidden" id="intern_pvz_id" name="intern_pvz_id" value="${(person.find{it.key=='id'} && person.id)?person.id:''}"/>
                            </div>
                        </g:if>

                        <g:link style="margin-top: 2em; padding: 0.5em 0.65em; width:40.42553%; max-width: 33em; cursor: pointer; text-align: center; background: #646464;" class="linkbutton" name="cancel" action="show" params="[id: doctID]"><g:message code="portal.label.cancel"/></g:link>
                        <input style="margin-top: 2em;" type="submit" id="submit" name="submit" value="${message(code: 'portal.label.submit')}"/>
                    </fieldset>
                </form>
                <div style="height: 5em;"></div>
            </section>
        </article>
    </content>
</g:applyLayout>