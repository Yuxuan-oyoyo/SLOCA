<%-- 
    Document   : home
    Created on : Sep 16, 2014, 10:53:01 PM
    Author     : G3T2
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.HashMap"%>
<%@page import="sloca.model.Group"%>
<%@page import="java.util.ArrayList"%>
<%@page import="sloca.model.User"%>
<%@ include file = "protect.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="shortcut icon" href="CSS/assets/ico/favicon.png">
        <link href ="CSS/assets/css/docs.css" rel="stylesheet" type="text/css">
        <title>Automatic Group Identification</title>
        <!-- Bootstrap core CSS -->
        <link href="CSS/dist/css/bootstrap.css" rel="stylesheet">
        <link href="navbar.css" rel="stylesheet">
    </head>
    <body background="img/tete.jpg">
        <div class="container">
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
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Basic Location Report <span class="caret"></span></a>
                                <ul class="dropdown-menu" role="menu">
                                    <li><a href="breakDown.jsp">Breakdown by year and gender</a></li>
                                    <li><a href="popularPlace.jsp">Top-k popular places</a></li>
                                    <li><a href="popularCompanions.jsp">Top-k companions</a></li>
                                    <li><a href="topKNextPlaces.jsp">Top-k next places</a></li>
                                </ul>
                            </li>
                            <li class="active"><a href="automaticGroupIdentification.jsp">Automatic Group Identification</a></li>
                            <!--<li><a href="groupAwareLocationReport.jsp">Group-aware Location Reports</a></li>-->
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Group-aware Location Reports <span class="caret"></span></a>
                                <ul class="dropdown-menu" role="menu">
                                    <li><a href="groupPop.jsp">Top-k popular places for groups</a></li>
                                    <li><a href="groupPopNext.jsp">The top K next places for groups</a></li>
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


            <%
                String defaultTime = (String) request.getParameter("dateTime");
            %>
            <br/>
            <div class ="universalform">
                <form action='ProcessAutomaticGroupIdentification.do' method='post' align="center">
                    <p align="center">
                        <span style="color: rgb(31, 73, 125); font-family: Aharoni; font-style: normal; font-variant: normal; letter-spacing: normal; line-height: 28px; orphans: auto; text-align: center; text-indent: 0px; text-transform: none; white-space: normal; widows: auto; word-spacing: 0px; -webkit-text-stroke-width: 0px; display: inline !important; float: none; ">
                            <font size="4">Automatic Group Identification</font></span>
                    </p>
                    <%if (defaultTime != null) {
                    %>
                    Date and Time (dd/mm/yyyy HH:MM AM):</br>
                    <input type='datetime-local' name='dateTime' value=<%=defaultTime%>><br/>         
                    <%
                    } else {
                    %>
                    Date and Time (dd/mm/yyyy HH:MM AM):</br>
                    <input type='datetime-local' name='dateTime' required><br/>

                    <%
                        }%>
                    <br>
                    <input class="btn btn-lg btn-primary btn-block" type="submit">
                    <br/>
                </form>
                <%
                    String timeErr = (String) request.getAttribute("error");
                    if (timeErr != null) {
                        out.println("<p class='error'>" + timeErr + "</p>");
                    }
                    ArrayList<Group> groups = (ArrayList<Group>) request.getAttribute("groups");
                    HashMap<String, String> macEmail = (HashMap<String, String>) request.getAttribute("macEmail");
                    if (groups != null) {
                        if (macEmail.size() != 0) {
                            out.println("<br/>");
                            out.println("<table class='table table-striped'>");
                            out.println("<tr class='danger'>");
                            out.println("<td>Total Group: " + groups.size() + "</td>");
                            out.println("</tr>");
                            out.println("<tr class='danger'>");
                            out.println("<td>Total User: " + macEmail.size() + "</td>");
                            out.println("</tr>");
                            out.println("</table>");
                        } else {
                            out.println("<table class='table table-striped'>");
                            out.println("<tr class='danger'>");
                            out.println("<td>No data available for this time slot</td>");
                            out.println("</tr>");
                            out.println("</table>");
                        }%>
            </div>
            <div class="results">
                <div class="col-xs-12 col-md-8">
                    <%
                        if (groups != null && groups.size() != 0) {
                    %>
                    <p><span style="background-color: #FFFFCC">
                            <font size="6" face="Adobe Gothic Std B">Group details:</font></span></p>
                            <%
                                for (Group group : groups) {
                                    int totalTimeSpent = 0;
                                    ArrayList<String> users = group.getUsers();

                            %>

                    <table class="table table-striped" border="1" align="center">
                        <tr class="success">
                            <td>Group Size</td>
                            <td><%=users.size()%></td>
                        </tr>
                        <tr class="warning">
                            <td>Members</td>
                            <td>
                                <table class="table table-striped">
                                    <tr>
                                        <td>macAddress</td>
                                        <td>Email</td>
                                    </tr>
                                    <%for (String u : users) {%>
                                    <tr>
                                        <td><%=u%></td>
                                        <%
                                            String email = macEmail.get(u);
                                            if (email.equalsIgnoreCase("null")) {
                                                email = "";
                                            }
                                        %>
                                        <td><%=email%></td>
                                    </tr>
                                    <%}%>
                                </table>
                            </td>
                        </tr>
                        <tr class="success">
                            <td>Locations</td>
                            <td>
                                <table class="table table-striped">
                                    <tr>
                                        <td>Location ID</td>
                                        <td>Time Spent</td>  
                                    </tr>
                                    <%
                                        HashMap<String, ArrayList<Timestamp>> maps = group.getTimeLine();
                                        Iterator<String> iter = maps.keySet().iterator();
                                        while (iter.hasNext()) {
                                    %>
                                    <tr>
                                        <%
                                            String location = iter.next();
                                            out.println("<td>" + location + "</td>");
                                            ArrayList<Timestamp> times = maps.get(location);
                                            int totalTime = 0;
                                            for (int i = 0; i < times.size(); i = i + 2) {
                                                Timestamp t1 = times.get(i);
                                                Timestamp t2 = times.get(i + 1);

                                                int timeGap = (int) (t2.getTime() - t1.getTime()) / 1000;
                                                totalTime += timeGap;
                                            }
                                            out.println("<td>" + totalTime + "</td>");

                                            totalTimeSpent += totalTime;
                                        %>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </table>
                        <tr class="warning">
                            <td align="center">Total Time Spent</td>
                            <td align="center"><%=totalTimeSpent%></td>
                        </tr>
                        </td>
                        </tr>
                    </table>
                    <br/>
                    <%}
                            }
                        }%>
                </div>
            </div>
    </body>
</html>
