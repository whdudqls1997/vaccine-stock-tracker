package com.server.axon;

import java.net.*;
import java.util.*;
import java.awt.geom.Point2D;
import org.apache.wink.json4j.*;
import org.opendatakit.sync.client.SyncClient;

/*
* A class to perform communication with the ODK-X server.
* Establishes connection & reads from the ODK-X server.
* Also formats the read data in correct form to create a Transaction object.
*
* @author   Young Bin Cho, XY Lim
* @version  1.0
*
* @see      Transaction.java
* @see      DataStructure.java
*/
public class ServerFunctions {

    private final String agg_url; // aggregated url of target
    private final String userName; // username for authentication
    private final String password; // password for authentication

    private SyncClient sc; // sync client from ODK-X : https://github.com/odk-x/sync-client

    private final String host; // agg_url in host format
    private static final String APP_ID = "odktables/default"; // default location for tables

    /*
    * Default constructor. Initializes the fields.
    * For aggregated URL, the constructor will make it into host format.
    *
    * @param    Values to set the fields to
    */
    public ServerFunctions(String agg_url, String userName, String password) throws MalformedURLException {
        this.agg_url = agg_url;
        this.userName = userName;
        this.password = password;
        
        URL url = new URL(agg_url);
        this.host = url.getHost();
    }

    /*
    * Creates new SyncClient with set fields.
    * Must be called before any other functions.
    */
    public void createNewSyncClient() {
        sc = new SyncClient();
        sc.init(host, userName, password);
    }

    /*
    * Reads data from ODK-X server & loads data into a DataStructure object.
    *
    * @see      DataStructure.populate()
    *
    * @return   DataStructure object created with read data
    */
    public DataStructure loadData() throws MalformedURLException, JSONException {
        DataStructure ds = new DataStructure();

        // Specified rows for our implementation
        JSONObject rowsTrans = this.getRows("stock_transactions");
        JSONObject rowsFac = this.getRows("facilities");
        JSONObject rowsVac = this.getRows("vaccine_types");

        ArrayList<Transaction> temp = this.extract(rowsTrans, rowsFac, rowsVac);

        // Add each row read into DataStructure
        for(Transaction t : temp)
            ds.addTransaction(t);

        ds.populate(); // populates some fields in DataStructure

        return ds;
    }

    /*
    * Returns the SchemaETag for given table name.
    * Used for reading tables, thus for reading rows.
    * Helper-method for this.getRows()
    *
    * @see                  this.getRows(String tableName)
    *
    * @param    tableName   Name of the table to fetch the SchemaETag
    * @return               SchemaETag of target table
    */
    private String getSchemaETagTable(String tableName){
        return sc.getSchemaETagForTable(agg_url, APP_ID, tableName);
    }

    /*
    * Returns the JSONObject representing the "Rows" for given table name.
    *
    * @param    tableName   Name of the table to fetch rows from
    * @return               Rows of target table in JSONObject format
    */
    public JSONObject getRows(String tableName){
        return sc.getRows(agg_url, APP_ID, tableName, getSchemaETagTable(tableName), null, null);
    }

    /*
    * Reads the ODK-X database and creates Transaction objects from the data.
    * Custom method for our implementation of database.
    *
    * @see                      DataStructure.java
    *
    * @param    transaction     Rows of "stock_transaction" table
    * @param    facility        Rows of "facilities" table
    * @param    vaccine         Rows of "vaccine_type" table
    * @return                   ArrayList of Transaction Objects, created from read data
    */
    public ArrayList<Transaction> extract(JSONObject transaction, JSONObject facility, JSONObject vaccine) throws JSONException {
        ArrayList<Transaction> result = new ArrayList<>();

        // Opening first encapsulation for given rows
        JSONArray rowsTransaction = transaction.getJSONArray("rows");
        JSONArray rowsFacility = facility.getJSONArray("rows");
        JSONArray rowsVaccine = vaccine.getJSONArray("rows");

        // facility_id : [facility name (String), location (Point2D), district (String)]
        Map<String, Object[]> facilityMapped = mapFacility(rowsFacility);

        // vaccine_id : vaccine name (String)
        Map<String, String> vaccineMapped = mapVaccine(rowsVaccine);

        // Reading data from rows. If needed data isn't in the stock_transaction table, read from the mapped ones.
        for(int i = 0; i < rowsTransaction.size(); ++i){
            JSONArray rowsTransactionBeta = rowsTransaction.getJSONObject(i).getJSONArray("orderedColumns");

            String num = "";
            String reason = "";
            String transactionId = rowsTransaction.getJSONObject(i).getString("id");

            String batchId = "";
            String vaccineId = "";
            String vaccineName = "";

            String fac = "";
            String district = "";
            Point2D location = new Point2D.Double();

            String startDate = "";
            String endDate = "";

            for(int j = 0; j < rowsTransactionBeta.size(); ++j){
                JSONObject data = rowsTransactionBeta.getJSONObject(j);
                if(data.getString("column").equals("number"))
                    num = data.getString("value");
                if(data.getString("column").equals("reason"))
                    reason = data.getString("value");
                if(data.getString("column").equals("batch_id"))
                    batchId = data.getString("value");
                if(data.getString("column").equals("vaccine_id")){
                    vaccineId = data.getString("value");
                    vaccineName = vaccineMapped.get(data.getString("value"));
                }
                if(data.getString("column").equals("facility_id")){
                    fac = (String) facilityMapped.get(data.getString("value"))[0];
                    district = (String) facilityMapped.get(data.getString("value"))[2];
                    location = (Point2D) facilityMapped.get(data.getString("value"))[1];
                }
                if(data.getString("column").equals("start_date"))
                    startDate = data.getString("value");
                if(data.getString("column").equals("end_date"))
                    endDate = data.getString("value");
            }

            // Create Transaction object from data and add to returning object
            result.add(new Transaction(Integer.parseInt(num), reason, transactionId,
                    batchId, vaccineId, vaccineName,
                    fac, district, location,
                    formatDateTime(startDate), formatDateTime(endDate)));
        }

        return result;
    }

