<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title><g:message code="doctorate.own.title"/></title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article class="main">
            <p><g:message code="doctorate.own.info"/></p>
            <section>
                <g:if test="${all}">
                    <fo:itemList>
                        <g:each in="${all}">
                            <fo:doctorate item="${it}"/>
                        </g:each>
                    </fo:itemList>
                    <nav class="pagerUss">
                        <g:paginate controller="doctorates" action="own" max="10" offset="0" total="${count}" prev="&lt;" next="&gt;" maxsteps="9" omitFirst="true" omitLast="true"/>
                    </nav>
                </g:if>
                <g:else>
                    <p><g:message code="doctorate.info.noDoctorateFound"/></p>
                </g:else>
            </section>
        </article>
    </content>
    <content tag="aside">
        <section>
            <uss:h1>Koop. Promotionen nach Jahr</uss:h1>
            <canvas id="myChart" width="600" height="450"></canvas>
            <p style="font-size: 0.9em;"><g:message code="doctorate.show.chartInfoText"/></p>
        </section>
        <!--section>
            <uss:h1>Diese Liste herunterladen</uss:h1>
            <ul>
                <li><a href="newest.json" target="_blank">JSON-Format</a></li>
                <li><a href="newest.xml" target="_blank">XML-Format</a></li>
            </ul>
        </section-->
        <g:render template="/research/aside"/>
    </content>
</g:applyLayout>