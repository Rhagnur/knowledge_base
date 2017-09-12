<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.title"/></title>
    </head>
    <content tag="main">
        <style>
            @import url("/assets/doctorates/main.css");
        </style>
        <section>
            <g:if test="${step >= 0}">
                <uss:h1>
                    <g:if test="${title}">
                        ${title}
                    </g:if>
                    <g:else>
                        <g:message code="doctorate.step.${view}"/>
                    </g:else>
                </uss:h1>
            </g:if>

            <g:render template="${view}"/>
        </section>
</content>


    <content tag="aside">




        <section class="special-1" style="background-color: white; color: black;">
            <!--section style="padding-bottom: 1em;">
                <figure>
                    <uss:jsimg src="/assets/doctorate.png"/>
                    <figcaption><g:message code="doctorate.aside.title"/></figcaption>
                </figure>
            </section-->
            <g:if test="${step==0}">
            <p>
                <i class="icon-sym-sprechzeiten-2" style="color: #0082D1;"></i> <g:message code="doctorate.aside.duration"/>
            </p>
            </g:if>
            <g:else>
                <% def style=''
                    def i=1
                %>
                <g:each in="${wizardData[1..wizardData.size()-1]}">
                    <%
                        style=it.enabled?'':'color: #afafaf;'
                        if (i==step) {
                            style+=' font-weight: 600;'
                        } else style+=' padding-left: 1.1em;'
                    %>
                        <div style="${style} padding-top: 0.25em;">
                            <g:if test="${i==step}"><i class="icon-sym-pfeil-rechts-gross" style="font-size: 0.75em; color: #0082D1; padding-right: 0.2em;"></i> </g:if>
                            <g:if test="${i<wizardData.size()-1}"><g:message code="portal.label.step"/> ${i}: </g:if>${message(code:it.title?it.title:'doctorate.step.'+it.view)}</div>
                    <% i++ %>
                </g:each>
            </g:else>
        </section>
    </content>
</g:applyLayout>
