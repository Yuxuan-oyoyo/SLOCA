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
public class LocationLookupDAO {

    /**
     *
     * Retrieve All LocationLookup Objects
     *
     * @return List of LocationLookup Object
     */
    public static List<LocationLookup> retrieveAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LocationLookup> results = new ArrayList<LocationLookup>();
        try {
            String sql = "select * from locationlookup";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                String locationId = rs.getString("locationId");
                String semanticPlace = rs.getString("semanticPlace");

                LocationLookup obj = new LocationLookup(locationId, semanticPlace);
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
     * Create a new locationLookup object
     *
     * @param locationLookup the LocationLookup object to be created
     */
    public static void create(LocationLookup locationLookup) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into locationLookup (locationid, semanticplace) values (?,?) ";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, locationLookup.getLocationId());
            pstmt.setString(2, locationLookup.getSemanticPlace());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }

    }

    /**
     * Retrieve specific LocationLookup object
     *
     * @param locationId the location ID of the LocationLookup object to be
     * retrieved
     * @return LocationLookup
     */
    public static LocationLookup retrieve(String locationId) {
        LocationLookup result = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "SELECT locationid, semanticplace FROM locationLookup "
                    + "where locationid = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, locationId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                String locationId_ = rs.getString("locationid");
                String semanticPlace_ = rs.getString("semanticplace");

                result = new LocationLookup(locationId_, semanticPlace_);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }

    }

    /**
     * Delete specific LocationLookup object
     *
     * @param locationId the location ID of the LocationLookup object to be
     * deleted
     */
    public static void delete(String locationId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "DELETE FROM locationLookup where "
                    + "locationid= ? ";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, locationId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }
    }

    /**
     * Update specific LocationLookup object
     *
     * @param toBeUpdated the LocationLookup object to be updated
     */
    public static void update(LocationLookup toBeUpdated) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();

            String sql = "UPDATE locationLookup set semanticplace = ? where locationid=? ";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, toBeUpdated.getSemanticPlace());
            pstmt.setString(2, toBeUpdated.getLocationId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt);
        }
    }

    /**
     * Delete all LocationLookup objects
     */
    public static void deleteAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            String sql = "TRUNCATE locationlookup";
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
     * to retrieve all the semantic places
     *
     * @return a list of semantic places
     */
    public static ArrayList<String> retrieveAllSemanticPlaces() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<String> result = new ArrayList<String>();
        try {
            String sql = "SELECT distinct semanticPlace FROM locationlookup";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                result.add(rs.getString(1));
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
     * to check whether a semantic place exists
     *
     * @param semanticplace the semantic place to be checked
     * @return a boolean value
     */
    public static boolean checkSemanticPlace(String semanticplace) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        LocationLookup llk = null;
        try {
            String sql = "SELECT * FROM locationlookup WHERE semanticPlace = ?";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, semanticplace);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String locationId_ = rs.getString("locationid");
                String semanticPlace_ = rs.getString("semanticplace");

                llk = new LocationLookup(locationId_, semanticPlace_);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            ConnectionManager.close(conn, pstmt, rs);
            if (llk != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     *
     * to retrieve all the location IDs
     *
     * @return a HashMap, which key is location ID
     */
    public static HashMap<String, String> retrieveAllLocationID() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            String sql = "SELECT distinct locationId FROM locationlookup";
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                result.put(rs.getString(1), "");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
            return result;
        }
    }
}
