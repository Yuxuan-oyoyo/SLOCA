/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.model;

import java.util.ArrayList;

/**
 *
 * @author g3t2
 */
public class BootstrapError implements Comparable<BootstrapError> {

    private long lineNum;
    private String[] line;
    private ArrayList<String> errMsg;

    /**
     *
     * Constructs a BootstrapError object, which has a line number, a string
     * array of the line and an array of error message
     *
     */
    public BootstrapError(long lineNum, String[] line, ArrayList<String> errMsg) {
        this.lineNum = lineNum;
        this.line = line;
        this.errMsg = errMsg;
    }

    /**
     *
     * to get the line number
     *
     * @return a long value
     */
    public long getLineNum() {
        return lineNum;
    }

    /**
     *
     * to get the line
     *
     * @return a String array value
     */
    public String[] getLine() {
        return line;
    }

    /**
     *
     * to get the error message
     *
     * @return an ArrayList of String
     */
    public ArrayList<String> getErrMsg() {
        return errMsg;
    }

    /**
     *
     * to set the line number
     *
     * @param lineNum the line number to be set
     */
    public void setLineNum(long lineNum) {
        this.lineNum = lineNum;
    }

    /**
     *
     * to set the line
     *
     * @param line the string array to be set
     */
    public void setLine(String[] line) {
        this.line = line;
    }

    /**
     *
     * to set the error message
     *
     * @param errMsg is the arraylist of error message to be set
     */
    public void setErrMsg(ArrayList<String> errMsg) {
        this.errMsg = errMsg;
    }

    /**
     *
     * to compare with another BootstrapError object
     *
     * @param bse the another BootstrapError object to be compared with
     * @return 1 if another BootstrapError object has less line number, -1 if
     * another BootstrapError object has more line number, 0 if they have the
     * same number of lines
     */
    public int compareTo(BootstrapError bse) {
        if (lineNum - bse.getLineNum() > 0) {
            return 1;
        } else if (lineNum == bse.getLineNum()) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     *
     * to print the BootstrapError object
     *
     * @return a String statement
     */
    public String toString() {
        return lineNum + "," + line + "," + errMsg;
    }
}
