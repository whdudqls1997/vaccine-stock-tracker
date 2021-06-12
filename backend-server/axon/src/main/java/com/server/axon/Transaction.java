package com.server.axon;

import java.util.Arrays;
import java.awt.geom.Point2D;
import org.apache.wink.json4j.*;

/*
* A class representing a single transaction.
* Holds all required information, which will be used to filter & return data.
* Constructed from data collected through DataStructure class.
*
* @author   Young Bin Cho, XY Lim
* @version  1.0
*
* @see      DataStructure.java
* @see      ServerFunctions.java
*/
public class Transaction {

    // Transaction Information
    private int num; // change in number of this transaction
    private String reason; // reason for this transaction
    private String transactionId; // transaction ID in rowId format

    // Vaccine Information
    private String batchId; // batchId in String format
    private String vaccineId; // vaccineId in rowId format
    private String vaccineName; // name of vaccine used in this transaction

    // Location Information
    private String facility;
    private String district;
    private Point2D location; // location of this transaction

    // Date Information
    private int[] startDate; // start date of this transaction
    private int[] endDate; // end date of this transaction

    /*
    * Default constructor. Initializes the fields.
    *
    * @param    Values to set the fields to
    */
    public Transaction(int num, String reason, String transactionId,
                       String batchId, String vaccineId, String vaccineName,
                       String facility, String district, Point2D location,
                       int[] startDate, int[] endDate){
        this.num = num;
        this.reason = reason;
        this.transactionId = transactionId;

        this.batchId = batchId;
        this.vaccineId = vaccineId;
        this.vaccineName = vaccineName;

        this.facility = facility;
        this.district = district;
        this.location = location;

        this.startDate = startDate;
        this.endDate = endDate;
    }

    /*
    * Returns the requested field, based on string input.
    *
    * @param    given   String that dictates the field to return
    * @return           Data requested
    */
    public Object get(String given){
        if(given.equals("num"))
            return this.num;
        if(given.equals("reason"))
            return this.reason;
        if(given.equals("transactionId"))
            return this.transactionId;

        if(given.equals("batchId"))
            return this.batchId;
        if(given.equals("vaccineId"))
            return this.vaccineId;
        if(given.equals("vaccineName"))
            return this.vaccineName;

        if(given.equals("facility"))
            return this.facility;
        if(given.equals("district"))
            return this.district;
        if(given.equals("location"))
            return this.location;

        if(given.equals("startDate"))
            return this.startDate;
        if(given.equals("endDate"))
            return this.endDate;

        return null;
    }

    /*
    * Returns a JSONObject in custom format.
    * Used in JSON exportation of this object.
    *
    * @return   Information about this class in JSONObject format
    */
    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        String date = endDate[0] + "-" + endDate[1] + "-" + endDate[2];

        result.put("facility", facility);
        result.put("date", date);
        result.put("number", num);
        result.put("reason", reason);
        result.put("vaccine", vaccineName);

        return result;
    }

    /*
    * Default generated toString() method. Used for debugging.
    *
    * @return   This class in string format
    */
    @Override
    public String toString() {
        return "Transaction{" +
                "num=" + num +
                ", reason='" + reason + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", batchId='" + batchId + '\'' +
                ", vaccineId='" + vaccineId + '\'' +
                ", vaccineName='" + vaccineName + '\'' +
                ", facility='" + facility + '\'' +
                ", district='" + district + '\'' +
                ", location=" + location +
                ", startDate=" + Arrays.toString(startDate) +
                ", endDate=" + Arrays.toString(endDate) +
                '}';
    }
}