/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

/**
 *
 * @author g3t2
 */
public class User {

    private String macAddress;
    private String name;
    private String password;
    private String email;
    private String gender;

    /**
     *
     * Constructs a User object, which has a mac-address, name, password, email
     * and gender
     *
     */
    public User(String macAddress, String name, String password, String email, String gender) {
        this.macAddress = macAddress;
        this.name = name;
        this.password = password;
        this.email = email;
        this.gender = gender;
    }

    /**
     * Get name
     *
     * @return name - full name of student
     */
    public String getName() {
        return name;
    }

    /**
     *
     * Get password
     *
     * @return password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get mac-address
     *
     * @return macAddress - the (hashed) MAC address indicating the unique id of
     * a user's device
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Get email
     *
     * @return email - email of student (2010 onwards)
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * Get gender
     *
     * @return gender - gender of user
     */
    public String getGender() {
        return gender;
    }

    /**
     * Authenticate if password is correct
     *
     * @param password password to authenticate against this student
     * @return a boolean value
     */
    public boolean authenticate(String password) {
        return password.equals(this.password);
    }

}
