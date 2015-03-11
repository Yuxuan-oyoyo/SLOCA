<%-- 
   Document   : home
   Created on : Sep 16, 2014, 10:53:01 PM
   Author     : G3T2
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="sloca.model.HeatmapDAO"%>
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

        <!--Legend-->
        <link rel="stylesheet" href="CSS/dist/css/heatmaplegend.css">

        <title>Heat Map</title>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>

        <!-- Bootstrap core CSS -->
        <link href="CSS/dist/css/bootstrap.css" rel="stylesheet">
        <link href="navbar.css" rel="stylesheet">

        <!--DatePicker-->
        <meta charset="utf-8">
        <title>jQuery UI Datepicker - Default functionality</title>
        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.1/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/jquery-1.10.2.js"></script>
        <script src="//code.jquery.com/ui/1.11.1/jquery-ui.js"></script>
        <link rel="stylesheet" href="/resources/demos/style.css">

        <!--Method #1 of Initializing Bootstrap--> 
        <script src="https://code.jquery.com/jquery-1.11.0.min.js"></script>
        <script src="https://code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>


    </head>
    <body background="img/tete.jpg">
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
                            <li class="active"><a href="heatMap.jsp">Heat Map</a></li>
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
                            <li><a href="automaticGroupIdentification.jsp">Automatic Group Identification</a></li>
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
            <%
                String level2 = (String) request.getAttribute("level");
                String dateTime = (String) request.getAttribute("dateTime");
            %>
            <div class="universalform">
                <p align="center">
                    <span style="color: rgb(31, 73, 125); font-family: Aharoni; font-style: normal; font-variant: normal; letter-spacing: normal; line-height: 28px; orphans: auto; text-align: center; text-indent: 0px; text-transform: none; white-space: normal; widows: auto; word-spacing: 0px; -webkit-text-stroke-width: 0px; display: inline !important; float: none; ">
                        <font size="4">Heatmap</font></span></p>


                <%
                    if (level2 != null && dateTime != null) {
                %>

                <form action="HeatMapController.do" align="center">
                    <h5>Floor:<select name="floor" required>
                            <%
                                String levelName = "";
                                for (int i = 0; i < 6; i++) {
                                    if (i == 0) {
                                        levelName = "B1";
                                    } else if (i == 1) {
                                        levelName = "L1";
                                    } else if (i == 2) {
                                        levelName = "L2";
                                    } else if (i == 3) {
                                        levelName = "L3";
                                    } else if (i == 4) {
                                        levelName = "L4";
                                    } else if (i == 5) {
                                        levelName = "L5";
                                    }
                                    String strI = "" + i;
                                    if (levelName.equals(level2)) {
                                        out.println("<option value = " + strI + " selected='selected'>" + levelName + "</option>");
                                    } else {
                                        out.println("<option value = " + strI + ">" + levelName + "</option>");
                                    }
                                }
                            %>
                        </select></h5>
                    <p>Date and Time (dd/mm/yyyy HH:MM AM): </br>
                        <input type='datetime-local' name='dateTime' value=<%=dateTime%>></p>
                    <input class="btn btn-lg btn-primary btn-block" type="submit">   
                </form>

                <%
                    String errorMsg = (String) request.getAttribute("error");
                    ArrayList<String> errList = (ArrayList<String>) request.getAttribute("errorList");
                    if (errorMsg != null) {
                %>
                <p class ="error"><%=errorMsg%></p>
                <%

                    }
                    if (errList != null) {
                        for (String err : errList) {
                %>
                <br/>
                <p class="error">err</p>
                <%
                        }
                    }
                %>


                <%
                } else {
                %>

                <form action="HeatMapController.do" align ="center">
                    <h5>Floor:<select name="floor" required>
                            <option value = "0">B1</option>
                            <option value = "1">L1</option>
                            <option value = "2">L2</option>
                            <option value = "3">L3</option>
                            <option value = "4">L4</option>
                            <option value = "5">L5</option>
                        </select></h5>


                    <p>Date and Time (dd/mm/yyyy HH:MM AM): </br>
                        <input type='datetime-local' name='dateTime' required></p>


                    <input class="btn btn-lg btn-primary btn-block" type="submit">  
                </form>
            </div>
            <%
                String errorMsg = (String) request.getAttribute("error");
                ArrayList<String> errList = (ArrayList<String>) request.getAttribute("errorList");
                if (errorMsg != null) {
            %>
            <p class ="error"><%=errorMsg%></p>

            <%

                }
                if (errList != null) {
                    for (String err : errList) {
//                        out.println(err + "</br>");
            %>
            <br/>
            <p class="error">err</p>
            <%
                    }
                }
            %>

            <%
                }
            %>




            </br>
        </div>

        <%
            HashMap<String, Integer> results = (HashMap<String, Integer>) session.getAttribute("heatmap");
            if (results != null) { %>
        <div class ="heatmapresults">
            <div class ="heatmaplegend">
                <div class="col-md-3">

                    <legend>
                        <h3>Legend</h3>
                    </legend>
                    <ul id="legend">
                        <li>
                            <span class="color-block" ></span>
                            0
                        </li>

                        <li>
                            <span class="color-block"></span>
                            1 to 2
                        </li>

                        <li>
                            <span class="color-block"></span>
                            3 to 5
                        </li>

                        <li>
                            <span class="color-block"></span>
                            6 to 10
                        </li>

                        <li>
                            <span class="color-block"></span>
                            11 to 20
                        </li>

                        <li>
                            <span class="color-block"></span>
                            21 to 30
                        </li>

                        <li>
                            <span class="color-block"></span>
                            31 and more
                        </li>
                    </ul>
                </div>	
            </div>
            <br/><br/>
            <div class="col-xs-12 col-md-4">
                <table class="table table-striped" border='1' align="center"> 
                    <tr class="danger">
                        <th>Place</th>
                        <th>Density</th>
                        <th>Number</th>
                    </tr>
                    <% Iterator ite = results.keySet().iterator();
                        while (ite.hasNext()) {
                            String place = (String) ite.next();
                            int number = results.get(place);
                            int intensity = 0;
                            if (number == 0) {
                                intensity = 0;
                            } else if (number <= 2) {
                                intensity = 1;
                            } else if (number <= 5) {
                                intensity = 2;
                            } else if (number <= 10) {
                                intensity = 3;
                            } else if (number <= 20) {
                                intensity = 4;
                            } else if (number <= 30) {
                                intensity = 5;
                            } else if (number > 30) {
                                intensity = 6;
                            }
                    %> 
                    <tr class="active">
                        <td><%=place%></td>
                        <td><%=intensity%></td>
                        <td><%=number%></td>
                    </tr>  
                    <% }
                        }
                    %>

                </table>
            </div>
            <br/>
            <br/>
            <div class="col-xs-12 col-md-6">
                <br/>
                <%
                    String dateTimeEnd = (String) request.getAttribute("dateTimeEnd");
                    String level = (String) request.getAttribute("levelDisplay");
                    System.out.println("double check : " + level);
                    if (level != null && dateTimeEnd != null) {
                        dateTimeEnd.replace("T", " at ");
                        out.println("<p align ='center'>You have selected : " + level);
                        out.println(" on " + dateTimeEnd + "</p>");
                    }
                %>
            </div>

        </div>


        <script>
            var bimboColors = ['rgb(240,249,232)', 'rgb(204,235,197)', 'rgb(168,221,181)', 'rgb(123,204,196)', 'rgb(78,179,211)', 'rgb(43,140,190)', 'rgb(8,88,158)'];
            $.getJSON('json/heatmapDisplay', function(result) {
                result.heatmap.forEach(function(area, i) {
                    var semanticPlace = area['semantic-place'];
                    var crowdDensity = area['crowd-density'];
                    $('#' + semanticPlace).css('fill', bimboColors[crowdDensity]);
                    console.log(semanticPlace + ' : ' + crowdDensity);

                });
            });

        </script>
        <%if (level != null) {%>
        <%if (level.equals("B1")) {  %>
        <!--insert B1 floor plan-->
        <div class="heatmapdiagram">
            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                 width="800px" height="600px" viewBox="0 0 800 600" enable-background="new 0 0 800 600" xml:space="preserve">
            <path fill="#494949" stroke="#000000" stroke-miterlimit="10" d="M99.604,143.5l0.407-109h602.253l-1.781,92.782l38.327,0.788
                  l-78.055,85.43H625.5v41h-244v87h1.145l-0.134-11.612c0,0,63.39,5.511,86.991,33.995c23.602,28.486,0.814,54.617,0.814,54.617
                  h-8.139l-1.629,9.023l-17.904,7.361l-5.696,19.55l-15.464,7.334l-3.484,19.539l-18.489,7.327l-8.953,17.091l-18.72,5.783
                  l-7.324,17.991h-19.533l-8.952,17.818l-17.905,5.802l-12.208,16.38h-9.766l1.628,7.586c0,0-0.814,4.607-21.16,3.793
                  c-20.346-0.812-69.178-27.347-93.594-55.017c-24.416-27.672-34.996-75.362-34.996-75.362h6.511l0.814-6.162l-48.019-2.673
                  c0,0,11.395-62.783,18.719-64.412c7.325-1.627-7.325,1.571-7.325,1.571l-4.069-128.618l12.208-1.267L93.5,143.5H99.604z"/>
            <polygon fill="#D8D8D8" stroke="#000000" stroke-miterlimit="10" points="625.5,213.553 625.5,254.5 382.5,254.5 382.5,389.347 
                     333.5,390.159 333.5,340.514 230.5,341.327 230.5,408.879 208.662,409.104 208.254,338.5 182.5,338.5 182.5,271.337 180.582,151.5 
                     625.5,151.5 "/>
            <rect x="230.5" y="341.5" fill="#D8D8D8" stroke="#D8D8D8" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="282.5" fill="#D8D8D8" stroke="#D8D8D8" stroke-miterlimit="10" width="100" height="28"/>
            <rect x="224.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="51" height="51"/>
            <rect x="275.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="31" height="31"/>
            <rect x="306.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="44" height="51"/>
            <rect x="382.5" y="268.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="37" height="51"/>
            <rect x="402.5" y="196.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="99" height="58"/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="230.5,389.5 382.5,389.5 382.5,426.5 314.055,426.5 
                     295.336,538.139 202.557,522.672 199.708,442.5 187.5,442.5 187.5,409.691 230.5,408.879 "/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="203.371,169.5 180.87,169.5 179.5,143.5 94.314,143.5 
                     115.474,211.895 205.812,212.671 "/>
            <path fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" d="M115.338,212.672l15.803,51.712c0,0,4.646,9.852,9.801,13.107
                  c5.153,3.255,10.333,3.162,10.333,3.162l31.225-9.045v-24.687l25.491-0.248l-1.903-33.934"/>
            <g>
            <!--main rectangle-->
            <rect id='SMUSISB1CORRIDORTOSOE' x="97.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="160" height="117"/>
            <!--bg for text-->
            <rect x="97.568" y="80.895" fill="none" width="153.006" height="38.522"/>
            <!--text label-->
            <text transform="matrix(1 0 0 1 155.3979 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Corridor to SOE</text>
            </g>
            <g>
            <rect id='SMUSISB1NEAROSL' x="290.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="118" height="117"/>
            <rect x="290.726" y="80.895" fill="none" width="116.106" height="38.522"/>
            <text transform="matrix(1 0 0 1 330.1055 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Near OSL</text>
            </g>
            <g>
            <rect id='SMUSISB1CORRIDORTOLKS' x="558.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="144" height="117"/>
            <rect x="559.025" y="80.895" fill="none" width="142.422" height="38.522"/>
            <text transform="matrix(1 0 0 1 591.5625 99.4868)" font-family="'Arial-BoldMT'" font-size="12">Corridor to LKS</text>
            </g>
            <g>
            <rect id='SMUSISB1STUDYAREA' x="402.5" y="196.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="223" height="58"/>
            <rect x="402.215" y="222.994" fill="none" width="125.547" height="32.506"/>
            <text transform="matrix(1 0 0 1 470.3086 231.5859)" font-family="'Arial-BoldMT'" font-size="12">Study Area</text>
            </g>
            <g>
            <rect id='SMUSISB1LIFTLOBBY' x="333.5" y="251.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="49" height="88"/>
            <rect x="333.5" y="302.609" fill="none" width="49" height="57.781"/>
            <text transform="matrix(1 0 0 1 340.0029 311.2012)" font-family="'Arial-BoldMT'" font-size="12">Lobby</text>
            </g>
            </svg>
            <%} else if (level.equals("L1")) {%>
            <!--insert L1 Floor plan-->
            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                 width="800px" height="600px" viewBox="0 0 800 600" enable-background="new 0 0 800 600" xml:space="preserve">
            <path fill="#494949" stroke="#000000" stroke-miterlimit="10" d="M99.604,143.5l0.407-109h602.253l-1.781,92.782l38.327,0.788
                  l-78.055,85.43H625.5v41h-244v87h1.145l-0.134-11.612c0,0,63.39,5.511,86.991,33.995c23.602,28.486,0.814,54.617,0.814,54.617
                  h-8.139l-1.629,9.023l-17.904,7.361l-5.696,19.55l-15.464,7.334l-3.484,19.539l-18.489,7.327l-8.953,17.091l-18.72,5.783
                  l-7.324,17.991h-19.533l-8.952,17.818l-17.905,5.802l-12.208,16.38h-9.766l1.628,7.586c0,0-0.814,4.607-21.16,3.793
                  c-20.346-0.812-69.178-27.347-93.594-55.017c-24.416-27.672-34.996-75.362-34.996-75.362h6.511l0.814-6.162l-48.019-2.673
                  c0,0,11.395-62.783,18.719-64.412c7.325-1.627-7.325,1.571-7.325,1.571l-4.069-128.618l12.208-1.267L93.5,143.5H99.604z"/>
            <polygon fill="#D8D8D8" stroke="#000000" stroke-miterlimit="10" points="625.5,213.553 625.5,254.5 382.5,254.5 382.5,389.347 
                     333.5,390.159 333.5,340.514 230.5,341.327 230.5,408.879 208.662,409.104 208.254,338.5 182.5,338.5 182.5,271.337 180.582,151.5 
                     625.5,151.5 "/>
            <rect x="230.5" y="341.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="282.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="100" height="28"/>
            <rect x="224.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="51" height="51"/>
            <rect x="275.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="31" height="31"/>
            <rect x="306.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="44" height="51"/>
            <rect x="382.5" y="268.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="37" height="51"/>
            <rect x="402.5" y="196.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="99" height="58"/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="230.5,389.5 382.5,389.5 382.5,426.5 314.055,426.5 
                     295.336,538.139 202.557,522.672 199.708,442.5 187.5,442.5 187.5,409.691 230.5,408.879 "/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="203.371,169.5 180.87,169.5 179.5,143.5 94.314,143.5 
                     115.474,211.895 205.812,212.671 "/>
            <path fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" d="M115.338,212.672l15.803,51.712c0,0,4.646,9.852,9.801,13.107
                  c5.153,3.255,10.333,3.162,10.333,3.162l31.225-9.045v-24.687l25.491-0.248l-1.903-33.934"/>
            <g>
            <rect id="SMUSISL1WAITINGAREA" x="230.5" y="341.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="345.609" fill="none" width="103" height="48"/>
            <text transform="matrix(1 0 0 1 250.0029 365.2012)" font-family="'Arial-BoldMT'" font-size="12">Waiting Area</text>
            </g>

            <g>
            <rect id='SMUSISL1RECEPTION' x="149.5" y="245.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="59" height="93"/>
            <rect x="150.47" y="268.489" fill="none" width="56.969" height="57.78"/>
            <text transform="matrix(1 0 0 1 152.2881 297.0815)"><tspan x="0" y="0" font-family="'Arial-BoldMT'" font-size="12">Reception</tspan></text>
            </g>
            <g>
            <rect id='SMUSISL1LOBBY' x="333.5" y="251.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="49" height="138"/>
            <rect x="333.5" y="302.609" fill="none" width="49" height="57.781"/>
            <text transform="matrix(1 0 0 1 340.0029 311.2012)" font-family="'Arial-BoldMT'" font-size="12">Lobby</text>
            </g>
            </svg>  
            <%} else if (level.equals("L2")) {%>
            <!--level 2 floor plan-->
            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                 width="800px" height="600px" viewBox="0 0 800 600" enable-background="new 0 0 800 600" xml:space="preserve">
            <path fill="#494949" stroke="#000000" stroke-miterlimit="10" d="M99.604,143.5l0.407-109h602.253l-1.781,92.782l38.327,0.788
                  l-78.055,85.43H625.5v41h-244v87h1.145l-0.134-11.612c0,0,63.39,5.511,86.991,33.995c23.602,28.486,0.814,54.617,0.814,54.617
                  h-8.139l-1.629,9.023l-17.904,7.361l-5.696,19.55l-15.464,7.334l-3.484,19.539l-18.489,7.327l-8.953,17.091l-18.72,5.783
                  l-7.324,17.991h-19.533l-8.952,17.818l-17.905,5.802l-12.208,16.38h-9.766l1.628,7.586c0,0-0.814,4.607-21.16,3.793
                  c-20.346-0.812-69.178-27.347-93.594-55.017c-24.416-27.672-34.996-75.362-34.996-75.362h6.511l0.814-6.162l-48.019-2.673
                  c0,0,11.395-62.783,18.719-64.412c7.325-1.627-7.325,1.571-7.325,1.571l-4.069-128.618l12.208-1.267L93.5,143.5H99.604z"/>
            <polygon fill="#D8D8D8" stroke="#000000" stroke-miterlimit="10" points="625.5,213.553 625.5,254.5 382.5,254.5 382.5,389.347 
                     333.5,390.159 333.5,340.514 230.5,341.327 230.5,408.879 208.662,409.104 208.254,338.5 182.5,338.5 182.5,271.337 180.582,151.5 
                     625.5,151.5 "/>
            <rect x="230.5" y="341.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="282.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="100" height="28"/>
            <rect x="224.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="51" height="51"/>
            <rect x="275.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="31" height="31"/>
            <rect x="306.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="44" height="51"/>
            <rect x="382.5" y="268.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="37" height="51"/>
            <rect x="402.5" y="196.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="99" height="58"/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="230.5,389.5 382.5,389.5 382.5,426.5 314.055,426.5 
                     295.336,538.139 202.557,522.672 199.708,442.5 187.5,442.5 187.5,409.691 230.5,408.879 "/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="203.371,169.5 180.87,169.5 179.5,143.5 94.314,143.5 
                     115.474,211.895 205.812,212.671 "/>
            <path fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" d="M115.338,212.672l15.803,51.712c0,0,4.646,9.852,9.801,13.107
                  c5.153,3.255,10.333,3.162,10.333,3.162l31.225-9.045v-24.687l25.491-0.248l-1.903-33.934"/>
            <g>
            <!--main rectangle-->
            <rect id='SMUSISL2SR2-1' x="97.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="160" height="117"/>
            <!--bg for text-->
            <rect x="97.568" y="80.895" fill="none" width="153.006" height="38.522"/>
            <!--text label-->
            <text transform="matrix(1 0 0 1 155.3979 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 2-1</text>
            </g>
            <g>
            <rect id='SMUSISL2SR2-2' x="290.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="118" height="117"/>
            <rect x="290.726" y="80.895" fill="none" width="116.106" height="38.522"/>
            <text transform="matrix(1 0 0 1 330.1055 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 2-2</text>
            </g>
            <g>
            <rect id='SMUSISL2SR2-3' x="408.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="115" height="117"/>
            <rect x="408.191" y="80.895" fill="none" width="116.107" height="38.522"/>
            <text transform="matrix(1 0 0 1 447.5723 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 2-3</text>
            </g>
            <g>
            <rect id='SMUSISL2SR2-4' x="558.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="144" height="117"/>
            <rect x="559.025" y="80.895" fill="none" width="142.422" height="38.522"/>
            <text transform="matrix(1 0 0 1 611.5625 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 2-4</text>
            </g>
            <g>
            <rect id='SMUSISL2STUDYAREA1' x="501.5" y="196.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="124" height="58"/>
            <rect x="500.215" y="222.994" fill="none" width="125.547" height="32.506"/>
            <text transform="matrix(1 0 0 1 526.3086 231.5859)" font-family="'Arial-BoldMT'" font-size="12">Study Area 1</text>
            </g>
            <g>
            <rect id='SMUSISL2STUDYAREA2' x="149.5" y="245.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="59" height="93"/>
            <rect x="150.47" y="268.489" fill="none" width="56.969" height="57.78"/>
            <text transform="matrix(1 0 0 1 162.2881 277.0815)"><tspan x="0" y="0" font-family="'Arial-BoldMT'" font-size="12">Study Area 2</tspan><tspan x="3.325" y="14.4" font-family="'Arial-BoldMT'" font-size="12">Area </tspan><tspan x="13.33" y="28.8" font-family="'Arial-BoldMT'" font-size="12">2</tspan></text>
            </g>
            <g>
            <rect id='SMUSISL2LOBBY' x="333.5" y="251.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="49" height="138"/>
            <rect x="333.5" y="302.609" fill="none" width="49" height="57.781"/>
            <text transform="matrix(1 0 0 1 340.0029 311.2012)" font-family="'Arial-BoldMT'" font-size="12">Lobby</text>
            </g>
            </svg>  
            <%} else if (level.equals("L3")) {%>
            <!--level 3 floor plan-->

            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                 width="800px" height="600px" viewBox="0 0 800 600" enable-background="new 0 0 800 600" xml:space="preserve">
            <path fill="#494949" stroke="#000000" stroke-miterlimit="10" d="M99.604,143.5l0.407-109h602.253l-1.781,92.782l38.327,0.788
                  l-78.055,85.43H625.5v41h-244v87h1.145l-0.134-11.612c0,0,63.39,5.511,86.991,33.995c23.602,28.486,0.814,54.617,0.814,54.617
                  h-8.139l-1.629,9.023l-17.904,7.361l-5.696,19.55l-15.464,7.334l-3.484,19.539l-18.489,7.327l-8.953,17.091l-18.72,5.783
                  l-7.324,17.991h-19.533l-8.952,17.818l-17.905,5.802l-12.208,16.38h-9.766l1.628,7.586c0,0-0.814,4.607-21.16,3.793
                  c-20.346-0.812-69.178-27.347-93.594-55.017c-24.416-27.672-34.996-75.362-34.996-75.362h6.511l0.814-6.162l-48.019-2.673
                  c0,0,11.395-62.783,18.719-64.412c7.325-1.627-7.325,1.571-7.325,1.571l-4.069-128.618l12.208-1.267L93.5,143.5H99.604z"/>
            <polygon fill="#D8D8D8" stroke="#000000" stroke-miterlimit="10" points="625.5,213.553 625.5,254.5 382.5,254.5 382.5,389.347 
                     333.5,390.159 333.5,340.514 230.5,341.327 230.5,408.879 208.662,409.104 208.254,338.5 182.5,338.5 182.5,271.337 180.582,151.5 
                     625.5,151.5 "/>
            <rect x="230.5" y="341.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="282.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="100" height="28"/>
            <rect x="224.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="51" height="51"/>
            <rect x="275.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="31" height="31"/>
            <rect x="306.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="44" height="51"/>
            <rect x="382.5" y="268.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="37" height="51"/>

            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="230.5,389.5 382.5,389.5 382.5,426.5 314.055,426.5 
                     295.336,538.139 202.557,522.672 199.708,442.5 187.5,442.5 187.5,409.691 230.5,408.879 "/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="203.371,169.5 180.87,169.5 179.5,143.5 94.314,143.5 
                     115.474,211.895 205.812,212.671 "/>
            <path fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" d="M115.338,212.672l15.803,51.712c0,0,4.646,9.852,9.801,13.107
                  c5.153,3.255,10.333,3.162,10.333,3.162l31.225-9.045v-24.687l25.491-0.248l-1.903-33.934"/>
            <g>
            <!--main rectangle-->
            <rect id='SMUSISL3SR3-1' x="97.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="160" height="117"/>
            <!--bg for text-->
            <rect x="97.568" y="80.895" fill="none" width="153.006" height="38.522"/>
            <!--text label-->
            <text transform="matrix(1 0 0 1 155.3979 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 3-1</text>
            </g>
            <g>
            <rect id='SMUSISL3SR3-2' x="290.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="118" height="117"/>
            <rect x="290.726" y="80.895" fill="none" width="116.106" height="38.522"/>
            <text transform="matrix(1 0 0 1 330.1055 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 3-2</text>
            </g>
            <g>
            <rect id='SMUSISL3SR3-3' x="408.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="115" height="117"/>
            <rect x="408.191" y="80.895" fill="none" width="116.107" height="38.522"/>
            <text transform="matrix(1 0 0 1 447.5723 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 3-3</text>
            </g>
            <g>
            <rect id='SMUSISL3SR3-4' x="558.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="144" height="117"/>
            <rect x="559.025" y="80.895" fill="none" width="142.422" height="38.522"/>
            <text transform="matrix(1 0 0 1 611.5625 89.4868)" font-family="'Arial-BoldMT'" font-size="12">SR 3-4</text>
            </g>
            <g>
            <rect id='SMUSISL3STUDYAREA1' x="501.5" y="196.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="124" height="58"/>
            <rect x="500.215" y="222.994" fill="none" width="125.547" height="32.506"/>
            <text transform="matrix(1 0 0 1 526.3086 231.5859)" font-family="'Arial-BoldMT'" font-size="12">Study Area 1</text>
            </g>
            <g>
            <rect id='SMUSISL3CLSRM' x="402.5" y="196.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="99" height="58"/>
            <rect x="401.215" y="222.994" fill="none" width="125.547" height="32.506"/>
            <text transform="matrix(1 0 0 1 426.3086 231.5859)"><tspan x="0" y="0" font-family="'Arial-BoldMT'" font-size="12">Class Room</tspan>
            </g>
            <g>
            <rect id='SMUSISL3STUDYAREA2' x="149.5" y="245.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="59" height="93"/>
            <rect x="150.47" y="268.489" fill="none" width="56.969" height="57.78"/>
            <text transform="matrix(1 0 0 1 162.2881 277.0815)"><tspan x="0" y="0" font-family="'Arial-BoldMT'" font-size="12">Study</tspan><tspan x="3.325" y="14.4" font-family="'Arial-BoldMT'" font-size="12">Area </tspan><tspan x="13.33" y="28.8" font-family="'Arial-BoldMT'" font-size="12">2</tspan></text>
            </g>
            <g>
            <rect id='SMUSISL3LOBBY' x="333.5" y="251.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="49" height="138"/>
            <rect x="333.5" y="302.609" fill="none" width="49" height="57.781"/>
            <text transform="matrix(1 0 0 1 340.0029 311.2012)" font-family="'Arial-BoldMT'" font-size="12">Lobby</text>
            </g>
            </svg> 
            <%} else if (level.equals("L4")) {%>
            <!--insert l4 floor plan-->
            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                 width="800px" height="600px" viewBox="0 0 800 600" enable-background="new 0 0 800 600" xml:space="preserve">
            <path fill="#494949" stroke="#000000" stroke-miterlimit="10" d="M99.604,143.5l0.407-109h602.253l-1.781,92.782l38.327,0.788
                  l-78.055,85.43H625.5v41h-244v87h1.145l-0.134-11.612c0,0,63.39,5.511,86.991,33.995c23.602,28.486,0.814,54.617,0.814,54.617
                  h-8.139l-1.629,9.023l-17.904,7.361l-5.696,19.55l-15.464,7.334l-3.484,19.539l-18.489,7.327l-8.953,17.091l-18.72,5.783
                  l-7.324,17.991h-19.533l-8.952,17.818l-17.905,5.802l-12.208,16.38h-9.766l1.628,7.586c0,0-0.814,4.607-21.16,3.793
                  c-20.346-0.812-69.178-27.347-93.594-55.017c-24.416-27.672-34.996-75.362-34.996-75.362h6.511l0.814-6.162l-48.019-2.673
                  c0,0,11.395-62.783,18.719-64.412c7.325-1.627-7.325,1.571-7.325,1.571l-4.069-128.618l12.208-1.267L93.5,143.5H99.604z"/>
            <polygon fill="#D8D8D8" stroke="#000000" stroke-miterlimit="10" points="625.5,213.553 625.5,254.5 382.5,254.5 382.5,389.347 
                     333.5,390.159 333.5,340.514 230.5,341.327 230.5,408.879 208.662,409.104 208.254,338.5 182.5,338.5 182.5,271.337 180.582,151.5 
                     625.5,151.5 "/>
            <rect x="230.5" y="341.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="282.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="100" height="28"/>
            <rect x="224.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="51" height="51"/>
            <rect x="275.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="31" height="31"/>
            <rect x="501.5" y="196.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="124" height="58"/>
            <rect x="382.5" y="268.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="37" height="51"/>
            <rect x="402.5" y="196.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="99" height="58"/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="230.5,389.5 382.5,389.5 382.5,426.5 314.055,426.5 
                     295.336,538.139 202.557,522.672 199.708,442.5 187.5,442.5 187.5,409.691 230.5,408.879 "/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="203.371,169.5 180.87,169.5 179.5,143.5 94.314,143.5 
                     115.474,211.895 205.812,212.671 "/>
            <path fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" d="M115.338,212.672l15.803,51.712c0,0,4.646,9.852,9.801,13.107
                  c5.153,3.255,10.333,3.162,10.333,3.162l31.225-9.045v-24.687l25.491-0.248l-1.903-33.934"/>
            <g>
            <!--main rectangle-->
            <rect id='SMUSISL4STUDYAREA1' x="97.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="160" height="117"/>
            <!--bg for text-->
            <rect x="97.568" y="80.895" fill="none" width="153.006" height="38.522"/>
            <!--text label-->
            <text transform="matrix(1 0 0 1 155.3979 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Study Area 1</text>
            </g>
            <g>
            <rect id='SMUSISL4ACADOFFICE' x="290.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="233" height="117"/>
            <rect x="290.726" y="80.895" fill="none" width="116.106" height="38.522"/>
            <text transform="matrix(1 0 0 1 408.1055 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Acad Office</text>
            </g>

            <g>
            <rect id='SMUSISL4STUDYAREA4' x="558.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="144" height="117"/>
            <rect x="559.025" y="80.895" fill="none" width="142.422" height="38.522"/>
            <text transform="matrix(1 0 0 1 611.5625 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Study Area 4</text>
            </g>
            <g>
            <rect id='SMUSISL4STUDYAREA3' x="306.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="44" height="51"/>
            <rect x="306.726" y="256.895" fill="none" width="116.106" height="38.522"/>
            <text transform="matrix(1 0 0 1 320.5625 220.4868)" font-family="'Arial-BoldMT'" font-size="12">Study Area 3</text>
            </g>

            <g>
            <rect id='SMUSISL4STUDYAREA2' x="149.5" y="245.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="59" height="93"/>
            <rect x="150.47" y="268.489" fill="none" width="56.969" height="57.78"/>
            <text transform="matrix(1 0 0 1 162.2881 277.0815)"><tspan x="0" y="0" font-family="'Arial-BoldMT'" font-size="12">Study</tspan><tspan x="3.325" y="14.4" font-family="'Arial-BoldMT'" font-size="12">Area </tspan><tspan x="13.33" y="28.8" font-family="'Arial-BoldMT'" font-size="12">2</tspan></text>
            </g>
            <g>
            <rect id='SMUSISL4LOBBY' x="333.5" y="251.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="49" height="138"/>
            <rect x="333.5" y="302.609" fill="none" width="49" height="57.781"/>
            <text transform="matrix(1 0 0 1 340.0029 311.2012)" font-family="'Arial-BoldMT'" font-size="12">Lobby</text>
            </g>
            </svg>

            <%} else if (level.equals("L5")) {%>
            <!--insert L5 floor plan-->
            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
                 width="800px" height="600px" viewBox="0 0 800 600" enable-background="new 0 0 800 600" xml:space="preserve">
            <path fill="#494949" stroke="#000000" stroke-miterlimit="10" d="M99.604,143.5l0.407-109h602.253l-1.781,92.782l38.327,0.788
                  l-78.055,85.43H625.5v41h-244v87h1.145l-0.134-11.612c0,0,63.39,5.511,86.991,33.995c23.602,28.486,0.814,54.617,0.814,54.617
                  h-8.139l-1.629,9.023l-17.904,7.361l-5.696,19.55l-15.464,7.334l-3.484,19.539l-18.489,7.327l-8.953,17.091l-18.72,5.783
                  l-7.324,17.991h-19.533l-8.952,17.818l-17.905,5.802l-12.208,16.38h-9.766l1.628,7.586c0,0-0.814,4.607-21.16,3.793
                  c-20.346-0.812-69.178-27.347-93.594-55.017c-24.416-27.672-34.996-75.362-34.996-75.362h6.511l0.814-6.162l-48.019-2.673
                  c0,0,11.395-62.783,18.719-64.412c7.325-1.627-7.325,1.571-7.325,1.571l-4.069-128.618l12.208-1.267L93.5,143.5H99.604z"/>
            <polygon fill="#D8D8D8" stroke="#000000" stroke-miterlimit="10" points="625.5,213.553 625.5,254.5 382.5,254.5 382.5,389.347 
                     333.5,390.159 333.5,340.514 230.5,341.327 230.5,408.879 208.662,409.104 208.254,338.5 182.5,338.5 182.5,271.337 180.582,151.5 
                     625.5,151.5 "/>
            <rect x="230.5" y="341.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="103" height="48"/>
            <rect x="233.5" y="282.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="100" height="28"/>
            <rect x="224.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="51" height="51"/>
            <rect x="275.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="31" height="31"/>
            <rect x="306.5" y="200.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="44" height="51"/>
            <rect x="382.5" y="268.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="37" height="51"/>
            <rect x="402.5" y="196.5" fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" width="99" height="58"/>
            <rect x="501.5" y="196.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="124" height="58"/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="230.5,389.5 382.5,389.5 382.5,426.5 314.055,426.5 
                     295.336,538.139 202.557,522.672 199.708,442.5 187.5,442.5 187.5,409.691 230.5,408.879 "/>
            <polygon fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" points="203.371,169.5 180.87,169.5 179.5,143.5 94.314,143.5 
                     115.474,211.895 205.812,212.671 "/>
            <path fill="#AAA9A9" stroke="#000000" stroke-miterlimit="10" d="M115.338,212.672l15.803,51.712c0,0,4.646,9.852,9.801,13.107
                  c5.153,3.255,10.333,3.162,10.333,3.162l31.225-9.045v-24.687l25.491-0.248l-1.903-33.934"/>
            <g>
            <!--main rectangle-->
            <rect id='SMUSISL5ACADOFFICE' x="97.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="393" height="117"/>
            <!--bg for text-->
            <rect x="97.568" y="80.895" fill="none" width="385.219" height="38.522"/>

            <!--text label-->
            <text transform="matrix(1 0 0 1 130.3979 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Acad Offices</text>
            </g>

            <g>
            <rect id='SMUSISL5STUDYAREA2' x="558.5" y="34.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="144" height="117"/>
            <rect x="559.025" y="80.895" fill="none" width="142.422" height="38.522"/>
            <text transform="matrix(1 0 0 1 611.5625 89.4868)" font-family="'Arial-BoldMT'" font-size="12">Study Area 2</text>
            </g>

            <g>
            <rect id='SMUSISL5STUDYAREA1' x="149.5" y="245.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="59" height="93"/>
            <rect x="150.47" y="268.489" fill="none" width="56.969" height="57.78"/>
            <text transform="matrix(1 0 0 1 162.2881 277.0815)"><tspan x="0" y="0" font-family="'Arial-BoldMT'" font-size="12">Study</tspan><tspan x="3.325" y="14.4" font-family="'Arial-BoldMT'" font-size="12">Area </tspan><tspan x="13.33" y="28.8" font-family="'Arial-BoldMT'" font-size="12">1</tspan></text>
            </g>
            <g>
            <rect id='SMUSISL5LOBBY' x="333.5" y="251.5" fill="#FFFFFF" stroke="#000000" stroke-miterlimit="10" width="49" height="138"/>
            <rect x="333.5" y="302.609" fill="none" width="49" height="57.781"/>
            <text transform="matrix(1 0 0 1 340.0029 311.2012)" font-family="'Arial-BoldMT'" font-size="12">Lobby</text>
            </g>
            </svg>
        </div>
        <%}%>
        <%}%>
    </body>
</html>
