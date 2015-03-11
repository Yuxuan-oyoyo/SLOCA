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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.controller.ProcessAutomaticGroupIdentification;
import sloca.model.AutomaticGroupDAO;
import sloca.model.Group;
import sloca.model.LocationLookup;
import sloca.model.LocationLookupDAO;
import sloca.model.PopularPlace;
import sloca.model.SharedSecretManager;

/**
 *
 * @author g3t2
 */
public class topkGrpPopPlaces extends HttpServlet {

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

        if (errList.isEmpty()) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(dateObj);
            //set query for 14mins 59s ago
            cal.add(cal.MINUTE, -14);
            cal.add(cal.SECOND, -59);
            String dateTimeStart = sdf.format(cal.getTime());
            String dateTime = dateTimeEnd;

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
            HashMap<Integer, HashMap<String, Integer>> ranking = getRanking(count, kValue);
            JsonObject output = new JsonObject();
            output.addProperty("status", "success");
            ArrayList<PopularPlace> plist = new ArrayList<PopularPlace>();
            JsonArray arr = new JsonArray();

            Iterator<Integer> iter = ranking.keySet().iterator();
            while (iter.hasNext()) {

                int rank = iter.next();
                HashMap<String, Integer> underRanking = ranking.get(rank);
                Iterator<String> iter2 = underRanking.keySet().iterator();
                while (iter2.hasNext()) {
                    String semantic = iter2.next();

                    int count1 = underRanking.get(semantic);
                    PopularPlace pp = new PopularPlace(rank, semantic, count1);
                    plist.add(pp);

                }

            }
            Collections.sort(plist);
            for (PopularPlace pp : plist) {
                JsonObject temp = new JsonObject();
                temp.addProperty("rank", pp.getRank());
                temp.addProperty("semantic-place", pp.getSemPlace());
                temp.addProperty("count", pp.getCount());
                arr.add(temp);
            }
            output.add("results", arr);

            out.println(gson.toJson(output));

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

        }
        out.close();
    }

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
    }
}
