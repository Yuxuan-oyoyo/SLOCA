/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * @author g3t2
 */
package sloca.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * to process the calculation of the top-k popular next place(s)
 */
public class NextPlaceDAO {

    /**
     *
     * to get the top-k popular next place(s)
     *
     * @param preTimeStart the time to start processing the data for the
     * previous 15 minutes
     * @param dateTime the time entered by the user
     * @param k the k value for processing the ranking level
     * @param postTimeEnd the time to stop processing the data for the post 15
     * minutes
     * @param origin the original place
     * @param postTimeStart the time to start processing the data for the post
     * 15 minutes
     * @return ArrayList of String, each String contains the user's mac-address,
     * timeStamp and the two semantic places, as well as the time interval
     * between the two semantic places
     */
    public static ArrayList<String> calculateTopNextPlaces(String preTimeStart, String dateTime,
            int k, String postTimeEnd, String origin, String postTimeStart) {
        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";
        try {

            sql = " SELECT M1.macAddress, M1.maxTimeStamp, M2.semanticPlace, M1.semanticPlace, M1.diff, M2.time1"
                    + " FROM "
                    + "(SELECT macAddress, MAX(timeStamp) as maxTimeStamp,"
                    + " TIMESTAMPDIFF(minute, timeStamp, ?) as diff,"
                    + " semanticPlace FROM location, locationlookup"
                    + " WHERE (timeStamp BETWEEN ? AND ?)"
                    + " AND location.locationId = locationlookup.locationId"
                    + " GROUP BY macAddress, semanticPlace"
                    + " ) M1"
                    + " RIGHT JOIN ("
                    + " SELECT macAddress, MAX(timeStamp) as time1, semanticPlace FROM location, locationlookup"
                    + " WHERE (timeStamp BETWEEN ? AND ?)"
                    + " AND location.locationId = locationlookup.locationId"
                    // + " AND semanticPlace = ?"
                    + " GROUP BY macAddress, semanticPlace"
                    + " ORDER BY timeStamp DESC) M2"
                    + " ON M1.macAddress = M2.macAddress"
                    //+ " GROUP BY macAddress"
                    + " ORDER BY M1.macAddress ASC, M2.time1 DESC, M1.maxTimeStamp DESC";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, postTimeEnd);
            pstmt.setString(2, postTimeStart);
            pstmt.setString(3, postTimeEnd);
            pstmt.setString(4, preTimeStart);
            pstmt.setString(5, dateTime);
           // pstmt.setString(6, origin);

            int rank = 0;
            int count = 0;
            int time = 0;
            String mac = "";
            Boolean check = false;
            ArrayList<String> dontCountMac = new ArrayList<String>();

            rs = pstmt.executeQuery();
            while (rs.next()) {

                if (!dontCountMac.contains(rs.getString(1))) {

                    if (!rs.getString(3).equals(origin)) {
                        dontCountMac.add(rs.getString(1));
                    } else {

                        if (!rs.getString(1).equals(mac)) {
                            if (rs.getString(3).equals(rs.getString(4)) || rs.getInt(5) < 5) {
                                check = true;
                                time = rs.getInt(5);
                                count++;
                            } else {

                                result.add(rs.getString(4));
                                check = false;
                                time = 0;
                                count++;
                            }
                        } else if (rs.getString(1).equals(mac) && check) {

                            time = rs.getInt(5) - time;
                            if (time < 5) {
                                check = true;
                            } else {
                                result.add(rs.getString(4));
                                check = false;
                                time = 0;
                            }
                        }
                        mac = rs.getString(1);
                    }
                }
            }
            result.add("" + count);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }

}
