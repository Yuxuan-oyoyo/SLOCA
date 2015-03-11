/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * @author g3t2
 */
package sloca.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.BreakdownDAO;

/**
 *
 * to process the break down the data according to different conditions
 */
public class ProcessBreakdownServlet extends HttpServlet {

    /**
     *
     * to validate the date
     *
     * @param dateObj the date to be validated
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
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods to process the break down
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result2 = new ArrayList<String>();
        ArrayList<String> result3 = new ArrayList<String>();
        ArrayList<String> yearSchoolResult = new ArrayList<String>();
        String order1 = "";
        String order2 = "";
        String order3 = "";
        String dateTime = request.getParameter("dateTime");
        String dateTimeStart = "";
        Integer span = null;
        Integer span2 = null;
        //check if time is HH:mm and append :00 if need be
        try {
            char c = dateTime.charAt(17);
        } catch (IndexOutOfBoundsException iobe) {
            dateTime += ":00";
        }
        try {
            try {
                char c = dateTime.charAt(17);
            } catch (IndexOutOfBoundsException iobe) {
                dateTime += ":00";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date dateObj = sdf.parse(dateTime);

            if (dateObj != null) {
                if (!validDate(dateObj)) {
                    request.setAttribute("error", "Please try again! Date should be within 2010 and 2014");
                    RequestDispatcher view = request.getRequestDispatcher("breakDown.jsp");
                    view.forward(request, response);
                    return;
                }
                Calendar cal = new GregorianCalendar();
                cal.setTime(dateObj);

                cal.add(cal.MINUTE, -14);
                cal.add(cal.SECOND, -59);
                dateTimeStart = sdf.format(cal.getTime());
            }
        } catch (ParseException e) {
            request.setAttribute("error", "Please try again!");
            RequestDispatcher view = request.getRequestDispatcher("breakDown.jsp");
            view.forward(request, response);
            return;
        }

        String firstChoice = request.getParameter("firstChoice");
        String secondChoice = request.getParameter("secondChoice");
        String thirdChoice = request.getParameter("thirdChoice");
        if (secondChoice.equals("00")) {
            secondChoice = "0";
        }
        if (thirdChoice.equals("000")) {
            thirdChoice = "0";
        }
        String header = "";

        if (firstChoice.equals(secondChoice) || firstChoice.equals(thirdChoice)) {
            request.setAttribute("errMsg", "invalid choice");
            RequestDispatcher view = request.getRequestDispatcher("breakDown.jsp");
            view.forward(request, response);
        } else if ((!secondChoice.equals("0")) && (secondChoice.equals(thirdChoice))) {
            request.setAttribute("errMsg", "invalid choice");
            RequestDispatcher view = request.getRequestDispatcher("breakDown.jsp");
            view.forward(request, response);
        } else {
            if (firstChoice.equals("1")) {
                order1 = "year";
                if (secondChoice.equals("12")) {
                    order2 = "gender";
                    if (thirdChoice.equals("123")) {
                        order3 = "school";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTime, order1, order2, order3);
                        header = "YEAR, YEAR COUNT, YEAR %, GENDER, GENDER COUNT, GENDER %, SCHOOL, SCHOOL COUNT, SCHOOL %";
                        span = 12;
                        span2 = 6;
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTime, order1, order2);
                        header = "YEAR, YEAR COUNT, YEAR %, GENDER, GENDER COUNT, GENDER %";
                        span = 2;
                    }
                } else if (secondChoice.equals("13")) {
                    order2 = "school";
                    if (thirdChoice.equals("132")) {
                        order3 = "gender";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTime, order1, order2, order3);
                        header = "YEAR, YEAR COUNT, YEAR %, SCHOOL, SCHOOL COUNT, SCHOOL %, GENDER, GENDER COUNT, GENDER %";
                        span = 12;
                        span2 = 2;
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTime, order1, order2);
                        header = "YEAR, YEAR COUNT, YEAR %, SCHOOL, SCHOOL COUNT, SCHOOL %";
                        span = 6;
                    }//end of 2nd choice
                } else {
                    result = BreakdownDAO.breakdownByOne(dateTimeStart, dateTime, order1);
                    header = "YEAR, YEAR COUNT, YEAR %";
                }

            } else if (firstChoice.equals("2")) {
                order1 = "gender";
                if (secondChoice.equals("21")) {
                    order2 = "year";
                    if (thirdChoice.equals("213")) {
                        order3 = "shcool";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTime, order1, order2, order3);
                        header = "GENDER, GENDER COUNT, GENDER %, YEAR, YEAR COUNT, YEAR %, SCHOOL, SCHOOL COUNT, SCHOOL %";
                        span = 30;
                        span2 = 6;
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTime, order1, order2);
                        header = "GENDER, GENDER COUNT, GENDER %, YEAR, YEAR COUNT, YEAR %";
                        span = 5;
                    }
                } else if (secondChoice.equals("23")) {
                    order2 = "school";
                    if (thirdChoice.equals("231")) {
                        order3 = "year";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTime, order1, order2, order3);
                        header = "GENDER, GENDER COUNT, GENDER %, SCHOOL, SCHOOL COUNT, SCHOOL %, YEAR, YEAR COUNT, YEAR %";
                        span = 30;
                        span2 = 5;
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTime, order1, order2);
                        header = "GENDER, GENDER COUNT, GENDER %, SCHOOL, SCHOOL COUNT, SCHOOL %";
                        span = 6;
                    }//end of 2nd choice
                } else {
                    result = BreakdownDAO.breakdownByOne(dateTimeStart, dateTime, order1);
                    header = "GENDER, GENDER COUNT, GENDER %";
                }

            } else {
                order1 = "school";
                if (secondChoice.equals("32")) {
                    order2 = "gender";
                    if (thirdChoice.equals("321")) {
                        order3 += "year";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTime, order1, order2, order3);
                        header = "SCHOOL, SCHOOL COUNT, SCHOOL %, GENDER, GENDER COUNT, GENDER %, YEAR, YEAR COUNT, YEAR %";
                        span = 10;
                        span2 = 5;
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTime, order1, order2);
                        header = "SCHOOL, SCHOOL COUNT, SCHOOL %, GENDER, GENDER COUNT, GENDER %";
                        span = 2;
                    }
                } else if (secondChoice.equals("31")) {
                    order2 = "year";
                    if (thirdChoice.equals("312")) {
                        order3 = "gender";
                        result3 = BreakdownDAO.breakdownByThree(dateTimeStart, dateTime, order1, order2, order3);
                        header = "SCHOOL, SCHOOL COUNT, SCHOOL %, YEAR, YEAR COUNT, YEAR %, GENDER, GENDER COUNT, GENDER %";
                        span = 10;
                        span2 = 2;
                    } else {
                        result2 = BreakdownDAO.breakdownByTwo(dateTimeStart, dateTime, order1, order2);
                        header = "SCHOOL, SCHOOL COUNT, SCHOOL %, YEAR, YEAR COUNT, YEAR %";
                        span = 5;
                    }//end of 2nd choice
                } else {
                    result = BreakdownDAO.breakdownByOne(dateTimeStart, dateTime, order1);
                    header = "SCHOOL, SCHOOL COUNT, SCHOOL %";
                }
            }
        }
        DecimalFormat df = new DecimalFormat("#0.00");
        int sum1 = 0;
        ArrayList<String> output1 = new ArrayList<String>();
        ArrayList<String> output2 = new ArrayList<String>();
        ArrayList<String> output3 = new ArrayList<String>();
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                String currLine = result.get(i);
                String arr[] = currLine.split(",");
                sum1 += Integer.parseInt(arr[1]);
            }
            for (int i = 0; i < result.size(); i++) {
                String currLine = result.get(i);
                String arr[] = currLine.split(",");
                output1.add(arr[0] + "," + arr[1] + "," + (int) Math.round((double) Integer.parseInt(arr[1]) / sum1 * 100));
            }

        }
        int sum2 = 0;

        if (result2 != null) {
            for (int i = 0; i < result2.size(); i++) {
                String currLine = result2.get(i);
                String arr[] = currLine.split(",");
                if (i % span == 0) {
                    sum2 += Integer.parseInt(arr[1]);
                }

            }
            for (int i = 0; i < result2.size(); i++) {
                String currLine = result2.get(i);
                String arr[] = currLine.split(",");
                output2.add(arr[0] + "," + arr[1] + "," + (int) Math.round((double) Integer.parseInt(arr[1]) / sum2 * 100)
                        + "," + arr[2] + "," + arr[3] + "," + (int) Math.round((double) Integer.parseInt(arr[3]) / sum2 * 100));

            }
        }
        int sum3 = 0;
        if (result3 != null) {
            for (int i = 0; i < result3.size(); i++) {
                String currLine = result3.get(i);
                String arr[] = currLine.split(",");
                if (i % span == 0) {
                    sum3 += Integer.parseInt(arr[1]);
                }
            }
            for (int i = 0; i < result3.size(); i++) {
                String currLine = result3.get(i);
                String arr[] = currLine.split(",");
                output3.add(arr[0] + "," + arr[1] + "," + (int) Math.round((double) Integer.parseInt(arr[1]) / sum3 * 100)
                        + "," + arr[2] + "," + arr[3] + "," + (int) Math.round((double) Integer.parseInt(arr[3]) / sum3 * 100)
                        + "," + arr[4] + "," + arr[5] + "," + (int) Math.round((double) Integer.parseInt(arr[5]) / sum3 * 100));
            }
        }

        request.setAttribute("output", output1);
        request.setAttribute("output2", output2);
        request.setAttribute("output3", output3);
        request.setAttribute("spanValue", span);
        request.setAttribute("spanValue2", span2);
        request.setAttribute("header", header);
        request.setAttribute("time", dateTime);

        RequestDispatcher dispatcher = request.getRequestDispatcher("breakDown.jsp");
        dispatcher.forward(request, response);

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
