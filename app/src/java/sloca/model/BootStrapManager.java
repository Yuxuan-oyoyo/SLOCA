/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author g3t2
 */
public class BootStrapManager {

    //public static HashMap<String,String> locErrorMap;
    //public static HashMap<String, ArrayList<String>> locLookUpErrorMap = new HashMap<String, ArrayList<String>>();
    //public static HashMap<String, ArrayList<String>> demoErrorMap = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, Location> locSuccess;
    public static HashMap<String, String> loclookupSuc;
    public static HashMap<String, Location> locSucUpdate;
    public static ArrayList<BootstrapError> locErrorList = new ArrayList<BootstrapError>();
    public static ArrayList<BootstrapError> locLookUpErrorList = new ArrayList<BootstrapError>();
    public static ArrayList<BootstrapError> demoErrorList = new ArrayList<BootstrapError>();
    public static ArrayList<String> demoUpdateSuc = new ArrayList<String>();
    public static HashMap<Long, Location> locErrorUpdate;
    public static HashMap<String, String> demoSuc;

    /**
     *
     * process demographics.csv, delete the user.csv from database and start to
     * validate the demographics.csv
     *
     * @param filePath is the path of the file to be processed
     */
    public static void processDemo(String filePath) {

        //clear all data before bootstrap
        UserDAO.deleteAll();
        demoErrorList.clear();
        
        demoSuc = new HashMap<String, String>();
        Connection conn = null;
        String line[] = null;
        CSVReader rdr= null;
        PreparedStatement ps = null;
        BufferedReader br = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                rdr = new CSVReader(br);
                String addquery = "insert into user values(?,?,?,?,?)";
                ps = conn.prepareStatement(addquery);

                int maxBatchCount = 30000; //batch insert counters
                int batchCounter = 1;

                long lineCounter = 2; //line counter
                rdr.readNext();
                while ((line = rdr.readNext())!= null) {
                    if (line.length != 0) {
                        ArrayList<String> errors = validateDemographics(line);
                        if (errors == null || errors.isEmpty()) {
                           
                            ps.setString(1, line[0]);
                            ps.setString(2, line[1]);
                            ps.setString(3, line[2]);
                            ps.setString(4, line[3]);
                            ps.setString(5, line[4]);
                            ps.addBatch();
                            batchCounter++;
                            if (batchCounter == maxBatchCount) {
                                ps.executeBatch();
                                conn.commit();
                                ps.clearParameters();
                                batchCounter = 1;
                            }
                        } else {
                            BootstrapError toAdd = new BootstrapError(lineCounter, line, errors);
                            demoErrorList.add(toAdd);

                        }
                        
                    }
                    lineCounter++;
                }
                ps.executeBatch();
                conn.commit();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (rdr != null) {
                    rdr.close();
                    br.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                ConnectionManager.close(conn, ps);
            }
        }
        //demoErrorList.clear();

    }

    /**
     *
     * process demographics.csv and start to validate the demographics.csv
     *
     * @param filePath is the path of the file to be processed
     */
    public static void updateDemo(String filePath) {
        //clear error list
        demoErrorList.clear();
        demoUpdateSuc.clear();
        //get the demographics in database
        if (demoSuc == null || demoSuc.isEmpty()) {
            demoSuc = UserDAO.retrieveAllLines();
        }
        Connection conn = null;
        String line[] = null;
        PreparedStatement ps = null;
        BufferedReader br = null;
        CSVReader rdr = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                String addquery = "insert into user values(?,?,?,?,?)";
                ps = conn.prepareStatement(addquery);

                int maxBatchCount = 30000; //batch insert counters
                int batchCounter = 1;

                long lineCounter = 2; //line counter
                rdr = new CSVReader(br);
                rdr.readNext();
                while ((line = rdr.readNext()) != null) {
                    if(line.length!=0){
                        ArrayList<String> errors = validateDemographics(line);
                        if (errors == null || errors.isEmpty()) {
                            String macadd = line[0];
                            String name = line[1];
                            String pw = line[2];
                            String gender = line[3];
                            String email = line[4];
                            demoUpdateSuc.add(macadd+","+name+","+pw+","+gender+","+email);
                            
                            ps.setString(1, macadd);
                            ps.setString(2, name);
                            ps.setString(3, pw);
                            ps.setString(4, gender);
                            ps.setString(5, email);
                            ps.addBatch();
                            batchCounter++;
                            if (batchCounter == maxBatchCount) {
                                ps.executeBatch();
                                conn.commit();
                                ps.clearParameters();
                                batchCounter = 1;
                            }
                    } else {
                        BootstrapError toAdd = new BootstrapError(lineCounter, line, errors);
                        demoErrorList.add(toAdd);

                    }
                    
                    } 
                    lineCounter++;
                }
                ps.executeBatch();
                conn.commit();
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (rdr != null) {
                    rdr.close();
                    br.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                ConnectionManager.close(conn, ps);
            }
        }
    }

    /**
     *
     * process locationLookup.csv, delete the locationLookup.csv from database
     * and start to validate the locationLookup.csv
     *
     * @param filePath is the path of the file to be processed
     */
    public static void processLocLookUp(String filePath) {
        //clear data before bootstrap
        LocationLookupDAO.deleteAll();
        locLookUpErrorList.clear();
        loclookupSuc = new HashMap<String, String>();

        BufferedReader br = null;
        CSVReader rdr =null;
        PreparedStatement ps = null;
        Connection conn = null;
        String line[] = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                String addquery = "insert into locationlookup values(?,?)";
                ps = conn.prepareStatement(addquery);

                int maxBatchCount = 30000; //batch insert counters
                int batchCounter = 1;
                long lineCounter = 2;
        
                rdr = new CSVReader(br);
                rdr.readNext();
                while ((line = rdr.readNext()) != null) {
                    if (line.length!=0) {
                        ArrayList<String> errors = validateLocationLookUp(line);
                        if (errors == null || errors.isEmpty()) {
                           
                            ps.setString(1, line[0]);
                            ps.setString(2, line[1]);

                            ps.addBatch();
                            batchCounter++;
                            if (batchCounter == maxBatchCount) {
                                ps.executeBatch();
                                conn.commit();
                                ps.clearParameters();
                                batchCounter = 1;
                            }
                        } else {
                            BootstrapError toAdd = new BootstrapError(lineCounter, line, errors);
                            locLookUpErrorList.add(toAdd);

                        }
                    }
                    lineCounter++;

                }
                ps.executeBatch();
                conn.commit();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (rdr != null) {
                    rdr.close();
                    br.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (conn != null) {
                ConnectionManager.close(conn, ps);
            }
        }
        //locLookUpErrorList.clear();

    }

    /**
     *
     * process location.csv, delete the location.csv from database and start to
     * validate the location.csv
     *
     * @param filePath is the path of the file to be processed
     */
    public static void processLoc(String filePath) {
        //clear data before bootstrap
        LocationDAO.deleteAll();
        locErrorList.clear();
        locSuccess = new HashMap<String, Location>();
        //locErrorMap = new HashMap<String, String>();

        Connection conn = null;
        BufferedReader br = null;
        PreparedStatement ps = null;
        String line[] = null;
        CSVReader rdr = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                String addquery = "insert into location (timeStamp,macAddress,locationID) values(?,?,?)";
                ps = conn.prepareStatement(addquery);

                int maxBatchCount = 30000; //batch insert counters
                int batchCounter = 1;
                long lineCounter = 2;
                
                rdr = new CSVReader(br);
                rdr.readNext();
                while ((line = rdr.readNext()) != null) {
                    if (line.length != 0) {
                        ArrayList<String> valid = validateLocation(line, lineCounter);
                        if (valid != null && !valid.isEmpty()) {
                            BootstrapError toAdd = new BootstrapError(lineCounter, line, valid);
                            locErrorList.add(toAdd);
                        }
                    }
                    lineCounter++;
                }
                Iterator<String> ite = locSuccess.keySet().iterator();
                while (ite.hasNext()) {
                    String currLine = ite.next();
                    Location toAdd = locSuccess.get(currLine);

                    ps.setString(1, toAdd.getTimeStamp());
                    ps.setString(2, toAdd.getMacAddress());
                    ps.setString(3, toAdd.getLocationId());
                    ps.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchCount) {

                        ps.executeBatch();
                        conn.commit();
                        ps.clearParameters();
                        batchCounter = 1;

                    }
                }


                ps.executeBatch();
                conn.commit();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                ConnectionManager.close(conn, ps);
            }
        }
        //locErrorList.clear();

    }

    /**
     *
     * process location.csv, add on the validated data in location.csv
     *
     * @param filePath is the path of the file to be processed
     */
    public static void updateLoc(String filePath) {
        //clear the error message list
        locErrorList.clear();
        locSucUpdate = new HashMap<String, Location>();
        locErrorUpdate = new HashMap<Long, Location>();

        //retrieve data stored inside the data base
        if (loclookupSuc == null || loclookupSuc.isEmpty()) {
            loclookupSuc = LocationLookupDAO.retrieveAllLocationID();

        }
        if (locSuccess == null || locSuccess.isEmpty()) {
            locSuccess = LocationDAO.retrieveAllLines();
        }
        //locErrorMap = new HashMap<String,String>();
        Connection conn = null;

        BufferedReader br = null;
        PreparedStatement ps = null;
        PreparedStatement psD = null;
        String[] line = null;
        CSVReader rdr = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                String addquery = "insert into location (timeStamp,macAddress,locationID) values(?,?,?)";
                String deleteQuery = "delete from location where id = ?";

                ps = conn.prepareStatement(addquery);
                psD = conn.prepareStatement(deleteQuery);
                int maxBatchCount = 30000; //batch insert counters
                int batchCounter = 1;
                long lineCounter = 2;
                
                rdr = new CSVReader(br);
                rdr.readNext();
                
                while ((line = rdr.readNext()) != null) {
                    if(line.length != 0){
                    ArrayList<String> valid = validateLocUpdate(line, lineCounter);
                    if (valid != null && !valid.isEmpty()) {
                        BootstrapError toAdd = new BootstrapError(lineCounter, line, valid);
                        locErrorList.add(toAdd);
                    }
                    
                    }
                    lineCounter++;
                
                }
                Iterator<Long> iteE = locErrorUpdate.keySet().iterator();
                while (iteE.hasNext()) {

                    Long id = iteE.next();
                    System.out.println("DELETE : " + id);
                    psD.setLong(1, id);
                    psD.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchCount) {
                        psD.executeBatch();
                        conn.commit();
                        psD.clearParameters();
                        batchCounter = 1;
                    }
                    psD.executeBatch();
                    conn.commit();
                }
                batchCounter = 1;
                Iterator<String> ite = locSucUpdate.keySet().iterator();
                while (ite.hasNext()) {
                    String currLine = ite.next();
                    Location toAdd = locSucUpdate.get(currLine);

                    ps.setString(1, toAdd.getTimeStamp());
                    ps.setString(2, toAdd.getMacAddress());
                    ps.setString(3, toAdd.getLocationId());
                    ps.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchCount) {

                        ps.executeBatch();
                        conn.commit();
                        ps.clearParameters();
                        batchCounter = 1;

                    }

                }

                ps.executeBatch();
                conn.commit();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BootStrapManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                ConnectionManager.close(conn, ps);
            }
        }
    }

    /**
     *
     * validate the data inside location.csv,which to be add into the database
     *
     * @param currentLine each line inside the location.csv
     * @param lineCounter the id of the location to be added in 
     * @return ArrayList of String to store the error message, if there is no error message return null
     */

    public static ArrayList<String> validateLocUpdate(String[] currentLine, long lineCounter) {
        ArrayList<String> lineErrMsg = new ArrayList<String>();

       
        //split the string into array, delimited by ","
        
            //loop through the String Array, trim the white space and check for empty
        //cell

        for (int i = 0; i < currentLine.length; i++) {
            String field = currentLine[i];
            field = field.trim();
            currentLine[i] = field;

            if (field == null || field.length() == 0) {
                String name = "";
                if (i == 0) {
                    name = "timestamp";
                } else if (i == 1) {
                    name = "mac-address";
                } else if (i == 2) {
                    name = "location-id";
                }

                lineErrMsg.add(name + " is blank");

            }

        }
        //remove the blank rows
        if(!lineErrMsg.isEmpty()){
            return lineErrMsg;
        }
   

        //valide time format
        String timeStamp = currentLine[0];

        if (!validateDate(timeStamp)) {
            lineErrMsg.add("invalid timestamp");
       
        }

        //validate mac-address
        String macAdd = currentLine[1];
        if (!validateMA(macAdd)) {
            lineErrMsg.add("invalid mac-address");
           
        }

        //validate location-id
        String location_id = currentLine[2];
        if (!loclookupSuc.containsKey(location_id)) {
            lineErrMsg.add("invalid location");
          
        }

        //prepare the key to check for duplicates
        String currentLineKey = timeStamp + "," + macAdd;
        //check if currentline has errors
        //and if errors are alread in the hashmap
        //**TO DO CHECK FOR DUPLICATE ENTRIES OF SUCCESS!

        if (!locSuccess.isEmpty() && locSuccess.containsKey(currentLineKey) && lineErrMsg.isEmpty()) {

            //create Bootstrap error
            Location errLine = locSuccess.get(currentLineKey);
            ArrayList<String> prevLineErrors = new ArrayList<String>();
            prevLineErrors.add("duplicate row");
            Long id = errLine.getID();
            BootstrapError bse = new BootstrapError(id, currentLine, prevLineErrors);
            //add bootstrap error to the list
            locErrorList.add(bse);
            locSuccess.remove(currentLineKey);

            //remove prev added row in db
            locErrorUpdate.put(id, errLine);

        } else if (locSucUpdate.containsKey(currentLineKey) && lineErrMsg.isEmpty()) {
            //check if locSucUpdate to add into db has duplicates
            //create bootstrap error
            Location errLine = locSucUpdate.get(currentLineKey);
            ArrayList<String> prevLineErrors = new ArrayList<String>();
            prevLineErrors.add("duplicate row");
            BootstrapError bse = new BootstrapError(errLine.getID(), currentLine, prevLineErrors);

            //add bootstrap error to list
            locErrorList.add(bse);

        }
        //sucessful
        if (!lineErrMsg.isEmpty()) {
            //locErrorMap.put(currentLine, "");
            
            return lineErrMsg;
        } else {

            Location toAdd = new Location(lineCounter, timeStamp, macAdd, location_id);

            locSucUpdate.put(currentLineKey, toAdd);
            return null;
        }

    }

    /**
     *
     * validate the data inside location.csv 
     *
     * @param currentLine each line inside the location.csv
     * @param lineCounter the id of the location to be added in 
     * @return ArrayList of String which stores the error message, if there is no error message return null
     */
    public static ArrayList<String> validateLocation(String[] currentLine, long lineCounter) {
        //TO DO add the arraylist<String[]>

//		ArrayList<String> errorMsg = new ArrayList<String[]>();
//        for (String currentLine : location) {
        ArrayList<String> lineErrMsg = new ArrayList<String>();

        //split the string into array, delimited by ","
//        String[] toValidate = currentLine.split(",");
        //loop through the String Array, trim the white space and check for empty
        //cell
        for (int i = 0; i < currentLine.length; i++) {
            String field = currentLine[i];
            field = field.trim();
            currentLine[i] = field;

            if (field == null || field.length() == 0) {
                String name = "";
                if (i == 0) {
                    name = "timestamp";
                } else if (i == 1) {
                    name = "mac-address";
                } else if (i == 2) {
                    name = "location-id";
                }

                lineErrMsg.add(name + " is blank");

            }

        }
        //remove the blank rows
        if(!lineErrMsg.isEmpty()){
            return lineErrMsg;
        }
        //check if the fileds has more commas than normal
       
        
        //valide time format
        String timeStamp = currentLine[0];

        if (!validateDate(timeStamp)) {
            lineErrMsg.add("invalid timestamp");
        
        }

        //validate mac-address
        String macAdd = currentLine[1];
        if (!validateMA(macAdd)) {
            lineErrMsg.add("invalid mac-address");
           
        }

        //validate location-id
        String location_id = currentLine[2];
        if (!loclookupSuc.containsKey(location_id)) {
            lineErrMsg.add("invalid location");
        
        }

        //prepare the key to check for duplicates
        String currentLineKey = timeStamp + "," + macAdd;
        //check if currentline has errors
        //and if errors are alread in the hashmap
        //**TO DO CHECK FOR DUPLICATE ENTRIES OF SUCCESS!
//        if (locErrorMap.containsKey(currentLineKey)) {
//            lineErrMsg.add("duplicate row");
//        } else 
        if (locSuccess.containsKey(currentLineKey) && lineErrMsg.isEmpty()) {
            //add it to the error message

            //create Bootstrap error
            Location errLine = locSuccess.get(currentLineKey);
            ArrayList<String> prevLineErrors = new ArrayList<String>();
            prevLineErrors.add("duplicate row");
            BootstrapError bse = new BootstrapError(errLine.getID(), currentLine, prevLineErrors);
            //add bootstrap error to the list
            locErrorList.add(bse);
            //remove previous "duplicated" line in db
//            String locID= errLine.getLocationId();
//            LocationDAO.delete(timeStamp, macAdd, locID);

        }
        //sucessful
        if (!lineErrMsg.isEmpty()) {
            //locErrorMap.put(currentLine, "");
            
            return lineErrMsg;
        } else {
            Location toAdd = new Location(lineCounter, timeStamp, macAdd, location_id);
            locSuccess.put(currentLineKey, toAdd);
            return null;
        }

    }

    /**
     *
     * validate the data inside locationLookup.csv
     *
     * @param currentLine each line inside the locationLookup.csv
     * @return ArrayList of String which stores the error message, if there is no error message return null
     */
    public static ArrayList<String> validateLocationLookUp(String[] currentLine) {
        //create new error message list
        ArrayList<String> lineErrMsg = new ArrayList<String>();
        //separate string into array with "," as a delimiter
       
        String locationId = null;
        String semanticPlace = null;
        //remove white space and check if field is blank
        for (int i = 0; i < currentLine.length; i++) {
            String field = currentLine[i];
            field = field.trim();
            currentLine[i] = field;

            if (field == null || field.length() == 0) {
                String name = "";
                if (i == 0) {
                    name = "location-id";
                } else if (i == 1) {
                    name = "semantic-place";
                }

                lineErrMsg.add(name + " is blank");

            }
        }
        //remove the blank rows
        if(!lineErrMsg.isEmpty()){
            return lineErrMsg;
        }
         //check if the fileds has more commas than normal
 
        locationId = currentLine[0];
        
        if (!validateLID(locationId)) {
            lineErrMsg.add("invalid location id");
           
        }
        semanticPlace = currentLine[1];
        
        if (!validateSemanticPlace(semanticPlace)) {
            lineErrMsg.add("invalid semantic place");
           
        }
        //check if errorList is empty
        if (!lineErrMsg.isEmpty()) {
            //locLookUpErrorMap.put(currentLine, lineErrMsg);
            
            return lineErrMsg;
        } else {
            loclookupSuc.put(locationId, "");
            return null;
            
        }

    }

    /**
     *
     * validate the data inside demographics.csv
     * 
     * @param currentLine each line inside the demographics.csv
     * @return ArrayList of String which stores the error message, if there is no error message return null
     */
    public static ArrayList<String> validateDemographics(String[] currentLine) {

        ArrayList<String> lineErrMsg = new ArrayList<String>();

        //split the string into array, delimited by ","
       
        //loop through the String Array, trim the white space and check for empty
        //cell
        for (int i = 0; i < currentLine.length; i++) {
            String field = currentLine[i];
            field = field.trim();
            currentLine[i] = field;

            if (field == null || field.length() == 0) {
                String name = "";
                if (i == 0) {
                    name = "mac-address";
                } else if (i == 1) {
                    name = "name";
                } else if (i == 2) {
                    name = "password";
                } else if (i == 3) {
                    name = "email";
                } else if (i == 4) {
                    name = "gender";
                }

                lineErrMsg.add(name + " is blank");

            }
        }
        //remove the blank rows
        if(!lineErrMsg.isEmpty()){
            return lineErrMsg;
        }
         //check if the fileds has more commas than normal
      
        //validate macaddress
        String macAdd = currentLine[0];
        if (!validateMA(macAdd)) {
            lineErrMsg.add("invalid mac address");
            

        }
        //validate pw
        String password = currentLine[2];
        if (password.indexOf(" ") != -1 || password.length() < 8) {
            lineErrMsg.add("invalid password");
          
        }
//        validate email
        String email = currentLine[3];
        if (!validateEmail(email)) {
            lineErrMsg.add("invalid email");
           
        }
        //validate gender
        String gender = currentLine[4];

        if (!(gender.equalsIgnoreCase("F") || gender.equalsIgnoreCase("M"))) {
            lineErrMsg.add("invalid gender");
           
        }
        String keyLine = macAdd+","+currentLine[1]+","+password+","+email+","+gender;

        if (demoSuc.containsKey(keyLine)) {
            lineErrMsg.add("duplicate row");
        }
        if (!lineErrMsg.isEmpty()) {
            //demoErrorMap.put(currentLine, lineErrMsg);
            
            return lineErrMsg;
        } else {
            demoSuc.put(keyLine, "");
            return null;
        }
    }

    /**
     *
     * process the validation of Date, check the date format
     *
     * @param timeStamp the time to be validated
     * @return an boolean value to show the results
     */
    public static boolean validateDate(String timeStamp) {
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        boolean toReturn = false;
        //whether date time interpretation is leniet
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(timeStamp);
            if(datePattern.length() == timeStamp.length()){
                toReturn = true;
            }
            
        } catch (ParseException ex) {
            return false;
        }

        return toReturn;
    }

    /**
     *
     * process the validation of mac-address, check whether it is a SHA-1 hash
     * value(a hexadecimal number, 40 digits long)
     *
     * @param macAdd the mac-address to be validated
     * @return an boolean value to show the results
     */
    public static boolean validateMA(String macAdd) {
        return (!(macAdd == null || macAdd.length() == 0 || macAdd.length() != 40 || !macAdd.matches("[0-9A-Fa-f]+$")));
    }

    /**
     *
     * process the validation of locationId, check whether the location id is a
     * positive long value
     *
     * @param locationId the location id to be validated
     * @return an boolean value to show the results
     */
    public static boolean validateLID(String locationId) {
        boolean toReturn = false;
        try {

            long locID = Long.parseLong(locationId);
            if (locID <= 0) {
                return toReturn;
            }
            toReturn = true;

        } catch (NumberFormatException nfe) {
            return false;
        }
        return toReturn;
    }

    /**
     *
     * process the validation of email, check the format
     * xxx.<year>@<school>.smu.edu.sg where school is either business,
     * accountancy, sis, economics, law, or socsc, and year is between 2010
     * (inclusive) to 2014 (inclusive). xxx should contains only letters (a-z or
     * A-Z), numbers or dot.
     *
     * @param email the email to be validated
     * @return an boolean value to show the results
     */
    public static boolean validateEmail(String email) {
        //xxx.<year>@<school>.smu.edu.sg
        int AT = email.indexOf("@");
        boolean emailIsValid = true;
        String name;
        //extract year
        try {
            if (AT != -1) {
                String year = email.substring(AT - 4, AT);
                //extract name
                name = email.substring(0, AT - 4);

                String afterAT = email.substring(AT + 1, email.length());
                int smuedu = afterAT.indexOf(".");

                //extract school
                String school = afterAT.substring(0, smuedu);
                //extract ".smu.edu.sg"
                String lastpart = afterAT.substring(smuedu, afterAT.length());

                //check name
                for (int i = 0; i < name.length(); i++) {
                    char nameChar = name.charAt(i);
                    if (!(Character.isLetterOrDigit(nameChar) || nameChar == '.')) {
                        emailIsValid = false;
                    }
                }

                //check year -works
                if (!(year.equals("2010") || year.equals("2011") || year.equals("2012") || year.equals("2013")
                        || year.equals("2014"))) {
                    emailIsValid = false;
                }
                //check school -works
                String[] arrSch = {"business", "accountancy", "sis", "economics", "law", "socsc"};
                int count = 0;

                for (String sch : arrSch) {
                    if (school.equals(sch)) {
                        count++;
                    }
                }
                if (count != 1) {
                    emailIsValid = false;
                }
                //works
                if (!lastpart.equals(".smu.edu.sg")) {
                    emailIsValid = false;
                }

            } else {
                emailIsValid = false;
            }
        } catch (StringIndexOutOfBoundsException e) {
            emailIsValid = false;
        }
        return emailIsValid;

    }

    /**
     *
     * process the validation of Semantic Place, check the format "SMUSISL" or
     * "SMUSISB" and the subsequent number
     *
     * @param String the school
     * @return an boolean value to show the results
     */
    public static boolean validateSemanticPlace(String school) {
        
        if (school.indexOf("SMUSISL") != -1 || school.indexOf("SMUSISB") != -1) {
            
            String s = school.charAt(7) + "";
            
            try {
                int num = Integer.parseInt(s);
                
            } catch (NumberFormatException nfe) {
                return false;
            }

            return true;
        }
        return false;
    }

}

   // }

