<%--
  edited: didschu
--%>
<g:applyLayout name="main">
    <head>
        <title>NOT FOUND</title>
    </head>

    <content tag="main">
    <content>
        <ul class="errors">
            <li>Error: Page Not Found (404)</li>
            <li>Path: ${request.forwardURI}</li>
        </ul>
    </content>
</g:applyLayout>
