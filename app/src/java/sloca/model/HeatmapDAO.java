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
import java.util.HashMap;

/**
 *
 * to get the heatmap
 */
public class HeatmapDAO {

    /**
     *
     * to retrieve the Heatmap
     *
     * @param dateTimeStart the time to start processing the data
     * @param dateTimeEnd the time entered by the user,also the time to stop
     * processing the data
     * @param level the floor level
     * @return HashMap which key is the semantic place, value is the number of
     * people at that place
     */
    public static HashMap<String, Integer> retrieveHeatmap(String dateTimeStart, String dateTimeEnd, String level) {
        HashMap<String, Integer> toReturn = new HashMap<String, Integer>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";

        try {
            //query to extract the heatmap requirement
            sql = "select llk.sp as 'location', count(loc.macadd)as 'num' from (select macAddress as 'macadd', timeStamp as 'ts', locationID as 'lid' from location where timeStamp between ? and ? ) as loc inner join (select macAddress as 'macadd2', max(timeStamp) as 'maxts' from location where timeStamp between ? and ? group by macadd2)as temp2 on loc.macAdd = temp2.macadd2 and loc.ts = temp2.maxts right outer join (select locationId as 'lid2', semanticPlace as sp from locationlookup where semanticPlace like ?) as llk on loc.lid = llk.lid2 group by llk.sp";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dateTimeStart);
            pstmt.setString(2, dateTimeEnd);
            pstmt.setString(3, dateTimeStart);
            pstmt.setString(4, dateTimeEnd);
            //change the level so that mySQL will search for words that have 'L2'(for example) inside
            String updateLevel = "%" + level + "%";
            pstmt.setString(5, updateLevel);
            rs = pstmt.executeQuery();

            while (rs.next()) {

                String semanticPlace = rs.getString("location");
                int number = rs.getInt("num");

                toReturn.put(semanticPlace, number);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return toReturn;
        }
    }

}
