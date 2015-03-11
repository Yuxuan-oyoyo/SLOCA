/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.AutomaticGroupDAO;
import sloca.model.Group;
import sloca.model.LocationLookup;
import sloca.model.LocationLookupDAO;

/**
 *
 * @author g3t2
 */
public class GroupProcessPopularPlaces extends HttpServlet {

    /**
     *
     * to validate the date
     *
     * @param dateObj the Date object to be validated
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

        // get the time interval
        String stringRank = request.getParameter("kValue");
        int rank = Integer.parseInt(stringRank);
        String dateTime = request.getParameter("dateTime");
        dateTime = dateTime.replace('T', ' ');
        String dateTimeStart = "";
        try {
            //catch corner case for date where seconds will be ignored if time it is :00
            try {
                char c = dateTime.charAt(17);
            } catch (IndexOutOfBoundsException iobe) {
                dateTime += ":00";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateObj = sdf.parse(dateTime);

            if (dateObj != null) {
                if (!validDate(dateObj)) {
                    request.setAttribute("error", "Please try again! Date should be within 2010 and 2014");
                    RequestDispatcher view = request.getRequestDispatcher("groupPop.jsp");
                    view.forward(request, response);
                    return;
                }
                Calendar cal = new GregorianCalendar();
                cal.setTime(dateObj);
                //set query for 14mins 59s ago
                cal.add(cal.MINUTE, -14);
                cal.add(cal.SECOND, -59);
                dateTimeStart = sdf.format(cal.getTime());
            }
        } catch (ParseException e) {
            request.setAttribute("timeErr", "time error,Please try again!");
            RequestDispatcher view = request.getRequestDispatcher("groupPop.jsp");
            view.forward(request, response);
        }

        ArrayList<String> dataArr = AutomaticGroupDAO.retrieveData(dateTimeStart, dateTime);

        ProcessAutomaticGroupIdentification pagi = new ProcessAutomaticGroupIdentification();
        ArrayList<Group> groups = pagi.getGroups(dataArr, dateTime);

        List<LocationLookup> locationLookupArr = LocationLookupDAO.retrieveAll();
        HashMap<String, String> locationLookup = new HashMap<String, String>();
        for (LocationLookup loclookup : locationLookupArr) {
            locationLookup.put(loclookup.getLocationId(), loclookup.getSemanticPlace());
        }
        //logic stats below

        HashMap<String, Integer> count = getSemanticPlaceCount(locationLookup, groups);
        HashMap<Integer, HashMap<String, Integer>> ranking = getRanking(count, rank);

        Iterator<Integer> iter = ranking.keySet().iterator();

        request.setAttribute("ranking", ranking);
        request.setAttribute("rank", stringRank);
        request.setAttribute("dateTime", dateTime);
        RequestDispatcher dispatcher = request.getRequestDispatcher("groupPop.jsp");
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

    /**
     * get the count of semantic place
     *
     * @param locationLookup the HashMap which key is location ID, value is
     * semantic place
     * @param groups is the ArrayList of Group object
     * @return a HashMap, which key is the semantic place and value is the count
     */
    private HashMap<String, Integer> getSemanticPlaceCount(HashMap<String, String> locationLookup, ArrayList<Group> groups) {
        HashMap<String, Integer> semanticPlaceCount = new HashMap<String, Integer>();

        for (Group group : groups) {
            String lastLoc = group.getLastLocation();
            String lastSeman = locationLookup.get(lastLoc);
            if (!semanticPlaceCount.containsKey(lastSeman)) {
                semanticPlaceCount.put(lastSeman, 1);
            } else {
                int existingNum = semanticPlaceCount.get(lastSeman);
                semanticPlaceCount.put(lastSeman, (existingNum + 1));
            }
        }

        return semanticPlaceCount;
    }

    /**
     * get rank for each semantic place
     *
     * @param count the HashMap of the semantic place and it counts
     * @param rank the rank value is required
     * @return a HashMap, which key is the rank value, and value is the HashMap
     * with a semantic place and its count
     */
    private HashMap<Integer, HashMap<String, Integer>> getRanking(HashMap<String, Integer> count, int rank) {
        HashMap<Integer, HashMap<String, Integer>> ranking = new HashMap<Integer, HashMap<String, Integer>>();
        for (int i = 1; i <= rank; i++) {
            HashMap<String, Integer> underRanking = new HashMap<String, Integer>();
            Iterator<String> iter = count.keySet().iterator();
            int maxCount = 0;
            while (iter.hasNext()) {
                String semantic = iter.next();
                int groupCount = count.get(semantic);
                if (groupCount > maxCount) {
                    maxCount = groupCount;
                }
            }
            Iterator<String> iter2 = count.keySet().iterator();
            ArrayList<String> remove = new ArrayList<String>();
            while (iter2.hasNext()) {
                String semantic = iter2.next();
                int groupCount = count.get(semantic);
                if (groupCount == maxCount) {
                    underRanking.put(semantic, maxCount);
                    remove.add(semantic);
                }
            }
            for (String rmv : remove) {
                count.remove(rmv);
            }
            ranking.put(i, underRanking);
        }
        return ranking;
    }

}
