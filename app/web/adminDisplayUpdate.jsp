<%-- 
    Document   : adminDisplay
    Created on : Sep 15, 2014, 11:10:40 AM
    Author     : G3T2
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">
        <title>Admin Display Update</title>
        <link href="CSS\dist\css\bootstrap.css" rel="stylesheet">
        <link href="CSS\examples\navbar\navbar.css" rel="stylesheet">
        <link href="CSS/assets/css/docs.css" type="text/css" rel="stylesheet">
    </head>
    <body>
        <%
            String username = (String) session.getAttribute("adminUser");
            if (username == null) {
                response.sendRedirect("index.jsp");
            }
        %>

        <div class="container">
            <div class="navbar navbar-default">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand">Welcome, admin</a>
                </div>
                <div class="navbar-collapse collapse">
                    <ul class="nav navbar-nav">
                        <li><a href="adminDisplay.jsp">Home</a></li>
                        <li class="dropdown active">
                        <li class="dropdown active">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">Bootstrap<span class="caret"></span></a>
                            <ul class="dropdown-menu" role="menu">
                                <li><a href="adminDisplay.jsp">Upload</a></li>
                                <li><a href="adminDisplayUpdate.jsp">Update</a></li>
                            </ul>
                        </li>
                        <li><a href="LogoutServlet">Logout</a></li>
                    </ul>
                </div><!--/.nav-collapse -->
            </div>
            <div class="universalform">
                <h3><span style="font-family: Aharoni; color: #1F497D; font-weight: bold">
                        <font size="5">Update Data</font></span></h3>
                        <%
                            String errorMsg = (String) request.getAttribute("fileUploadError");
                            if (errorMsg == null) {
                                errorMsg = "";
                            }
                        %>
                <b style="color:red;"><%=errorMsg%></b>
                <form action="fileUpdate.do" method="post" enctype="multipart/form-data">
                    <input type="file" name="uploadFile" />
                    </br>
                    <input type="submit" value="Upload" />
                </form>   
            </div>                       
        </div>
        <!--Method #1 of Initializing Bootstrap--> 
        <script src="https://code.jquery.com/jquery-1.11.0.min.js"></script>
        <script src="https://code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
    </body>
</html>
