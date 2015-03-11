/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 *@author g3t2
 */
package sloca.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * to get the data for the companions
 */
public class TopCompanionsDAO {

    /**
     *
     * to get the user time line
     *
     * @param timeStart the time to start processing the data
     * @param time the time entered by the user,also the time to stop processing
     * the data
     * @param mac the user's mac-address
     * @return ArrayList of String, each String contains the user's mac-address,
     * first timeStamp, second timeStamp and location ID
     */
    public static ArrayList<String> retrieveUserTimeLine(String timeStart, String time, String mac) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";

        ArrayList<String> result = new ArrayList<String>();
        try {

            sql = " SELECT timeStamp, locationId, macAddress"
                    + " FROM location"
                    + " WHERE macAddress = ? "
                    + "AND timeStamp BETWEEN ? AND ?"
                    + " ORDER BY timeStamp";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, mac);
            pstmt.setString(2, timeStart);
            pstmt.setString(3, time);

            rs = pstmt.executeQuery();

            Timestamp endTime = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date parsedDate = dateFormat.parse(time);
                endTime = new java.sql.Timestamp(parsedDate.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (rs.next()) {
                Timestamp time1 = rs.getTimestamp(1);
                String res = rs.getString(3) + "," + time1;
                if (!rs.isLast()) {
                    rs.next();
                    Timestamp time2 = rs.getTimestamp(1);
                    if ((time2.getTime() - time1.getTime()) > 540000) {
                        time2.setTime(time1.getTime() + 540000);
                    }
                    res += "," + time2;
                    rs.previous();
                    res += "," + rs.getString(2);
                } else {
                    if ((endTime.getTime() - time1.getTime()) > 540000) {
                        endTime.setTime(time1.getTime() + 540000);
                    }
                    res += "," + endTime + "," + rs.getString(2);
                }
                result.add(res);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }

    /**
     *
     * to get the companion's time line
     *
     * @param mac the user's mac-address
     * @param timeStart the user's first timeStamp
     * @param time the user's second timeStamp
     * @param id the location ID
     * @param dateStart the time to start processing the data
     * @return ArrayList of String, each String contains the companion's
     * mac-address and duration
     */
    public static ArrayList<String> retrieveCompanionTime(String mac,
            String timeStart, String time, String id,
            String dateStart) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";

        ArrayList<String> result = new ArrayList<String>();
        try {

            sql = " SELECT macAddress,"
                    + " TIMESTAMPDIFF(second, timeStamp, ?) as diff,"
                    + " locationID"
                    + " FROM location"
                    + " WHERE macAddress <> ?"
                    + " AND timeStamp BETWEEN ? AND ?"
                    // + " AND locationID = ?"
                    //+ " ORDER BY timeStamp"
                    //+ " GROUP BY macAddress, locationId"
                    + " ORDER BY macAddress, diff ASC";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            Timestamp time9MinBefore = new Timestamp(0);
            Timestamp timeStart2 = null;
            long preTime = 0;
            try {
                timeStart2 = Timestamp.valueOf(timeStart);
            } catch (Exception ts2) {
                ts2.printStackTrace();
            }
            Timestamp dateStart2 = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date parsedDate = dateFormat.parse(dateStart);
                dateStart2 = new java.sql.Timestamp(parsedDate.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

            pstmt.setString(1, time);
            pstmt.setString(2, mac);

            time9MinBefore.setTime(timeStart2.getTime() - 540000);

            if (time9MinBefore.after(dateStart2)) {
                pstmt.setString(3, time9MinBefore.toString());
            } else {
                pstmt.setString(3, dateStart);

            }

            pstmt.setString(4, time);
            //pstmt.setString(5, id);

            rs = pstmt.executeQuery();

            long duration1 = 0;
            long duration2 = 0;
            long diff = 0;
            boolean check = false;
            String macAdd = "";
            Timestamp time2 = null;
            try {
                time2 = Timestamp.valueOf(time);
            } catch (Exception t2) {
                t2.printStackTrace();
            }
            long totalWindow = (time2.getTime() - timeStart2.getTime()) / 1000;

            while (rs.next()) {

                diff = rs.getInt(2);
                if (!rs.getString(1).equals(macAdd)) {

                    if ((totalWindow - diff) >= 0) {
                        duration1 = diff;
                        if (id.equals(rs.getString(3))) {
                            if (duration1 <= 540) {
                                result.add(rs.getString(1) + "," + duration1);
                            } else {
                                result.add(rs.getString(1) + "," + 540);
                            }
                        }
                        check = false;
                    } else {
                        duration1 = 540 - (diff - totalWindow);
                        if (id.equals(rs.getString(3))) {
                            if (totalWindow < duration1) {
                                result.add(rs.getString(1) + "," + totalWindow);
                            } else {
                                result.add(rs.getString(1) + "," + duration1);
                            }
                        }
                        check = true;
                    }
                } else if (rs.getString(1).equals(macAdd)) {

                    if ((totalWindow - diff) >= 0) {

                        duration2 = diff - duration1;
                        duration1 += duration2;
                        if (duration2 > 540) {
                            duration2 = 540;
                        }
                        if (id.equals(rs.getString(3))) {
                            result.add(rs.getString(1) + "," + duration2);
                        }
                    } else if (!check) { //change 1
                        preTime = 540 - (diff - totalWindow);
                        if (id.equals(rs.getString(3))) {
                            if (preTime >= (totalWindow - duration1)) {
                                result.add(rs.getString(1) + "," + (totalWindow - duration1));
                            } else {

                                if (totalWindow < preTime) {
                                    result.add(rs.getString(1) + "," + totalWindow);
                                } else {
                                    result.add(rs.getString(1) + "," + preTime);
                                }
                            }
                        }
                        check = true;
                    }
                }
                macAdd = rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }

    /**
     *
     * to get the specific person's email
     *
     * @param mac the person's mac-address
     * @return String of the person's email
     */
    public static String retrieveEmail(String mac) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";

        String result = "";
        try {

            sql = " SELECT email from user WHERE macAddress = ?";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, mac);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                result = rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }
}
