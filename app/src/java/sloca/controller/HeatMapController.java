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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sloca.model.HeatmapDAO;

/**
 *
 * to generate the heatmap
 */
public class HeatMapController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods, to generate the heatmap
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        RequestDispatcher view = request.getRequestDispatcher("heatMap.jsp");
        //error list
        ArrayList<String> errList = new ArrayList<String>();
        //get the floor of the reuqest
        String level = request.getParameter("floor");
        //check if the floor is valid
        int floor = -1;
        try {
            floor = Integer.parseInt(level);
            if (floor < 0 || floor > 5) {
                errList.add("invalid floor");
            } else if (floor == 0) {
                level = "B1";
            } else {
                level = "L" + floor;
            }

        } catch (NumberFormatException nfe) {
            errList.add("invalid floor");
        }

        //get the date and time from the query
        String dateTimeEnd = request.getParameter("dateTime");
        //get the time for the date and query

        try {
            //check if time is HH:mm and append :00 if need be
            try {
                char c = dateTimeEnd.charAt(17);
            } catch (IndexOutOfBoundsException iobe) {
                dateTimeEnd += ":00";
            }
            //concat date and time tgt
            //date formatter to check validtiy
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //will not accept any other date type apart from the sepcified above format
            sdf.setLenient(false);
            Date dateObj = null;
            //check if it is a proper date
            try {
                dateObj = sdf.parse(dateTimeEnd);
            } catch (ParseException e) {
                errList.add("invalid date");
            }
            //check if date is within 2010 and 2014
            if (!validDate(dateObj)) {
                request.setAttribute("error", "Please enter a date between 2010 and 2014");
                request.setAttribute("level", level);
                request.setAttribute("dateTime", dateTimeEnd);
                view.forward(request, response);
                return;
            }
            //sucess case
            if (dateObj != null && errList.size() == 0) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(dateObj);
                //set time for the the start query
                cal.add(cal.MINUTE, -14);
                cal.add(cal.SECOND, -59);
                String dateTimeStart = sdf.format(cal.getTime());

                //get result set
                HashMap<String, Integer> heatmapResults = HeatmapDAO.retrieveHeatmap(dateTimeStart, dateTimeEnd, level);
                RequestDispatcher rd = request.getRequestDispatcher("heatMap.jsp");
                request.setAttribute("levelDisplay", level);
                request.setAttribute("dateTimeEnd", dateTimeEnd);
                request.setAttribute("level", level);
                request.setAttribute("dateTime", dateTimeEnd);
                HttpSession sess = request.getSession();
                sess.setAttribute("heatmap", heatmapResults);

                rd.forward(request, response);
                return;
            } else {
                request.setAttribute("errorList", errList);
                request.setAttribute("level", level);
                request.setAttribute("dateTime", dateTimeEnd);
                view.forward(request, response);
            }
        } catch (NullPointerException e1) {
            request.setAttribute("error", "Please try again!!");
            request.setAttribute("level", level);
            request.setAttribute("dateTime", dateTimeEnd);
            view.forward(request, response);
            return;
        }

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
}
