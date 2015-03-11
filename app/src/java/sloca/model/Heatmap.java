/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * @author g3t2
 */
package sloca.model;

import java.util.Date;

/**
 *
 * to get details of Heatmap
 */
public class Heatmap {

    private String semanticPlace;
    private String dateTime;
    private String macAdd;

    /**
     *
     * Constructs a Heatmap object, which has a semantic place, date time and
     * mac-address
     *
     */
    public Heatmap(String semanticPlace, String dateTime, String macAdd) {
        this.semanticPlace = semanticPlace;
        this.dateTime = dateTime;
        this.macAdd = macAdd;
    }

    /**
     *
     * to get the semantic place
     *
     * @return String of the semantic place
     */
    public String getSemanticPlace() {
        return semanticPlace;
    }

    /**
     *
     * to get the date and time
     *
     * @return String of the date and time
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     *
     * to get the mac-address
     *
     * @return String of the mac-address
     */
    public String getMacAdd() {
        return macAdd;
    }

}
