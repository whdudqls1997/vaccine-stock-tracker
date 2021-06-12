package com.server.axon;

import java.util.*;
import java.awt.geom.Point2D;
import org.apache.wink.json4j.*;

/*
* A class representing our implementation of database.
* Holds given transactions and filters them in various ways.
*
* @author   Young Bin Cho, XY Lim
* @version  2.0
*
* @see      Transaction.java
* @see      ServerFunctions.java
*/
public class DataStructure{

    private ArrayList<Transaction> allTransactions; // all Transaction objects read from ODK-X database
    private ArrayList<Transaction> filtered; // Transaction objects that is malformed
    private Set<String> allDistricts; // name of all Districts read from ODK-X database
    private Set<String> allFacilities; // name of all Facilities read from ODK-X database
    private Set<String> allVaccines; // name of all Vaccines read from ODK-X database

    // Transaction objects with "reason" field matching these should have positive "num" field
    private static final ArrayList<String> increased = new ArrayList<String>(
            Arrays.asList("received")
    );

    // Transaction objects with "reason" field matching these should have negative "num" field
    private static final ArrayList<String> decreased = new ArrayList<String>(
            Arrays.asList("spoiled", "sent", "administered")
    );

    /*
    * Default constructor. Initializes the fields.
    */
    public DataStructure(){
        allTransactions = new ArrayList<>();
        filtered = new ArrayList<>();
        allDistricts = new HashSet<>();
        allFacilities = new HashSet<>();
        allVaccines = new HashSet<>();
    }

    /*
    * Getter methods for this class.
    *
    * @return   this.allTransactions & this.filtered & this.allDistricts & this.allFacilities & this.allVaccines
    */
    public ArrayList<Transaction> getAllTransactions() { return this.allTransactions; }
    public ArrayList<Transaction> getFiltered() { return this.filtered; }
    public Set<String> getAllDistricts() { return this.allDistricts; }
    public Set<String> getAllFacilities() { return this.allFacilities; }
    public Set<String> getAllVaccines() { return this.allVaccines; }

    /*
    * Adds given Transaction object to this database.
    * Checks if the given Transaction object is malformed & if it is filters it.
    *
    * @param    Transaction object to add to this database
    */
    public void addTransaction(Transaction t){
        if(increased.contains((String) t.get("reason"))
                && (Integer) t.get("num") < 0) {
            filtered.add(t);
        }else if(decreased.contains((String) t.get("reason"))
                && (Integer) t.get("num") >= 0 ){
            filtered.add(t);
        }else
            allTransactions.add(t);
    }

    /*
    * Populates allDistricts & allFacilities & allVaccines field.
    * Creates from transactions, thus doesn't contain districts or
    * facilities without any transactions.
    */
    public void populate(){
        for(Transaction t : allTransactions){
            if(t.get("district") != null)
                allDistricts.add((String) t.get("district"));
            if(t.get("facility") != null)
                allFacilities.add((String) t.get("facility"));
            if(t.get("vaccineName") != null)
                allVaccines.add((String) t.get("vaccineName"));
        }
    }

    /*
    * Filter all Transaction objects by selected field that matches the given object.
    *
    * @param    given   List of Transaction objects to filter
    * @param    type    What to filter by (i.e. filter by "facility", filter by "district")
    * @param    want    What the Transaction.get(type) should be equal to
    * @return           List of Transaction objects filtered by given parameters
    */
    public ArrayList<Transaction> filterBy(ArrayList<Transaction> given, String type, Object want){
        ArrayList<Transaction> result = new ArrayList<>();
        for(Transaction t : given){
            if(t.get(type).equals(want))
                result.add(t);
        }

        return result;
    }

    /*
    * Filter all Transaction objects by given time frame.
    *
    * @param    given   List of Transaction objects to filter
    * @param    start   Start date of given time frame
    * @param    end     End date of given time frame
    * @return           List of Transaction objects filtered by given time frame
    */
    public ArrayList<Transaction> filterByDateRange(ArrayList<Transaction> given, int[] start, int[] end){
        ArrayList<Transaction> result = new ArrayList<>();
        for(Transaction t : given){
            int[] tDate = (int[]) t.get("endDate");
            int tDateParsed = tDate[0] * 10000 + tDate[1] * 100 + tDate[2];
            int startDateParsed = start[0] * 10000 + start[1] * 100 + start[2];
            int endDateParsed = end[0] * 10000 + end[1] * 100 + end[2];

            if(startDateParsed <= tDateParsed && tDateParsed <= endDateParsed)
                result.add(t);
        }

        return result;
    }

