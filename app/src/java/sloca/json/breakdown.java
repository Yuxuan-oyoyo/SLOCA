/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.BreakdownDAO;
import sloca.model.LocationLookupDAO;
import sloca.model.SharedSecretManager;

/**
 *
 * @author admin
 */
public class breakdown extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/JSON");
        PrintWriter out = response.getWriter();
        ArrayList<String> errList = new ArrayList<String>();

        //creates a new gson object
        //by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //creats a new json object for printing the desired json output
        JsonObject jsonOutput = new JsonObject();
        //create a json array to store errors
        JsonArray errMsg = new JsonArray();

        String dateTimeEnd = request.getParameter("date");
        String token = request.getParameter("token");
        String order = request.getParameter("order");

        //preset variable to determine the order of breakdown  
        String firstChoice = null;
        String secondChoice = null;
        String thirdChoice = null;
        //valdiate token
        if (token == null) {

            errList.add("missing token");
        } else if (token.isEmpty()) {

            errList.add("blank token");
        } else {
            try {
                JWTUtility.verify(token, SharedSecretManager.getSharedSecretKeyAdmin());
            } catch (JWTException e) {
                errList.add("invalid token");
            }
        }
        //validate date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setLenient(false);
        Date dateObj = null;
        if (dateTimeEnd == null) {
            errList.add("missing date");
        } else if (dateTimeEnd.isEmpty()) {
            errList.add("blank date");
        } else {

            try {
                dateObj = sdf.parse(dateTimeEnd);
                //check if date is within 2010 to 2014
                if (!validDate(dateObj)) {
                    errList.add("invalid date");
                }
            } catch (ParseException e) {
                errList.add("invalid date");
            }

        }
        //validate user input
        if (order == null) {
            errList.add("missing order");
        } else if (order.isEmpty()) {
            errList.add("blank order");
        } else {

            String[] breakdown = order.split(",");

            firstChoice = breakdown[0];
            if (!checkOrderField(firstChoice)) {
                errList.add("invalid order");
            }

            try {
                secondChoice = breakdown[1];
                if (!checkOrderField(secondChoice)) {
                    errList.add("invalid order");
                }
                thirdChoice = breakdown[2];
                if (!checkOrderField(thirdChoice)) {
                    errList.add("invalid order");
                }
                if (breakdown[3] != null) {
                    errList.add("invalid order");
                }
            } catch (ArrayIndexOutOfBoundsException a) {

            }
            //check if choice is invalid

            if (firstChoice.equalsIgnoreCase(secondChoice)) {
                errList.add("invalid order");
            } else if (firstChoice.equalsIgnoreCase(thirdChoice)) {
                errList.add("invalid order");
            } else if (secondChoice != null && secondChoice.equalsIgnoreCase(thirdChoice)) {
                errList.add("invalid order");
            }

        }

        if (errList.isEmpty()) {
            //logice here
            String order1 = "";
            String order2 = "";
            String order3 = "";
            String header = "";

            //arraylist for separate result
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<String> result2 = new ArrayList<String>();
            ArrayList<String> result3 = new ArrayList<String>();

            String dateTimeStart = getStartDate(dateTimeEnd);

            jsonOutput.addProperty("status", "success");
            JsonArray breakdown = new JsonArray();
            if ("year".equalsIgnoreCase(firstChoice)) {
                order1 = "year";
                if ("gender".equalsIgnoreCase(secondChoice)) {
                    order2 = "gender";
                    if ("school".equalsIgnoreCase(thirdChoice)) {
                        order3 = "school";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTimeEnd, order1, order2, order3);
                        header = "YEAR, YEAR COUNT, GENDER, GENDER COUNT, SCHOOL, SCHOOL COUNT";
                        if (!result3.get(0).contains("2010") || !result3.get(0).contains("M") || !result3.get(0).contains("accountancy")) {
                            result3.add(0, "2010,0,M,0,accountancy,0");
                        }
                        if (!result3.get(1).contains("2010") || !result3.get(1).contains("M") || !result3.get(1).contains("business")) {
                            result3.add(1, "2010,0,M,0,business,0");
                        }
                        if (!result3.get(2).contains("2010") || !result3.get(2).contains("M") || !result3.get(2).contains("economics")) {
                            result3.add(2, "2010,0,M,0,economics,0");
                        }
                        if (!result3.get(3).contains("2010") || !result3.get(3).contains("M") || !result3.get(3).contains("law")) {
                            result3.add(3, "2010,0,M,0,law,0");
                        }
                        if (!result3.get(4).contains("2010") || !result3.get(4).contains("M") || !result3.get(4).contains("sis")) {
                            result3.add(4, "2010,0,M,0,sis,0");
                        }
                        if (!result3.get(5).contains("2010") || !result3.get(5).contains("M") || !result3.get(5).contains("socsc")) {
                            result3.add(5, "2010,0,M,0,socsc,0");
                        }
                        if (!result3.get(6).contains("2010") || !result3.get(6).contains("F") || !result3.get(6).contains("accountancy")) {
                            result3.add(6, "2010,0,F,0,accountancy,0");
                        }
                        if (!result3.get(7).contains("2010") || !result3.get(7).contains("F") || !result3.get(7).contains("business")) {
                            result3.add(7, "2010,0,F,0,business,0");
                        }
                        if (!result3.get(8).contains("2010") || !result3.get(8).contains("F") || !result3.get(8).contains("economics")) {
                            result3.add(8, "2010,0,F,0,economics,0");
                        }
                        if (!result3.get(9).contains("2010") || !result3.get(9).contains("F") || !result3.get(9).contains("law")) {
                            result3.add(9, "2010,0,F,0,law,0");
                        }
                        if (!result3.get(10).contains("2010") || !result3.get(10).contains("F") || !result3.get(10).contains("sis")) {
                            result3.add(10, "2010,0,F,0,sis,0");
                        }
                        if (!result3.get(11).contains("2010") || !result3.get(11).contains("F") || !result3.get(11).contains("socsc")) {
                            result3.add(11, "2010,0,F,0,socsc,0");
                        }
                        if (!result3.get(12).contains("2011") || !result3.get(12).contains("M") || !result3.get(12).contains("accountancy")) {
                            result3.add(12, "2011,0,M,0,accountancy,0");
                        }
                        if (!result3.get(13).contains("2011") || !result3.get(13).contains("M") || !result3.get(13).contains("business")) {
                            result3.add(13, "2011,0,M,0,business,0");
                        }
                        if (!result3.get(14).contains("2011") || !result3.get(14).contains("M") || !result3.get(14).contains("economics")) {
                            result3.add(14, "2011,0,M,0,economics,0");
                        }
                        if (!result3.get(15).contains("2011") || !result3.get(15).contains("M") || !result3.get(15).contains("law")) {
                            result3.add(15, "2011,0,M,0,law,0");
                        }
                        if (!result3.get(16).contains("2011") || !result3.get(16).contains("M") || !result3.get(16).contains("sis")) {
                            result3.add(16, "2011,0,M,0,sis,0");
                        }
                        if (!result3.get(17).contains("2011") || !result3.get(17).contains("M") || !result3.get(17).contains("socsc")) {
                            result3.add(17, "2011,0,M,0,socsc,0");
                        }
                        if (!result3.get(18).contains("2011") || !result3.get(18).contains("F") || !result3.get(18).contains("accountancy")) {
                            result3.add(18, "2011,0,F,0,accountancy,0");
                        }
                        if (!result3.get(19).contains("2011") || !result3.get(19).contains("F") || !result3.get(19).contains("business")) {
                            result3.add(19, "2011,0,F,0,business,0");
                        }
                        if (!result3.get(20).contains("2011") || !result3.get(20).contains("F") || !result3.get(20).contains("economics")) {
                            result3.add(20, "2011,0,F,0,economics,0");
                        }
                        if (!result3.get(21).contains("2011") || !result3.get(21).contains("F") || !result3.get(21).contains("law")) {
                            result3.add(21, "2011,0,F,0,law,0");
                        }
                        if (!result3.get(22).contains("2011") || !result3.get(22).contains("F") || !result3.get(22).contains("sis")) {
                            result3.add(22, "2011,0,F,0,sis,0");
                        }
                        if (!result3.get(23).contains("2011") || !result3.get(23).contains("F") || !result3.get(23).contains("socsc")) {
                            result3.add(23, "2011,0,F,0,socsc,0");
                        }
                        if (!result3.get(24).contains("2012") || !result3.get(24).contains("M") || !result3.get(24).contains("accountancy")) {
                            result3.add(24, "2012,0,M,0,accountancy,0");
                        }
                        if (!result3.get(25).contains("2012") || !result3.get(25).contains("M") || !result3.get(25).contains("business")) {
                            result3.add(25, "2012,0,M,0,business,0");
                        }
                        if (!result3.get(26).contains("2012") || !result3.get(26).contains("M") || !result3.get(26).contains("economics")) {
                            result3.add(26, "2012,0,M,0,economics,0");
                        }
                        if (!result3.get(27).contains("2012") || !result3.get(27).contains("M") || !result3.get(27).contains("law")) {
                            result3.add(27, "2012,0,M,0,law,0");
                        }
                        if (!result3.get(28).contains("2012") || !result3.get(28).contains("M") || !result3.get(28).contains("sis")) {
                            result3.add(28, "2012,0,M,0,sis,0");
                        }
                        if (!result3.get(29).contains("2012") || !result3.get(29).contains("M") || !result3.get(29).contains("socsc")) {
                            result3.add(29, "2012,0,M,0,socsc,0");
                        }
                        if (!result3.get(30).contains("2012") || !result3.get(30).contains("F") || !result3.get(30).contains("accountancy")) {
                            result3.add(30, "2012,0,F,0,accountancy,0");
                        }
                        if (!result3.get(31).contains("2012") || !result3.get(31).contains("F") || !result3.get(31).contains("business")) {
                            result3.add(31, "2012,0,F,0,business,0");
                        }
                        if (!result3.get(32).contains("2012") || !result3.get(32).contains("F") || !result3.get(32).contains("economics")) {
                            result3.add(32, "2012,0,F,0,economics,0");
                        }
                        if (!result3.get(33).contains("2012") || !result3.get(33).contains("F") || !result3.get(33).contains("law")) {
                            result3.add(33, "2012,0,F,0,law,0");
                        }
                        if (!result3.get(34).contains("2012") || !result3.get(34).contains("F") || !result3.get(34).contains("sis")) {
                            result3.add(34, "2012,0,F,0,sis,0");
                        }
                        if (!result3.get(35).contains("2012") || !result3.get(35).contains("F") || !result3.get(35).contains("socsc")) {
                            result3.add(35, "2012,0,F,0,socsc,0");
                        }
                        if (!result3.get(36).contains("2013") || !result3.get(36).contains("M") || !result3.get(36).contains("accountancy")) {
                            result3.add(36, "2013,0,M,0,accountancy,0");
                        }
                        if (!result3.get(37).contains("2013") || !result3.get(37).contains("M") || !result3.get(37).contains("business")) {
                            result3.add(37, "2013,0,M,0,business,0");
                        }
                        if (!result3.get(38).contains("2013") || !result3.get(38).contains("M") || !result3.get(38).contains("economics")) {
                            result3.add(38, "2013,0,M,0,economics,0");
                        }
                        if (!result3.get(39).contains("2013") || !result3.get(39).contains("M") || !result3.get(39).contains("law")) {
                            result3.add(39, "2013,0,M,0,law,0");
                        }
                        if (!result3.get(40).contains("2013") || !result3.get(40).contains("M") || !result3.get(40).contains("sis")) {
                            result3.add(40, "2013,0,M,0,sis,0");
                        }
                        if (!result3.get(41).contains("2013") || !result3.get(41).contains("M") || !result3.get(41).contains("socsc")) {
                            result3.add(41, "2013,0,M,0,socsc,0");
                        }
                        if (!result3.get(42).contains("2013") || !result3.get(42).contains("F") || !result3.get(42).contains("accountancy")) {
                            result3.add(42, "2013,0,F,0,accountancy,0");
                        }
                        if (!result3.get(43).contains("2013") || !result3.get(43).contains("F") || !result3.get(43).contains("business")) {
                            result3.add(43, "2013,0,F,0,business,0");
                        }
                        if (!result3.get(44).contains("2013") || !result3.get(44).contains("F") || !result3.get(44).contains("economics")) {
                            result3.add(44, "2013,0,F,0,economics,0");
                        }
                        if (!result3.get(45).contains("2013") || !result3.get(45).contains("F") || !result3.get(45).contains("law")) {
                            result3.add(45, "2013,0,F,0,law,0");
                        }
                        if (!result3.get(46).contains("2013") || !result3.get(46).contains("F") || !result3.get(46).contains("sis")) {
                            result3.add(46, "2013,0,F,0,sis,0");
                        }
                        if (!result3.get(47).contains("2013") || !result3.get(47).contains("F") || !result3.get(47).contains("socsc")) {
                            result3.add(47, "2013,0,F,0,socsc,0");
                        }
                        if (!result3.get(48).contains("2014") || !result3.get(48).contains("M") || !result3.get(48).contains("accountancy")) {
                            result3.add(48, "2014,0,M,0,accountancy,0");
                        }
                        if (!result3.get(49).contains("2014") || !result3.get(49).contains("M") || !result3.get(49).contains("business")) {
                            result3.add(49, "2014,0,M,0,business,0");
                        }
                        if (!result3.get(50).contains("2014") || !result3.get(50).contains("M") || !result3.get(50).contains("economics")) {
                            result3.add(50, "2014,0,M,0,economics,0");
                        }
                        if (!result3.get(51).contains("2014") || !result3.get(51).contains("M") || !result3.get(51).contains("law")) {
                            result3.add(51, "2014,0,M,0,law,0");
                        }
                        if (!result3.get(52).contains("2014") || !result3.get(52).contains("M") || !result3.get(52).contains("sis")) {
                            result3.add(52, "2014,0,M,0,sis,0");
                        }
                        if (!result3.get(53).contains("2014") || !result3.get(53).contains("M") || !result3.get(53).contains("socsc")) {
                            result3.add(53, "2014,0,M,0,socsc,0");
                        }
                        if (!result3.get(54).contains("2014") || !result3.get(54).contains("F") || !result3.get(54).contains("accountancy")) {
                            result3.add(54, "2014,0,F,0,accountancy,0");
                        }
                        if (!result3.get(55).contains("2014") || !result3.get(55).contains("F") || !result3.get(55).contains("business")) {
                            result3.add(55, "2014,0,F,0,business,0");
                        }
                        if (!result3.get(56).contains("2014") || !result3.get(56).contains("F") || !result3.get(56).contains("economics")) {
                            result3.add(56, "2014,0,F,0,economics,0");
                        }
                        if (!result3.get(57).contains("2014") || !result3.get(57).contains("F") || !result3.get(57).contains("law")) {
                            result3.add(57, "2014,0,F,0,law,0");
                        }
                        if (!result3.get(58).contains("2014") || !result3.get(58).contains("F") || !result3.get(58).contains("sis")) {
                            result3.add(58, "2014,0,F,0,sis,0");
                        }
                        if (!result3.get(59).contains("2014") || !result3.get(59).contains("F") || !result3.get(59).contains("socsc")) {
                            result3.add(59, "2014,0,F,0,socsc,0");
                        }
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTimeEnd, order1, order2);
                        header = "YEAR, YEAR COUNT, GENDER, GENDER COUNT";
                        if (!result2.get(0).contains("2010") || !result2.get(0).contains("M")) {
                            result2.add(0, "2010,0,M,0");
                        }
                        if (!result2.get(1).contains("2010") || !result2.get(1).contains("F")) {
                            result2.add(1, "2010,0,F,0");
                        }
                        if (!result2.get(2).contains("2011") || !result2.get(2).contains("M")) {
                            result2.add(2, "2011,0,M,0");
                        }
                        if (!result2.get(3).contains("2011") || !result2.get(3).contains("F")) {
                            result2.add(3, "2011,0,F,0");
                        }
                        if (!result2.get(4).contains("2010") || !result2.get(4).contains("M")) {
                            result2.add(4, "2012,0,M,0");
                        }
                        if (!result2.get(5).contains("2010") || !result2.get(5).contains("F")) {
                            result2.add(5, "2012,0,F,0");
                        }
                        if (!result2.get(6).contains("2010") || !result2.get(6).contains("M")) {
                            result2.add(6, "2013,0,M,0");
                        }
                        if (!result2.get(7).contains("2010") || !result2.get(7).contains("F")) {
                            result2.add(7, "2013,0,F,0");
                        }
                        if (!result2.get(8).contains("2014") || !result2.get(8).contains("M")) {
                            result2.add(8, "2014,0,M,0");
                        }
                        if (!result2.get(9).contains("2014") || !result2.get(9).contains("F")) {
                            result2.add(9, "2014,0,F,0");
                        }

                    }
                } else if ("school".equalsIgnoreCase(secondChoice)) {
                    order2 = "school";
                    if ("gender".equalsIgnoreCase(thirdChoice)) {
                        order3 = "gender";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTimeEnd, order1, order2, order3);
                        header = "YEAR, YEAR COUNT, SCHOOL, SCHOOL COUNT, GENDER, GENDER COUNT";
                        if (!result3.get(0).contains("2010") || !result3.get(0).contains("M") || !result3.get(0).contains("accountancy")) {
                            result3.add(0, "2010,0,accountancy,0,M,0");
                        }
                        if (!result3.get(1).contains("2010") || !result3.get(1).contains("F") || !result3.get(1).contains("accountancy")) {
                            result3.add(1, "2010,0,accountancy,0,F,0");
                        }
                        if (!result3.get(2).contains("2010") || !result3.get(2).contains("M") || !result3.get(2).contains("business")) {
                            result3.add(2, "2010,0,business,0,M,0");
                        }
                        if (!result3.get(3).contains("2010") || !result3.get(3).contains("F") || !result3.get(3).contains("business")) {
                            result3.add(3, "2010,0,business,0,F,0");
                        }
                        if (!result3.get(4).contains("2010") || !result3.get(4).contains("M") || !result3.get(4).contains("economics")) {
                            result3.add(4, "2010,0,economics,0,M,0");
                        }
                        if (!result3.get(5).contains("2010") || !result3.get(5).contains("F") || !result3.get(5).contains("economics")) {
                            result3.add(5, "2010,0,economics,0,F,0");
                        }
                        if (!result3.get(6).contains("2010") || !result3.get(6).contains("M") || !result3.get(6).contains("law")) {
                            result3.add(6, "2010,0,law,0,M,0");
                        }
                        if (!result3.get(7).contains("2010") || !result3.get(9).contains("F") || !result3.get(9).contains("law")) {
                            result3.add(7, "2010,0,law,0,F,0");
                        }
                        if (!result3.get(8).contains("2010") || !result3.get(8).contains("M") || !result3.get(8).contains("sis")) {
                            result3.add(8, "2010,0,sis,0,M,0");
                        }
                        if (!result3.get(9).contains("2010") || !result3.get(9).contains("F") || !result3.get(9).contains("sis")) {
                            result3.add(9, "2010,0,sis,0,F,0");
                        }
                        if (!result3.get(10).contains("2010") || !result3.get(10).contains("M") || !result3.get(10).contains("socsc")) {
                            result3.add(10, "2010,0,socsc,0,M,0");
                        }
                        if (!result3.get(11).contains("2010") || !result3.get(11).contains("F") || !result3.get(11).contains("socsc")) {
                            result3.add(11, "2010,0,socsc,0,F,0");
                        }
                        if (!result3.get(12).contains("2011") || !result3.get(12).contains("M") || !result3.get(12).contains("accountancy")) {
                            result3.add(12, "2011,0,accountancy,0,M,0");
                        }
                        if (!result3.get(13).contains("2011") || !result3.get(13).contains("F") || !result3.get(13).contains("accountancy")) {
                            result3.add(13, "2011,0,accountancy,0,F,0");
                        }
                        if (!result3.get(14).contains("2011") || !result3.get(14).contains("M") || !result3.get(14).contains("business")) {
                            result3.add(14, "2011,0,business,0,M,0");
                        }
                        if (!result3.get(15).contains("2011") || !result3.get(15).contains("F") || !result3.get(15).contains("business")) {
                            result3.add(15, "2011,0,business,0,F,0");
                        }
                        if (!result3.get(16).contains("2011") || !result3.get(16).contains("M") || !result3.get(16).contains("economics")) {
                            result3.add(16, "2011,0,economics,0,M,0");
                        }
                        if (!result3.get(17).contains("2011") || !result3.get(17).contains("F") || !result3.get(17).contains("economics")) {
                            result3.add(17, "2011,0,economics,0,F,0");
                        }
                        if (!result3.get(18).contains("2011") || !result3.get(18).contains("M") || !result3.get(18).contains("law")) {
                            result3.add(18, "2011,0,law,0,M,0");
                        }
                        if (!result3.get(19).contains("2011") || !result3.get(19).contains("F") || !result3.get(19).contains("law")) {
                            result3.add(19, "2011,0,law,0,F,0");
                        }
                        if (!result3.get(20).contains("2011") || !result3.get(20).contains("M") || !result3.get(20).contains("sis")) {
                            result3.add(20, "2011,0,sis,0,M,0");
                        }
                        if (!result3.get(21).contains("2011") || !result3.get(21).contains("F") || !result3.get(21).contains("sis")) {
                            result3.add(21, "2011,0,sis,0,F,0");
                        }
                        if (!result3.get(22).contains("2011") || !result3.get(22).contains("M") || !result3.get(22).contains("socsc")) {
                            result3.add(22, "2011,0,socsc,0,M,0");
                        }
                        if (!result3.get(23).contains("2011") || !result3.get(23).contains("F") || !result3.get(23).contains("socsc")) {
                            result3.add(23, "2011,0,socsc,0,F,0");
                        }
                        if (!result3.get(24).contains("2012") || !result3.get(24).contains("M") || !result3.get(24).contains("accountancy")) {
                            result3.add(24, "2012,0,accountancy,0,M,0");
                        }
                        if (!result3.get(25).contains("2012") || !result3.get(25).contains("F") || !result3.get(25).contains("accountancy")) {
                            result3.add(25, "2012,0,accountancy,0,F,0");
                        }
                        if (!result3.get(26).contains("2012") || !result3.get(26).contains("M") || !result3.get(26).contains("business")) {
                            result3.add(26, "2012,0,business,0,M,0");
                        }
                        if (!result3.get(27).contains("2012") || !result3.get(27).contains("F") || !result3.get(27).contains("business")) {
                            result3.add(27, "2012,0,business,0,F,0");
                        }
                        if (!result3.get(28).contains("2012") || !result3.get(28).contains("M") || !result3.get(28).contains("economics")) {
                            result3.add(28, "2012,0,economics,0,M,0");
                        }
                        if (!result3.get(29).contains("2012") || !result3.get(29).contains("F") || !result3.get(29).contains("economics")) {
                            result3.add(29, "2012,0,economics,0,F,0");
                        }
                        if (!result3.get(30).contains("2012") || !result3.get(30).contains("M") || !result3.get(30).contains("law")) {
                            result3.add(30, "2012,0,law,0,M,0");
                        }
                        if (!result3.get(31).contains("2012") || !result3.get(31).contains("F") || !result3.get(31).contains("law")) {
                            result3.add(31, "2012,0,law,0,F,0");
                        }
                        if (!result3.get(32).contains("2012") || !result3.get(32).contains("M") || !result3.get(32).contains("sis")) {
                            result3.add(32, "2012,0,sis,0,M,0");
                        }
                        if (!result3.get(33).contains("2012") || !result3.get(33).contains("F") || !result3.get(33).contains("sis")) {
                            result3.add(33, "2012,0,sis,0,F,0");
                        }
                        if (!result3.get(34).contains("2012") || !result3.get(34).contains("M") || !result3.get(34).contains("socsc")) {
                            result3.add(34, "2012,0,socsc,0,M,0");
                        }
                        if (!result3.get(35).contains("2012") || !result3.get(35).contains("F") || !result3.get(35).contains("socsc")) {
                            result3.add(35, "2012,0,socsc,0,F,0");
                        }
                        if (!result3.get(36).contains("2013") || !result3.get(36).contains("M") || !result3.get(36).contains("accountancy")) {
                            result3.add(36, "2013,0,accountancy,0,M,0");
                        }
                        if (!result3.get(37).contains("2013") || !result3.get(37).contains("F") || !result3.get(37).contains("accountancy")) {
                            result3.add(37, "2013,0,accountancy,0,F,0");
                        }
                        if (!result3.get(38).contains("2013") || !result3.get(38).contains("M") || !result3.get(38).contains("business")) {
                            result3.add(38, "2013,0,business,0,M,0");
                        }
                        if (!result3.get(39).contains("2013") || !result3.get(39).contains("F") || !result3.get(39).contains("business")) {
                            result3.add(39, "2013,0,business,0,F,0");
                        }
                        if (!result3.get(40).contains("2013") || !result3.get(40).contains("M") || !result3.get(40).contains("economics")) {
                            result3.add(40, "2013,0,economics,0,M,0");
                        }
                        if (!result3.get(41).contains("2013") || !result3.get(41).contains("F") || !result3.get(41).contains("economics")) {
                            result3.add(41, "2013,0,economics,0,F,0");
                        }
                        if (!result3.get(42).contains("2013") || !result3.get(42).contains("M") || !result3.get(42).contains("law")) {
                            result3.add(42, "2013,0,law,0,M,0");
                        }
                        if (!result3.get(43).contains("2013") || !result3.get(43).contains("F") || !result3.get(43).contains("law")) {
                            result3.add(43, "2013,0,law,0,F,0");
                        }
                        if (!result3.get(44).contains("2013") || !result3.get(44).contains("M") || !result3.get(44).contains("sis")) {
                            result3.add(44, "2013,0,sis,0,M,0");
                        }
                        if (!result3.get(45).contains("2013") || !result3.get(45).contains("F") || !result3.get(45).contains("sis")) {
                            result3.add(45, "2013,0,sis,0,F,0");
                        }
                        if (!result3.get(46).contains("2013") || !result3.get(46).contains("M") || !result3.get(46).contains("socsc")) {
                            result3.add(46, "2013,0,socsc,0,M,0");
                        }
                        if (!result3.get(47).contains("2013") || !result3.get(47).contains("F") || !result3.get(47).contains("socsc")) {
                            result3.add(47, "2013,0,socsc,0,F,0");
                        }
                        if (!result3.get(48).contains("2014") || !result3.get(48).contains("M") || !result3.get(48).contains("accountancy")) {
                            result3.add(48, "2014,0,accountancy,0,M,0");
                        }
                        if (!result3.get(49).contains("2014") || !result3.get(49).contains("F") || !result3.get(49).contains("accountancy")) {
                            result3.add(49, "2014,0,accountancy,0,F,0");
                        }
                        if (!result3.get(50).contains("2014") || !result3.get(50).contains("M") || !result3.get(50).contains("business")) {
                            result3.add(50, "2014,0,business,0,M,0");
                        }
                        if (!result3.get(51).contains("2014") || !result3.get(51).contains("F") || !result3.get(51).contains("business")) {
                            result3.add(51, "2014,0,business,0,F,0");
                        }
                        if (!result3.get(52).contains("2014") || !result3.get(52).contains("M") || !result3.get(52).contains("economics")) {
                            result3.add(52, "2014,0,economics,0,M,0");
                        }
                        if (!result3.get(53).contains("2014") || !result3.get(53).contains("F") || !result3.get(53).contains("economics")) {
                            result3.add(53, "2014,0,economics,0,F,0");
                        }
                        if (!result3.get(54).contains("2014") || !result3.get(54).contains("M") || !result3.get(54).contains("law")) {
                            result3.add(54, "2014,0,law,0,M,0");
                        }
                        if (!result3.get(55).contains("2014") || !result3.get(55).contains("F") || !result3.get(55).contains("law")) {
                            result3.add(55, "2014,0,law,0,F,0");
                        }
                        if (!result3.get(56).contains("2014") || !result3.get(56).contains("M") || !result3.get(56).contains("sis")) {
                            result3.add(56, "2014,0,sis,0,M,0");
                        }
                        if (!result3.get(57).contains("2014") || !result3.get(57).contains("F") || !result3.get(57).contains("sis")) {
                            result3.add(57, "2014,0,sis,0,F,0");
                        }
                        if (!result3.get(58).contains("2014") || !result3.get(58).contains("M") || !result3.get(58).contains("socsc")) {
                            result3.add(58, "2014,0,socsc,0,M,0");
                        }
                        if (!result3.get(59).contains("2014") || !result3.get(59).contains("F") || !result3.get(59).contains("socsc")) {
                            result3.add(59, "2014,0,socsc,0,F,0");
                        }

                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTimeEnd, order1, order2);
                        header = "YEAR, YEAR COUNT, SCHOOL, SCHOOL COUNT";

                        if (!result2.get(0).contains("2010") || !result2.get(0).contains("accountancy")) {
                            result2.add(0, "2010,0,accountancy,0");
                        }
                        if (!result2.get(1).contains("2010") || !result2.get(1).contains("business")) {
                            result2.add(1, "2010,0,business,0");
                        }
                        if (!result2.get(2).contains("2010") || !result2.get(2).contains("economics")) {
                            result2.add(2, "2010,0,economics,0");
                        }
                        if (!result2.get(3).contains("2010") || !result2.get(3).contains("law")) {
                            result2.add(3, "2010,0,law,0");
                        }
                        if (!result2.get(4).contains("2010") || !result2.get(4).contains("sis")) {
                            result2.add(4, "2010,0,sis,0");
                        }
                        if (!result2.get(5).contains("2010") || !result2.get(5).contains("socsc")) {
                            result2.add(5, "2010,0,socsc,0");
                        }
                        if (!result2.get(6).contains("2011") || !result2.get(6).contains("accountancy")) {
                            result2.add(6, "2011,0,accountancy,0");
                        }
                        if (!result2.get(7).contains("2011") || !result2.get(7).contains("business")) {
                            result2.add(7, "2011,0,business,0");
                        }
                        if (!result2.get(8).contains("2011") || !result2.get(8).contains("economics")) {
                            result2.add(8, "2011,0,economics,0");
                        }
                        if (!result2.get(9).contains("2011") || !result2.get(9).contains("law")) {
                            result2.add(9, "2011,0,law,0");
                        }
                        if (!result2.get(10).contains("2011") || !result2.get(10).contains("sis")) {
                            result2.add(10, "2011,0,sis,0");
                        }
                        if (!result2.get(11).contains("2011") || !result2.get(11).contains("socsc")) {
                            result2.add(11, "2011,0,socsc,0");
                        }
                        if (!result2.get(12).contains("2012") || !result2.get(12).contains("accountancy")) {
                            result2.add(12, "2012,0,accountancy,0");
                        }
                        if (!result2.get(13).contains("2012") || !result2.get(13).contains("business")) {
                            result2.add(13, "2012,0,business,0");
                        }
                        if (!result2.get(14).contains("2012") || !result2.get(14).contains("economics")) {
                            result2.add(14, "2012,0,economics,0");
                        }
                        if (!result2.get(15).contains("2012") || !result2.get(15).contains("law")) {
                            result2.add(15, "2012,0,law,0");
                        }
                        if (!result2.get(16).contains("2012") || !result2.get(16).contains("sis")) {
                            result2.add(16, "2012,0,sis,0");
                        }
                        if (!result2.get(17).contains("2012") || !result2.get(17).contains("socsc")) {
                            result2.add(17, "2012,0,socsc,0");
                        }
                        if (!result2.get(18).contains("2013") || !result2.get(18).contains("accountancy")) {
                            result2.add(18, "2013,0,accountancy,0");
                        }
                        if (!result2.get(19).contains("2013") || !result2.get(19).contains("business")) {
                            result2.add(19, "2013,0,business,0");
                        }
                        if (!result2.get(20).contains("2013") || !result2.get(20).contains("economics")) {
                            result2.add(20, "2013,0,economics,0");
                        }
                        if (!result2.get(21).contains("2013") || !result2.get(21).contains("law")) {
                            result2.add(21, "2013,0,law,0");
                        }
                        if (!result2.get(22).contains("2013") || !result2.get(22).contains("sis")) {
                            result2.add(22, "2013,0,sis,0");
                        }
                        if (!result2.get(23).contains("2013") || !result2.get(23).contains("socsc")) {
                            result2.add(23, "2013,0,socsc,0");
                        }
                        if (!result2.get(24).contains("2014") || !result2.get(24).contains("accountancy")) {
                            result2.add(24, "2014,0,accountancy,0");
                        }
                        if (!result2.get(25).contains("2014") || !result2.get(25).contains("business")) {
                            result2.add(25, "2014,0,business,0");
                        }
                        if (!result2.get(26).contains("2014") || !result2.get(26).contains("economics")) {
                            result2.add(26, "2014,0,economics,0");
                        }
                        if (!result2.get(27).contains("2014") || !result2.get(27).contains("law")) {
                            result2.add(27, "2014,0,law,0");
                        }
                        if (!result2.get(28).contains("2014") || !result2.get(28).contains("sis")) {
                            result2.add(28, "2014,0,sis,0");
                        }
                        if (!result2.get(29).contains("2014") || !result2.get(29).contains("socsc")) {
                            result2.add(29, "2014,0,socsc,0");
                        }
                    }//end of 2nd choice
                } else {
                    result = BreakdownDAO.breakdownByOne(dateTimeStart, dateTimeEnd, order1);
                    header = "YEAR, YEAR COUNT";
                    if (result.size() != 0) {
                        if (!result.get(0).contains("2010")) {
                            result.add(0, "2010,0");
                        }
                        if (!result.get(1).contains("2011")) {
                            result.add(1, "2011,0");
                        }
                        if (!result.get(2).contains("2012")) {
                            result.add(2, "2012,0");
                        }
                        if (!result.get(3).contains("2013")) {
                            result.add(3, "2013,0");
                        }
                        if (!result.get(4).contains("2014")) {
                            result.add(4, "2014,0");
                        }
                    }

                }

            } else if ("gender".equalsIgnoreCase(firstChoice)) {
                order1 = "gender";
                if ("year".equalsIgnoreCase(secondChoice)) {
                    order2 = "year";
                    if ("school".equalsIgnoreCase(thirdChoice)) {
                        order3 = "shcool";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTimeEnd, order1, order2, order3);
                        header = "GENDER, GENDER COUNT, YEAR, YEAR COUNT, SCHOOL, SCHOOL COUNT";
                        if (!result3.get(0).contains("2010") || !result3.get(0).contains("M") || !result3.get(0).contains("accountancy")) {
                            result3.add(0, "M,0,2010,0,accountancy,0");
                        }
                        if (!result3.get(1).contains("2010") || !result3.get(1).contains("M") || !result3.get(1).contains("business")) {
                            result3.add(1, "M,0,2010,0,business,0");
                        }
                        if (!result3.get(2).contains("2010") || !result3.get(2).contains("M") || !result3.get(2).contains("economics")) {
                            result3.add(2, "M,0,2010,0,economics,0");
                        }
                        if (!result3.get(3).contains("2010") || !result3.get(3).contains("M") || !result3.get(3).contains("law")) {
                            result3.add(3, "M,0,2010,0,law,0");
                        }
                        if (!result3.get(4).contains("2010") || !result3.get(4).contains("M") || !result3.get(4).contains("sis")) {
                            result3.add(4, "M,0,2010,0,sis,0");
                        }
                        if (!result3.get(5).contains("2010") || !result3.get(5).contains("M") || !result3.get(5).contains("socsc")) {
                            result3.add(5, "M,0,2010,0,socsc,0");
                        }
                        if (!result3.get(6).contains("2011") || !result3.get(6).contains("M") || !result3.get(6).contains("accountancy")) {
                            result3.add(6, "M,0,2011,0,accountancy,0");
                        }
                        if (!result3.get(7).contains("2011") || !result3.get(7).contains("M") || !result3.get(7).contains("business")) {
                            result3.add(7, "M,0,2011,0,business,0");
                        }
                        if (!result3.get(8).contains("2011") || !result3.get(8).contains("M") || !result3.get(8).contains("economics")) {
                            result3.add(8, "M,0,2011,0,economics,0");
                        }
                        if (!result3.get(9).contains("2011") || !result3.get(9).contains("M") || !result3.get(9).contains("law")) {
                            result3.add(9, "M,0,2011,0,law,0");
                        }
                        if (!result3.get(10).contains("2011") || !result3.get(10).contains("M") || !result3.get(10).contains("sis")) {
                            result3.add(10, "M,0,2011,0,sis,0");
                        }
                        if (!result3.get(11).contains("2011") || !result3.get(11).contains("M") || !result3.get(11).contains("socsc")) {
                            result3.add(11, "M,0,2011,0,socsc,0");
                        }
                        if (!result3.get(12).contains("2012") || !result3.get(12).contains("M") || !result3.get(12).contains("accountancy")) {
                            result3.add(12, "M,0,2012,0,accountancy,0");
                        }
                        if (!result3.get(13).contains("2012") || !result3.get(13).contains("M") || !result3.get(13).contains("business")) {
                            result3.add(13, "M,0,2012,0,business,0");
                        }
                        if (!result3.get(14).contains("2012") || !result3.get(14).contains("M") || !result3.get(14).contains("economics")) {
                            result3.add(14, "M,0,2012,0,economics,0");
                        }
                        if (!result3.get(15).contains("2012") || !result3.get(15).contains("M") || !result3.get(15).contains("law")) {
                            result3.add(15, "M,0,2012,0,law,0");
                        }
                        if (!result3.get(16).contains("2012") || !result3.get(16).contains("M") || !result3.get(16).contains("sis")) {
                            result3.add(16, "M,0,2012,0,sis,0");
                        }
                        if (!result3.get(17).contains("2012") || !result3.get(17).contains("M") || !result3.get(17).contains("socsc")) {
                            result3.add(17, "M,0,2012,0,socsc,0");
                        }
                        if (!result3.get(18).contains("2013") || !result3.get(18).contains("M") || !result3.get(18).contains("accountancy")) {
                            result3.add(18, "M,0,2013,0,accountancy,0");
                        }
                        if (!result3.get(19).contains("2013") || !result3.get(19).contains("M") || !result3.get(19).contains("business")) {
                            result3.add(19, "M,0,2013,0,business,0");
                        }
                        if (!result3.get(20).contains("2013") || !result3.get(20).contains("M") || !result3.get(20).contains("economics")) {
                            result3.add(20, "M,0,2013,0,economics,0");
                        }
                        if (!result3.get(21).contains("2013") || !result3.get(21).contains("M") || !result3.get(21).contains("law")) {
                            result3.add(21, "M,0,2013,0,law,0");
                        }
                        if (!result3.get(22).contains("2013") || !result3.get(22).contains("M") || !result3.get(22).contains("sis")) {
                            result3.add(22, "M,0,02013,0,sis,0");
                        }
                        if (!result3.get(23).contains("2013") || !result3.get(23).contains("M") || !result3.get(23).contains("socsc")) {
                            result3.add(23, "M,0,2013,0,socsc,0");
                        }
                        if (!result3.get(24).contains("2014") || !result3.get(24).contains("M") || !result3.get(24).contains("accountancy")) {
                            result3.add(24, "M,0,2014,0,accountancy,0");
                        }
                        if (!result3.get(25).contains("2014") || !result3.get(25).contains("M") || !result3.get(25).contains("business")) {
                            result3.add(25, "M,0,2014,0,business,0");
                        }
                        if (!result3.get(26).contains("2014") || !result3.get(26).contains("M") || !result3.get(26).contains("economics")) {
                            result3.add(26, "M,0,2014,0,economics,0");
                        }
                        if (!result3.get(27).contains("2014") || !result3.get(27).contains("M") || !result3.get(27).contains("law")) {
                            result3.add(27, "M,0,2014,0,law,0");
                        }
                        if (!result3.get(28).contains("2014") || !result3.get(28).contains("M") || !result3.get(28).contains("sis")) {
                            result3.add(28, "M,0,2014,0,sis,0");
                        }
                        if (!result3.get(29).contains("2014") || !result3.get(29).contains("M") || !result3.get(29).contains("socsc")) {
                            result3.add(29, "M,0,2014,0,socsc,0");
                        }
                        if (!result3.get(30).contains("2010") || !result3.get(30).contains("F") || !result3.get(30).contains("accountancy")) {
                            result3.add(30, "F,0,2010,0,accountancy,0");
                        }
                        if (!result3.get(31).contains("2010") || !result3.get(31).contains("F") || !result3.get(31).contains("business")) {
                            result3.add(31, "F,0,2010,0,business,0");
                        }
                        if (!result3.get(32).contains("2010") || !result3.get(32).contains("F") || !result3.get(32).contains("economics")) {
                            result3.add(32, "F,0,2010,0,economics,0");
                        }
                        if (!result3.get(33).contains("2010") || !result3.get(33).contains("F") || !result3.get(33).contains("law")) {
                            result3.add(33, "F,0,2010,0,law,0");
                        }
                        if (!result3.get(34).contains("2010") || !result3.get(34).contains("F") || !result3.get(34).contains("sis")) {
                            result3.add(34, "F,0,2010,0,sis,0");
                        }
                        if (!result3.get(35).contains("2010") || !result3.get(35).contains("F") || !result3.get(35).contains("socsc")) {
                            result3.add(35, "F,0,2010,0,socsc,0");
                        }
                        if (!result3.get(36).contains("2011") || !result3.get(36).contains("F") || !result3.get(36).contains("accountancy")) {
                            result3.add(36, "F,0,2011,0,accountancy,0");
                        }
                        if (!result3.get(37).contains("2011") || !result3.get(37).contains("F") || !result3.get(37).contains("business")) {
                            result3.add(37, "F,0,2011,0,business,0");
                        }
                        if (!result3.get(38).contains("2011") || !result3.get(38).contains("F") || !result3.get(38).contains("economics")) {
                            result3.add(38, "F,0,2011,0,economics,0");
                        }
                        if (!result3.get(39).contains("2011") || !result3.get(39).contains("F") || !result3.get(39).contains("law")) {
                            result3.add(39, "F,0,2011,0,law,0");
                        }
                        if (!result3.get(40).contains("2011") || !result3.get(40).contains("F") || !result3.get(40).contains("sis")) {
                            result3.add(40, "F,0,2011,0,sis,0");
                        }
                        if (!result3.get(41).contains("2011") || !result3.get(41).contains("F") || !result3.get(41).contains("socsc")) {
                            result3.add(41, "F,0,2011,0,socsc,0");
                        }
                        if (!result3.get(42).contains("2012") || !result3.get(42).contains("F") || !result3.get(42).contains("accountancy")) {
                            result3.add(42, "F,0,2012,0,accountancy,0");
                        }
                        if (!result3.get(43).contains("2012") || !result3.get(43).contains("F") || !result3.get(43).contains("business")) {
                            result3.add(43, "F,0,2012,0,business,0");
                        }
                        if (!result3.get(44).contains("2012") || !result3.get(44).contains("F") || !result3.get(44).contains("economics")) {
                            result3.add(44, "F,0,2012,0,economics,0");
                        }
                        if (!result3.get(45).contains("2012") || !result3.get(45).contains("F") || !result3.get(45).contains("law")) {
                            result3.add(45, "F,0,2012,0,law,0");
                        }
                        if (!result3.get(46).contains("2012") || !result3.get(46).contains("F") || !result3.get(46).contains("sis")) {
                            result3.add(46, "F,0,2012,0,sis,0");
                        }
                        if (!result3.get(47).contains("2012") || !result3.get(47).contains("F") || !result3.get(47).contains("socsc")) {
                            result3.add(47, "F,0,2012,0,socsc,0");
                        }
                        if (!result3.get(48).contains("2013") || !result3.get(48).contains("F") || !result3.get(48).contains("accountancy")) {
                            result3.add(48, "F,0,2013,0,accountancy,0");
                        }
                        if (!result3.get(49).contains("2013") || !result3.get(49).contains("F") || !result3.get(49).contains("business")) {
                            result3.add(49, "F,0,2013,0,business,0");
                        }
                        if (!result3.get(50).contains("2013") || !result3.get(50).contains("F") || !result3.get(50).contains("economics")) {
                            result3.add(50, "F,0,2013,0,economics,0");
                        }
                        if (!result3.get(51).contains("2013") || !result3.get(51).contains("F") || !result3.get(51).contains("law")) {
                            result3.add(51, "F,0,2013,0,law,0");
                        }
                        if (!result3.get(52).contains("2013") || !result3.get(52).contains("F") || !result3.get(52).contains("sis")) {
                            result3.add(52, "F,0,02013,0,sis,0");
                        }
                        if (!result3.get(53).contains("2013") || !result3.get(53).contains("F") || !result3.get(53).contains("socsc")) {
                            result3.add(53, "F,0,2013,0,socsc,0");
                        }
                        if (!result3.get(54).contains("2014") || !result3.get(54).contains("F") || !result3.get(54).contains("accountancy")) {
                            result3.add(54, "F,0,2014,0,accountancy,0");
                        }
                        if (!result3.get(55).contains("2014") || !result3.get(55).contains("F") || !result3.get(55).contains("business")) {
                            result3.add(55, "F,0,2014,0,business,0");
                        }
                        if (!result3.get(56).contains("2014") || !result3.get(56).contains("F") || !result3.get(56).contains("economics")) {
                            result3.add(56, "F,0,2014,0,economics,0");
                        }
                        if (!result3.get(57).contains("2014") || !result3.get(57).contains("F") || !result3.get(57).contains("law")) {
                            result3.add(57, "F,0,2014,0,law,0");
                        }
                        if (!result3.get(58).contains("2014") || !result3.get(58).contains("F") || !result3.get(58).contains("sis")) {
                            result3.add(58, "F,0,2014,0,sis,0");
                        }
                        if (!result3.get(59).contains("2014") || !result3.get(59).contains("F") || !result3.get(59).contains("socsc")) {
                            result3.add(59, "F,0,2014,0,socsc,0");
                        }

                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTimeEnd, order1, order2);
                        header = "GENDER, GENDER COUNT, YEAR, YEAR COUNT";
                        if (!result2.get(0).contains("2010") || !result2.get(0).contains("M")) {
                            result2.add(0, "M,0,2010,0");
                        }
                        if (!result2.get(1).contains("2011") || !result2.get(1).contains("M")) {
                            result2.add(1, "M,0,2011,0");
                        }
                        if (!result2.get(2).contains("2012") || !result2.get(2).contains("M")) {
                            result2.add(2, "M,0,2012,0");
                        }
                        if (!result2.get(3).contains("2013") || !result2.get(3).contains("M")) {
                            result2.add(3, "M,0,2013,0");
                        }
                        if (!result2.get(4).contains("2014") || !result2.get(4).contains("M")) {
                            result2.add(4, "M,0,2014,0");
                        }
                        if (!result2.get(5).contains("2010") || !result2.get(5).contains("F")) {
                            result2.add(5, "F,0,2010,0");
                        }
                        if (!result2.get(6).contains("2011") || !result2.get(6).contains("F")) {
                            result2.add(6, "F,0,2011,0");
                        }
                        if (!result2.get(7).contains("2012") || !result2.get(7).contains("F")) {
                            result2.add(7, "F,0,2012,0");
                        }
                        if (!result2.get(8).contains("2013") || !result2.get(8).contains("F")) {
                            result2.add(8, "F,0,2013,0");
                        }
                        if (!result2.get(9).contains("2014") || !result2.get(9).contains("F")) {
                            result2.add(9, "F,0,2014,0");
                        }
                    }
                } else if ("school".equalsIgnoreCase(secondChoice)) {
                    order2 = "school";
                    if ("year".equals(thirdChoice)) {
                        order3 = "year";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTimeEnd, order1, order2, order3);
                        header = "GENDER, GENDER COUNT, SCHOOL, SCHOOL COUNT, YEAR, YEAR COUNT";
                        if (!result3.get(0).contains("2010") || !result3.get(0).contains("M") || !result3.get(0).contains("accountancy")) {
                            result3.add(0, "M,0,accountancy,0,2010,0");
                        }
                        if (!result3.get(1).contains("2011") || !result3.get(1).contains("M") || !result3.get(1).contains("accountancy")) {
                            result3.add(1, "M,0,accountancy,0,2011,0");
                        }
                        if (!result3.get(2).contains("2012") || !result3.get(2).contains("M") || !result3.get(2).contains("accountancy")) {
                            result3.add(2, "M,0,accountancy,0,2012,0");
                        }
                        if (!result3.get(3).contains("2013") || !result3.get(3).contains("M") || !result3.get(3).contains("accountancy")) {
                            result3.add(3, "M,0,accountancy,0,2013,0");
                        }
                        if (!result3.get(4).contains("2014") || !result3.get(4).contains("M") || !result3.get(4).contains("accountancy")) {
                            result3.add(4, "M,0,accountancy,0,2014,0");
                        }
                        if (!result3.get(5).contains("2010") || !result3.get(5).contains("M") || !result3.get(5).contains("business")) {
                            result3.add(5, "M,0,business,0,2010,0");
                        }
                        if (!result3.get(6).contains("2011") || !result3.get(6).contains("M") || !result3.get(6).contains("business")) {
                            result3.add(6, "M,0,business,0,2011,0");
                        }
                        if (!result3.get(7).contains("2012") || !result3.get(7).contains("M") || !result3.get(7).contains("business")) {
                            result3.add(7, "M,0,business,0,2012,0");
                        }
                        if (!result3.get(8).contains("2013") || !result3.get(8).contains("M") || !result3.get(8).contains("business")) {
                            result3.add(8, "M,0,business,0,2013,0");
                        }
                        if (!result3.get(9).contains("2014") || !result3.get(9).contains("M") || !result3.get(9).contains("business")) {
                            result3.add(9, "M,0,business,0,2014,0");
                        }
                        if (!result3.get(10).contains("2010") || !result3.get(10).contains("M") || !result3.get(10).contains("economics")) {
                            result3.add(10, "M,0,economics,0,2010,0");
                        }
                        if (!result3.get(11).contains("2011") || !result3.get(11).contains("M") || !result3.get(11).contains("economics")) {
                            result3.add(11, "M,0,economics,0,2011,0");
                        }
                        if (!result3.get(12).contains("2012") || !result3.get(12).contains("M") || !result3.get(12).contains("economics")) {
                            result3.add(12, "M,0,economics,0,2012,0");
                        }
                        if (!result3.get(13).contains("2013") || !result3.get(13).contains("M") || !result3.get(13).contains("economics")) {
                            result3.add(13, "M,0,economics,0,2013,0");
                        }
                        if (!result3.get(14).contains("2014") || !result3.get(14).contains("M") || !result3.get(14).contains("economics")) {
                            result3.add(14, "M,0,economics,0,2014,0");
                        }
                        if (!result3.get(15).contains("2010") || !result3.get(15).contains("M") || !result3.get(15).contains("law")) {
                            result3.add(15, "M,0,law,0,2010,0");
                        }
                        if (!result3.get(16).contains("2011") || !result3.get(16).contains("M") || !result3.get(16).contains("law")) {
                            result3.add(16, "M,0,law,0,2011,0");
                        }
                        if (!result3.get(17).contains("2012") || !result3.get(17).contains("M") || !result3.get(17).contains("law")) {
                            result3.add(17, "M,0,law,0,2012,0");
                        }
                        if (!result3.get(18).contains("2013") || !result3.get(18).contains("M") || !result3.get(18).contains("law")) {
                            result3.add(18, "M,0,law,0,2013,0");
                        }
                        if (!result3.get(19).contains("2014") || !result3.get(19).contains("M") || !result3.get(19).contains("law")) {
                            result3.add(19, "M,0,law,0,2014,0");
                        }
                        if (!result3.get(20).contains("2010") || !result3.get(20).contains("M") || !result3.get(20).contains("sis")) {
                            result3.add(20, "M,0,sis,0,2010,0");
                        }
                        if (!result3.get(21).contains("2011") || !result3.get(21).contains("M") || !result3.get(21).contains("sis")) {
                            result3.add(21, "M,0,sis,0,2011,0");
                        }
                        if (!result3.get(22).contains("2012") || !result3.get(22).contains("M") || !result3.get(22).contains("sis")) {
                            result3.add(22, "M,0,sis,0,2012,0");
                        }
                        if (!result3.get(23).contains("2013") || !result3.get(23).contains("M") || !result3.get(23).contains("sis")) {
                            result3.add(23, "M,0,sis,0,2013,0");
                        }
                        if (!result3.get(24).contains("2014") || !result3.get(24).contains("M") || !result3.get(24).contains("sis")) {
                            result3.add(24, "M,0,sis,0,2014,0");
                        }
                        if (!result3.get(25).contains("2010") || !result3.get(25).contains("M") || !result3.get(25).contains("socsc")) {
                            result3.add(25, "M,0,socsc,0,2010,0");
                        }
                        if (!result3.get(26).contains("2011") || !result3.get(26).contains("M") || !result3.get(26).contains("socsc")) {
                            result3.add(26, "M,0,socsc,0,2011,0");
                        }
                        if (!result3.get(27).contains("2012") || !result3.get(27).contains("M") || !result3.get(27).contains("socsc")) {
                            result3.add(27, "M,0,socsc,0,2012,0");
                        }
                        if (!result3.get(28).contains("2013") || !result3.get(28).contains("M") || !result3.get(28).contains("socsc")) {
                            result3.add(28, "M,0,socsc,0,2013,0");
                        }
                        if (!result3.get(29).contains("2014") || !result3.get(29).contains("M") || !result3.get(29).contains("socsc")) {
                            result3.add(29, "M,0,socsc,0,2014,0");
                        }
                        if (!result3.get(30).contains("2010") || !result3.get(30).contains("F") || !result3.get(30).contains("accountancy")) {
                            result3.add(30, "F,0,accountancy,0,2010,0");
                        }
                        if (!result3.get(31).contains("2011") || !result3.get(31).contains("F") || !result3.get(31).contains("accountancy")) {
                            result3.add(31, "F,0,accountancy,0,2011,0");
                        }
                        if (!result3.get(32).contains("2012") || !result3.get(32).contains("F") || !result3.get(32).contains("accountancy")) {
                            result3.add(32, "F,0,accountancy,0,2012,0");
                        }
                        if (!result3.get(33).contains("2013") || !result3.get(33).contains("F") || !result3.get(33).contains("accountancy")) {
                            result3.add(33, "F,0,accountancy,0,2013,0");
                        }
                        if (!result3.get(34).contains("2014") || !result3.get(34).contains("F") || !result3.get(34).contains("accountancy")) {
                            result3.add(34, "F,0,accountancy,0,2014,0");
                        }
                        if (!result3.get(35).contains("2010") || !result3.get(35).contains("F") || !result3.get(35).contains("business")) {
                            result3.add(35, "F,0,business,0,2010,0");
                        }
                        if (!result3.get(36).contains("2011") || !result3.get(36).contains("F") || !result3.get(36).contains("business")) {
                            result3.add(36, "F,0,business,0,2011,0");
                        }
                        if (!result3.get(37).contains("2012") || !result3.get(37).contains("F") || !result3.get(37).contains("business")) {
                            result3.add(37, "F,0,business,0,2012,0");
                        }
                        if (!result3.get(38).contains("2013") || !result3.get(38).contains("F") || !result3.get(38).contains("business")) {
                            result3.add(38, "F,0,business,0,2013,0");
                        }
                        if (!result3.get(39).contains("2014") || !result3.get(39).contains("F") || !result3.get(39).contains("business")) {
                            result3.add(39, "F,0,business,0,2014,0");
                        }
                        if (!result3.get(40).contains("2010") || !result3.get(40).contains("F") || !result3.get(40).contains("economics")) {
                            result3.add(40, "F,0,economics,0,2010,0");
                        }
                        if (!result3.get(41).contains("2011") || !result3.get(41).contains("F") || !result3.get(41).contains("economics")) {
                            result3.add(41, "F,0,economics,0,2011,0");
                        }
                        if (!result3.get(42).contains("2012") || !result3.get(42).contains("F") || !result3.get(42).contains("economics")) {
                            result3.add(42, "F,0,economics,0,2012,0");
                        }
                        if (!result3.get(43).contains("2013") || !result3.get(43).contains("F") || !result3.get(43).contains("economics")) {
                            result3.add(43, "F,0,economics,0,2013,0");
                        }
                        if (!result3.get(44).contains("2014") || !result3.get(44).contains("F") || !result3.get(44).contains("economics")) {
                            result3.add(44, "F,0,economics,0,2014,0");
                        }
                        if (!result3.get(45).contains("2010") || !result3.get(45).contains("F") || !result3.get(45).contains("law")) {
                            result3.add(45, "F,0,law,0,2010,0");
                        }
                        if (!result3.get(46).contains("2011") || !result3.get(46).contains("F") || !result3.get(46).contains("law")) {
                            result3.add(46, "F,0,law,0,2011,0");
                        }
                        if (!result3.get(47).contains("2012") || !result3.get(47).contains("F") || !result3.get(47).contains("law")) {
                            result3.add(47, "F,0,law,0,2012,0");
                        }
                        if (!result3.get(48).contains("2013") || !result3.get(48).contains("F") || !result3.get(48).contains("law")) {
                            result3.add(48, "F,0,law,0,2013,0");
                        }
                        if (!result3.get(49).contains("2014") || !result3.get(49).contains("F") || !result3.get(49).contains("law")) {
                            result3.add(49, "F,0,law,0,2014,0");
                        }
                        if (!result3.get(50).contains("2010") || !result3.get(50).contains("F") || !result3.get(50).contains("sis")) {
                            result3.add(50, "F,0,sis,0,2010,0");
                        }
                        if (!result3.get(51).contains("2011") || !result3.get(51).contains("F") || !result3.get(51).contains("sis")) {
                            result3.add(51, "F,0,sis,0,2011,0");
                        }
                        if (!result3.get(52).contains("2012") || !result3.get(52).contains("F") || !result3.get(52).contains("sis")) {
                            result3.add(52, "F,0,sis,0,2012,0");
                        }
                        if (!result3.get(53).contains("2013") || !result3.get(53).contains("F") || !result3.get(53).contains("sis")) {
                            result3.add(53, "F,0,sis,0,2013,0");
                        }
                        if (!result3.get(54).contains("2014") || !result3.get(54).contains("F") || !result3.get(54).contains("sis")) {
                            result3.add(54, "F,0,sis,0,2014,0");
                        }
                        if (!result3.get(55).contains("2010") || !result3.get(55).contains("F") || !result3.get(55).contains("socsc")) {
                            result3.add(55, "F,0,socsc,0,2010,0");
                        }
                        if (!result3.get(56).contains("2011") || !result3.get(56).contains("F") || !result3.get(56).contains("socsc")) {
                            result3.add(56, "F,0,socsc,0,2011,0");
                        }
                        if (!result3.get(57).contains("2012") || !result3.get(57).contains("F") || !result3.get(57).contains("socsc")) {
                            result3.add(57, "F,0,socsc,0,2012,0");
                        }
                        if (!result3.get(58).contains("2013") || !result3.get(58).contains("F") || !result3.get(58).contains("socsc")) {
                            result3.add(58, "F,0,socsc,0,2013,0");
                        }
                        if (!result3.get(59).contains("2014") || !result3.get(59).contains("F") || !result3.get(59).contains("socsc")) {
                            result3.add(59, "F,0,socsc,0,2014,0");
                        }

                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTimeEnd, order1, order2);
                        header = "GENDER, GENDER COUNT, SCHOOL, SCHOOL COUNT";
                        if (!result2.get(0).contains("accountancy") || !result2.get(0).contains("M")) {
                            result2.add(0, "M,0,accountancy,0");
                        }
                        if (!result2.get(1).contains("business") || !result2.get(1).contains("M")) {
                            result2.add(1, "M,0,business,0");
                        }
                        if (!result2.get(2).contains("economics") || !result2.get(2).contains("M")) {
                            result2.add(2, "M,0,economics,0");
                        }
                        if (!result2.get(3).contains("law") || !result2.get(3).contains("M")) {
                            result2.add(3, "M,0,law,0");
                        }
                        if (!result2.get(4).contains("sis") || !result2.get(4).contains("M")) {
                            result2.add(4, "M,0,sis,0");
                        }
                        if (!result2.get(5).contains("socsc") || !result2.get(5).contains("M")) {
                            result2.add(5, "M,0,socsc,0");
                        }
                        if (!result2.get(6).contains("accountancy") || !result2.get(6).contains("F")) {
                            result2.add(6, "F,0,accountancy,0");
                        }
                        if (!result2.get(7).contains("business") || !result2.get(7).contains("F")) {
                            result2.add(7, "F,0,business,0");
                        }
                        if (!result2.get(8).contains("economics") || !result2.get(8).contains("F")) {
                            result2.add(8, "F,0,economics,0");
                        }
                        if (!result2.get(9).contains("law") || !result2.get(9).contains("F")) {
                            result2.add(9, "F,0,law,0");
                        }
                        if (!result2.get(10).contains("sis") || !result2.get(10).contains("F")) {
                            result2.add(10, "F,0,sis,0");
                        }
                        if (!result2.get(11).contains("socsc") || !result2.get(11).contains("F")) {
                            result2.add(11, "F,0,socsc,0");
                        }
                    }//end of 2nd choice
                } else {
                    result = BreakdownDAO.breakdownByOne(dateTimeStart, dateTimeEnd, order1);
                    header = "GENDER, GENDER COUNT";
                    if (!result.get(0).contains("M")) {
                        result.add(0, "M,0");
                    }
                    if (!result.get(1).contains("F")) {
                        result.add(1, "F,0");
                    }
                }

            } else {
                order1 = "school";
                if ("gender".equalsIgnoreCase(secondChoice)) {
                    order2 = "gender";
                    if ("year".equalsIgnoreCase(thirdChoice)) {
                        order3 += "year";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTimeEnd, order1, order2, order3);
                        header = "SCHOOL, SCHOOL COUNT, GENDER, GENDER COUNT, YEAR, YEAR COUNT";
                        if (!result3.get(0).contains("2010") || !result3.get(0).contains("accountancy") || !result3.get(0).contains("M")) {
                            result3.add(0, "accountancy,0,M,0,2010,0");
                        }
                        if (!result3.get(1).contains("2011") || !result3.get(1).contains("accountancy") || !result3.get(1).contains("M")) {
                            result3.add(1, "accountancy,0,M,0,2011,0");
                        }
                        if (!result3.get(2).contains("2012") || !result3.get(2).contains("accountancy") || !result3.get(2).contains("M")) {
                            result3.add(2, "accountancy,0,M,0,2012,0");
                        }
                        if (!result3.get(3).contains("2013") || !result3.get(3).contains("accountancy") || !result3.get(3).contains("M")) {
                            result3.add(3, "accountancy,0,M,0,2013,0");
                        }
                        if (!result3.get(4).contains("2014") || !result3.get(4).contains("accountancy") || !result3.get(4).contains("M")) {
                            result3.add(4, "accountancy,0,M,0,2014,0");
                        }
                        if (!result3.get(5).contains("2010") || !result3.get(5).contains("accountancy") || !result3.get(5).contains("F")) {
                            result3.add(5, "accountancy,0,F,0,2010,0");
                        }
                        if (!result3.get(6).contains("2011") || !result3.get(6).contains("accountancy") || !result3.get(6).contains("F")) {
                            result3.add(6, "accountancy,0,F,0,2011,0");
                        }
                        if (!result3.get(7).contains("2012") || !result3.get(7).contains("accountancy") || !result3.get(7).contains("F")) {
                            result3.add(7, "accountancy,0,F,0,2012,0");
                        }
                        if (!result3.get(8).contains("2013") || !result3.get(8).contains("accountancy") || !result3.get(8).contains("F")) {
                            result3.add(8, "accountancy,0,F,0,2013,0");
                        }
                        if (!result3.get(9).contains("2014") || !result3.get(9).contains("accountancy") || !result3.get(9).contains("F")) {
                            result3.add(9, "accountancy,0,F,0,2014,0");
                        }
                        if (!result3.get(10).contains("2010") || !result3.get(10).contains("business") || !result3.get(0).contains("M")) {
                            result3.add(10, "business,0,M,0,2010,0");
                        }
                        if (!result3.get(11).contains("2011") || !result3.get(11).contains("business") || !result3.get(1).contains("M")) {
                            result3.add(11, "business,0,M,0,2011,0");
                        }
                        if (!result3.get(12).contains("2012") || !result3.get(12).contains("business") || !result3.get(2).contains("M")) {
                            result3.add(12, "business,0,M,0,2012,0");
                        }
                        if (!result3.get(13).contains("2013") || !result3.get(13).contains("business") || !result3.get(3).contains("M")) {
                            result3.add(13, "business,0,M,0,2013,0");
                        }
                        if (!result3.get(14).contains("2014") || !result3.get(14).contains("business") || !result3.get(4).contains("M")) {
                            result3.add(14, "business,0,M,0,2014,0");
                        }
                        if (!result3.get(15).contains("2010") || !result3.get(15).contains("business") || !result3.get(5).contains("F")) {
                            result3.add(15, "business,0,F,0,2010,0");
                        }
                        if (!result3.get(16).contains("2011") || !result3.get(16).contains("business") || !result3.get(6).contains("F")) {
                            result3.add(16, "business,0,F,0,2011,0");
                        }
                        if (!result3.get(17).contains("2012") || !result3.get(17).contains("business") || !result3.get(7).contains("F")) {
                            result3.add(17, "business,0,F,0,2012,0");
                        }
                        if (!result3.get(18).contains("2013") || !result3.get(18).contains("business") || !result3.get(8).contains("F")) {
                            result3.add(18, "business,0,F,0,2013,0");
                        }
                        if (!result3.get(19).contains("2014") || !result3.get(19).contains("business") || !result3.get(9).contains("F")) {
                            result3.add(19, "business,0,F,0,2014,0");
                        }
                        if (!result3.get(20).contains("2010") || !result3.get(20).contains("economics") || !result3.get(0).contains("M")) {
                            result3.add(20, "economics,0,M,0,2010,0");
                        }
                        if (!result3.get(21).contains("2011") || !result3.get(21).contains("economics") || !result3.get(1).contains("M")) {
                            result3.add(21, "economics,0,M,0,2011,0");
                        }
                        if (!result3.get(22).contains("2012") || !result3.get(22).contains("economics") || !result3.get(2).contains("M")) {
                            result3.add(22, "economics,0,M,0,2012,0");
                        }
                        if (!result3.get(23).contains("2013") || !result3.get(23).contains("economics") || !result3.get(3).contains("M")) {
                            result3.add(23, "economics,0,M,0,2013,0");
                        }
                        if (!result3.get(24).contains("2014") || !result3.get(24).contains("economics") || !result3.get(4).contains("M")) {
                            result3.add(24, "economics,0,M,0,2014,0");
                        }
                        if (!result3.get(25).contains("2010") || !result3.get(25).contains("economics") || !result3.get(5).contains("F")) {
                            result3.add(25, "economics,0,F,0,2010,0");
                        }
                        if (!result3.get(26).contains("2011") || !result3.get(26).contains("economics") || !result3.get(6).contains("F")) {
                            result3.add(26, "economics,0,F,0,2011,0");
                        }
                        if (!result3.get(27).contains("2012") || !result3.get(27).contains("economics") || !result3.get(7).contains("F")) {
                            result3.add(27, "economics,0,F,0,2012,0");
                        }
                        if (!result3.get(28).contains("2013") || !result3.get(28).contains("economics") || !result3.get(8).contains("F")) {
                            result3.add(28, "economics,0,F,0,2013,0");
                        }
                        if (!result3.get(29).contains("2014") || !result3.get(29).contains("economics") || !result3.get(9).contains("F")) {
                            result3.add(29, "economics,0,F,0,2014,0");
                        }
                        if (!result3.get(30).contains("2010") || !result3.get(30).contains("law") || !result3.get(0).contains("M")) {
                            result3.add(30, "law,0,M,0,2010,0");
                        }
                        if (!result3.get(31).contains("2011") || !result3.get(31).contains("law") || !result3.get(1).contains("M")) {
                            result3.add(31, "law,0,M,0,2011,0");
                        }
                        if (!result3.get(32).contains("2012") || !result3.get(32).contains("law") || !result3.get(2).contains("M")) {
                            result3.add(32, "law,0,M,0,2012,0");
                        }
                        if (!result3.get(33).contains("2013") || !result3.get(33).contains("law") || !result3.get(3).contains("M")) {
                            result3.add(33, "law,0,M,0,2013,0");
                        }
                        if (!result3.get(34).contains("2014") || !result3.get(34).contains("law") || !result3.get(4).contains("M")) {
                            result3.add(34, "law,0,M,0,2014,0");
                        }
                        if (!result3.get(35).contains("2010") || !result3.get(35).contains("law") || !result3.get(5).contains("F")) {
                            result3.add(35, "law,0,F,0,2010,0");
                        }
                        if (!result3.get(36).contains("2011") || !result3.get(36).contains("law") || !result3.get(6).contains("F")) {
                            result3.add(36, "law,0,F,0,2011,0");
                        }
                        if (!result3.get(37).contains("2012") || !result3.get(37).contains("law") || !result3.get(7).contains("F")) {
                            result3.add(37, "law,0,F,0,2012,0");
                        }
                        if (!result3.get(38).contains("2013") || !result3.get(38).contains("law") || !result3.get(8).contains("F")) {
                            result3.add(38, "law,0,F,0,2013,0");
                        }
                        if (!result3.get(39).contains("2014") || !result3.get(39).contains("law") || !result3.get(9).contains("F")) {
                            result3.add(39, "law,0,F,0,2014,0");
                        }
                        if (!result3.get(40).contains("2010") || !result3.get(40).contains("sis") || !result3.get(0).contains("M")) {
                            result3.add(40, "sis,0,M,0,2010,0");
                        }
                        if (!result3.get(41).contains("2011") || !result3.get(41).contains("sis") || !result3.get(1).contains("M")) {
                            result3.add(41, "sis,0,M,0,2011,0");
                        }
                        if (!result3.get(42).contains("2012") || !result3.get(42).contains("sis") || !result3.get(2).contains("M")) {
                            result3.add(42, "sis,0,M,0,2012,0");
                        }
                        if (!result3.get(43).contains("2013") || !result3.get(43).contains("sis") || !result3.get(3).contains("M")) {
                            result3.add(43, "sis,0,M,0,2013,0");
                        }
                        if (!result3.get(44).contains("2014") || !result3.get(44).contains("sis") || !result3.get(4).contains("M")) {
                            result3.add(44, "sis,0,M,0,2014,0");
                        }
                        if (!result3.get(45).contains("2010") || !result3.get(45).contains("sis") || !result3.get(5).contains("F")) {
                            result3.add(45, "sis,0,F,0,2010,0");
                        }
                        if (!result3.get(46).contains("2011") || !result3.get(46).contains("sis") || !result3.get(6).contains("F")) {
                            result3.add(46, "sis,0,F,0,2011,0");
                        }
                        if (!result3.get(47).contains("2012") || !result3.get(47).contains("sis") || !result3.get(7).contains("F")) {
                            result3.add(47, "sis,0,F,0,2012,0");
                        }
                        if (!result3.get(48).contains("2013") || !result3.get(48).contains("sis") || !result3.get(8).contains("F")) {
                            result3.add(48, "sis,0,F,0,2013,0");
                        }
                        if (!result3.get(49).contains("2014") || !result3.get(49).contains("sis") || !result3.get(9).contains("F")) {
                            result3.add(49, "sis,0,F,0,2014,0");
                        }
                        if (!result3.get(50).contains("2010") || !result3.get(50).contains("socsc") || !result3.get(0).contains("M")) {
                            result3.add(50, "socsc,0,M,0,2010,0");
                        }
                        if (!result3.get(51).contains("2011") || !result3.get(51).contains("socsc") || !result3.get(1).contains("M")) {
                            result3.add(51, "socsc,0,M,0,2011,0");
                        }
                        if (!result3.get(52).contains("2012") || !result3.get(52).contains("socsc") || !result3.get(2).contains("M")) {
                            result3.add(52, "socsc,0,M,0,2012,0");
                        }
                        if (!result3.get(53).contains("2013") || !result3.get(53).contains("socsc") || !result3.get(3).contains("M")) {
                            result3.add(53, "socsc,0,M,0,2013,0");
                        }
                        if (!result3.get(54).contains("2014") || !result3.get(54).contains("socsc") || !result3.get(4).contains("M")) {
                            result3.add(54, "socsc,0,M,0,2014,0");
                        }
                        if (!result3.get(55).contains("2010") || !result3.get(55).contains("socsc") || !result3.get(5).contains("F")) {
                            result3.add(55, "socsc,0,F,0,2010,0");
                        }
                        if (!result3.get(56).contains("2011") || !result3.get(56).contains("socsc") || !result3.get(6).contains("F")) {
                            result3.add(56, "socsc,0,F,0,2011,0");
                        }
                        if (!result3.get(57).contains("2012") || !result3.get(57).contains("socsc") || !result3.get(7).contains("F")) {
                            result3.add(57, "socsc,0,F,0,2012,0");
                        }
                        if (!result3.get(58).contains("2013") || !result3.get(58).contains("socsc") || !result3.get(8).contains("F")) {
                            result3.add(58, "socsc,0,F,0,2013,0");
                        }
                        if (!result3.get(59).contains("2014") || !result3.get(59).contains("socsc") || !result3.get(9).contains("F")) {
                            result3.add(59, "socsc,0,F,0,2014,0");
                        }
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTimeEnd, order1, order2);
                        header = "SCHOOL, SCHOOL COUNT, GENDER, GENDER COUNT";
                        if (!result2.get(0).contains("M") || !result2.get(0).contains("accountancy")) {
                            result2.add(0, "accountancy,0,M,0");
                        }
                        if (!result2.get(1).contains("F") || !result2.get(1).contains("accountancy")) {
                            result2.add(1, "accountancy,0,F,0");
                        }
                        if (!result2.get(2).contains("M") || !result2.get(2).contains("business")) {
                            result2.add(2, "business,0,M,0");
                        }
                        if (!result2.get(3).contains("F") || !result2.get(3).contains("business")) {
                            result2.add(3, "business,0,F,0");
                        }
                        if (!result2.get(4).contains("M") || !result2.get(4).contains("economics")) {
                            result2.add(4, "economics,0,M,0");
                        }
                        if (!result2.get(5).contains("F") || !result2.get(5).contains("economics")) {
                            result2.add(5, "economics,0,F,0");
                        }
                        if (!result2.get(6).contains("M") || !result2.get(6).contains("law")) {
                            result2.add(6, "law,0,M,0");
                        }
                        if (!result2.get(7).contains("F") || !result2.get(7).contains("law")) {
                            result2.add(7, "law,0,F,0");
                        }
                        if (!result2.get(8).contains("M") || !result2.get(8).contains("sis")) {
                            result2.add(8, "sis,0,M,0");
                        }
                        if (!result2.get(9).contains("F") || !result2.get(9).contains("sis")) {
                            result2.add(9, "sis,0,F,0");
                        }
                        if (!result2.get(10).contains("M") || !result2.get(10).contains("socsc")) {
                            result2.add(10, "socsc,0,M,0");
                        }
                        if (!result2.get(11).contains("F") || !result2.get(11).contains("socsc")) {
                            result2.add(11, "socsc,0,F,0");
                        }

                    }
                } else if ("year".equalsIgnoreCase(secondChoice)) {
                    order2 = "year";
                    if ("gender".equalsIgnoreCase(thirdChoice)) {
                        order3 = "gender";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTimeEnd, order1, order2, order3);
                        header = "SCHOOL, SCHOOL COUNT, YEAR, YEAR COUNT, GENDER, GENDER COUNT";
                        if (!result3.get(0).contains("2010") || !result3.get(0).contains("accountancy") || !result3.get(0).contains("M")) {
                            result3.add(0, "accountancy,0,2010,0,M,0");
                        }
                        if (!result3.get(1).contains("2010") || !result3.get(1).contains("accountancy") || !result3.get(1).contains("F")) {
                            result3.add(1, "accountancy,0,2010,0,F,0");
                        }
                        if (!result3.get(2).contains("2011") || !result3.get(2).contains("accountancy") || !result3.get(2).contains("M")) {
                            result3.add(2, "accountancy,0,2011,0,M,0");
                        }
                        if (!result3.get(3).contains("2011") || !result3.get(3).contains("accountancy") || !result3.get(3).contains("F")) {
                            result3.add(3, "accountancy,0,2011,0,F,0");
                        }
                        if (!result3.get(4).contains("2010") || !result3.get(4).contains("accountancy") || !result3.get(4).contains("M")) {
                            result3.add(4, "accountancy,0,2012,0,M,0");
                        }
                        if (!result3.get(5).contains("2010") || !result3.get(5).contains("accountancy") || !result3.get(5).contains("F")) {
                            result3.add(5, "accountancy,0,2012,0,F,0");
                        }
                        if (!result3.get(6).contains("2010") || !result3.get(6).contains("accountancy") || !result3.get(6).contains("M")) {
                            result3.add(6, "accountancy,0,2013,0,M,0");
                        }
                        if (!result3.get(7).contains("2010") || !result3.get(7).contains("accountancy") || !result3.get(7).contains("F")) {
                            result3.add(7, "accountancy,0,2013,0,F,0");
                        }
                        if (!result3.get(8).contains("2014") || !result3.get(8).contains("accountancy") || !result3.get(8).contains("M")) {
                            result3.add(8, "accountancy,0,2014,0,M,0");
                        }
                        if (!result3.get(9).contains("2014") || !result3.get(9).contains("accountancy") || !result3.get(9).contains("F")) {
                            result3.add(9, "accountancy,0,2014,0,F,0");
                        }
                        if (!result3.get(10).contains("2010") || !result3.get(10).contains("business") || !result3.get(10).contains("M")) {
                            result3.add(10, "business,0,2010,0,M,0");
                        }
                        if (!result3.get(11).contains("2010") || !result3.get(11).contains("business") || !result3.get(11).contains("F")) {
                            result3.add(11, "business,0,2010,0,F,0");
                        }
                        if (!result3.get(12).contains("2011") || !result3.get(12).contains("business") || !result3.get(12).contains("M")) {
                            result3.add(12, "business,0,2011,0,M,0");
                        }
                        if (!result3.get(13).contains("2011") || !result3.get(13).contains("business") || !result3.get(13).contains("F")) {
                            result3.add(13, "business,0,2011,0,F,0");
                        }
                        if (!result3.get(14).contains("2010") || !result3.get(14).contains("business") || !result3.get(14).contains("M")) {
                            result3.add(14, "business,0,2012,0,M,0");
                        }
                        if (!result3.get(15).contains("2010") || !result3.get(15).contains("business") || !result3.get(15).contains("F")) {
                            result3.add(15, "business,0,2012,0,F,0");
                        }
                        if (!result3.get(16).contains("2010") || !result3.get(16).contains("business") || !result3.get(16).contains("M")) {
                            result3.add(16, "business,0,2013,0,M,0");
                        }
                        if (!result3.get(17).contains("2010") || !result3.get(17).contains("business") || !result3.get(17).contains("F")) {
                            result3.add(17, "business,0,2013,0,F,0");
                        }
                        if (!result3.get(18).contains("2014") || !result3.get(18).contains("business") || !result3.get(18).contains("M")) {
                            result3.add(18, "business,0,2014,0,M,0");
                        }
                        if (!result3.get(19).contains("2014") || !result3.get(19).contains("business") || !result3.get(19).contains("F")) {
                            result3.add(19, "business,0,2014,0,F,0");
                        }
                        if (!result3.get(20).contains("2010") || !result3.get(20).contains("economics") || !result3.get(20).contains("M")) {
                            result3.add(20, "economics,0,2010,0,M,0");
                        }
                        if (!result3.get(21).contains("2010") || !result3.get(21).contains("economics") || !result3.get(21).contains("F")) {
                            result3.add(21, "economics,0,2010,0,F,0");
                        }
                        if (!result3.get(22).contains("2011") || !result3.get(22).contains("economics") || !result3.get(22).contains("M")) {
                            result3.add(22, "economics,0,2011,0,M,0");
                        }
                        if (!result3.get(23).contains("2011") || !result3.get(23).contains("economics") || !result3.get(23).contains("F")) {
                            result3.add(23, "economics,0,2011,0,F,0");
                        }
                        if (!result3.get(24).contains("2010") || !result3.get(24).contains("economics") || !result3.get(24).contains("M")) {
                            result3.add(24, "economics,0,2012,0,M,0");
                        }
                        if (!result3.get(25).contains("2010") || !result3.get(25).contains("economics") || !result3.get(25).contains("F")) {
                            result3.add(25, "economics,0,2012,0,F,0");
                        }
                        if (!result3.get(26).contains("2010") || !result3.get(26).contains("economics") || !result3.get(26).contains("M")) {
                            result3.add(26, "economics,0,2013,0,M,0");
                        }
                        if (!result3.get(27).contains("2010") || !result3.get(27).contains("economics") || !result3.get(27).contains("F")) {
                            result3.add(27, "economics,0,2013,0,F,0");
                        }
                        if (!result3.get(28).contains("2014") || !result3.get(28).contains("economics") || !result3.get(28).contains("M")) {
                            result3.add(28, "economics,0,2014,0,M,0");
                        }
                        if (!result3.get(29).contains("2014") || !result3.get(29).contains("economics") || !result3.get(29).contains("F")) {
                            result3.add(29, "economics,0,2014,0,F,0");
                        }
                        if (!result3.get(30).contains("2010") || !result3.get(30).contains("law") || !result3.get(30).contains("M")) {
                            result3.add(30, "law,0,2010,0,M,0");
                        }
                        if (!result3.get(31).contains("2010") || !result3.get(31).contains("law") || !result3.get(31).contains("F")) {
                            result3.add(31, "law,0,2010,0,F,0");
                        }
                        if (!result3.get(32).contains("2011") || !result3.get(32).contains("law") || !result3.get(32).contains("M")) {
                            result3.add(32, "law,0,2011,0,M,0");
                        }
                        if (!result3.get(33).contains("2011") || !result3.get(33).contains("law") || !result3.get(33).contains("F")) {
                            result3.add(33, "law,0,2011,0,F,0");
                        }
                        if (!result3.get(34).contains("2010") || !result3.get(34).contains("law") || !result3.get(34).contains("M")) {
                            result3.add(34, "law,0,2012,0,M,0");
                        }
                        if (!result3.get(35).contains("2010") || !result3.get(35).contains("law") || !result3.get(35).contains("F")) {
                            result3.add(35, "law,0,2012,0,F,0");
                        }
                        if (!result3.get(36).contains("2010") || !result3.get(36).contains("law") || !result3.get(36).contains("M")) {
                            result3.add(36, "law,0,2013,0,M,0");
                        }
                        if (!result3.get(37).contains("2010") || !result3.get(37).contains("law") || !result3.get(37).contains("F")) {
                            result3.add(37, "law,0,2013,0,F,0");
                        }
                        if (!result3.get(38).contains("2014") || !result3.get(38).contains("law") || !result3.get(38).contains("M")) {
                            result3.add(38, "law,0,2014,0,M,0");
                        }
                        if (!result3.get(39).contains("2014") || !result3.get(39).contains("law") || !result3.get(39).contains("F")) {
                            result3.add(39, "law,0,2014,0,F,0");
                        }
                        if (!result3.get(40).contains("2010") || !result3.get(40).contains("sis") || !result3.get(40).contains("M")) {
                            result3.add(40, "sis,0,2010,0,M,0");
                        }
                        if (!result3.get(41).contains("2010") || !result3.get(41).contains("sis") || !result3.get(41).contains("F")) {
                            result3.add(41, "sis,0,2010,0,F,0");
                        }
                        if (!result3.get(42).contains("2011") || !result3.get(42).contains("sis") || !result3.get(42).contains("M")) {
                            result3.add(42, "sis,0,2011,0,M,0");
                        }
                        if (!result3.get(43).contains("2011") || !result3.get(43).contains("sis") || !result3.get(43).contains("F")) {
                            result3.add(43, "sis,0,2011,0,F,0");
                        }
                        if (!result3.get(44).contains("2010") || !result3.get(44).contains("sis") || !result3.get(44).contains("M")) {
                            result3.add(44, "sis,0,2012,0,M,0");
                        }
                        if (!result3.get(45).contains("2010") || !result3.get(45).contains("sis") || !result3.get(45).contains("F")) {
                            result3.add(45, "sis,0,2012,0,F,0");
                        }
                        if (!result3.get(46).contains("2010") || !result3.get(46).contains("sis") || !result3.get(46).contains("M")) {
                            result3.add(46, "sis,0,2013,0,M,0");
                        }
                        if (!result3.get(47).contains("2010") || !result3.get(47).contains("sis") || !result3.get(47).contains("F")) {
                            result3.add(47, "sis,0,2013,0,F,0");
                        }
                        if (!result3.get(48).contains("2014") || !result3.get(48).contains("sis") || !result3.get(48).contains("M")) {
                            result3.add(48, "sis,0,2014,0,M,0");
                        }
                        if (!result3.get(49).contains("2014") || !result3.get(49).contains("sis") || !result3.get(49).contains("F")) {
                            result3.add(49, "sis,0,2014,0,F,0");
                        }
                        if (!result3.get(50).contains("2010") || !result3.get(50).contains("socsc") || !result3.get(50).contains("M")) {
                            result3.add(50, "socsc,0,2010,0,M,0");
                        }
                        if (!result3.get(51).contains("2010") || !result3.get(51).contains("socsc") || !result3.get(51).contains("F")) {
                            result3.add(51, "socsc,0,2010,0,F,0");
                        }
                        if (!result3.get(52).contains("2011") || !result3.get(52).contains("socsc") || !result3.get(52).contains("M")) {
                            result3.add(52, "socsc,0,2011,0,M,0");
                        }
                        if (!result3.get(53).contains("2011") || !result3.get(53).contains("socsc") || !result3.get(53).contains("F")) {
                            result3.add(53, "socsc,0,2011,0,F,0");
                        }
                        if (!result3.get(54).contains("2010") || !result3.get(54).contains("socsc") || !result3.get(54).contains("M")) {
                            result3.add(54, "socsc,0,2012,0,M,0");
                        }
                        if (!result3.get(55).contains("2010") || !result3.get(55).contains("socsc") || !result3.get(55).contains("F")) {
                            result3.add(55, "socsc,0,2012,0,F,0");
                        }
                        if (!result3.get(56).contains("2010") || !result3.get(56).contains("socsc") || !result3.get(56).contains("M")) {
                            result3.add(56, "socsc,0,2013,0,M,0");
                        }
                        if (!result3.get(57).contains("2010") || !result3.get(57).contains("socsc") || !result3.get(57).contains("F")) {
                            result3.add(57, "socsc,0,2013,0,F,0");
                        }
                        if (!result3.get(58).contains("2014") || !result3.get(58).contains("socsc") || !result3.get(58).contains("M")) {
                            result3.add(58, "socsc,0,2014,0,M,0");
                        }
                        if (!result3.get(59).contains("2014") || !result3.get(59).contains("socsc") || !result3.get(59).contains("F")) {
                            result3.add(59, "socsc,0,2014,0,F,0");
                        }

                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTimeEnd, order1, order2);
                        header = "SCHOOL, SCHOOL COUNT, YEAR, YEAR COUNT";
                        if (!result2.get(0).contains("2010") || !result2.get(0).contains("accountancy")) {
//                      
                            result2.add(0, "accountancy,0,2010,0");
                        }
                        if (!result2.get(1).contains("2011") || !result2.get(1).contains("accountancy")) {
                            result2.add(1, "accountancy,0,2011,0");
                        }
                        if (!result2.get(2).contains("2012") || !result2.get(2).contains("accountancy")) {
                            result2.add(2, "accountancy,0,2012,0");
                        }
                        if (!result2.get(3).contains("2013") || !result2.get(3).contains("accountancy")) {
                            result2.add(3, "accountancy,0,2013,0");
                        }
                        if (!result2.get(4).contains("2014") || !result2.get(4).contains("accountancy")) {
                            result2.add(4, "accountancy,0,2014,0");
                        }
                        if (!result2.get(5).contains("2010") || !result2.get(5).contains("business")) {
                            result2.add(5, "business,0,2010,0");
                        }
                        if (!result2.get(6).contains("2011") || !result2.get(6).contains("business")) {
                            result2.add(6, "business,0,2011,0");
                        }
                        if (!result2.get(7).contains("2012") || !result2.get(7).contains("business")) {
                            result2.add(7, "business,0,2012,0");
                        }
                        if (!result2.get(8).contains("2013") || !result2.get(8).contains("business")) {
                            result2.add(8, "business,0,2013,0");
                        }
                        if (!result2.get(9).contains("2014") || !result2.get(9).contains("business")) {
                            result2.add(9, "business,0,2014,0");
                        }
                        if (!result2.get(10).contains("2010") || !result2.get(10).contains("economics")) {
                            result2.add(10, "economics,0,2010,0");
                        }
                        if (!result2.get(11).contains("2011") || !result2.get(11).contains("economics")) {
                            result2.add(11, "economics,0,2011,0");
                        }
                        if (!result2.get(12).contains("2012") || !result2.get(12).contains("economics")) {
                            result2.add(12, "economics,0,2012,0");
                        }
                        if (!result2.get(13).contains("2013") || !result2.get(13).contains("economics")) {
                            result2.add(13, "economics,0,2013,0");
                        }
                        if (!result2.get(14).contains("2014") || !result2.get(14).contains("economics")) {
                            result2.add(14, "economics,0,2014,0");
                        }
                        if (!result2.get(15).contains("2010") || !result2.get(15).contains("law")) {
                            result2.add(15, "law,0,2010,0");
                        }
                        if (!result2.get(16).contains("2011") || !result2.get(16).contains("law")) {
                            result2.add(16, "law,0,2011,0");
                        }
                        if (!result2.get(17).contains("2012") || !result2.get(17).contains("law")) {
                            result2.add(17, "law,0,2012,0");
                        }
                        if (!result2.get(18).contains("2013") || !result2.get(18).contains("law")) {
                            result2.add(18, "law,0,2013,0");
                        }
                        if (!result2.get(19).contains("2014") || !result2.get(19).contains("law")) {
                            result2.add(19, "law,0,2014,0");
                        }
                        if (!result2.get(20).contains("2010") || !result2.get(20).contains("sis")) {
                            result2.add(20, "sis,0,2010,0");
                        }
                        if (!result2.get(21).contains("2011") || !result2.get(21).contains("sis")) {
                            result2.add(21, "sis,0,2011,0");
                        }
                        if (!result2.get(22).contains("2012") || !result2.get(22).contains("sis")) {
                            result2.add(22, "sis,0,2012,0");
                        }
                        if (!result2.get(23).contains("2013") || !result2.get(23).contains("sis")) {
                            result2.add(23, "sis,0,2013,0");
                        }
                        if (!result2.get(24).contains("2014") || !result2.get(24).contains("sis")) {
                            result2.add(24, "sis,0,2014,0");
                        }
                        if (!result2.get(25).contains("2010") || !result2.get(25).contains("socsc")) {
                            result2.add(25, "socsc,0,2010,0");
                        }
                        if (!result2.get(26).contains("2011") || !result2.get(26).contains("socsc")) {
                            result2.add(26, "socsc,0,2011,0");
                        }
                        if (!result2.get(27).contains("2012") || !result2.get(27).contains("socsc")) {
                            result2.add(27, "socsc,0,2012,0");
                        }
                        if (!result2.get(28).contains("2013") || !result2.get(28).contains("socsc")) {
                            result2.add(28, "socsc,0,2013,0");
                        }
                        if (!result2.get(29).contains("2014") || !result2.get(29).contains("socsc")) {
                            result2.add(29, "socsc,0,2014,0");
                        }

                    }//end of 2nd choice
                } else {
                    result = BreakdownDAO.breakdownByOne(dateTimeStart, dateTimeEnd, order1);

                    header = "SCHOOL, SCHOOL COUNT";
                    if (!result.get(0).contains("accountancy")) {
                        result.add(0, "accountancy,0");
                    }
                    if (!result.get(1).contains("business")) {
                        result.add(1, "business,0");
                    }
                    if (!result.get(2).contains("economics")) {
                        result.add(2, "economics,0");
                    }
                    if (!result.get(3).contains("law")) {
                        result.add(3, "law,0");
                    }
                    if (!result.get(4).contains("sis")) {
                        result.add(4, "sis,0");
                    }
                    if (!result.get(5).contains("socsc")) {
                        result.add(5, "socsc,0");
                    }
                }
            }
            //fill in the missing rows

            if (!order3.isEmpty()) {
                //means there are 3 choices 
                JsonArray breakdown1 = new JsonArray();
                for (int i = 0; i < result3.size(); i++) {
                    String currentLine = result3.get(i);
                    String[] data = currentLine.split(",");
                    //add the first and second column
                    JsonObject firstObj = new JsonObject();
                    //data type, data
                    try {
                        int yearTest = Integer.parseInt(data[0]);
                        firstObj.addProperty(firstChoice, yearTest);
                    } catch (NumberFormatException e) {
                        firstObj.addProperty(firstChoice, data[0]);
                    }

                    int countData = 0;
                    int tempCount1 = 0;
                    //get the 2nd breakdown
                    int count = 0;

                    //get the next breakdown arr
                    JsonArray Breakdown2 = new JsonArray();
                    for (int j = 0; j < result3.size(); j++) {

                        String secondBreakdown = result3.get(j);
                        String[] secondData = secondBreakdown.split(",");

                        if ((data[0]).equals(secondData[0])) {
                            JsonObject temp = new JsonObject();
                            //get the 3rd col and data
                            try {
                                int yearTest = Integer.parseInt(secondData[2]);
                                temp.addProperty(secondChoice, yearTest);
                            } catch (NumberFormatException e) {
                                temp.addProperty(secondChoice, secondData[2]);
                            }

                            int countData2 = 0;
                            int tempCount2 = 0;

                            JsonArray breakdown3 = new JsonArray();
                            int count2 = 0;
                            for (int k = 0; k < result3.size(); k++) {
                                String thirdBreakdown = result3.get(k);
                                String[] thirdData = thirdBreakdown.split(",");
                                String compare1 = secondData[0] + secondData[2];
                                String compare2 = thirdData[0] + thirdData[2];
                                if (compare1.equals(compare2)) {
                                    JsonObject temp2 = new JsonObject();
                                    //get the 5th col and data
                                    try {
                                        int yearTest = Integer.parseInt(thirdData[4]);
                                        temp2.addProperty(thirdChoice, yearTest);
                                    } catch (NumberFormatException e) {
                                        temp2.addProperty(thirdChoice, thirdData[4]);
                                    }

                                    int countData3 = Integer.parseInt(thirdData[5]);
                                    temp2.addProperty("count", countData3);
                                    breakdown3.add(temp2);
                                    count2 = k;
                                    countData2 = Integer.parseInt(thirdData[3]);
                                    if (countData2 >= tempCount2) {
                                        tempCount2 = countData2;
                                    }
                                }

                                temp.addProperty("count", tempCount2);
                                temp.add("breakdown", breakdown3);

                            }
                            j = ++count2;

                            Breakdown2.add(temp);
                            count = j;
                            countData = Integer.parseInt(secondData[1]);
                            if (countData >= tempCount1) {
                                tempCount1 = countData;
                            }
                        }
                        firstObj.addProperty("count", tempCount1);
                    }
                    //add the breakdown into the obj arr
                    firstObj.add("breakdown", Breakdown2);
                    //add the first obj into outermost breakdown arr
                    breakdown1.add(firstObj);
                    i = ++count;

                }

                jsonOutput.add("breakdown", breakdown1);
            } else if (!order2.isEmpty()) {
                //school, count, gender, count

                JsonArray breakdown1 = new JsonArray();
                for (int i = 0; i < result2.size(); i++) {
                    String currentLine = result2.get(i);
                    String[] data = currentLine.split(",");
                    //add the first and second column
                    JsonObject firstObj = new JsonObject();
                    //data type, data
                    try {
                        int yearTest = Integer.parseInt(data[0]);
                        firstObj.addProperty(firstChoice, yearTest);
                    } catch (NumberFormatException e) {
                        firstObj.addProperty(firstChoice, data[0]);
                    }
                    int countData = 0;
                    int tempCount = 0;
                    //get the 2nd breakdown
                    int count = 0;
                    //get the next breakdown arr
                    JsonArray Breakdown2 = new JsonArray();
                    for (int j = 0; j < result2.size(); j++) {
                        String secondBreakdown = result2.get(j);
                        String[] secondData = secondBreakdown.split(",");
                        if ((data[0]).equals(secondData[0])) {
                            JsonObject temp = new JsonObject();
                            //get the 3rd col and data
                            try {
                                int yearTest = Integer.parseInt(secondData[2]);
                                temp.addProperty(secondChoice, yearTest);
                            } catch (NumberFormatException e) {
                                temp.addProperty(secondChoice, secondData[2]);
                            }
                            int countData2 = Integer.parseInt(secondData[3]);
                            temp.addProperty("count", countData2);
                            Breakdown2.add(temp);
                            count = j;
                            countData = Integer.parseInt(secondData[1]);
                            if (countData >= tempCount) {
                                tempCount = countData;
                            }
                        }

                        firstObj.addProperty("count", tempCount);
                    }
                    //add the breakdown into the obj arr
                    firstObj.add("breakdown", Breakdown2);
                    //add the first obj into outermost breakdown arr
                    breakdown1.add(firstObj);
                    i = ++count;

                }

                jsonOutput.add("breakdown", breakdown1);
            } else if (!order1.isEmpty()) {
                for (String line : result) {
                    String[] data = line.split(",");
                    JsonObject temp = new JsonObject();
                    try {
                        int yearTest = Integer.parseInt(data[0]);
                        temp.addProperty(firstChoice, yearTest);
                    } catch (NumberFormatException e) {
                        temp.addProperty(firstChoice, data[0]);
                    }
                    int countData = Integer.parseInt(data[1]);
                    temp.addProperty("count", countData);
                    breakdown.add(temp);

                }
                jsonOutput.add("breakdown", breakdown);
            }

            out.println(gson.toJson(jsonOutput));

        } else {
            jsonOutput.addProperty("status", "error");
            //sorting the errlist into alphabetical order
            HashSet hs = new HashSet();
            hs.addAll(errList);
            errList.clear();
            errList.addAll(hs);

            Collections.sort(errList);
            //loop through the errors to add into the json error array
            for (String err : errList) {
                errMsg.add(new JsonPrimitive(err));
            }

            jsonOutput.add("messages", errMsg);

            out.println(gson.toJson(jsonOutput));

        }
        out.close();
    }

    /**
     *
     * to validate the date
     *
     * @param dateObj is the Date object to be validated
     * @return a boolean to show whether the date is valid or invalid
     */
    private boolean validDate(Date dateObj) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setLenient(false);
        try {
            Date validateAfter2010 = sdf.parse("2009-12-31T23:59:59");
            Date validateBefore2015 = sdf.parse("2015-01-01T00:00:00");
            return dateObj.before(validateBefore2015) && dateObj.after(validateAfter2010);
        } catch (ParseException e) {

        }
        return false;
    }

    /**
     *
     * to get the start date time to process the data
     *
     * @param date is the date time user enters
     * @return the start data time
     */
    private String getStartDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateObj;
        try {
            dateObj = sdf.parse(date);
            Calendar cal = new GregorianCalendar();
            cal.setTime(dateObj);

            cal.add(cal.MINUTE, -14);
            cal.add(cal.SECOND, -59);
            return sdf.format(cal.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(topkpopplaces.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    /**
     *
     * to check whether the order is validated or not
     *
     * @param choice the order
     * @return a boolean to show whether the order is valid or invalid
     */
    private boolean checkOrderField(String choice) {

        String[] arr = {"gender", "year", "school"};
        for (String field : arr) {
            if (field.equalsIgnoreCase(choice)) {
                return true;
            }
        }
        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
