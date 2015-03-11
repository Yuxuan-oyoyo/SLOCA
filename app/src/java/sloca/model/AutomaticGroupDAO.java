/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.\
 * 
 */
package sloca.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Get details for each user within the specific time period
 */
public class AutomaticGroupDAO {

    /**
     *
     * to retrieve data between a specific period of time
     *
     * @param timeStart the start time to retrieve the data
     * @param time the end time to retrieve the data
     * @return String ArrayList of the list of data, which contains timeStamp,
     * mac-address, location ID and email
     */
    public static ArrayList<String> retrieveData(String timeStart, String time) {

        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql;

        try {

            sql = "select timeStamp, table1.macAddress, locationID, email from \n"
                    + "(select * from location \n"
                    + "where TIMESTAMP \n"
                    + "BETWEEN ? \n"
                    + "AND  ?) table1 left join\n"
                    + "(select * from user) table2\n"
                    + "on table1.macAddress = table2.macAddress\n"
                    + "order by timeStamp";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timeStart);
            pstmt.setString(2, time);

            rs = pstmt.executeQuery();
            while (rs.next()) {

                result.add(rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }
}
