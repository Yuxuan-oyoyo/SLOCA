<%-- 
    Document   : breakDown
    Created on : Oct 8, 2014, 9:39:08 AM
    Author     : G3T2
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.ResultSet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="sloca.model.User"%>
<%@ include file = "protect.jsp" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="shortcut icon" href="CSS/assets/ico/favicon.png">
        <link href ="CSS/assets/css/docs.css" rel="stylesheet" type="text/css">

        <title>Breakdown by year and gender</title>

        <!-- Bootstrap core CSS -->
        <link href="CSS/dist/css/bootstrap.css" rel="stylesheet">
        <link href="navbar.css" rel="stylesheet">

        <script type='text/javascript'>
            function setOptions(chosen, selbox) {
                // selbox assignment deleted

                selbox.options.length = 0;
                if (chosen == "0") {
                    selbox.options[selbox.options.length] = new
                            Option('-Select one of the options above-', '00');
                    setTimeout(setOptions('00', document.myform.thirdChoice), 5);

                }

                if (chosen == "1") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '00');
                    selbox.options[selbox.options.length] = new
                            Option('Gender', '12');
                    selbox.options[selbox.options.length] = new
                            Option('School', '13');
                    setTimeout(setOptions('12', document.myform.thirdChoice), 5);
                    setTimeout(setOptions('13', document.myform.thirdChoice), 5);
                    setTimeout(setOptions('00', document.myform.thirdChoice), 5);
                }
                if (chosen == "2") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '00');
                    selbox.options[selbox.options.length] = new
                            Option('Year', '21');
                    selbox.options[selbox.options.length] = new
                            Option('School', '23');
                    setTimeout(setOptions('21', document.myform.thirdChoice), 5);
                    setTimeout(setOptions('23', document.myform.thirdChoice), 5);
                    setTimeout(setOptions('00', document.myform.thirdChoice), 5);
                }
                if (chosen == "3") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '00');
                    selbox.options[selbox.options.length] = new
                            Option('Year', '31');
                    selbox.options[selbox.options.length] = new
                            Option('Gender', '32');
                    setTimeout(setOptions('31', document.myform.thirdChoice), 5);
                    setTimeout(setOptions('32', document.myform.thirdChoice), 5);
                    setTimeout(setOptions('00', document.myform.thirdChoice), 5);
                }
                // repeat for entries in first dropdown list
                if (chosen == "12") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '0');
                    selbox.options[selbox.options.length] = new
                            Option('School', '123');
                }
                if (chosen == "13") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '0');
                    selbox.options[selbox.options.length] = new
                            Option('Gender', '132');
                }
                if (chosen == "21") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '0');
                    selbox.options[selbox.options.length] = new
                            Option('School', '213');
                }
                if (chosen == "23") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '0');
                    selbox.options[selbox.options.length] = new
                            Option('Year', '231');
                }
                if (chosen == "31") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '0');
                    selbox.options[selbox.options.length] = new
                            Option('Gender', '312');
                }
                if (chosen == "32") {

                    selbox.options[selbox.options.length] = new
                            Option('-Select-', '0');
                    selbox.options[selbox.options.length] = new
                            Option('Year', '321');
                }
                if (chosen == "00") {
                    selbox.options[selbox.options.length] = new
                            Option('-Select one of the options above-', '000');

                }
                // repeat for all the possible entries in second dropdown list
            }
        </script>
    </head>
    <body  background="img/tete.jpg">
        <div class="container">
            <!-- Static navbar -->
            <div class="container">
                <img src="img\name.jpg" width="600"/>  
                <!-- Static navbar -->
                <div class="navbar navbar-default">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="navbar-brand">Welcome, <%=user.getName()%></a>
                    </div>
                    <div class="navbar-collapse collapse">
                        <ul class="nav navbar-nav">
                            <li><a href="home.jsp">Home</a></li>
                            <li><a href="heatMap.jsp">Heat Map</a></li>
                            <!--<li><a href="basicLocationReport.jsp">Basic Location Report</a></li>-->
                            <li class="dropdown active">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Basic Location Report <span class="caret"></span></a>
                                <ul class="dropdown-menu" role="menu">
                                    <li><a href="breakDown.jsp">Breakdown by year and gender</a></li>
                                    <li><a href="popularPlace.jsp">Top-k popular places</a></li>
                                    <li><a href="popularCompanions.jsp">Top-k companions</a></li>
                                    <li><a href="topKNextPlaces.jsp">Top-k next places</a></li>
                                </ul>
                            </li>
                            <li ><a href="automaticGroupIdentification.jsp">Automatic Group Identification</a></li>
                            <!--<li><a href="groupAwareLocationReport.jsp">Group-aware Location Reports</a></li>-->
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Group-aware Location Reports <span class="caret"></span></a>
                                <ul class="dropdown-menu" role="menu">
                                    <li><a href="groupPop.jsp">Top-k popular places for groups</a></li>
                                    <li><a href="groupKPopNext.jsp">The top K next places for groups</a></li>
                                </ul>
                            </li>
                            <li><a href="LogoutServlet">Logout</a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <!--Method #1 of Initializing Bootstrap--> 
            <script src="https://code.jquery.com/jquery-1.11.0.min.js"></script>
            <script src="https://code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
            <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
            <br/>
            <!--<h1>Break Down by Year/Gender/School</h1>-->
            <div class="universalform">
                <p align="center">
                    <span style="color: rgb(31, 73, 125); font-family: Aharoni; font-style: normal; font-variant: normal; letter-spacing: normal; line-height: 28px; orphans: auto; text-align: center; text-indent: 0px; text-transform: none; white-space: normal; widows: auto; word-spacing: 0px; -webkit-text-stroke-width: 0px; display: inline !important; float: none; background-color: rgb(255, 255, 255)">
                        <font size="4">Break Down by Year/Gender/School</font></span></p>
                        <%
                            String time = (String) request.getAttribute("time");
                            if (time != null) {
                        %>
                <form name ='myform' action='processBreakdownServlet.do' method='post' align="center">
                    First Choice:
                    <select name="firstChoice" size="1"
                            onchange="setOptions(document.myform.firstChoice.options[document.myform.firstChoice.selectedIndex].value, document.myform.secondChoice);">
                        <option value="0" selected="selected">-Select-</option>
                        <option value="1">Year</option>
                        <option value="2">Gender</option>
                        <option value="3">School</option>
                    </select><br/><br/>
                    Second Choice:
                    <select name="secondChoice" size="1"
                            onchange="setOptions(document.myform.secondChoice.options[document.myform.secondChoice.selectedIndex].value, document.myform.thirdChoice);">
                        <option value="00" selected="selected">-Select one of the options above-</option>
                    </select><br/><br/>
                    Third Choice:
                    <select name="thirdChoice" size="1">
                        <option value="0" selected="selected">-Select one of the options above-</option>
                    </select><br/><br/>
                    Date and Time (dd/mm/yyyy HH:MM AM)
                    <br/>
                    <input type='datetime-local' name='dateTime' value=<%=time%>><br/><br/>
                    <%
                        String err = (String) request.getAttribute("errMsg");
                        if (err != null) {
                            out.println("<font color='red'>" + err + "</font>");
                        }
                        String timeErr = (String) request.getAttribute("error");
                        if (timeErr != null) {
                            out.println("<font color='red'>" + timeErr + "</font>");
                        }
                    %>
                    <input class="btn btn-lg btn-primary btn-block" type="submit" value="submit">
                </form>  
            </div>
            <%
            } else {
            %>
            <form name ='myform' action='processBreakdownServlet.do' method='post' align="center">
                First Choice:
                <select name="firstChoice" size="1"
                        onchange="setOptions(document.myform.firstChoice.options[document.myform.firstChoice.selectedIndex].value, document.myform.secondChoice);">
                    <option value="0" selected="selected">-Select-</option>
                    <option value="1">Year</option>
                    <option value="2">Gender</option>
                    <option value="3">School</option>
                </select><br/><br/>
                Second Choice:
                <select name="secondChoice" size="1"
                        onchange="setOptions(document.myform.secondChoice.options[document.myform.secondChoice.selectedIndex].value, document.myform.thirdChoice);">
                    <option value="00" selected="selected">-Select one of the options above-</option>
                </select><br/><br/>
                Third Choice:
                <select name="thirdChoice" size="1">
                    <option value="0" selected="selected">-Select one of the options above-</option>
                </select><br/><br/>
                Date and Time (dd/mm/yyyy HH:MM AM)
                <br/>
                <input type='datetime-local' name='dateTime' required><br/><br/>
                <%
                    String err = (String) request.getAttribute("errMsg");
                    if (err != null) {
                        out.println("<p class='error'>" + err + "</p>");
                    }
                    String timeErr = (String) request.getAttribute("error");
                    if (timeErr != null) {
                        out.println("<p class='error'>" + timeErr + "</p>");
                    }
                %>
                <input class="btn btn-lg btn-primary btn-block" type="submit" value="submit">
            </form>  
            <%
                }
            %>
            <div class="results">
                <div class="col-xs-12 col-md-8">
                    <br/>
                    <br><br>
                    <%
                        ArrayList<String> result = (ArrayList<String>) request.getAttribute("output");
                        ArrayList<String> result2 = (ArrayList<String>) request.getAttribute("output2");
                        ArrayList<String> result3 = (ArrayList<String>) request.getAttribute("output3");

                        Integer span = (Integer) request.getAttribute("spanValue");
                        Integer span2 = (Integer) request.getAttribute("spanValue2");
                        String header = (String) request.getAttribute("header");
                        String[] headerArr = null;
                        if (header != null) {
                            headerArr = header.split(",");
                        }
                        if (span == null) {
                            span = 1;
                        }
                    %> 
                    <% if (result3 != null) { %>
                    <table class="table table-striped" border=1 align="center">
                        <% if (headerArr.length == 9) { %>
                        <tr class="danger">
                            <% for (int i = 0; i < headerArr.length; i++) {%>   
                            <th><%= headerArr[i]%></th>
                                <% } %>
                        </tr>
                        <%  if (result3.isEmpty()) { %>
                        <tr class="active"><td class="lalign" colspan =9>No data available for this time slot</td></tr><br>
                        <%  } %>
                        <% } %>
                        <% for (int j = 0; j < result3.size(); j++) {
                                String currLine = result3.get(j);
                                String[] arr = currLine.split(",");  %>
                        <tr class="active">
                            <% for (int i = 0; i < arr.length; i++) {
                                    if (i < 3) {
                                        if (j % span == 0) {%>
                            <td rowspan= '<%= span%>'>
                                <% if (arr[i].equals("NaN")) {%>
                                <%= "0"%>
                                <% } else {%>
                                <%= arr[i]%>
                                <% } %>
                            </td>
                            <% } %>
                            <%  } else if (i >= 3 && i < 6) {
                                if (j % span2 == 0) {%>
                            <td rowspan='<%= span2%>'>
                                <% if (arr[i].equals("NaN")) {%>
                                <%= "0"%>
                                <% } else {%>
                                <%= arr[i]%>
                                <% } %>
                            </td>
                            <% }
                            } else {%>
                            <td>
                                <% if (arr[i].equals("NaN")) {%>
                                <%= "0"%>
                                <% } else {%>
                                <%= arr[i]%>
                                <% } %>
                            </td>   
                            <% }
                                } %>
                        </tr>  
                        <%  } %>
                    </table>
                    <br/>
                    <% } %>
                    <% if (result != null) {
                    %>
                    <table class="table table-striped" border = 1 align="center">
                        <% if (headerArr.length == 3) { %>
                        <tr class="danger">
                            <% for (int i = 0; i < headerArr.length; i++) {%>   
                            <th><%= headerArr[i]%></th>
                                <% } %>
                        </tr>
                        <%  if (result.isEmpty()) { %>
                        <tr><td class="lalign" colspan =3>No data available for this time slot</td></tr><br>
                        <%  } %>
                        <% } %>
                        <% for (String currLine : result) {
                                String[] arr = currLine.split(",");
                        %>
                        <tr class="active">
                            <% for (String curr : arr) {%>
                            <td>
                                <% if (curr.equals("NaN")) {%>
                                <%= "0"%>
                                <% } else {%>
                                <%= curr%>
                                <% } %>
                            </td>
                            <% } %>
                        </tr>
                        <%
                            } %>   
                    </table>
                    <% } %>   
                    <%    if (result2 != null) {
                    %>
                    <br/>
                    <table class="table table-striped" border = 1 align="center">
                        <% if (headerArr.length == 6) { %>
                        <tr class="danger">
                            <% for (int i = 0; i < headerArr.length; i++) {%>   
                            <th><%= headerArr[i]%></th>
                                <% } %>
                        </tr>
                        <%  if (result2.isEmpty()) { %>
                        <tr class="active"><td  class="lalign" colspan =6>No data available for this time slot</td></tr><br>
                        <%  } %>
                        <% } %>
                        <% for (int j = 0; j < result2.size(); j++) {
                                String currLine = result2.get(j);
                                String[] arr = currLine.split(",");
                        %>
                        <tr class="active">
                            <% for (int i = 0; i < arr.length; i++) { %>
                            <% if (i < 3) { %>
                            <% if (j % span == 0) {%> 
                            <td rowspan='<%= span%>'>
                                <%= arr[i]%>      
                            </td>
                            <% } %>
                            <% } else {%>
                            <td>
                                <% if (arr[i].equals("NaN")) {%>
                                <%= "0"%>
                                <% } else {%>
                                <%= arr[i]%>
                                <% } %>
                            </td>
                            <% } %>
                            <% } %>
                        </tr>
                        <%
                            } %>   
                    </table>
                    <% }%>   
                </div>
            </div>
        </div>
    </body>
</html>
