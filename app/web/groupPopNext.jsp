<%-- 
    Document   : home
    Created on : Sep 16, 2014, 10:53:01 PM
    Author     : G3T2
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="sloca.model.LocationLookupDAO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="sloca.model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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

        <title>The top K next places for groups</title>

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
                            <li ><a href="automaticGroupIdentification.jsp">Automatic Group Identification</a></li>
                            <!--<li><a href="groupAwareLocationReport.jsp">Group-aware Location Reports</a></li>-->
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Group-aware Location Reports <span class="caret"></span></a>
                                <ul class="dropdown-menu active" role="menu">
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




            <br/>
            <%
                String r = (String) request.getAttribute("rank");
                String t = (String) request.getAttribute("dateTime");
                String s = (String) request.getAttribute("semanticPlace");

            %>
            <div class ="universalform">
                <p align="center">
                    <span style="color: rgb(31, 73, 125); font-family: Aharoni; font-style: normal; font-variant: normal; letter-spacing: normal; line-height: 28px; orphans: auto; text-align: center; text-indent: 0px; text-transform: none; white-space: normal; widows: auto; word-spacing: 0px; -webkit-text-stroke-width: 0px; display: inline !important; float: none;">
                        <font size="4">The Top-K popular NEXT place(s) for groups</font></span></p>
                <!--<h1>The Top-K popular NEXT place(s)</h1>-->
                <% ArrayList<String> semanticPlaces = LocationLookupDAO.retrieveAllSemanticPlaces();%>
                <form action='GroupProcessTopNextPlaces' method='post' align="center">
                    Choice the K value:<select name='kValue'>
                        <%
                            if (r != null && t != null) {
                                t = t.replace(' ', 'T');
                                out.println("T: " + t);
                                int rr = Integer.parseInt(r);
                                for (int i = 1; i < 11; i++) {
                                    if (i == rr) {
                                        out.println("<option selected='selected'>" + i + "</option>");
                                    } else {
                                        out.println("<option>" + i + "</option>");
                                    }
                                }
                        %>
                    </select><br/><br/>
                    Date and Time (dd/mm/yyyy HH:MM AM):</br>
                    <input type='datetime-local' name='dateTime' value=<%=t%>><br>
                    <br/>
                    The origin: <select name='origin'>
                        <%for (String curr : semanticPlaces) {%>
                        <%if (curr.equals(s)) {%>
                        <option selected='selected'><%= curr%></option>
                        <%} else {%>
                        <option><%= curr%></option>
                        <% }
                            } %>
                    </select>
                    <br/><br/>
                    <input class="btn btn-lg btn-primary btn-block" type="submit">
                    <br/>
                    <%
                    } else {
                        for (int i = 1; i < 11; i++) {
                            if (i == 3) {
                                out.println("<option selected='selected'>" + i + "</option>");
                            } else {
                                out.println("<option>" + i + "</option>");
                            }
                        }
                    %></select><br/><br/>
                    Date and Time (dd/mm/yyyy HH:MM AM):</br>
                    <input type='datetime-local' name='dateTime' required><br>
                    <br/>
                    The origin: </br>
                    <select name='origin'>
                        <%for (String curr : semanticPlaces) {%>
                        <option><%= curr%></option>
                        <% } %>
                    </select>
                    <br/><br/>
                    <input class="btn btn-lg btn-primary btn-block" type="submit">
                    <br/>

                    <%
                        }
                        String timeErr = (String) request.getAttribute("error");
                        if (timeErr != null) {
                            out.println("<p='error'>" + timeErr + "</p>");
                        }
                        if ((Integer) request.getAttribute("semanGroup") != null) {
                            out.println("<table class='table table-striped' align='center'>");
                            int totalGroupHere = (Integer) request.getAttribute("semanGroup");
                            if (totalGroupHere == 0) {
                            } else {
                                out.println("<tr class='danger'>");
                                out.println("<td>Total Group: " + request.getAttribute("totalGroup") + "</td>");
                                out.println("</tr>");
                                out.println("<tr class='danger'>");
                                out.println("<td>Next Place Groups: " + request.getAttribute("semanGroup") + "</td>");
                                out.println("</tr>");
                            }
                            out.println("</table>");
                        }

                    %>

                </form>
                <br/>
            </div>
            <div class="col-xs-12 col-md-6">
                <%HashMap<Integer, HashMap<String, Integer>> ranking = (HashMap<Integer, HashMap<String, Integer>>) request.getAttribute("ranking");
                    if (ranking != null) {
                        Iterator<Integer> iter3 = ranking.keySet().iterator();
                        HashMap<String, Integer> underRanking3 = new HashMap<String, Integer>();
                        while (iter3.hasNext()) {
                            out.println("<tr>");
                            int rank = iter3.next();
                            underRanking3 = ranking.get(rank);
                            break;
                        }

                        out.println("<table class='table table-striped' border='1' align='center'>");
                        out.println("<thead>");
                        out.println("<tr class='danger'>");
                        out.println("<th>Rank</th>");
                        out.println("<th>Semantic-Place</th>");
                        out.println("<th>Count</th>");
                        out.println("<th>Percentage</th>");
                        out.println("</tr>");
                        out.println("</thead>");

                        if (underRanking3.size() == 0) {
                            out.println("<tbody>");
                            out.println("<tr class='active'>");
                            out.println("<td colspan =4>No Group applicable</td>");
                            out.println("</tr>");
                            out.println("</tbody>");
                        } else {

                            out.println("<tbody>");
                            Iterator<Integer> iter = ranking.keySet().iterator();
                            while (iter.hasNext()) {

                                int rank = iter.next();
                                HashMap<String, Integer> underRanking = ranking.get(rank);
                                Iterator<String> iter2 = underRanking.keySet().iterator();
                                while (iter2.hasNext()) {
                                    String semantic = iter2.next();
                                    int count = underRanking.get(semantic);
                                    int percentage = count * 100 / ((Integer) request.getAttribute("totalGroup"));
                                    out.println("<tr class='active'>");
                                    out.println("<td>" + rank + "</td>");
                                    out.println("<td>" + semantic + "</td>");
                                    out.println("<td>" + count + "</td>");
                                    out.println("<td>" + percentage + "</td>");
                                    out.println("</tr>");
                                }

                            }

                            out.println("</tbody>");
                        }
                        out.println("</table>");

                    }
                %>
            </div>
    </body>
</html>