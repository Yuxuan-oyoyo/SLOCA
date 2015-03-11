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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sloca.model.LocationDAO;
import sloca.model.LocationLookupDAO;
import sloca.model.NextPlaceDAO;
import sloca.model.PopularPlace;
import sloca.model.SharedSecretManager;

/**
 *
 * @author ag3t2
 */
public class topknextplaces extends HttpServlet {

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
        String origin = request.getParameter("origin");
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
        //validate origin
        if (origin == null) {
            errList.add("missing origin");
        } else if (origin.isEmpty()) {
            errList.add("blank origin");
        } else {
            if (!LocationLookupDAO.checkSemanticPlace(origin)) {
                errList.add("invalid origin");
            }
        }
        if (errList.isEmpty()) {
            //preTimeStart
            String preTimeStart = getStartDate(dateTimeEnd);
            String nextTimeStart = getNextDate(dateTimeEnd);

            //postTimeEnd
            String postTimeEnd = getEndDate(nextTimeStart);
            ArrayList<String> result = NextPlaceDAO.calculateTopNextPlaces(preTimeStart, dateTimeEnd,
                    kValue, postTimeEnd, origin, nextTimeStart);
            ArrayList<String> output = new ArrayList<String>();
            int c = 0;
            int rank = 0;

            String count = "0";
            if (result != null || result.size() != 0) {
                count = result.get(result.size() - 1);
                result.remove(result.size() - 1);
            }
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
                    if (rank <= kValue) {
                        output2.add(rank + "," + arr[0] + "," + arr[1] + "," + df.format((double) Integer.parseInt(arr[1]) / Integer.parseInt(count) * 100));
                    }
                }

                int totalUsers = LocationDAO.retrieveAllUsersAtTime(preTimeStart, dateTimeEnd, origin);
                jsonOutput.addProperty("status", "success");
                int totalCount = Integer.parseInt(count);
                jsonOutput.addProperty("total-users", totalCount);
                jsonOutput.addProperty("total-next-place-users", sum);
                JsonArray arr = new JsonArray();
                ArrayList<PopularPlace> poplist = new ArrayList<PopularPlace>();

                for (String currentLine : output2) {
                    String[] data = currentLine.split(",");
                    String rank1 = data[0].trim();
                    int rankInt = Integer.parseInt(rank1);
                    String semPlace = data[1].trim();
                    String countS = data[2].trim();
                    int countInt = Integer.parseInt(countS);
                    poplist.add(new PopularPlace(rankInt, semPlace, countInt));
                }

                Collections.sort(poplist);

                for (PopularPlace pp : poplist) {

                    JsonObject toAdd = new JsonObject();
                    toAdd.addProperty("rank", pp.getRank());
                    toAdd.addProperty("semantic-place", pp.getSemPlace());
                    toAdd.addProperty("count", pp.getCount());
                    arr.add(toAdd);
                }
                jsonOutput.add("results", arr);
                out.println(gson.toJson(jsonOutput));
                return;

            } else {
                //display errors
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
     * to convert the date into required format
     *
     * @param date is the date time user enters
     * @return the next date time in correct form, otherwise return null
     */
    private String getNextDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateObj;
        try {
            dateObj = sdf.parse(date);
            Calendar cal = new GregorianCalendar();
            cal.setTime(dateObj);

            cal.add(cal.SECOND, 1);
            return sdf.format(cal.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(topkpopplaces.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * to get the start end time to process the data
     *
     * @param date is the date time user enters
     * @return the end data time
     */
    private String getEndDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateObj;
        try {
            dateObj = sdf.parse(date);
            Calendar cal = new GregorianCalendar();
            cal.setTime(dateObj);

            cal.add(cal.MINUTE, 14);
            cal.add(cal.SECOND, 59);
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
