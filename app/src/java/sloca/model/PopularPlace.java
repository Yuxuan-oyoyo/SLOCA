/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

/**
 *
 * @author admin
 */
public class PopularPlace implements Comparable<PopularPlace> {

    private int rank;
    private String semPlace;
    private int count;

    /**
     *
     * Constructs a PopularPlace object, which has a rank, semanticplace and
     * count
     *
     */
    public PopularPlace(int rank, String semPlace, int count) {
        this.rank = rank;
        this.semPlace = semPlace;
        this.count = count;
    }

    /**
     *
     * to get the rank
     *
     * @return int of the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     *
     * to get the semantic place
     *
     * @return String of the semantic place
     */
    public String getSemPlace() {
        return semPlace;
    }

    /**
     *
     * to get the count
     *
     * @return int of the count
     */
    public int getCount() {
        return count;
    }

    /**
     *
     * to compare this PopularPlace with another PopularPlace
     *
     * @param pp is another PopularPlace object to compare with this group
     * @return String sorted alphabetically if rank is the same, otherwise it
     * returns the difference of rank
     */
    public int compareTo(PopularPlace pp) {
        if (rank == pp.getRank()) {
            return semPlace.compareTo(pp.getSemPlace());
        } else {
            return rank - pp.getRank();
        }
    }

}
