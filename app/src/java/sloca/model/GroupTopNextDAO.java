/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author g3t2
 */
public class GroupTopNextDAO {

    /**
     *
     * to retrieve data from location
     *
     * @param timeStart the start time to retrieve the data
     * @param time the end time to retrieve the data
     * @param macAddress is the user's mac-address
     * @return String ArrayList of the list of data, which contains the
     * timeStamp and location ID
     */
    public static ArrayList<String> retrieveData(String timeStart, String time) {

        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql;

        try {

            sql = "select timeStamp, locationID, macAddress from location where timeStamp between ? and ?\n"
                    + "order by timeStamp;";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timeStart);
            pstmt.setString(2, time);

            rs = pstmt.executeQuery();
            while (rs.next()) {

                result.add(rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }
}