    /*
    * Helper method for AxonController.
    * Supports the /current endpoint.
    *
    * @see              AxonController.java ... param("/current")
    *
    * @param    isFac   Boolean that specifies if it's for facility (True) or district (False)
    * @param    name    Name to filter with
    * @param    type    Vaccine name to filter with
    * @return           JSONObject in wanted format
    */
    public JSONObject currentHelper(Boolean isFac, String name, String type) throws JSONException {
        Map<String, Integer> vacNameToNum = new HashMap<>();
        ArrayList<Transaction> filter;

        JSONObject o1 = new JSONObject();

        if(isFac){
            filter = filterBy(this.allTransactions, "facility", name);
            o1.put("facility", name);
            o1.put("latitude", ((Point2D) filter.get(0).get("location")).getX());
            o1.put("longitude", ((Point2D) filter.get(0).get("location")).getY());
        } else{
            filter = filterBy(this.allTransactions, "district", name);
            o1.put("district", name);
        }

        for(Transaction t : filter){
            int num = vacNameToNum.getOrDefault((String) t.get("vaccineName"), 0);
            vacNameToNum.put((String) t.get("vaccineName"), num + (Integer) t.get("num"));
        }

        if(!type.equals("all")){
            if (vacNameToNum.containsKey(type))
                o1.put(type, vacNameToNum.get(type));
            else
                o1.put(type, "null");
        } else {
            for (Map.Entry<String, Integer> entry : vacNameToNum.entrySet())
                o1.put(entry.getKey(), entry.getValue());
        }

        return o1;
    }

    /*
    * Helper method for AxonController.
    * Supports the /current endpoint.
    *
    * @see              AxonController.java ... param("/current")
    *
    * @param    type    Vaccine name to filter with
    * @return           JSONObject in wanted format
    */
    public JSONObject currentHelperAll(String type) throws JSONException {
        Map<String, Integer> vacNameToNum = new HashMap<>();

        JSONObject o1 = new JSONObject();

        for(Transaction t : this.allTransactions){
            int num = vacNameToNum.getOrDefault((String) t.get("vaccineName"), 0);
            vacNameToNum.put((String) t.get("vaccineName"), num + (Integer) t.get("num"));
        }

        if(!type.equals("all")){
            if (vacNameToNum.containsKey(type))
                o1.put(type, vacNameToNum.get(type));
            else
                o1.put(type, "null");
        } else {
            for (Map.Entry<String, Integer> entry : vacNameToNum.entrySet())
                o1.put(entry.getKey(), entry.getValue());
        }

        return o1;
    }

    /*
    * Helper method for AxonController.
    * Supports the /used endpoint.
    *
    * @see                  AxonController.java ... param("/used")
    *
    * @param    arr         List of all facilities or districts
    * @param    divider     Type to filter by
    * @param    start       Start date of given time frame
    * @param    end         End date of given time frame
    * @return               JSONObject in wanted format
    */
    public JSONObject usedHelper(Set<String> arr, String divider, int[] start, int[] end) throws JSONException {
        Map<String, Map<String, Map<String, Integer>>> bigMap = new HashMap<>();

        for (String by : arr) {
            ArrayList<Transaction> filter = filterByDateRange(filterBy(this.allTransactions, divider, by), start, end);

            for (Transaction t : filter) {
                if ((Integer) t.get("num") < 0) {

                    int[] tempEndDate = (int[]) t.get("endDate");
                    String date = tempEndDate[0] + "-" + tempEndDate[1] + "-" + tempEndDate[2];

                    if (!bigMap.containsKey(date))
                        bigMap.put(date, new HashMap<>());
                    if (!bigMap.get(date).containsKey(by))
                        bigMap.get(date).put(by, new HashMap<>());

                    int used = bigMap.get(date).get(by).getOrDefault((String) t.get("vaccineName"), 0);
                    bigMap.get(date).get(by).put((String) t.get("vaccineName"), used + (-1 * (Integer) t.get("num")));
                }
            }
        }

        return new JSONObject(bigMap);
    }

    /*
    * Helper method for AxonController.
    * Supports the /used endpoint.
    *
    * @see                  AxonController.java ... param("/used")
    *
    * @param    start       Start date of given time frame
    * @param    end         End date of given time frame
    * @return               JSONObject in wanted format
    */
    public JSONObject usedHelperAll(int[] start, int[] end) throws JSONException {
        Map<String, Map<String, Integer>> bigMap = new HashMap<>();

        ArrayList<Transaction> filter = filterByDateRange(this.allTransactions, start, end);

        for (Transaction t : filter) {
            if ((Integer) t.get("num") < 0) {

                int[] tempEndDate = (int[]) t.get("endDate");
                String date = tempEndDate[0] + "-" + tempEndDate[1] + "-" + tempEndDate[2];

                if (!bigMap.containsKey(date))
                    bigMap.put(date, new HashMap<>());

                int used = bigMap.get(date).getOrDefault((String) t.get("vaccineName"), 0);
                bigMap.get(date).put((String) t.get("vaccineName"), used + (-1 * (Integer) t.get("num")));
            }
        }

        return new JSONObject(bigMap);
    }

