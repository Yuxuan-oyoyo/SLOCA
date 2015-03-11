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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.AutomaticGroupDAO;
import sloca.model.Group;
import sloca.model.SharedSecretManager;

/**
 *
 * @author g3t2
 */
/**
 *
 * to do JSON automatic group detection
 */
public class autogroupdetect extends HttpServlet {

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

        String dateTimeEnd = request.getParameter("date");
        String token = request.getParameter("token");

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

        //validate date and tiem
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

            String dateTimeStart = getStartDate(dateTimeEnd);
            //gets all users who are within the 15min time window in the form
            //of timestamp, macadd, locid, email
            ArrayList<String> dataArr = AutomaticGroupDAO.retrieveData(dateTimeStart, dateTimeEnd);
            //returns a hashmap of all macaddress and emails from the 15min query
            HashMap<String, String> macEmail = getMacEmail(dataArr);
            //get the groups that are within the 15min query
            ArrayList<Group> groups = getGroups(dataArr, dateTimeEnd);

            JsonObject output = new JsonObject();
            output.addProperty("status", "success");
            //get total users
            output.addProperty("total-users", macEmail.size());
            //get total groups
            output.addProperty("total-groups", groups.size());

            //get the breakdown of the groups
            JsonArray breakdown = new JsonArray();
            Collections.sort(groups);
            for (Group g : groups) {
                JsonObject group = new JsonObject();
                group.addProperty("size", g.getUsers().size());
                group.addProperty("total-time-spent", g.computeTotalTime());
                //new json array for members
                JsonArray memberArr = new JsonArray();
                ArrayList<String> macAddInGrp = g.getUsers();
                HashMap<String, String> macAddEmail = getUserMacAddEmail(macEmail, macAddInGrp);
                //ArrayList<String> emailList = new ArrayList<String>(macAddEmail.values());
                //Collections.sort(emailList);
//                for (String member : emailList) {
//                    JsonObject memberJSON = new JsonObject();
//                    String memberToChange = member;
//                    if (member == null || member.equalsIgnoreCase("null")) {
//                        memberToChange = "";
//                    }
//                    memberJSON.addProperty("email", memberToChange);
//                    memberJSON.addProperty("mac-address", getMacAdd(member, macAddEmail));
//                    memberArr.add(memberJSON);
//                }
                TreeMap<String, String> sortedMap = new TreeMap<String, String>();
                sortedMap.putAll(macAddEmail);
                Iterator<String> ite = sortedMap.keySet().iterator();
                while (ite.hasNext()) {
                    JsonObject memberJson = new JsonObject();
                    String macAdd = ite.next();
                    String emailToCheck = sortedMap.get(macAdd);
                    if (emailToCheck == null || emailToCheck.equalsIgnoreCase("null")) {
                        emailToCheck = "";
                    }
                    memberJson.addProperty("email", emailToCheck);
                    memberJson.addProperty("mac-address", macAdd);
                    memberArr.add(memberJson);
                }

                group.add("members", memberArr);
                //get location
                HashMap<String, ArrayList<Timestamp>> locationList = g.getLocations();
                TreeMap<String, ArrayList<Timestamp>> sortedList = new TreeMap<String, ArrayList<Timestamp>>();
                sortedList.putAll(locationList);

                JsonArray locArr = new JsonArray();
                for (String key : sortedList.keySet()) {
                    JsonObject loc = new JsonObject();
                    loc.addProperty("location", key);

                    ArrayList<Timestamp> timeline = sortedList.get(key);
                    int timeSpent = 0;
                    for (int i = 0; i < timeline.size(); i += 2) {
                        Timestamp ts1 = timeline.get(i);
                        Timestamp ts2 = timeline.get(i + 1);
                        timeSpent += (int) ((ts2.getTime() - ts1.getTime()) / 1000);

                    }
                    loc.addProperty("time-spent", timeSpent);
                    locArr.add(loc);
                }
                group.add("locations", locArr);
                breakdown.add(group);

            }
            output.add("groups", breakdown);
            out.println(gson.toJson(output));
            return;

        } else {
            //display error msg
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
     * to retrieve the mac-address with an email
     *
     * @param email the email to compared
     * @param macAddEmail the HashMap contains the email and mac-address
     * @return mac-address, if the email does not match, return null
     */
    private String getMacAdd(String email, HashMap<String, String> macAddEmail) {
        Iterator<String> ite = macAddEmail.keySet().iterator();
        while (ite.hasNext()) {
            String macAdd = ite.next();
            String hsEmail = macAddEmail.get(macAdd);
            if (hsEmail != null && hsEmail.equals(email)) {
                return macAdd;

            }
        }
        return null;

    }

    /**
     *
     * to get the user's mac-address and email
     *
     * @param macEmailAll the Hashmap contains all the mac-address and email
     * @param madAddGrp the mac-address within the group
     * @return a HashMap, which key is mac-address and value is email
     */
    private HashMap<String, String> getUserMacAddEmail(HashMap<String, String> macEmailAll, ArrayList<String> macAddGrp) {
        //key= macadd, value = email
        HashMap<String, String> toReturn = new HashMap<String, String>();
        for (String macAdd : macAddGrp) {
            String email = macEmailAll.get(macAdd);
            toReturn.put(macAdd, email);
        }
        return toReturn;
    }

    /**
     *
     * to validate the date
     *
     * @param dateObj the Date object array to be validated
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

    /**
     *
     * compare the time line of two users
     *
     * @param timeMapA the user A's time line
     * @param timeMapB the user B's time line
     * @return a HashMap, the key is location ID, value is the arraylist of
     * timestamp if the two user spent more than 12 minutes in the same
     * location, otherwise return null
     */
    //check between 2 people if they shareed more than 12 mins in the same locaiton
    private HashMap<String, ArrayList<Timestamp>> compareTimelines(HashMap<String, ArrayList<Timestamp>> timeMapA, HashMap<String, ArrayList<Timestamp>> timeMapB) {
        //get the iterator to go through the first timeMap of checkin and checkouts per location
        Iterator<String> iter = timeMapA.keySet().iterator();
        //create a new hashmap of key = locationid and value = arraylists of timestamps obj
        HashMap<String, ArrayList<Timestamp>> timeLineAB = new HashMap<String, ArrayList<Timestamp>>();
        int totalTime = 0;
        while (iter.hasNext()) {
            //get the first location in the iteration
            String locationA = iter.next();
            //get the arraylist of timestamps obj
            ArrayList<Timestamp> locTimeA = timeMapA.get(locationA);
            //if the second timeMap contains the current location
            if (timeMapB.containsKey(locationA)) {
                //add equal if statement
                //get the arraylist of timestamps for the current location
                ArrayList<Timestamp> locTimeB = timeMapB.get(locationA);
                //loop through the first arraylist of timestmaps
                //note the i = i+2
                //as the arraylist is stored in the form of startTime1,endTime1,startTime2,endTime2...
                for (int i = 0; i < locTimeA.size(); i = i + 2) {
                    //results come in pairs
                    //starting timestamp A1 aka "check in"
                    Timestamp A1 = locTimeA.get(i);
                    //check out time stamp
                    Timestamp A2 = locTimeA.get(i + 1);
                    //similarly loop through the second arraylist and get the pairs of check in and check out
                    for (int p = 0; p < locTimeB.size(); p = p + 2) {
                        Timestamp B1 = locTimeB.get(p);
                        Timestamp B2 = locTimeB.get(p + 1);

                        //if  EITHER person A arrives before Person B OR person A arrives the same time as person B IS TRUE
                        // AND
                        // if EITHER person A leaves after B OR if both of them leave to tgt IS TRUE
                        if ((A1.before(B1) || A1.equals(B1)) && (A2.after(B2) || A2.equals(B2))) {
                            //cehck if the hashmap to return has already stored a location 
                            if (timeLineAB.containsKey(locationA)) {
                                //get the arralist of timestamps from the hashmap
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                //add the Person B's check in and check out time
                                //NOTE: we are using B's time as B has the shorter time spent in the room in this case
                                locTimeAB.add(B1);
                                locTimeAB.add(B2);
                                //update the hashmap with the new arraylist
                                timeLineAB.put(locationA, locTimeAB);
                                //calculate the time difference
                                int timeGap = (int) (B2.getTime() - B1.getTime()) / 1000;
                                //add the time that B spends in the room to the total time
                                totalTime += timeGap;
                            } else {
                                // if the hashmap to return does not contain the current location
                                //create a new arraylist of timestamps
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                //add B's check in and check out time
                                locTimeAB.add(B1);
                                locTimeAB.add(B2);
                                //update the hashmap with the location and arraylist
                                timeLineAB.put(locationA, locTimeAB);
                                //calculate the time B spends in the current location and add it to the total time
                                int timeGap = (int) (B2.getTime() - B1.getTime()) / 1000;
                                totalTime += timeGap;
                            }

                            //IF EITHER B arrives before A OR B and A arrive tgt
                            //AND 
                            //IF EITHER B leaves after A or they both leave tgt
                        } else if ((B1.before(A1) || B1.equals(A1)) && (B2.after(A2) || B2.equals(A2))) {
                            //if hashmap contains current location, ie, they spent time tgt in the same location before
                            if (timeLineAB.containsKey(locationA)) {
                                // get the prev stored arraylist from the hashmap
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                //NOTE: we are using A cause between A and B, in this case, A spends less or equal amounts of time with B
                                //add the check in and check out times of A to the arraylust
                                locTimeAB.add(A1);
                                locTimeAB.add(A2);
                                //update the hashmap with the new arraylist
                                timeLineAB.put(locationA, locTimeAB);
                                //get the time A spent in the location
                                int timeGap = (int) (A2.getTime() - A1.getTime()) / 1000;
                                //add to total time
                                totalTime += timeGap;
                            } else {
                                //else if hashmap doesnt contain the current location
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                //Add A's check in and check out times, update hashmap and update total time
                                locTimeAB.add(A1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                                int timeGap = (int) (A2.getTime() - A1.getTime()) / 1000;
                                totalTime += timeGap;
                            }

                            //IF EITHER A arrives before B OR they arrive tgt
                            //AND 
                            // A leaves after B arrives
                            //TAKE: B's arrival and A's Departure
                        } else if ((A1.before(B1) || A1.equals(B1)) && A2.after(B1)) {
                            if (timeLineAB.containsKey(locationA)) {
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                locTimeAB.add(B1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                                int timeGap = (int) (A2.getTime() - B1.getTime()) / 1000;
                                totalTime += timeGap;
                            } else {
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                locTimeAB.add(B1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                                int timeGap = (int) (A2.getTime() - B1.getTime()) / 1000;
                                totalTime += timeGap;
                            }
                            //IF EITHER A arrives after B OR they arrive tgt
                            // AND 
                            //A arrives before B leaves
                            //TAKE A's arrival and B's departure
                        } else if ((A1.after(B1) || A1.equals(B1)) && A1.before(B2)) {
                            if (timeLineAB.containsKey(locationA)) {
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                locTimeAB.add(A1);
                                locTimeAB.add(B2);
                                timeLineAB.put(locationA, locTimeAB);
                                int timeGap = (int) (B2.getTime() - A1.getTime()) / 1000;
                                totalTime += timeGap;
                            } else {
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                locTimeAB.add(A1);
                                locTimeAB.add(B2);
                                timeLineAB.put(locationA, locTimeAB);
                                int timeGap = (int) (B2.getTime() - A1.getTime()) / 1000;
                                totalTime += timeGap;
                            }
                        }
                    }
                }
            }
        }
        //if total time more than 12 mins 
        if (totalTime >= 720) {
            return timeLineAB;
        } else {
            return null;
        }
    }

    /**
     *
     * to compare the time line of a group
     *
     * @param g the group to be compared with
     * @param timeMapAB the group's timeStamp
     * @param macB is the mac-address of the user
     * @return a boolean value to check whether the user spent more than or
     * equals to 12 minutes with the group
     */
    private boolean compareTimeLineWithGroup(Group g, HashMap<String, ArrayList<Timestamp>> timeMapAB, String macB) {
        //get the share check in and check out times for each location from the group
        HashMap<String, ArrayList<Timestamp>> groupTimeMap = g.getTimeLine();
        //create a new hashmap to location ids and time stamp to return
        HashMap<String, ArrayList<Timestamp>> timeLineABC = new HashMap<String, ArrayList<Timestamp>>();
        int totalTime = 0;
        //get the group time map and iterate
        Iterator<String> iter = groupTimeMap.keySet().iterator();
        while (iter.hasNext()) {
            //get the group location
            String locationGroup = iter.next();
            //get the group timestamps
            ArrayList<Timestamp> locTimeGroup = groupTimeMap.get(locationGroup);
            //if the initial group hashmap of locationids and timestamps contains the locationid of the group
            if (timeMapAB.containsKey(locationGroup)) {
                //retireve arraylist of timestamps from A and B's hashmap
                ArrayList<Timestamp> locTimeAB = timeMapAB.get(locationGroup);
                for (int i = 0; i < locTimeGroup.size(); i = i + 2) {
                    //get grp start and end time
                    Timestamp G1 = locTimeGroup.get(i);
                    Timestamp G2 = locTimeGroup.get(i + 1);

                    for (int p = 0; p < locTimeAB.size(); p = p + 2) {
                        //get A and B's start and end time
                        Timestamp AB1 = locTimeAB.get(p);
                        Timestamp AB2 = locTimeAB.get(p + 1);

                        //IF EITHER Group arrives before A&B arives OR they both arrive tgt
                        //AND
                        //IF EITHER Group leaves after A&B leaves OR they both leave tgt
                        if ((G1.before(AB1) || G1.equals(AB1)) && (G2.after(AB2) || G2.equals(AB2))) {
                            if (timeLineABC.containsKey(locationGroup)) {
                                ArrayList<Timestamp> locTimeABC = timeLineABC.get(locationGroup);
                                locTimeABC.add(AB1);
                                locTimeABC.add(AB2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (AB2.getTime() - AB1.getTime()) / 1000;
                                totalTime += timeGap;
                            } else {
                                ArrayList<Timestamp> locTimeABC = new ArrayList<Timestamp>();
                                locTimeABC.add(AB1);
                                locTimeABC.add(AB2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (AB2.getTime() - AB1.getTime()) / 1000;
                                totalTime += timeGap;
                            }
                            //IF EITHER Group arrives after A&B arives OR they both arrive tgt
                            //AND
                            //IF EITHER Group leaves before A&B leaves OR they both leave tgt
                        } else if ((AB1.before(G1) || AB1.equals(G1)) && (AB2.after(G2) || AB2.equals(G2))) {
                            if (timeLineABC.containsKey(locationGroup)) {
                                ArrayList<Timestamp> locTimeABC = timeLineABC.get(locationGroup);
                                locTimeABC.add(G1);
                                locTimeABC.add(G2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (G2.getTime() - G1.getTime()) / 1000;
                                totalTime += timeGap;
                            } else {
                                ArrayList<Timestamp> locTimeABC = new ArrayList<Timestamp>();
                                locTimeABC.add(G1);
                                locTimeABC.add(G2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (G2.getTime() - G1.getTime()) / 1000;
                                totalTime += timeGap;
                            }
                            //IF EITHER Group arrives before AB OR they arrive tgt
                            //AND
                            //Group leaves after AB arrives
                        } else if ((G1.before(AB1) || G1.equals(AB1)) && G2.after(AB1)) {
                            if (timeLineABC.containsKey(locationGroup)) {
                                ArrayList<Timestamp> locTimeABC = timeLineABC.get(locationGroup);
                                locTimeABC.add(AB1);
                                locTimeABC.add(G2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (G2.getTime() - AB1.getTime()) / 1000;
                                totalTime += timeGap;
                            } else {
                                ArrayList<Timestamp> locTimeABC = new ArrayList<Timestamp>();
                                locTimeABC.add(AB1);
                                locTimeABC.add(G2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (G2.getTime() - AB1.getTime()) / 1000;
                                totalTime += timeGap;
                            }
                            //IF EITHER Group arrives after AB OR they arrive tgt
                            //AND
                            //IF group arrives before AB leaves
                        } else if ((G1.after(AB1) || G1.equals(AB1)) && G1.before(AB2)) {
                            if (timeLineABC.containsKey(locationGroup)) {
                                ArrayList<Timestamp> locTimeABC = timeLineABC.get(locationGroup);
                                locTimeABC.add(G1);
                                locTimeABC.add(AB2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (AB2.getTime() - G1.getTime()) / 1000;
                                totalTime += timeGap;
                            } else {
                                ArrayList<Timestamp> locTimeABC = new ArrayList<Timestamp>();
                                locTimeABC.add(G1);
                                locTimeABC.add(AB2);
                                timeLineABC.put(locationGroup, locTimeABC);
                                int timeGap = (int) (AB2.getTime() - G1.getTime()) / 1000;
                                totalTime += timeGap;
                            }
                        }
                    }
                }
            }
        }
        //Calc if they spend >=12mins tgt
        if (totalTime >= 720) {
            //add new member to the group
            g.addUser(macB);
            //update timeline in group object
            g.setTimeLine(timeLineABC);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * to convert the String into timeStamp
     *
     * @param dataArr the String need to be converted
     * @return a HashMap, which key is mac-address, value is an String Arraylist
     * of location ID and timeStamp
     */
    private HashMap<String, ArrayList<String>> getStringTimeLines(ArrayList<String> dataArr) {
        //create a new hashmap where key = macaddress and value = arraylist of locationid and timestamp strings
        HashMap<String, ArrayList<String>> stringTimeLines = new HashMap<String, ArrayList<String>>();
        for (String data : dataArr) {
            String[] arr = data.split(",");
            //get macaddress
            String macAddress = arr[1];
            //if hashmap does not contain macaddress
            if (!stringTimeLines.containsKey(macAddress)) {
                //create a new arraylist of strings
                ArrayList<String> timeLine = new ArrayList<String>();
                //concat locationid and datetime tgt to form a string
                String SingleTimeLine = arr[2] + "," + arr[0];
                //add this string into the arraylist
                timeLine.add(SingleTimeLine);
                //set the macadd as key and store the arraylist as value
                stringTimeLines.put(macAddress, timeLine);
            } else {
                //if macaddress is already is in the hashmap, means, user's data has been stored
                ArrayList<String> timeLine = stringTimeLines.get(macAddress);
                //concat locationid and date time tgt to form a string
                String SingleTimeLine = arr[2] + "," + arr[0];
                //add this string to the current arraylist
                timeLine.add(SingleTimeLine);
                //update hashmap with the updated arraylist
                stringTimeLines.put(macAddress, timeLine);
            }
        }
        //return the hashmap
        return stringTimeLines;
    }

    /**
     *
     * to get the mac-address and email
     *
     * @param daraArr the String need to be split
     * @return a HashMap, which key is mac-address, value is email
     */
    private HashMap<String, String> getMacEmail(ArrayList<String> dataArr) {
        //create new hashmap where key = macadd and value = email
        HashMap<String, String> macEmail = new HashMap<String, String>();

        for (String data : dataArr) {
            String[] arr = data.split(",");
            String macAddress = arr[1];
            String email = arr[3];
            //if hashmap's key does not have macaddress
            if (!macEmail.containsKey(macAddress)) {
                //if the email retrieved from the data is null
                if (email == null || email.equals("NULL")) {
                    //store empty string as value
                    macEmail.put(macAddress, "");
                } else {
                    //store user's email as value
                    macEmail.put(macAddress, email);
                }
            }
        }
        //return a hashmap with macdress and the corressponding email
        return macEmail;
    }

    /**
     *
     * to get the time lines
     *
     * @param stringTimeLines HashMap which key is mac-address and value is an
     * String ArrayList contains the location ID and timeStamp
     * @param dateTimeEnd is the time to stop processing data
     * @return a HashMap, which key is mac-address, value is an ArrayList of
     * timeStamp object
     */
    private HashMap<String, HashMap<String, ArrayList<Timestamp>>> getTimeLines(HashMap<String, ArrayList<String>> stringTimeLines, String dateTimeEnd) {
        //creates a hashmap of where the key = macaddress and values consists of an arraylist of timestamp objects
        HashMap<String, HashMap<String, ArrayList<Timestamp>>> timeLines = new HashMap<String, HashMap<String, ArrayList<Timestamp>>>();
        //Get the string iterator to go through the hashmap passed in through the parameter
        //hashmap in paramter has key = macadd and value = arraylist of strings , each string contains "locationid, timestamp"
        Iterator<String> iter = stringTimeLines.keySet().iterator();

        while (iter.hasNext()) {
            //get the macadd
            String macAddress = iter.next();
            //get the arraylist of "locationid,timestamp"
            ArrayList<String> stringTimeLine = stringTimeLines.get(macAddress);
            //create a new hashmap of strings and arraylists of timestamp
            HashMap<String, ArrayList<Timestamp>> timeMap = new HashMap<String, ArrayList<Timestamp>>();
            //string for currentline
            String lineCurrent;
            //array to store currentline after ".split()"
            String[] arrCurrent;
            //string for nextLine and immediately assigned
            String lineNext = stringTimeLine.get(0);
            //array to store the string separated by ","
            String[] arrNext;

            String timeString1;
            String timeString2;
            Timestamp timeStamp1 = null;
            Timestamp timeStamp2 = null;

            int timeGap = 0;

            for (int i = 0; i < stringTimeLine.size(); i++) {
                //initialise current line to the next to start the for loop
                //as the line next line was declared on top and is actually the starting line
                lineCurrent = lineNext;
                //split the currentline
                arrCurrent = lineCurrent.split(",");
                //store the location
                String location = arrCurrent[0];
                //store the datetime
                timeString1 = arrCurrent[1];

                //if the loop has not reached the last value
                if (i != (stringTimeLine.size() - 1)) {
                    //assign the nextline
                    lineNext = stringTimeLine.get(i + 1);
                    //split the line into the array
                    arrNext = lineNext.split(",");

                    //store the date time of the next line
                    timeString2 = arrNext[1];
                } else {
                    //if it has reached the end, then timestamp has reached the max query time
                    timeString2 = dateTimeEnd;
                }

                try {
                    //create new simpledataformat
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (timeString1.indexOf("T") != -1) {
                        timeString1 = timeString1.replace('T', ' ');
                    }
                    if (timeString2.indexOf("T") != -1) {
                        timeString2 = timeString2.replace('T', ' ');
                    }

                    //parse currentline timestamp into a date obj
                    Date parsedDate1 = dateFormat.parse(timeString1.substring(0, 19));
                    //initialise currentline date into a timestamp obj
                    timeStamp1 = new java.sql.Timestamp(parsedDate1.getTime());
                    //parse nextline timestamp into a date obj
                    Date parsedDate2 = dateFormat.parse(timeString2.substring(0, 19));
                    //initialise nextline date into a timestamp obj
                    timeStamp2 = new java.sql.Timestamp(parsedDate2.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //time gap:difference in time
                //nextline timestamp minus currentline timestamp
                //divide by 1000 to convert milliseconds into seconds
                timeGap = (int) (timeStamp2.getTime() - timeStamp1.getTime()) / 1000;

                //if timeMap hashmap does not contain the location of the currentline
                if (!timeMap.containsKey(location)) {
                    //create a new arraylist of timestamp obj
                    ArrayList<Timestamp> timeLine = new ArrayList<Timestamp>();
                    //add the currentline timestamp into arraylist
                    timeLine.add(timeStamp1);
                    //if time gap less than 9mins
                    if (timeGap <= 540) {
                        //add the second timestamp
                        timeLine.add(timeStamp2);
                    } else {
                        //or else if time gap is more than 9mins, it is assummed that the user spends 9mins at the same location
                        //therefore, +54000 becasue its 9mins in milliseconds
                        Timestamp timeStamp3 = new Timestamp(timeStamp1.getTime() + 540000);
                        //add it into the arraylist
                        timeLine.add(timeStamp3);
                    }
                    //put the amount of time spent per location into the timeMap hashmap
                    timeMap.put(location, timeLine);
                } else {
                    //if the hashmap alr has a past record the user in the same location
                    //get the previously stored arraylist of timestamp from the timeMap hashmap
                    ArrayList<Timestamp> timeLine = timeMap.get(location);
                    //add the current line timestamp to the arraylist
                    timeLine.add(timeStamp1);
                    if (timeGap <= 540) {
                        //if the user spends less than 9mins in a location, add the second timestamp
                        timeLine.add(timeStamp2);
                    } else {
                        //else, it is assummed user spends 9mins in the same location
                        Timestamp timeStamp3 = new Timestamp(timeStamp1.getTime() + 540000);
                        //add the second timestamp into the arraylist
                        timeLine.add(timeStamp3);
                    }
                    //update the hashmap with the new values in the arraylist
                    timeMap.put(location, timeLine);
                }
            }
            //put the timeMap, a hashmap of "checkin" and "checkout" times that the use spends at a particular location
            timeLines.put(macAddress, timeMap);
        }
        return timeLines;
    }

    /**
     *
     * to get the groups
     *
     * @param daraArr the String need to be converted into time line
     * @param dateTime is the time user enters
     * @return a ArrayList of Group object
     */
    private ArrayList<Group> getGroups(ArrayList<String> dataArr, String dateTime) {
        //create new arraylist to store group objects
        ArrayList<Group> groups = new ArrayList<Group>();
        //gets a hashmap that where key = macaddress and  value = arraylist of "location,timestamps" of users within the 15min query
        //note: in the arraylist, each string value is stored as "location,timestamp"
        HashMap<String, ArrayList<String>> stringTimeLines = getStringTimeLines(dataArr);

        //gets a hashmap where key = macadd and value = hashmap of string(locationid) and arraylist of timestamp objs
        //arraylist of timestamp objects contains the check in and check out times a user spends a certain location within the 
        //15 min query
        HashMap<String, HashMap<String, ArrayList<Timestamp>>> timeLines = getTimeLines(stringTimeLines, dateTime);

        //get an interator to run through the hashmap       
        Iterator<String> iter = timeLines.keySet().iterator();
        while (iter.hasNext()) {
            //store the current macadd
            String macA = iter.next();
            //store the current hashmap of user check in and check outs per location
            HashMap<String, ArrayList<Timestamp>> timeLineA = timeLines.get(macA);

            //get another iterator to run through the same hashmap
            //this is double looping of the same hashmap
            //running the first value of the hashmap with the rest of the values of the hashmap..
            Iterator<String> iter2 = timeLines.keySet().iterator();
            while (iter2.hasNext()) {
                //store the macadd of the inner loop
                String macB = iter2.next();
                //if the macaddress arent equal to each other
                if (!macB.equals(macA)) {
                    //get the hashmap of check in and outs per location for the innter loop
                    HashMap<String, ArrayList<Timestamp>> timeLineB = timeLines.get(macB);

                    //get a hashmap where key = locationid and value = arraylist of timestamps
                    //returns null if both A and B do not spend 12mins or more at the same location
                    HashMap<String, ArrayList<Timestamp>> timeMapAB = compareTimelines(timeLineA, timeLineB);
                    //if A and B spent more than 12mins
                    if (timeMapAB != null) {

                        boolean sameGroup = false;

                        //will not loop through the first run...
                        for (Group g : groups) {

                            //check if macA is the same as the first entry in the arraylist
                            if (g.leadByUser(macA)) {
                                //get the shared timeline between the groups
                                HashMap<String, ArrayList<Timestamp>> groupTimeLine = g.getTimeLine();

                                if (compareTimeLineWithGroup(g, timeMapAB, macB)) {

                                    sameGroup = true;
                                }
                            }
                        }
                        //for first run especially, since boolean is false
                        if (!sameGroup) {
                            //create a new arraylist of users
                            ArrayList<String> users = new ArrayList<String>();
                            //store macadd of A and B in the arraylist 
                            users.add(macA);
                            users.add(macB);
                            //create a new group 
                            Group newGroup = new Group(users, timeMapAB);
                            //add group to arraylist
                            groups.add(newGroup);
                        }
                    }
                }
            }
        }

        int totalSize = groups.size();
        //check for any subgroups present and remove
        HashMap<Integer, String> gonnaRemove = new HashMap<Integer, String>();
        for (int temp = totalSize - 1; temp >= 0; temp--) {
            Group lastGroup = groups.get(temp);
            for (int temp2 = totalSize - 1; temp2 >= 0; temp2--) {
                if (temp != temp2) {
                    Group testGroup = groups.get(temp2);
                    if (lastGroup.subGroup(testGroup)) {
                        gonnaRemove.put(temp, "");
                        break;
                    }
                }
            }

            for (int temp3 = temp - 1; temp3 >= 0; temp3--) {
                Group testGroup = groups.get(temp3);
                if (lastGroup.sameGroup(testGroup)) {
                    gonnaRemove.put(temp, "");
                    break;
                }
            }
        }

        ArrayList<Integer> remove = new ArrayList<Integer>();
        Iterator<Integer> iter2 = gonnaRemove.keySet().iterator();
        while (iter2.hasNext()) {
            int index = iter2.next();
            remove.add(index);
        }

        Collections.sort(remove);
        for (int i = remove.size() - 1; i >= 0; i--) {
            int ii = remove.get(i);
            groups.remove(ii);
        }
        return groups;
    }

}
