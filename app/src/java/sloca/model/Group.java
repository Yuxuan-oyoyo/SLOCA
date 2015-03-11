package sloca.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author g3t2
 */
public class Group implements Comparable<Group> {

    ArrayList<String> users;
    HashMap<String, ArrayList<Timestamp>> locations;

    /**
     *
     * Constructs a Group object, which has a list of users and a list of
     * location records
     *
     */
    public Group(ArrayList<String> users, HashMap<String, ArrayList<Timestamp>> locations) {
        this.users = users;
        this.locations = locations;

    }

    /**
     *
     * to add User
     *
     * @param mac the user's mac-address
     */
    public void addUser(String mac) {
        users.add(mac);
    }

    /**
     *
     * to check whether another group belongs to this group's sub-group
     *
     * @param g the group to be checked whether is a sub-group of this group
     * @return a boolean value
     */
    public boolean subGroup(Group g) {
        ArrayList<String> largerG = this.users;
        ArrayList<String> smallerG = g.getUsers();

        int i = 0;
        for (String u2 : largerG) {
            for (String u1 : smallerG) {
                if (u1.equals(u2)) {
                    i++;
                }
            }
        }
        return i == largerG.size() && i != smallerG.size();
    }

    public boolean sameGroup(Group g) {
        ArrayList<String> largerG = this.users;
        ArrayList<String> smallerG = g.getUsers();

        int i = 0;
        for (String u2 : largerG) {
            for (String u1 : smallerG) {
                if (u1.equals(u2)) {
                    i++;
                }
            }
        }
        return i == largerG.size() && i == smallerG.size();
    }

    /**
     *
     * to check whether the group is leaded by the specific user
     *
     * @param mac the mac-address of the specific user
     * @return a boolean value
     */
    public boolean leadByUser(String mac) {
        return users.get(0).equals(mac);
    }

    /**
     *
     * to get the group's time line
     *
     * @return HashMap which key is location ID, value is timeStamp
     */
    public HashMap<String, ArrayList<Timestamp>> getTimeLine() {
        return locations;
    }

    /**
     *
     * to set the group's time line
     *
     * @param newTimeLine the new time line to set to this group
     */
    public void setTimeLine(HashMap<String, ArrayList<Timestamp>> newTimeLine) {
        locations = newTimeLine;
    }

    /**
     *
     * to get the users within this group
     *
     * @return String ArrayList contains the user(s) within this group
     */
    public ArrayList<String> getUsers() {
        return users;
    }

    /**
     *
     * to get the number of users within this group
     *
     * @return the number of users in this group
     */
    public int getNumUsers() {
        return users.size();
    }

    /**
     *
     * to get the last location of this group
     *
     * @return the location ID of the last location of this group
     */
    public String getLastLocation() {
        String lastLoc = "";
        Timestamp tempTime = new Timestamp(0);
        Iterator<String> iter = locations.keySet().iterator();
        while (iter.hasNext()) {
            String locationID = iter.next();
            ArrayList<Timestamp> timeLine = locations.get(locationID);
            int timeSize = timeLine.size();
            Timestamp time = timeLine.get(timeSize - 1);
            if (time.after(tempTime)) {
                tempTime = time;
                lastLoc = locationID;
            }
        }
        return lastLoc;
    }

    /**
     *
     * to calculate the total time of the group
     *
     * @return the total time of the group has been formed in second
     */
    public int computeTotalTime() {
        Iterator<String> ite = locations.keySet().iterator();
        int totalTime = 0;
        while (ite.hasNext()) {
            String location = ite.next();
            //get arraylist of timestamps
            ArrayList<Timestamp> tsList = locations.get(location);
            for (int i = 0; i < tsList.size(); i += 2) {
                Timestamp ts1 = tsList.get(i);
                Timestamp ts2 = tsList.get(i + 1);
                totalTime += (ts2.getTime() - ts1.getTime()) / 1000;
            }
        }
        return totalTime;
    }

    /**
     *
     * to compare this group with another group
     *
     * @param g is another group to compare with this group
     * @return the time difference between the groups if they have the same
     * number of users, otherwise it returns the difference of number of users
     */
    public int compareTo(Group g) {
        if (users.size() == g.getNumUsers()) {
            return g.computeTotalTime() - computeTotalTime();
        } else {
            return g.getNumUsers() - users.size();
        }
    }

    /**
     *
     * to get the set of locations where this group has gone to
     *
     * @return HashMap of the records of locations, with key is location ID,
     * value is timeStamp
     */
    public HashMap<String, ArrayList<Timestamp>> getLocations() {
        return locations;
    }

}
