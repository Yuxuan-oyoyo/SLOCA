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
 * to process the break down of the data by according to different condition(s)
 */
public class BreakdownDAO {

    static String yearString = " REVERSE( SUBSTRING( REVERSE(email), LOCATE ('@', REVERSE(email)) + 1, 4 ))";
    static String schoolString = " SUBSTRING(email, LOCATE('@',email) + 1,"
            + " LOCATE('.', email, LOCATE('@',email)) - LOCATE('@',email) - 1)";
    static String genderString = " gender";

    /**
     *
     * to get attribute name of the order
     *
     * @param name the name of the condition
     * @return String of the specific attribute name, to be used in the sql
     * statement
     */
    public static String getName(String name) {
        String attribute = "";
        if (name.equals("year")) {
            attribute = yearString;
        } else if (name.equals("gender")) {
            attribute = genderString;
        } else {
            attribute = schoolString;
        }
        return attribute;
    }

    /**
     *
     * to break down the data by using one condition
     *
     * @param timeStart the start time to retrieve the data
     * @param time the end time to retrieve the data
     * @param name is the name of the break down condition
     * @return String ArrayList of the list of data, which contains the
     * condition and the count number
     */
    public static ArrayList<String> breakdownByOne(String timeStart, String time, String name) {

        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";
        String attribute1 = "";

        try {

            attribute1 = getName(name);

            sql = "SELECT " + attribute1
                    + " as param, COUNT( DISTINCT M1.macAddress) FROM user LEFT JOIN "
                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?) M1 "
                    + "ON user.macAddress = M1.macAddress "
                    + "GROUP BY param";

            if (attribute1.equals(" gender")) {
                sql += "ORDER BY param desc";
            }

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timeStart);
            pstmt.setString(2, time);
            //pstmt.setString(3, name);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1) + "," + rs.getString(2));
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
     * to break down the data by using two conditions
     *
     * @param timeStart the start time to retrieve the data
     * @param time the end time to retrieve the data
     * @param name1 is the name of the first break down condition
     * @param name2 is the name of the second break down condition
     * @return String ArrayList of the list of data, which contains the
     * conditions and their corresponding count number
     */
    public static ArrayList<String> breakdownByTwo(String timeStart, String time, String name1, String name2) {
        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";
        String attribute1 = "";
        String attribute2 = "";

        try {
            attribute1 = getName(name1);
            attribute2 = getName(name2);

            sql = "SELECT T1.param1, T1.param1Count, T0.param2, IFNULL(T2.param2Count, 0) "
                    + "FROM"
                    + "(SELECT" + attribute1 + " AS param1, IFNULL(COUNT( DISTINCT M1.macAddress),0) as param1count"
                    + " FROM user LEFT JOIN "
                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?) M1 "
                    + " ON user.macAddress = M1.macAddress "
                    + "GROUP BY param1"
                    + ") T1 "
                    + "INNER JOIN"
                    + " (SELECT " + attribute2 + " AS param2 FROM user GROUP BY param2) T0"
                    + " LEFT OUTER JOIN "
                    + "(SELECT" + attribute1 + " AS param1," + attribute2 + " AS param2 ,"
                    + " IFNULL(COUNT( DISTINCT M2.macAddress),0) as param2Count FROM user LEFT JOIN "
                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?) M2 "
                    + " ON user.macAddress = M2.macAddress "
                    + "GROUP BY param1, param2 ORDER BY param1, param2) T2 "
                    + "ON T1.param1 = T2.param1 AND T0.param2 = T2.param2 ";

