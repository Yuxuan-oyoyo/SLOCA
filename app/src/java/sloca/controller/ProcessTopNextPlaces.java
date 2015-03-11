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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import sloca.model.NextPlaceDAO;

/**
 *
 * to process the rank of top-k popular next places
 */
public class ProcessTopNextPlaces extends HttpServlet {

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
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Top-K Next Popular Places</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Top-K Next Popular Places</h1>");

            String kValue = (String) request.getParameter("kValue");
            int k = Integer.parseInt(kValue);

            String dateTime = request.getParameter("dateTime");
            //dateTime = dateTime.replace('T', ' ');
            String preTimeStart = "";
            String postTimeEnd = "";
            String postTimeStart = "";
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
                        RequestDispatcher view = request.getRequestDispatcher("topKNextPlaces.jsp");
                        view.forward(request, response);
                        return;
                    }
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(dateObj);

                    cal.add(cal.MINUTE, -14);
                    cal.add(cal.SECOND, -59);
                    preTimeStart = sdf.format(cal.getTime());

                    cal.add(cal.MINUTE, 15);

                    postTimeStart = sdf.format(cal.getTime());

                    cal.add(cal.MINUTE, 14);
                    cal.add(cal.SECOND, 59);
                    postTimeEnd = sdf.format(cal.getTime());
                }
            } catch (ParseException e) {
                request.setAttribute("error", "Please try again!");
                RequestDispatcher view = request.getRequestDispatcher("topKNextPlaces.jsp");
                view.forward(request, response);
                return;
            }

            String origin = (String) request.getParameter("origin");
            ArrayList<String> result = NextPlaceDAO.calculateTopNextPlaces(preTimeStart, dateTime,
                    k, postTimeEnd, origin, postTimeStart);
            String count = "0";
            if (result != null || result.size() != 0) {
                count = result.get(result.size() - 1);
                result.remove(result.size() - 1);
            }

            ArrayList<String> output = new ArrayList<String>();
            int c = 0;
            int rank = 0;

            Set<String> uniqueSet = new HashSet<String>(result);
            for (String temp : uniqueSet) {

                c = Collections.frequency(result, temp);
                output.add(temp + "," + c);

            }
            int sum = 0;
            int max = 0;
            int pos = 0;
            String maxPlace = "";
            String temp = "";
            ArrayList<String> output2 = new ArrayList<String>();
            DecimalFormat df = new DecimalFormat("#.00");
            if (output != null) {
                for (int i = 0; i < output.size(); i++) {
                    String currLine = output.get(i);
                    String arr[] = currLine.split(",");
                    sum += Integer.parseInt(arr[1]);
                }

                for (int i = 0; i < output.size(); i++) {
                    String currLine = output.get(i);
                    String arr[] = currLine.split(",");
                    max = Integer.parseInt(arr[1]);
                    maxPlace = arr[0];
                    pos = i;
                    for (int j = i + 1; j < output.size(); j++) {
                        String currLine2 = output.get(j);
                        String arr2[] = currLine2.split(",");
                        if (max < Integer.parseInt(arr2[1])) {
                            max = Integer.parseInt(arr2[1]);
                            maxPlace = arr2[0];
                            pos = j;
                        }
                    }
                    temp = output.get(pos);
                    output.set(pos, output.get(i));
                    output.set(i, temp);
//                    rank++;
//                    output2.add(rank + "," + maxPlace + "," + max + "," + df.format((double)max/Integer.parseInt(count)*100));                
                    //output.remove(pos);
                }
                int tempCheck = -1;
                for (int i = 0; i < output.size(); i++) {
                    String currLine = output.get(i);
                    String arr[] = currLine.split(",");
                    if (tempCheck == -1) {
                        rank = 1;
                    } else if (Integer.parseInt(arr[1]) != tempCheck) {
                        rank++;
                    }
                    tempCheck = Integer.parseInt(arr[1]);
                    if (rank <= k) {
                        output2.add(rank + "," + arr[0] + "," + arr[1] + "," + (int) Math.round((double) Integer.parseInt(arr[1]) / Integer.parseInt(count) * 100));
                    }
                }
            }

            request.setAttribute("output", output2);
            request.setAttribute("time", dateTime);
            request.setAttribute("k", kValue);
            request.setAttribute("ori", origin);
            request.setAttribute("count1", "" + sum);
            request.setAttribute("count2", count);
            RequestDispatcher dispatcher = request.getRequestDispatcher("topKNextPlaces.jsp");
            dispatcher.forward(request, response);

            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
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