    /*
    * Maps the information in "facilities" table.
    * Helper method for this.extract().
    *
    * @see              this.extract(JSONObject transaction, JSONObject facility, JSONObject vaccine)
    *
    * @param    rows    Rows to read the facility information off of
    * @return           Map that maps the ID of facility to it's name, location, and district
    */
    private Map<String, Object[]> mapFacility(JSONArray rows) throws JSONException {
        Map<String, Object[]> result = new HashMap<>();

        for(int i = 0; i < rows.size(); ++i){

            String rowId = rows.getJSONObject(i).getString("id");
            JSONArray rowsBeta = rows.getJSONObject(i).getJSONArray("orderedColumns");

            Object[] tempArr = new Object[3]; // [facility name (String), location (Point2D), district (String)]
            double tempX = 0.0;
            double tempY = 0.0;

            for(int j = 0; j < rowsBeta.size(); ++j){
                JSONObject data = rowsBeta.getJSONObject(j);
                if(data.getString("column").equals("district_id"))
                    tempArr[2] = data.getString("value");
                if(data.getString("column").equals("facility_name"))
                    tempArr[0] = data.getString("value");
                if(data.getString("column").equals("facility_location_latitude"))
                    tempX = Double.parseDouble(data.getString("value"));
                if(data.getString("column").equals("facility_location_longitude"))
                    tempY = Double.parseDouble(data.getString("value"));
            }

            tempArr[1] = new Point2D.Double(tempX, tempY);

            result.put(rowId, tempArr);
        }

        return result;
    }

    /*
    * Maps the information in "vaccine_type" table.
    * Helper method for this.extract().
    *
    * @see              this.extract(JSONObject transaction, JSONObject facility, JSONObject vaccine)
    *
    * @param    rows    Rows to read the vaccine information off of
    * @return           Map that maps the ID of vaccine to it's name
    */
    private Map<String, String> mapVaccine(JSONArray rows) throws JSONException {
        Map<String, String> result = new HashMap<>();

        for(int i = 0; i < rows.size(); ++i){

            String rowId = rows.getJSONObject(i).getString("id");
            String vaccineName = "";

            JSONArray rowsBeta = rows.getJSONObject(i).getJSONArray("orderedColumns");

            for(int j = 0; j < rowsBeta.size(); ++j){
                JSONObject data = rowsBeta.getJSONObject(j);
                if(data.getString("column").equals("vaccine_name"))
                    vaccineName = data.getString("value");
            }

            result.put(rowId, vaccineName);
        }

        return result;
    }

    /*
    * Formats the string representing date&time.
    * Helper method for this.extract()
    *
    * @see              this.extract(JSONObject transaction, JSONObject facility, JSONObject vaccine)
    *
    * @param    input   String to format
    * @return           Integer array that holds : [year, month, day] ; [2021, 05, 31]
    */
    private int[] formatDateTime(String input){
        int[] result = new int[3];

        result[0] = Integer.parseInt(input.substring(0, 4)); // year
        result[1] = Integer.parseInt(input.substring(5, 7)); // month
        result[2] = Integer.parseInt(input.substring(8, 10)); // day

        return result;
    }
}
