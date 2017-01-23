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
    <asset:stylesheet src="style2.css"/>
    <g:layoutHead/>
</head>

<body>
<header class="header" role="banner">
    <section id="menubar-secondary">Platzhalter Header</section>
    <section id="menubar-primary">Platzhalter Title, Image and Navigation</section>
</header>

<article id="page" class="l-2">
    <div class="inner">
        <g:pageProperty name="page.navigation"/>
        <section id="content" class="l-has-aside">
            <main id="main" role="main">
                <g:if test="${flash.error}">
                    <div class="alert alert-error" style="display: block">
                        <g:each in="${flash.error}">
                            <p>${it}</p>
                        </g:each>
                    </div><br/><br/>
                </g:if>
                <g:if test="${flash.info}">
                    <div class="alert alert-info" style="display: block">
                        <g:each in="${flash.info}">
                            <p>${it}</p>
                        </g:each>
                    </div><br/><br/>
                </g:if>
                <g:pageProperty name="page.main"/>
            </main>
            <aside id="sidebar" role="complementary">
                <g:pageProperty name="page.side"/>
                <sec:ifLoggedIn>
                    <section class="sidebox">
                        <p>Sie sind angemeldet als <b>${principal?.fullname}</b>!</p>
                        <form action="/logout">
                            <div class="logout-button"><g:submitButton name="submit" value="Ausloggen"/></div>
                        </form>
                    </section>
                </sec:ifLoggedIn>
                <sec:ifNotLoggedIn>
                    <section class="sidebox">
                        <form action="/login/authenticate" method="POST" autocomplete="off" id="loginForm">
                            <label for="username">Benutzername</label>
                            <g:textField name="username"/>
                            <label for="password">Passwort:</label>
                            <g:passwordField name="password"/>
                            <br/>
                            <div class="login-button"><g:submitButton name="submit" value="Anmelden"/></div>
                        </form>
                    </section>

                </sec:ifNotLoggedIn>
            </aside>
        </section>
    </div>
</article>


<div id="footer">Platzhalter Footer</div>
<asset:javascript src="jquery-2.2.0.min.js"/>
<asset:javascript src="stupidtable.min.js"/>
<asset:javascript src="mainKB.js"/>
<script>
    $( document ).ready(function() {
        $("#search-results-table").stupidtable();
    });
</script>
</body>
</html>
