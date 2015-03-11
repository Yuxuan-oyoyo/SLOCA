/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.servlet.http.HttpSession;
import sloca.model.HeatmapDAO;
import sloca.model.SharedSecretManager;
import sloca.model.User;

/**
 *
 * @author g3t2
 */
public class heatmap extends HttpServlet {

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


        //creates a new gson object
        //by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //creats a new json object for printing the desired json output
        JsonObject jsonOutput = new JsonObject();
        //create a json array to store errors
        JsonArray errMsg = new JsonArray();
        ArrayList<String> errList = new ArrayList<String>();

        String token = request.getParameter("token");

        //display json/heatmap
        //check token request
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
        //check floor param
        String level = request.getParameter("floor");
        //check if floor is within 0 to 5
        if (level == null) {
            errList.add("missing floor");
        } else if (level.isEmpty()) {
            errList.add("blank floor");
        } else {
            try {
                int floor = Integer.parseInt(level);
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
        }
        //get date from request
        String dateTimeEnd = request.getParameter("date");
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

        //concate dateTim for query
        if (errList.size() == 0) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(dateObj);
            //set dateTime start to 14min and 59s before queried time
            cal.add(cal.MINUTE, -14);
            cal.add(cal.SECOND, -59);
            String dateTimeStart = sdf.format(cal.getTime());
            //get results from DAO
            HashMap<String, Integer> heatmapResults = HeatmapDAO.retrieveHeatmap(dateTimeStart, dateTimeEnd, level);
            TreeMap<String, Integer> treeMap2 = new TreeMap<String, Integer>();
            Iterator<String> ite2 = heatmapResults.keySet().iterator();
            while (ite2.hasNext()) {
                String semantic = ite2.next();
                int num = heatmapResults.get(semantic);
                treeMap2.put(semantic, num);
            }
            //retreive success
            jsonOutput.addProperty("status", "success");
            //creates the jsonarray to be printed later
            JsonArray heatMapToPrint = new JsonArray();
            Iterator<String> ite = treeMap2.keySet().iterator();
            //iterate through the hashmap
            while (ite.hasNext()) {
                String semanticplace = ite.next();
                int numPeople = treeMap2.get(semanticplace);
                //store attributes into a jsonobject
                JsonObject tempObject = new JsonObject();
                tempObject.addProperty("semantic-place", semanticplace);
                tempObject.addProperty("num-people", numPeople);
                tempObject.addProperty("crowd-density", getDensity(numPeople));
                heatMapToPrint.add(tempObject);
            }
            //add the json array
            jsonOutput.add("heatmap", heatMapToPrint);
            //print with pretty prints!
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
     * to get the density number
     *
     * @param num is the number of people
     * @return the density number
     */
    private int getDensity(int num) {
        if (num == 0) {
            return 0;
        } else if (num <= 2) {
            return 1;
        } else if (num <= 5) {
            return 2;
        } else if (num <= 10) {
            return 3;
        } else if (num <= 20) {
            return 4;
        } else if (num <= 30) {
            return 5;
        } else if (num > 30) {
            return 6;
        }
        return 0;
    }

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
