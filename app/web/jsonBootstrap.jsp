<%-- 
    Document   : jsonBootstrap
    Created on : Oct 21, 2014, 3:25:11 AM
    Author     : admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Json Bootstrap</title>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">

        <!--<link rel="stylesheet" type="text/css" href="style.css" />-->
        <link href="CSS\dist\css\bootstrap.css" rel="stylesheet">
        <link href="CSS\examples\navbar\navbar.css" rel="stylesheet">
        <link href ="CSS/assets/css/docs.css" rel="stylesheet" type="text/css">

    </head>
    <body>
        <div class="container">

            <div class="navbar navbar-default">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" >Welcome, User</a>
                </div>
                <div class="navbar-collapse collapse">
                    <ul class="nav navbar-nav">
                        <li class="active"><a href="adminDisplay.jsp">Bootstrap</a></li>
                        <li><a href="LogoutServlet">Logout</a></li>


                    </ul>


                </div><!--/.nav-collapse -->

            </div>
            <div class="universalform">
                <h1>Bootstrap!</h1>
                <form action="json/bootstrap"  method="post" enctype="multipart/form-data">

                    Filename:
                    <input type="file" name="bootstrap-file" /><br />
                    <input type="text" name="token" value="eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0MDk3MTIxNTMsImlhdCI6MTQwOTcwODU1M30.h66rOPHh992gpEPtErfqBP3Hrfkh_nNxYwPG0gcAuCc" />
                    <input type="submit" value="Bootstrap" />
                </form>
            </div>
    </body>
</html>
