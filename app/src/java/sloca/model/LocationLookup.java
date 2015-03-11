/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

public class LocationLookup {

    private String location_id;
    private String semantic_place;

    /**
     *
     * Constructs a LocationLookup object, which has a location ID and a
     * semantic place
     *
     */
    public LocationLookup(String location_id, String semantic_place) {
        this.location_id = location_id;
        this.semantic_place = semantic_place;
    }

    /**
     *
     * to get the Location ID
     *
     * @return Location ID
     */
    public String getLocationId() {
        return location_id;
    }

    /**
     *
     * to get the semantic place
     *
     * @return the semantic place
     */
    public String getSemanticPlace() {
        return semantic_place;
    }
}
