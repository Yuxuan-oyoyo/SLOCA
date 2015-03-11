/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * @author smu
 */
package sloca.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import sloca.model.ConnectionManager;

/**
 *
 * to process the calculation of the top-k popular place(s)
 */
public class PopularPlacesDAO {

    /**
     *
     * to get the top-k popular place(s)
     *
     * @param dateTimeStart the time to start processing the data
     * @param dateTime the time entered by the user,also the time to stop
     * processing the data
     * @param k the k value for processing the ranking level
     * @return ArrayList of String, each String contains the rank number,
     * semantic places and the corresponding count
     */
    public static ArrayList<String> calculatePopularRanking(String dateTimeStart, String dateTime, int k) {
        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";
        try {

            sql = "select llk.semanticPlace as 'location', count(loc.macadd)as 'num' from "
                    + "(select macAddress as 'macadd', timeStamp as 'ts',"
                    + " locationID as 'lid' from location where timeStamp between ? and ? )"
                    + " as loc inner join "
                    + "(select macAddress as 'macadd2', max(timeStamp) as 'maxts' from location"
                    + " where timeStamp between ? and ? group by macadd2)"
                    + "as temp2 on loc.macAdd = temp2.macadd2 and loc.ts = temp2.maxts"
                    + " inner join locationlookup as llk on loc.lid = llk.locationId group by llk.semanticPlace ORDER BY num desc";
//            sql="SELECT semanticPlace,"
//                    + " COUNT( location.macAddress) as counter"
//                    + " FROM locationlookup, location where location.macAddress IN "
//                    + " (SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ? "
//                    + " GROUP BY macAddress)"
//                    + " AND location.macAddress IN "
//                    + " (SELECT macAdr"
//                    + " AND location.locationId = locationlookup.locationId "
//                    + " GROUP BY semanticPlace"
//                    + " ORDER BY counter DESC";

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dateTimeStart);
            pstmt.setString(2, dateTime);
            pstmt.setString(3, dateTimeStart);
            pstmt.setString(4, dateTime);
            int rank = 0;
            int count = 0;

            rs = pstmt.executeQuery();

            while (rs.next() && rank <= k) {

                if (count != 0) {
                    if (count != rs.getInt(2)) {
                        rank++;
                    }
                } else {
                    rank = 1;
                }
                count = rs.getInt(2);
                if (rank <= k) {
                    result.add(rank + "," + rs.getString(1) + ", " + rs.getInt(2));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }
}
