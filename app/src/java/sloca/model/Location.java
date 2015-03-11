/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

public class Location {

    private long id;
    private String macAddress;
    private String timeStamp;
    private String location_id;

    /**
     *
     * Constructs a Location object, which has an id, timeStamp, mac-address and
     * location ID
     *
     */
    public Location(Long id, String timeStamp, String macAddress, String location_id) {
        this.id = id;
        this.macAddress = macAddress;
        this.timeStamp = timeStamp;
        this.location_id = location_id;
    }

    /**
     *
     * to get the mac-address
     *
     * @return macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *
     * to get the timeStamp
     *
     * @return timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     *
     * to get the locationId
     *
     * @return locationId
     */
    public String getLocationId() {
        return location_id;
    }

    /**
     *
     * to get the ID
     *
     * @return ID
     */
    public long getID() {
        return id;
    }

    /**
     *
     * to print the Location object
     *
     * @return a String statement
     */
    public String toString() {
        return id + "," + timeStamp + "," + macAddress + "," + location_id;
    }
}
