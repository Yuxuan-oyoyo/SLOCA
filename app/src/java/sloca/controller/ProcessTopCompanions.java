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
import java.util.Iterator;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.Location;
import sloca.model.LocationDAO;
import sloca.model.TopCompanionsDAO;
import sloca.model.User;
import sloca.model.UserDAO;

/**
 *
 * to process the rank of top-k companions
 */
@WebServlet(name = "ProcessTopCompanions", urlPatterns = {"/ProcessTopCompanions"})
public class ProcessTopCompanions extends HttpServlet {

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
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String kValue = (String) request.getParameter("kValue");
        int k = Integer.parseInt(kValue);

        // get the time interval
        String dateTime = request.getParameter("dateTime");
        String dateTimeStart = "";
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
                    RequestDispatcher view = request.getRequestDispatcher("popularCompanions.jsp");
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
            request.setAttribute("timeErr", "Please try again! Date should be within 2010 and 2014");
            RequestDispatcher view = request.getRequestDispatcher("popularCompanions.jsp");
            view.forward(request, response);
            return;
        }
        String macadd = request.getParameter("macAddress");
        if (!validateMacadd(macadd)) {
            request.setAttribute("macAddressErr", "Invalid MAC address.Please try again!");
            request.setAttribute("time", dateTime);
            request.setAttribute("k", kValue);
            request.setAttribute("macadd", macadd);
            RequestDispatcher view = request.getRequestDispatcher("popularCompanions.jsp");
            view.forward(request, response);
            return;
        } else {
            ArrayList<String> userTimeLine = TopCompanionsDAO.retrieveUserTimeLine(dateTimeStart, dateTime, macadd);
            TreeMap<String, Integer> companionTotalTime = new TreeMap<String, Integer>();
            ArrayList<String> companionTimeLine = new ArrayList<String>();

            for (String curr : userTimeLine) {
                String arr[] = curr.split(",");
                for (int i = 0; i < arr.length; i++) {
                    companionTimeLine = TopCompanionsDAO.retrieveCompanionTime(arr[0], arr[1], arr[2], arr[3], dateTimeStart);
                }
                for (String record : companionTimeLine) {

                    String arr2[] = record.split(",");
                    if (!companionTotalTime.containsKey(arr2[0])) {
                        companionTotalTime.put(arr2[0], Integer.parseInt(arr2[1]));
                    } else {
                        Integer temp = companionTotalTime.get(arr2[0]);
                        temp += Integer.parseInt(arr2[1]);
                        companionTotalTime.remove(arr2[0]);
                        companionTotalTime.put(arr2[0], temp);
                    }
                }
            }

            ArrayList<String> result = new ArrayList<String>();
            int rank = 0;
            int check = 0;
            while (rank <= k && companionTotalTime.size() != 0) {

                int max = 0;
                String maxMac = "";

                Iterator<String> iter = companionTotalTime.keySet().iterator();
                while (iter.hasNext()) {
                    String macAddress = iter.next();
                    int totalTime = companionTotalTime.get(macAddress);
                    if (totalTime > max) {
                        maxMac = macAddress;
                        max = totalTime;
                    }
                }
                String email = TopCompanionsDAO.retrieveEmail(maxMac);
                if (check != 0) {
                    if (check != max) {
                        rank++;
                    }
                } else {
                    rank = 1;
                }
                check = max;
                if (rank <= k) {
                    result.add(rank + "," + maxMac + "," + email + "," + max);
                }
                companionTotalTime.remove(maxMac);

            }

            request.setAttribute("output", result);
            request.setAttribute("time", dateTime);
            request.setAttribute("k", kValue);
            request.setAttribute("macadd", macadd);
            RequestDispatcher dispatcher = request.getRequestDispatcher("popularCompanions.jsp");
            dispatcher.forward(request, response);
        }

    }

    /**
     *
     * to validate the mac-address
     *
     * @param macAdd the mac-address to be validated
     * @return a boolean to show whether the mac-address is valid or invalid
     */
    private boolean validateMacadd(String macAdd) {
        Location l = LocationDAO.retrieveUserByMacAdd(macAdd);
        if (l == null) {
            return false;
        } else {
            return true;
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

}
