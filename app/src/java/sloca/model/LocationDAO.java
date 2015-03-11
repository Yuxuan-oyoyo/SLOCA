/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * LocationLookupDAO handles the data update/retrieval of locationLookup
 *
 * @author G3T2
 */
public class LocationDAO {

    /**
     * Retrieve all location data
     *
     * @return list of location objects
     */
    public static List<Location> retrieveAll() {
        List<Location> results = new ArrayList<Location>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from location";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong("id");
                String timestamp = rs.getString("timestamp");
                String macAddress = rs.getString("macaddress");
                String locationId = rs.getString("locationid");

                Location obj = new Location(id, timestamp, macAddress, locationId);
                results.add(obj);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return results;
        }
    }

    /**
     *
     * to get all the lines
     *
     * @return a HashMap, which key is the line, value is the location object
     */
    public static HashMap<String, Location> retrieveAllLines() {
        HashMap<String, Location> results = new HashMap<String, Location>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from location";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong("id");
                String timestamp = rs.getString("timeStamp");
                String macAddress = rs.getString("macAddress");
                String locationId = rs.getString("locationId");

                String line = id + timestamp.substring(0, 19) + "," + macAddress;
                Location toAdd = new Location(id, timestamp, macAddress, locationId);
                results.put(line, toAdd);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return results;
        }
    }

    /**
     * Create a new Location object given its details. Auto-generate its id.
     *
     * @param location the Location object to be created.
     *
     */
    public static void create(Location location) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into location (timeStamp, macAddress, locationID) values (?,?,?) ";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, location.getTimeStamp());
            pstmt.setString(2, location.getMacAddress());
            pstmt.setString(3, location.getLocationId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }

    }

    /**
     * Retrieve specific Location object
     *
     * @param timestamp the time of the location update
     * @param macAddress the (hashed) MAC address indicating the unique id of
     * the traced device
     * @return a Location object
     */
    public static Location retrieve(String timestamp, String macAddress) {
        Location result = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "SELECT * FROM location "
                    + "where timeStamp = ? and macAddress=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timestamp);
            pstmt.setString(2, macAddress);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                long id = rs.getLong("id");
                String timestamp_ = rs.getString("timestamp");
                String macAddress_ = rs.getString("macaddress");
                String locationId_ = rs.getString("locationid");

                result = new Location(id, timestamp_.substring(0, 19), macAddress_, locationId_);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }

    }

    /**
     * Delete specific Location data
     *
     * @param timestamp the time of the location update
     * @param macAddress the (hashed) MAC address indicating the unique id of
     * the traced device
     * @param locationID the unique identifier for the location
     */
    public static void delete(String timestamp, String macAddress, String locationID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "DELETE FROM location where "
                    + "timeStamp = ? and macAddress = ? and locationID = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, timestamp);
            pstmt.setString(2, macAddress);
            pstmt.setString(3, locationID);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }
    }

    /**
     * Delete specific location data
     *
     * @param id the id of the Location object need to be deleted
     */
    public static void delete(long id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "DELETE FROM location where "
                    + "id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }
    }

    /**
     *
     * to delete all the data inside location
     *
     */
    public static void deleteAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            String sql = "TRUNCATE location";
            pstmt = conn.prepareStatement(sql);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }
    }

    /**
     * Upload all data
     *
     * @param filepath is the path of the file to be processed
     */
    public static void uploadAll(String filepath) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            String sql = "LOAD DATA INFILE '" + filepath + "'INTO TABLE location FIELDS TERMINATED BY ',' lines terminated by '\r\n'";
            pstmt = conn.prepareStatement(sql);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }
    }

    /**
     *
     * to all the users at a specific time
     *
     * @param timeStampBefore is the start time to retrieve the data
     * @param timeStampAfter is the end time to retrieve the data
     * @param origin is the origin
     * @return the number of different mac-address
     */
    public static int retrieveAllUsersAtTime(String timeStampBefore, String timeStampAfter, String origin) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int result = 0;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "SELECT count(distinct macAddress)FROM location "
                    + ",locationlookup where semanticPlace = ? "
                    + "and timeStamp between ? and ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, origin);
            pstmt.setString(2, timeStampBefore);
            pstmt.setString(3, timeStampAfter);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                result = rs.getInt(1);

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
     * to retrieve a specific location object by mac-address
     *
     * @param macAdd is the user's mac-address
     * @return a Location object
     */
    public static Location retrieveUserByMacAdd(String macAdd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Location location = null;
        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select * from location where macAddress = ?");
            pstmt.setString(1, macAdd);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                location = new Location(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return location;
    }
}
