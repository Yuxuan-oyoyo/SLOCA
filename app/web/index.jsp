<%-- 
    Document   : home
    Created on : Sep 16, 2014, 10:53:01 PM
    Author     : G3T2
--%>
<%@page import="sloca.model.User"%>

<%
    Object user;
    String admin;
// check if user is authenticated

    user = session.getAttribute("LoggedInUser");
    admin = (String) session.getAttribute("adminUser");

    String errorMsg = (String) request.getAttribute("errorMsg");
    if (errorMsg == null) {
        errorMsg = "";
    }

    if (user instanceof User) {
        response.sendRedirect("home.jsp");
        return;
    } else if (admin != null) {
        response.sendRedirect("adminDisplay.jsp");
        return;

    } else {
        // restart
        session.invalidate();

    }
%>
<html>
    <head>
        <style>
            #wrap { 
                position: absolute;
                width: 100%; 
                margin: 0 auto; 
            }


            body{
                text-align: center;
            }
        </style>
        <link rel="stylesheet" type="text/css" href="CSS\dist\css\bootstrap.css" />
        <link href="signin.css" rel="stylesheet">
    </head>
    <body>

        <img src="img\logo.jpg" width="600" height="250"/>
        <div id="wrap" frame="box" align="center">


            <p>
            <p style="line-height: 200%; text-align: center; direction: ltr; unicode-bidi: embed; word-break: normal; punctuation-wrap: hanging; margin-left: 0in; margin-top: 0pt; margin-bottom: 0pt">&nbsp;</p>

            <p style="line-height: 200%; text-align: center; direction: ltr; unicode-bidi: embed; word-break: normal; punctuation-wrap: hanging; margin-left: 0in; margin-top: 0pt; margin-bottom: 0pt">
                <span style="font-size: 24.0pt; font-family: Trajan Pro 3, Arial; color: #1F497D; font-weight: bold">
                    SLOCA Login</span></p> 
            <form class="form-signin" name="login" method="post" action="ValidateUser">
                <%
                    String username = (String) request.getAttribute("username");
                    String password = (String) request.getAttribute("password");
                    if (username != null && password != null) {
                %>
                <input type="text" name="username" class="form-control" placeholder="Username" value=<%=username%> autofocus>
                <input type="password" name="password" class="form-control" placeholder="Password">
                <%
                } else {
                %>
                <input type="text" name="username" class="form-control" placeholder="Username" autofocus>
                <input type="password" name="password" class="form-control" placeholder="Password" autofocus>        
                <%
                    }
                %>
                <input class="btn btn-lg btn-primary btn-block" type="submit">
                <b style="color:red;"><%=errorMsg%></b>
            </form>
        </div>
    </body>
</html>