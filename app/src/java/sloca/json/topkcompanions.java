/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.json;

import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import sloca.model.SharedSecretManager;
import sloca.model.TopCompanionsDAO;
import sloca.model.User;
import sloca.model.UserDAO;

/**
 *
 * @author admin
 */
public class topkcompanions extends HttpServlet {

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
        String macAdd = request.getParameter("mac-address");
        int kValue = 0;
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
                //check if date is within 2010 to 2014
                if (!validDate(dateObj)) {
                    errList.add("invalid date");
                }
            } catch (ParseException e) {
                errList.add("invalid date");
            }
        }
        //validatea mac add
        if (macAdd == null) {
            errList.add("missing mac address");
        } else if (macAdd.isEmpty()) {
            errList.add("blank mac address");
        } else {
            if (!validateMacadd(macAdd)) {
                errList.add("invalid mac address");
            }
        }
        if (errList.isEmpty()) {

            //get start date
            String dateTimeStart = getStartDate(dateTimeEnd);
            //get results
            ArrayList<String> results = processCompanion(kValue, dateTimeStart, dateTimeEnd, macAdd);
            //status message
            jsonOutput.addProperty("status", "success");
            //new json array to store results
            JsonArray companions = new JsonArray();
            for (String line : results) {
                String[] data = line.split(",");
                JsonObject toadd = new JsonObject();
                int rankInt = Integer.parseInt(data[0].trim());
                int timeInt = Integer.parseInt(data[3].trim());
                toadd.addProperty("rank", rankInt);
                toadd.addProperty("companion", data[2]);
                toadd.addProperty("mac-address", data[1]);
                toadd.addProperty("time-together", timeInt);

                companions.add(toadd);
            }
            jsonOutput.add("results", companions);
            out.println(gson.toJson(jsonOutput));
            return;

        } else {
            //display errormessages
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
     *
     * to validate the mac-address
     *
     * @param macAdd the mac-address to be validated
     * @return a boolean to show whether the mac-address is valid or invalid
     */
    private boolean validateMacadd(String macAdd) {
        return (!(macAdd == null || macAdd.length() == 0 || macAdd.length() != 40 || !macAdd.matches("[0-9A-Fa-f]+$")));
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

    /**
     *
     * to process the top-k companions
     *
     * @param k is the k value for the ranking
     * @param dateTimeStrat is the date time to start to process the data
     * @param mac-address is the user's mac-address
     * @return the ArrayList of the top-k companions details
     */
    private ArrayList<String> processCompanion(int k, String dateTimeStart, String dateTime, String macadd) {

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

        //Iterator<String> iter = companionTotalTime.keySet().iterator();
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
        return result;
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
