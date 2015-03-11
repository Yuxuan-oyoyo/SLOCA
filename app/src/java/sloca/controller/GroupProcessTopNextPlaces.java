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
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.AutomaticGroupDAO;
import sloca.model.Group;
import sloca.model.GroupTopNextDAO;
import sloca.model.LocationLookup;
import sloca.model.LocationLookupDAO;

/**
 *
 * @author g3t2
 */
public class GroupProcessTopNextPlaces extends HttpServlet {

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
        String semanticPlace = request.getParameter("origin");

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
                    RequestDispatcher view = request.getRequestDispatcher("groupPopNext.jsp");
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
            RequestDispatcher view = request.getRequestDispatcher("groupPopNext.jsp");
            view.forward(request, response);
        }

        //1. Generate all the groups using the automatic group detection report "method" that you have implemented earlier.
        ArrayList<String> dataArr = AutomaticGroupDAO.retrieveData(dateTimeStart, dateTime);

        ProcessAutomaticGroupIdentification pagi = new ProcessAutomaticGroupIdentification();
        ArrayList<Group> groups = pagi.getGroups(dataArr, dateTime);
        int totalGroup = groups.size();
        //logic start here
        List<LocationLookup> locationLookupArr = LocationLookupDAO.retrieveAll();
        HashMap<String, String> locationLookup = new HashMap<String, String>();
        for (LocationLookup loclookup : locationLookupArr) {
            locationLookup.put(loclookup.getLocationId(), loclookup.getSemanticPlace());
        }

        //2. Identifying groups --> according to 
        //"For each of the group identified using the automatic group detection logic, 
        //check that their last semantic place (the time spent at this semantic place 
        //need NOT be 12 minutes) in the previous window matches the query semantic place."
        ArrayList<Group> semanGroups = new ArrayList<Group>();
        for (Group g : groups) {
            String groupLocID = g.getLastLocation();
            String groupSemantic = locationLookup.get(groupLocID);
            if (groupSemantic.equals(semanticPlace)) {
                semanGroups.add(g);
            }
        }

        int semanGroup = semanGroups.size();
        //3-1. Retrieve group next timeLine;
        String dateTimeStart2 = "";
        String dateTimeEnd2 = "";
        try {
            //catch corner case for date where seconds will be ignored if time it is :00
            try {
                char c = dateTime.charAt(17);
            } catch (IndexOutOfBoundsException iobe) {
                dateTime += ":00";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateObj = sdf.parse(dateTime);
            //get next 15 minutes time period
            if (dateObj != null) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(dateObj);
                cal.add(cal.SECOND, +1);
                dateTimeStart2 = sdf.format(cal.getTime());
            }
            if (dateObj != null) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(dateObj);
                cal.add(cal.MINUTE, +15);
                dateTimeEnd2 = sdf.format(cal.getTime());
            }

        } catch (ParseException e) {
            request.setAttribute("timeErr", "Date input error,Please try again!");
            RequestDispatcher view = request.getRequestDispatcher("groupPopNext.jsp");
            view.forward(request, response);
        }

        ArrayList<String> totalTimeLine = GroupTopNextDAO.retrieveData(dateTimeStart2, dateTimeEnd2);
        ArrayList<String> next = new ArrayList<String>();
        for (Group group : semanGroups) {
            ArrayList<String> macAddress = group.getUsers();
            //HashMap<MacAddress, HashMap<locationID,ArrayList<Timestamp>>>
            HashMap<String, HashMap<String, ArrayList<Timestamp>>> groupTimeLine = new HashMap<String, HashMap<String, ArrayList<Timestamp>>>();
            for (String macAdd : macAddress) {
                ArrayList<String> memberStringTimeline
                        = retrieveData(totalTimeLine, macAdd);
                HashMap<String, ArrayList<Timestamp>> memberTimeline = new HashMap<String, ArrayList<Timestamp>>();
                for (int i = 0; i < memberStringTimeline.size(); i++) {
                    String[] arrCurrent;
                    arrCurrent = memberStringTimeline.get(i).split(",");
                    String locID = arrCurrent[1];
                    String semantic = locationLookup.get(locID);
                    String t1 = arrCurrent[0];
                    String t2 = "";
                    Timestamp time1 = null;
                    Timestamp time2 = null;
                    if (i != (memberStringTimeline.size() - 1)) {
                        String[] arrNext;
                        arrNext = memberStringTimeline.get(i + 1).split(",");
                        t2 = arrNext[0];
                    } else {
                        t2 = dateTimeEnd2;
                    }

                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date parsedDate1 = dateFormat.parse(t1);
                        time1 = new java.sql.Timestamp(parsedDate1.getTime());
                        Date parsedDate2 = dateFormat.parse(t2);
                        time2 = new java.sql.Timestamp(parsedDate2.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!memberTimeline.containsKey(semantic)) {
                        ArrayList<Timestamp> timeList = new ArrayList<Timestamp>();
                        if ((time2.getTime() - time1.getTime()) / 1000 <= 540) {
                            timeList.add(time1);
                            timeList.add(time2);
                        } else {
                            timeList.add(time1);
                            timeList.add(new Timestamp(time1.getTime() + 540000));
                        }
                        memberTimeline.put(semantic, timeList);
                    } else {
                        ArrayList<Timestamp> timeList = memberTimeline.get(semantic);

                        if ((time2.getTime() - time1.getTime()) / 1000 <= 540) {
                            timeList.add(time1);
                            timeList.add(time2);
                        } else {
                            timeList.add(time1);
                            timeList.add(new Timestamp(time1.getTime() + 540000));
                        }
                        memberTimeline.put(semantic, timeList);
                    }
                }
                //resort the Timestamps
                Iterator<String> iter1 = memberTimeline.keySet().iterator();
                while (iter1.hasNext()) {
                    String sem = iter1.next();
                    ArrayList<Timestamp> timeLine = memberTimeline.get(sem);
                    ArrayList<Integer> index = new ArrayList<Integer>();
                    for (int i = 1; i < timeLine.size() - 1; i += 2) {
                        Timestamp t1 = timeLine.get(i);
                        Timestamp t2 = timeLine.get(i + 1);
                        if (t1.equals(t2)) {
                            index.add(i);
                            index.add(i + 1);
                        }
                    }
                    for (int i = index.size() - 1; i >= 0; i--) {
                        int x = index.get(i);
                        timeLine.remove(x);
                    }
                    memberTimeline.put(sem, timeLine);
                }

                groupTimeLine.put(macAdd, memberTimeline);
            }
            String nextSemantic = getNextSemantic(groupTimeLine);
            if (nextSemantic != null) {
                next.add(nextSemantic);
            }

        }
        HashMap<Integer, HashMap<String, Integer>> ranking = getRanking(rank, next);

        request.setAttribute("rank", stringRank);
        request.setAttribute("dateTime", dateTime);
        request.setAttribute("totalGroup", semanGroup);
        request.setAttribute("ranking", ranking);
        request.setAttribute("semanGroup", next.size());
        request.setAttribute("semanticPlace", semanticPlace);
        RequestDispatcher dispatcher = request.getRequestDispatcher("groupPopNext.jsp");
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
     *
     * to get the next semantic place
     *
     * @param groupTimeLine the time line of the group
     * @return a String value which is the next semantic place
     */
    private String getNextSemantic(HashMap<String, HashMap<String, ArrayList<Timestamp>>> groupTimeLine) {
        String nextSemantic = null;

        Iterator<String> iter = groupTimeLine.keySet().iterator();

        HashMap<String, ArrayList<Timestamp>> timeLineA = null;
        while (iter.hasNext()) {
            String macA = iter.next();
            timeLineA = groupTimeLine.get(macA);
            break;
        }

        Iterator<String> iter2 = groupTimeLine.keySet().iterator();
        while (iter2.hasNext()) {
            String macB = iter2.next();
            HashMap<String, ArrayList<Timestamp>> timeLineB = groupTimeLine.get(macB);
            HashMap<String, ArrayList<Timestamp>> timeMapAB = compareTimelines(timeLineA, timeLineB);
            timeLineA = timeMapAB;
        }
        Iterator<String> iter3 = timeLineA.keySet().iterator();
        TreeMap<Timestamp, String> tree = new TreeMap<Timestamp, String>();
        while (iter3.hasNext()) {
            String seman = iter3.next();
            ArrayList<Timestamp> times = timeLineA.get(seman);
            for (int i = 0; i < times.size(); i = i + 2) {
                Timestamp t1 = times.get(i);
                Timestamp t2 = times.get(i + 1);
                if (((t2.getTime() - t1.getTime()) / 1000) >= 300) {
                    tree.put(t1, seman);
                }
            }
        }
        if (tree.size() != 0) {
            Timestamp tLast = tree.firstKey();
            nextSemantic = tree.get(tLast);
        }
        return nextSemantic;
    }

    /**
     * compare the time line of two users
     *
     * @param timeMapA is the user A's time line
     * @param timeMapB is the user B's time line
     * @return a HashMap, which key is the semantic place, and value is an
     * ArrayList of timeStamp
     */
    private HashMap<String, ArrayList<Timestamp>> compareTimelines(HashMap<String, ArrayList<Timestamp>> timeMapA, HashMap<String, ArrayList<Timestamp>> timeMapB) {
        Iterator<String> iter = timeMapA.keySet().iterator();
        HashMap<String, ArrayList<Timestamp>> timeLineAB = new HashMap<String, ArrayList<Timestamp>>();
        while (iter.hasNext()) {
            String locationA = iter.next();
            ArrayList<Timestamp> locTimeA = timeMapA.get(locationA);
            if (timeMapB.containsKey(locationA)) {
                //add equal if statement
                ArrayList<Timestamp> locTimeB = timeMapB.get(locationA);
                for (int i = 0; i < locTimeA.size(); i = i + 2) {
                    Timestamp A1 = locTimeA.get(i);
                    Timestamp A2 = locTimeA.get(i + 1);
                    for (int p = 0; p < locTimeB.size(); p = p + 2) {
                        Timestamp B1 = locTimeB.get(p);
                        Timestamp B2 = locTimeB.get(p + 1);
                        if ((A1.before(B1) || A1.equals(B1)) && (A2.after(B2) || A2.equals(B2))) {
                            if (timeLineAB.containsKey(locationA)) {
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                locTimeAB.add(B1);
                                locTimeAB.add(B2);
                                timeLineAB.put(locationA, locTimeAB);
                            } else {
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                locTimeAB.add(B1);
                                locTimeAB.add(B2);
                                timeLineAB.put(locationA, locTimeAB);
                            }
                        } else if ((B1.before(A1) || B1.equals(A1)) && (B2.after(A2) || B2.equals(A2))) {
                            if (timeLineAB.containsKey(locationA)) {
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                locTimeAB.add(A1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                            } else {
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                locTimeAB.add(A1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                            }
                        } else if ((A1.before(B1) || A1.equals(B1)) && A2.after(B1)) {
                            if (timeLineAB.containsKey(locationA)) {
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                locTimeAB.add(B1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                            } else {
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                locTimeAB.add(B1);
                                locTimeAB.add(A2);
                                timeLineAB.put(locationA, locTimeAB);
                            }
                        } else if ((A1.after(B1) || A1.equals(B1)) && A1.before(B2)) {
                            if (timeLineAB.containsKey(locationA)) {
                                ArrayList<Timestamp> locTimeAB = timeLineAB.get(locationA);
                                locTimeAB.add(A1);
                                locTimeAB.add(B2);
                                timeLineAB.put(locationA, locTimeAB);
                            } else {
                                ArrayList<Timestamp> locTimeAB = new ArrayList<Timestamp>();
                                locTimeAB.add(A1);
                                locTimeAB.add(B2);
                                timeLineAB.put(locationA, locTimeAB);
                            }
                        }
                    }
                }
            }
        }
        return timeLineAB;
    }

    /**
     * get rank for the next semantic place
     *
     * @param rank the rank value is required
     * @param next is the ArrayList of semantic place
     * @return a HashMap, which key is the rank value, and value is the HashMap
     * with a semantic place and its count
     */
    private HashMap<Integer, HashMap<String, Integer>> getRanking(int rank, ArrayList<String> next) {
        HashMap<Integer, HashMap<String, Integer>> ranking = new HashMap<Integer, HashMap<String, Integer>>();
        HashMap<String, Integer> count = new HashMap<String, Integer>();
        for (String str : next) {
            if (!count.containsKey(str)) {
                count.put(str, 1);
            } else {
                int num = count.get(str);
                count.put(str, num + 1);
            }
        }

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
     * To get the string of the time line of the specific user
     *
     * @param totalTimeLine the ArrayList of all
     * "timestamp,locationid,macaddress"
     * @param macAdd the mac-address of the user that is inside the timeline
     * @return ArrayList<String> in the form of "timestamp,location"
     */
    private ArrayList<String> retrieveData(ArrayList<String> totalTimeLine, String macAdd) {
        ArrayList<String> userStringTimeLine = new ArrayList<String>();
        for (String str : totalTimeLine) {
            String[] arrCurrent = str.split(",");
            String mac = arrCurrent[2];
            if (mac.equals(macAdd)) {
                userStringTimeLine.add(str);
            }
        }

        return userStringTimeLine;
    }

}
