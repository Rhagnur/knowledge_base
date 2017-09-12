<%--
  Created by IntelliJ IDEA.
  User: didschu
  Date: 13.06.17
  Time: 14:18
--%>

<g:applyLayout name="subPage">
    <head>
        <title>
            <g:if test="${cat}">
                <g:message code="doctorate.headline.doctsFor"/> ${cat}
            </g:if>
            <g:else>
                <g:message code="doctorate.categories.title"/>
            </g:else>
        </title>
    </head>
    <content tag="main">
        <style type="text/css">
        @import url("/assets/doctorates/main.css");
        </style>
        <article class="main">
            <section>
                <g:if test="${cat}">
                    <g:if test="${all}">
                        <fo:itemList>
                            <g:each in="${all}">
                                <fo:doctorate item="${it}"/>
                            </g:each>
                        </fo:itemList>
                        <nav class="pagerUss">
                            <g:paginate controller="doctorates" action="categories" id="${catID}" max="10" offset="0" total="${count}" prev="&lt;" next="&gt;" maxsteps="9" omitFirst="true" omitLast="true"/>
                        </nav>
                    </g:if>
                    <g:else>
                        <p><g:message code="doctorate.info.noDoctorateFound"/></p>
                    </g:else>
                </g:if>
                <g:else>
                    <g:if test="${usedCats}">
                        <section>
                            <ul class="doct_categories">
                                <g:each in="${usedCats}">
                                    <li>
                                        <g:link action="categories" params="[id: it.catID]">${it.catLabel} (${it.count})</g:link>
                                    </li>
                                </g:each>
                            </ul>
                        </section>
                        <section>
                            <uss:h1>Diagramm</uss:h1>
                            <canvas id="myPieChart" width="400" height="400"></canvas>
                        </section>
                    </g:if>
                </g:else>
            </section>
        </article>
    </content>
    <content tag="aside">
        <g:if test="${cat}">
            <section>
                <uss:h1>Koop. Promotionen nach Jahr</uss:h1>
                <canvas id="myChart" width="600" height="450"></canvas>
                <p style="font-size: 0.9em;"><g:message code="doctorate.show.chartInfoText"/></p>
            </section>
        </g:if>

        <g:render template="/research/aside"/>
    </content>
</g:applyLayout>