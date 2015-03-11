/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author g3t2
 */
public class SharedSecretManager {

    private static final String PROPS_FILENAME = "/sharedSecret.properties";
    private static String user;
    private static String admin;

    static {
        try {
            // Retrieve properties from connection.properties via the CLASSPATH
            // WEB-INF/classes is on the CLASSPATH
            InputStream is = SharedSecretManager.class.getResourceAsStream(PROPS_FILENAME);
            Properties props = new Properties();
            props.load(is);

            // load database connection details
                /*host = props.getProperty("db.host").trim();
             String port = props.getProperty("db.port").trim();
             String dbName = props.getProperty("db.name").trim();*/
            user = props.getProperty("ss.user").trim();
            admin = props.getProperty("ss.admin").trim();

        } catch (Exception ex) {
            // unable to load properties file
            String message = "Unable to load '" + PROPS_FILENAME + "'.";
            System.out.println(message);
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);

        }
    }

    /**
     * get the admin who shared the secret key
     *
     * @return String statement
     */
    public static String getSharedSecretKeyAdmin() {
        return admin;
    }

    /**
     * get the user who shared the secret key
     *
     * @return String statement
     */
    public static String getSharedSecretKeyUser() {
        return user;
    }
}
