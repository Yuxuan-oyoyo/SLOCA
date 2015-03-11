/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.json;

import sloca.model.PopularPlacesDAO;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.PopularPlace;
import sloca.model.SharedSecretManager;

/**
 *
 * @author g3t2
 */
public class topkpopplaces extends HttpServlet {

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

        String k = request.getParameter("k");
        String dateTimeEnd = request.getParameter("date");
        String token = request.getParameter("token");
        int kValue = 0;
        //validate token
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
        //validate k 
        if (k == null || k.isEmpty()) {
            kValue = 3;
        } else {
            try {
                kValue = Integer.parseInt(k);
                if (kValue < 1 || kValue > 10) {
                    errList.add("invalid k");
                }
            } catch (NumberFormatException nfe) {
                errList.add("invalid k");
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
                if (!validDate(dateObj)) {
                    errList.add("invalid date");
                }
            } catch (ParseException e) {
                errList.add("invalid date");
            }

        }
        if (errList.isEmpty()) {
            //calc end time
            String dateTimeStart = getStartDate(dateTimeEnd);
            //get results
            ArrayList<String> result = PopularPlacesDAO.calculatePopularRanking(dateTimeStart, dateTimeEnd, kValue);
            ArrayList<PopularPlace> poplist = new ArrayList<PopularPlace>();

            for (String currentLine : result) {
                String[] data = currentLine.split(",");
                String rank = data[0].trim();
                int rankInt = Integer.parseInt(rank);
                String semPlace = data[1].trim();
                String count = data[2].trim();
                int countInt = Integer.parseInt(count);
                poplist.add(new PopularPlace(rankInt, semPlace, countInt));
            }

            Collections.sort(poplist);
            //print json results
            jsonOutput.addProperty("status", "success");
            JsonArray results = new JsonArray();
            for (PopularPlace pp : poplist) {
                //rank,semantic place, counter

                JsonObject toAdd = new JsonObject();
                toAdd.addProperty("rank", pp.getRank());
                toAdd.addProperty("semantic-place", pp.getSemPlace());
                toAdd.addProperty("count", pp.getCount());
                results.add(toAdd);
            }
            jsonOutput.add("results", results);
            out.println(gson.toJson(jsonOutput));

        } else {
            jsonOutput.addProperty("status", "error");
            //sorting the errlist into alphabetical order
            Collections.sort(errList);
            //loop through the errors to add into the json error array
            for (String err : errList) {
                errMsg.add(new JsonPrimitive(err));
            }

            jsonOutput.add("messages", errMsg);

            out.println(gson.toJson(jsonOutput));
            return;
        }

        out.close();

    }

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
     *
     * to get the start date to process the data
     *
     * @param date the date time the user enters
     * @return a String that is the start date time to process the data
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
