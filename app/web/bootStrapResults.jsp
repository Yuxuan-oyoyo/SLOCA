<%-- 
    Document   : home
    Created on : Sep 16, 2014, 10:53:01 PM
    Author     : G3T2
--%>
<%@page import="sloca.model.Location"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Collections"%>
<%@page import="sloca.model.BootstrapError"%>
<%@page import="sloca.model.BootStrapManager"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Bootstrap Results</title>
    </head>
    <body>
        <%
            String username = (String) session.getAttribute("adminUser");
            if (username == null) {
                response.sendRedirect("index.jsp");
            }
        %>
        <h1>Bootstrap Results page</h1>
        <br/>
        <a href="admin">Go Back!</a>
        <br/>
        <%
            ArrayList<BootstrapError> locErrorList = BootStrapManager.locErrorList;
            ArrayList<BootstrapError> locLookUpErrorList = BootStrapManager.locLookUpErrorList;
            ArrayList<BootstrapError> demoErrorList = BootStrapManager.demoErrorList;
            //success
            HashMap<String, Location> locSuccess = BootStrapManager.locSuccess;
            HashMap<String, String> loclookupSuc = BootStrapManager.loclookupSuc;
            HashMap<String, String> demoSuc = BootStrapManager.demoSuc;
            //sort errmessages
            Collections.sort(locErrorList);
            Collections.sort(locLookUpErrorList);
            Collections.sort(demoErrorList);
        %>
        <table border ="2">
            <h2>Files Uploaded</h2>
            <tr>
                <th>File Name</th>
                <th>Rows Inserted</th>
            </tr>
            <tr>
                <td>location.csv</td>
                <td><%=locSuccess.size()%></td>
            </tr>
            <tr>
                <td>demographics.csv</td>
                <td><%=demoSuc.size()%></td>
            </tr>
            <tr>
                <td>location-lookup.csv</td>
                <td><%=loclookupSuc.size()%></td>
            </tr>
        </table>      
        <table border="1">            
            <h2><%=locErrorList.size()%> Location Errors</h2>            
            <tr>
                <th>line Number</th>
                <th>Error Messages</th>
            </tr>
            <%
                for (BootstrapError bse : locErrorList) {
            %>
            <tr>
                <td>Line <%=bse.getLineNum()%></td>
                <td><%=bse.getErrMsg()%></td>
            </tr>
            <%}%>            
        </table>
        </br>
        <table border="1">
            <h2><%=locLookUpErrorList.size()%> Location Look Up Errors</h2>
            <tr>
                <th>line Number</th>
                <th>Error Messages</th>
            </tr>
            <%
                for (BootstrapError bse : locLookUpErrorList) {
            %>
            <tr>
                <td>Line <%=bse.getLineNum()%></td>
                <td><%=bse.getErrMsg()%></td>
            </tr>
            <%}%>
        </table>
        <br/>
        <table border="1">
            <h2><%=demoErrorList.size()%> Demographic Errors</h2>
            <tr>
                <th>line Number</th>
                <th>Error Messages</th>
            </tr>
            <%
                for (BootstrapError bse : demoErrorList) {
            %>
            <tr>
                <td>Line <%=bse.getLineNum()%></td>
                <td><%=bse.getErrMsg()%></td>
            </tr>
            <%}%>
        </table>
    </body>
</html>
