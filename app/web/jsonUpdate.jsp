<%-- 
    Document   : jsonUpdate
    Created on : Oct 21, 2014, 3:38:07 AM
    Author     : admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Json Update</title>
    </head>
    <body>
        <h1>Update!</h1>
        <form action="json/update"  method="post" enctype="multipart/form-data">

            Filename:
            <input type="file" name="bootstrap-file" /><br />
            <input type='text' name='token'   />
            <input type="submit" value="Bootstrap" />
        </form>
    </body>
</html>