    /*
    * Helper method for AxonController.
    * Supports the /all endpoint.
    *
    * @see                  AxonController.java ... param("/all")
    *
    * @param    start       Start date of given time frame
    * @param    end         End date of given time frame
    * @return               JSONArray in wanted format
    */
    public JSONArray allHelper(int[] start, int[] end) throws JSONException {

        JSONArray result = new JSONArray();

        for (String fac : this.allFacilities) {
            ArrayList<Transaction> filter = filterByDateRange(filterBy(this.allTransactions, "facility", fac), start, end);
            for (Transaction t : filter)
                result.add(t.toJSON());
        }

        return result;
    }

    /*
    * Helper method for AxonController.
    * Supports the /report endpoint.
    *
    * @see                  AxonController.java ... param("/report")
    *
    * @param    start       Start date of given time frame
    * @param    end         End date of given time frame
    * @return               String in wanted format
    */
    public String reportHelper(int[] start, int[] end) {
        List<String[]> data = new ArrayList<>();

        String startDate = start[0] + "-" + start[1] + "-" + start[2];

        data.add(new String[]{"Facility Name", "Vaccine Type", "Carryover from before " + startDate, "Expected Current Stock ", "Received",
                "Administered", "Spoiled", "Transferred", "Stock Correction"});

        for(String fac : this.allFacilities){
            Map<String, Map<String, Integer>> reasonToNum = new HashMap<>(); // vaccine name : <reason : num>
            ArrayList<Transaction> filter = filterByDateRange(filterBy(this.allTransactions, "facility", fac), start, end);
            for(Transaction t : filter){
                if(!reasonToNum.containsKey((String) t.get("vaccineName")))
                    reasonToNum.put((String) t.get("vaccineName"), new HashMap<>());

                int count1 = reasonToNum.get((String) t.get("vaccineName")).getOrDefault((String) t.get("reason"), 0);
                reasonToNum.get((String) t.get("vaccineName")).put((String) t.get("reason"), count1 + (Integer) t.get("num"));

                int count2 = reasonToNum.get((String) t.get("vaccineName")).getOrDefault("total", 0);
                reasonToNum.get((String) t.get("vaccineName")).put("total", count2 + (Integer) t.get("num"));
            }

            // counting usage before given time period
            Map<String, Integer> vaccineToNum = new HashMap<>(); // vaccine name : num
            ArrayList<Transaction> beforeFilter = filterByDateRange(filterBy(this.allTransactions, "facility", fac),
                    new int[]{1, 1, 1},
                    new int[]{start[0], start[1], start[2] - 1});
            for(Transaction t : beforeFilter){
                int count1 = vaccineToNum.getOrDefault((String) t.get("vaccineName"), 0);
                vaccineToNum.put((String) t.get("vaccineName"), count1 + (Integer) t.get("num"));
            }

            for(String s : reasonToNum.keySet()) {
                int total = 0;
                if(vaccineToNum.containsKey(s))
                    total = vaccineToNum.get(s);

                String[] row = new String[]{
                        fac, s,
                        Integer.toString(total),
                        Integer.toString(reasonToNum.get(s).get("total") + total),
                        Integer.toString(reasonToNum.get(s).getOrDefault("received", 0)),
                        Integer.toString(reasonToNum.get(s).getOrDefault("administered", 0)),
                        Integer.toString(reasonToNum.get(s).getOrDefault("spoiled", 0)),
                        Integer.toString(reasonToNum.get(s).getOrDefault("sent", 0)),
                        Integer.toString(reasonToNum.get(s).getOrDefault("stat_correction", 0))
                };
                data.add(row);
            }
        }

        String result = "";
        for (String[] line : data) {
            String toWrite = Arrays.toString(line);
            toWrite = toWrite.substring(1, toWrite.length() - 1);
            toWrite += "\n";
            result += toWrite;
        }

        return result;
    }

    /*
    * Helper method for AxonController.
    * Supports the /report endpoint.
    *
    * @see                  AxonController.java ... param("/report")
    *
    * @return               String in wanted format
    */
    public String reportHelperFlagged() {

        String result = "TransactionId(s) of transaction(s) with error";

        List<String> data = new ArrayList<>();
        for(Transaction t : this.filtered)
            data.add((String) t.get("transactionId"));

        for(String s : data)
            result += "\n" + s;

        return result;
    }
}