            if (attribute1.equals(" gender")) {
                sql += "ORDER BY T1.param1 desc, T0.param2 asc";
            } else if (attribute2.equals(" gender")) {
                sql += "ORDER BY T1.param1 asc, T0.param2 desc";
            } else {
                sql += "ORDER BY T1.param1, T0.param2";
            }

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timeStart);
            pstmt.setString(2, time);
            pstmt.setString(3, timeStart);
            pstmt.setString(4, time);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3)
                        + "," + rs.getString(4));
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
     * to break down the data by using three conditions
     *
     * @param timeStart the start time to retrieve the data
     * @param time the end time to retrieve the data
     * @param name1 is the name of the first break down condition
     * @param name2 is the name of the second break down condition
     * @param name3 is the name of the third break down condition
     * @return String ArrayList of the list of data, which contains the
     * conditions and their corresponding count number
     */
    public static ArrayList<String> breakdownByThree(String timeStart, String time, String name1, String name2, String name3) {
        ArrayList<String> result = new ArrayList<String>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "";
        String attribute1 = "";
        String attribute2 = "";
        String attribute3 = "";

        try {
            attribute1 = getName(name1);
            attribute2 = getName(name2);
            attribute3 = getName(name3);

            sql = "SELECT T1.param1, T1.param1Count, T01.param2, IFNULL(T2.param2Count,0), T02.param3, IFNULL(T3.param3Count,0) "
                    + "FROM"
                    + "(SELECT" + attribute1 + " AS param1, IFNULL(COUNT( DISTINCT M1.macAddress),0) as param1Count FROM user LEFT JOIN "
                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?) M1 "
                    + " ON user.macAddress = M1.macAddress "
                    + "GROUP BY param1) T1 "
                    + "INNER JOIN"
                    + " (SELECT " + attribute2 + " AS param2 FROM user GROUP BY param2) T01 "
                    + "INNER JOIN"
                    + " (SELECT " + attribute3 + " AS param3 FROM user GROUP BY param3) T02 "
                    + "LEFT OUTER JOIN"
                    + "(SELECT" + attribute1 + " AS param1," + attribute2 + " AS param2 ,"
                    + " IFNULL(COUNT( DISTINCT M2.macAddress),0) as param2Count FROM user LEFT JOIN "
                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?) M2 "
                    + " ON user.macAddress = M2.macAddress "
                    + "GROUP BY param1, param2) T2 "
                    + "ON T1.param1 = T2.param1 AND T01.param2 = T2.param2"
                    + " LEFT JOIN "
                    + " (SELECT" + attribute1 + " AS param1," + attribute2 + " AS param2, "
                    + attribute3 + " AS param3, "
                    + "IFNULL(COUNT( DISTINCT M3.macAddress),0) as param3Count FROM user LEFT JOIN "
                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?) M3 "
                    + " ON user.macAddress = M3.macAddress "
                    + "GROUP BY param1, param2, param3) T3 "
                    + "ON T1.param1 = T3.param1 AND T01.param2 = T3.param2 AND T02.param3 = T3.param3 ";

            if (attribute1.equals(" gender")) {
                sql += "ORDER BY T1.param1 desc, T01.param2, T02.param3";
            } else if (attribute2.equals(" gender")) {
                sql += "ORDER BY T1.param1 asc, T01.param2 desc, T02.param3 asc";
            } else {
                sql += "ORDER BY T1.param1 asc, T01.param2 asc, T02.param3 desc";
            }

            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timeStart);
            pstmt.setString(2, time);
            pstmt.setString(3, timeStart);
            pstmt.setString(4, time);
            pstmt.setString(5, timeStart);
            pstmt.setString(6, time);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3)
                        + "," + rs.getString(4) + "," + rs.getString(5) + "," + rs.getString(6));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }

//    public static ArrayList<String> breakdownBySchool(String timeStart, String time) {
//        ArrayList<String> result = new ArrayList<String>();
//        try {
//
//            sql = "SELECT SUBSTRING(email, LOCATE('@',email) + 1,"
//                    + " LOCATE('.', email, LOCATE('@',email)) - LOCATE('@',email) - 1)"
//                    + "AS school, COUNT( DISTINCT macAddress) FROM user WHERE macAddress IN "
//                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?)"
//                    + "GROUP BY school ";
//
//            conn = ConnectionManager.getConnection();
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, timeStart);
//            pstmt.setString(2, time);
//            rs = pstmt.executeQuery();
//            while (rs.next()) {
//                result.add(rs.getString(1) + ", " + rs.getString(2));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            ConnectionManager.close(conn, pstmt, rs);
//            return result;
//        }
//
//    }
//    public static ArrayList<String> breakdownByYearAndGender(String timeStart, String time) {
//
//        ArrayList<String> result = new ArrayList<String>();
//        try {
//
//            sql = "SELECT REVERSE( SUBSTRING( REVERSE(email), LOCATE ('@', REVERSE(email)) + 1, 4 ))"
//                    + " AS year, gender, COUNT( DISTINCT macAddress) FROM user WHERE macAddress IN "
//                    + "(SELECT macAddress FROM location WHERE timeStamp BETWEEN ? AND ?)"
//                    + "GROUP BY year, gender";
//
//            conn = ConnectionManager.getConnection();
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, timeStart);
//            pstmt.setString(2, time);
//            rs = pstmt.executeQuery();
//            while (rs.next()) {
//                result.add(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            ConnectionManager.close(conn, pstmt, rs);
//            return result;
//        }
//    }
//    
}
