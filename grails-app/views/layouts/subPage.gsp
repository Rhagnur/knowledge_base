<%--
  Created by IntelliJ IDEA.
  User: didschu
--%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><g:layoutTitle default="Knowledge Base"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <asset:stylesheet src="mainKB.css"/>
    <asset:javascript src="mainKB.js"/>

    <g:layoutHead/>
</head>

<body>
<header class="header" role="banner">
    <section id="menubar-secondary">Platzhalter Header</section>
    <section id="menubar-primary">Platzhalter Title, Image and Navigation</section>
</header>

<article id="page">
    <nav id="nav-context" role="navigation">
        <section id="subnav">
            Blablabla Navi hier
        </section>
    </nav>
    <div id="main">
        <g:if test="${flash.error}">
            <div class="alert alert-error" style="display: block">${flash.error}</div><br/><br/>
        </g:if>
        <g:if test="${flash.info}">
            <div class="alert alert-error" style="display: block">${flash.info}</div><br/><br/>
        </g:if>
        <g:pageProperty name="page.main"/>
    </div>
    <div id="sidebar">
        <sec:ifLoggedIn>
            <p>Sie sind angemeldet als <b>${principal.fullname}</b>!</p>
            <form action="/logout">
                <g:submitButton name="submit" value="Ausloggen"></g:submitButton>
            </form>
        </sec:ifLoggedIn>
        <sec:ifNotLoggedIn>
            <form action="/login/authenticate" method="POST" autocomplete="off" id="loginForm">
                <label for="username">Benutzername</label><br/>
                <g:textField name="username"></g:textField><br/><br/>
                <label for="password">Passwort:</label><br/>
                <g:passwordField name="password"></g:passwordField><br/><br/>
                <g:submitButton name="submit" value="Einloggen"></g:submitButton>
            </form>
        </sec:ifNotLoggedIn>
    </div>
    <div class="clear"></div>
</article>


<div id="footer">Platzhalter Footer</div>
<asset:javascript src="jquery-2.2.0.min.js"/>
<asset:javascript src="stupidtable.min.js"/>
<script>
    $( document ).ready(function() {
        $("#search-results-table").stupidtable();
    });
</script>
</body>
</html>
