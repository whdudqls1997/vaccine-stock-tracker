package com.server.axon;

import java.util.*;
import java.net.MalformedURLException;
import org.apache.wink.json4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

enum Param {
    TYPE,
    BY,
    FACILITY,
    DISTRICT,
    START,
    END,
    PERIOD,
    FLAGGED
}

/*
* A class that host our webservice.
* Is a REST api, handling given requests.
* Hosts specific endpoints for supported requests.
*
* @author   XY Lim, Young Bin Cho
* @version  2.0
*
* @see      Transaction.java
* @see      ServerFunctions.java
* @see      DataStructure.java
* @see      WrongParameterAdvice.java
* @see      WrongParameterException.java
*/
@RestController
class AxonController {

    private final ServerFunctions sf; // ServerFunctions object to manage communication between ODK-X server
    private DataStructure ds; // DataStructure object to manage data read from ODK-X server
    private Timer timer; // Timer to run refresh of database off of

    class PeriodicLoader extends TimerTask {
        public void run() {
            try {
                System.out.println("Loading data periodically...");
                AxonController.this.ds = AxonController.this.sf.loadData();
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * Default constructor. Initializes the fields.
    */
    AxonController() throws MalformedURLException {
        this.sf = new ServerFunctions("https://capstone.odkxdemo.com",
                "superuser", "x123SuperUser!");
        sf.createNewSyncClient();
        PeriodicLoader loadTask = new PeriodicLoader();
        this.timer = new Timer();
        this.timer.schedule(loadTask, 0, 60000); // delay for 0ms, scheduled at every minute (60000ms)
    }

    /*
    * Checks if the given parameters for requests are valid.
    * Throws WrongParameterException if given parameter isn't supported or isn't in the database.
    *
    * @param    paramTypeList   List of types of given parameters
    * @param    params          List of actual string of given parameters
    */
    void checkParam(List<Param> paramTypeList, List<String> params) {
        assert(paramTypeList.size() == params.size());
        for (int i = 0; i < paramTypeList.size(); i++) {
            String param = params.get(i);
            Param paramType = paramTypeList.get(i);
            switch (paramType) {
                case TYPE:
                    if (!param.equals("all") && !ds.getAllVaccines().contains(param))
                        throw new WrongParameterException(paramType, param);
                    break;
                case BY:
                    if (!param.equals("none") && !param.equals("facility") && !param.equals("district"))
                        throw new WrongParameterException(paramType, param, "Must be none, facility or district.");
                    break;
                case FACILITY:
                    if (!param.equals("all") && !ds.getAllFacilities().contains(param))
                        throw new WrongParameterException(paramType, param);
                    break;
                case DISTRICT:
                    if (!param.equals("all") && !ds.getAllDistricts().contains(param))
                        throw new WrongParameterException(paramType, param);
                    break;
                case START:
                    String[] date = param.split("-");
                    if (date.length != 3)
                        throw new WrongParameterException(paramType, param, "Date should be in YYYY-MM-DD format.");
                    if (Integer.parseInt(date[0]) < 1 || Integer.parseInt(date[1]) < 1 || Integer.parseInt(date[2]) < 1)
                        throw new WrongParameterException(paramType, param, "Date should be in YYYY-MM-DD format.");
                    break;
                case END:
                    String[] endDate = param.split("-");
                    if (endDate.length != 3)
                        throw new WrongParameterException(paramType, param, "Date should be in YYYY-MM-DD format.");
                    if (Integer.parseInt(endDate[0]) < 1 || Integer.parseInt(endDate[1]) < 1 || Integer.parseInt(endDate[2]) < 1)
                        throw new WrongParameterException(paramType, param, "Date should be in YYYY-MM-DD format.");
                    if (Integer.parseInt(endDate[0]) > 9999 || Integer.parseInt(endDate[1]) > 12 || Integer.parseInt(endDate[2]) > 31)
                        throw new WrongParameterException(paramType, param, "Date should be in YYYY-MM-DD format.");
                    break;
                case PERIOD:
                    if (!param.equals("week") && !param.equals("month") && !param.equals("all"))
                        throw new WrongParameterException(paramType, param, "Must be week, month or all.");
                    break;
                case FLAGGED:
                    if (!param.equals("true") && !param.equals("false")) {
                        throw new WrongParameterException(paramType, param, "Must be either 'true' or 'false'.");
                    }
                default:
                    break;
            }
        }
    }

    /*
    * Supports the "/current" endpoint.
    * Returns a JSONArray of current remaining vaccine stock in every facility (or district).
    * Default returns total number in all facilities.
    *
    * The JSONArray is formatted like below:
    * [ {"vaccine_name_1" : number, "vaccine_name_2" : number, ... ,
    *    "latitude" : latitude, "longitude" : longitude,                     <--- if "by" is facility
    *    "facility" : facility_name_1 OR "district" : district_name_1}       <--- depending on "by"
    *   {"vaccine_name_1" : number, "vaccine_name_2" : number, ... ,
    *    "latitude" : latitude, "longitude" : longitude,                     <--- if "by" is facility
    *    "facility" : facility_name_2 OR "district" : district_name_2},
    *   ... ]
    *
    * @see                  DataStructure.currentHelper()
    * @see                  DataStructure.currentHelperAll()
    *
    * @param    type        Type of vaccine
    * @param    by          What to filter by; "none" or "facility" or "district"
    * @param    facility    Name of facility to filter by
    * @param    district    Name of district to filter by
    * @return               JSONArray formatted like written above
    */
    @CrossOrigin()
    @GetMapping("/current")
    public JSONArray current(@RequestParam(value = "type", defaultValue = "all") String type,
                             @RequestParam(value = "by", defaultValue = "none") String by,
                             @RequestParam(value = "facility", defaultValue = "all") String facility,
                             @RequestParam(value = "district", defaultValue = "all") String district)
            throws JSONException {

        // Validate parameters
        List<Param> paramTypes = new ArrayList<>();
        List<String> params = new ArrayList<>();
        paramTypes.add(Param.TYPE);
        params.add(type);
        paramTypes.add(Param.BY);
        params.add(by);
        paramTypes.add(Param.FACILITY);
        params.add(facility);
        paramTypes.add(Param.DISTRICT);
        params.add(district);
        checkParam(paramTypes, params);

        // Returning Object
        JSONArray result = new JSONArray();

        // case : given "facility" isn't "all"
        if (!facility.equals("all")) {
            result.put(ds.currentHelper(true, facility, type));
            return result;
        }

        // case : given "district" isn't "all"
        if (!district.equals("all")) {
            result.put(ds.currentHelper(false, district, type));
            return result;
        }

        if (by.equals("facility")) { // case : given "by" is "facility"
            for(String s : ds.getAllFacilities())
                result.put(ds.currentHelper(true, s, type));
            return result;
        } else if (by.equals("district")) { // case : given "by" is "district"
            for(String s : ds.getAllDistricts())
                result.put(ds.currentHelper(false, s, type));
            return result;
        } else { // case : return total number
            result.put(ds.currentHelperAll(type));
            return result;
        }
    }

    /*
    * Supports the "/used" endpoint.
    * Returns a JSONObject the number of vaccine used for the stated time period,
    * sorted by day, in every facility (or district).
    * Dates are formatted like "2021-05-30".
    *
    * The JSONObject is formatted like below:
    * { "date_1" : { "facility_or_district_name_1" : { "vaccine_name_1" : number,
    *                                                  "vaccine_name_2" : number, ... },
    *                "facility_or_district_name_2" : { "vaccine_name_1" : number,
    *                                                  "vaccine_name_2" : number, ... }, ... },
    *   "date_2" : { "facility_or_district_name_1" : { "vaccine_name_1" : number,
    *                                                  "vaccine_name_2" : number, ... },
    *                "facility_or_district_name_2" : { "vaccine_name_1" : number,
    *                                                  "vaccine_name_2" : number, ... }, ... },
    *   ... }
    *
    * @see                  DataStructure.usedHelper()
    * @see                  DataStructure.usedHelperAll()
    *
    * @param    type        Type of vaccine; can be "all" for all vaccine types
    * @param    by          What to filter by; "facility" or "district"
    * @param    facility    Name of facility to filter by
    * @param    district    Name of district to filter by
    * @param    start       Start date for given time period
    * @param    end         End date for given time period
    * @return               JSONObject formatted like written above
    */
    @CrossOrigin()
    @GetMapping("/used")
    public JSONObject used(@RequestParam(value = "type", defaultValue = "all") String type,
                            @RequestParam(value = "by", defaultValue = "none") String by,
                            @RequestParam(value = "start") String start,
                            @RequestParam(value = "end") String end)
            throws JSONException {

        // Validate parameters
        List<Param> paramTypes = new ArrayList<>();
        List<String> params = new ArrayList<>();
        paramTypes.add(Param.TYPE);
        params.add(type);
        paramTypes.add(Param.BY);
        params.add(by);
        paramTypes.add(Param.START);
        params.add(start);
        paramTypes.add(Param.END);
        params.add(end);
        checkParam(paramTypes, params);

        // Split given date string
        String[] startDate = start.split("-");
        String[] endDate = end.split("-");
        int[] from = new int[]{Integer.parseInt(startDate[0]), Integer.parseInt(startDate[1]), Integer.parseInt(startDate[2])};
        int[] to = new int[]{Integer.parseInt(endDate[0]), Integer.parseInt(endDate[1]), Integer.parseInt(endDate[2])};

        if (by.equals("none")) {
            return ds.usedHelperAll(from, to);
        } else if (by.equals("facility")) { // Check facility
            return ds.usedHelper(ds.getAllFacilities(), "facility", from, to);
        } else { // Check district
            return ds.usedHelper(ds.getAllDistricts(), "district", from, to);
        }
    }

    /*
    * Supports the "/all" endpoint.
    * Returns a JSONArray including data of all transactions from past 7 days (week)
    * or past 30 days (month) or all (all).
    *
    * The JSONArray is formatted like below:
    * [ {"date" : date_1, "vaccine_number" : number_1,
    *    "reason" : reason_1, "vaccine_name" : vaccine_name_1,
    *    "facility" : facility_name_1}
    *   {"date" : date_2, "vaccine_number" : number_2,
    *    "reason" : reason_2, "vaccine_name" : vaccine_name_2,
    *    "facility" : facility_name_2},
    *   ... ]
    *
    * @see              DataStructure.allHelper()
    *
    * @param    period  Time period to return transactions for; "week" or "month" or "all"
    * @return           JSONArray formatted like written above
    */
    @CrossOrigin()
    @GetMapping("/all")
    public JSONArray all(@RequestParam(value = "period", defaultValue = "all") String period) throws JSONException {
        // check parameter
        List<Param> paramTypes = new ArrayList<>();
        List<String> params = new ArrayList<>();
        paramTypes.add(Param.PERIOD);
        params.add(period);
        checkParam(paramTypes, params);

        // Get current date
        // note : Calendar.MONTH starts from 0, adding 1 to buffer the difference
        Calendar cal = new GregorianCalendar();
        int[] today = new int[] {cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};

        // Calculate the wanted time frame
        int[] from = new int[] {0, 1, 1};
        if (period.equals("week")) {
            cal.add(Calendar.DATE, -7);
            from = new int[] {cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
        } else if (period.equals("month")) {
            cal.add(Calendar.DATE, -30);
            from = new int[] {cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
        }

        return ds.allHelper(from, today);
    }

    /*
    * Supports the "/report" endpoint.
    * Returns a report in .csv format.
    * The report contains transactions in given time period.
    * Also supports report generation of all transactions with malformed data.
    * Prompts a download when this endpoint is called.
    *
    * @see                 DataStructure.reportHelper()
    *
    * @param    flagged    Flagged report for true, usage report for false
    * @param    period     Time period to return transactions for; "week" or "month" or "all"
    * @return              A response that prompts the download of generated .csv file
    */
    @CrossOrigin
    @GetMapping("/report")
    public ResponseEntity generateReport(@RequestParam(value = "flagged", defaultValue = "false") boolean flagged,
                                         @RequestParam(value = "period", defaultValue = "all") String period) {
        // check parameters
        List<Param> paramTypes = new ArrayList<>();
        List<String> params = new ArrayList<>();
        paramTypes.add(Param.FLAGGED);
        params.add(Boolean.toString(flagged));
        paramTypes.add(Param.PERIOD);
        params.add(period);
        checkParam(paramTypes, params);
        // Get current date
        // note : Calendar.MONTH starts from 0, adding 1 to buffer the difference
        Calendar cal = new GregorianCalendar();
        int[] today = new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
        String todayString = today[0] + "-" + today[1] + "-" + today[2];

        // Calculate the wanted time frame
        int[] from = new int[] {0, 1, 1};
        if (period.equals("week")) {
            cal.add(Calendar.DATE, -7);
            from = new int[] {cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
        } else if (period.equals("month")) {
            cal.add(Calendar.DATE, -30);
            from = new int[] {cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
        }
        String startString = from[0] + "-" + from[1] + "-" + from[2];

        // Set returning file name
        String type = (flagged) ? "Flagged_Report" : "Usage_Report";
        String outputFileName = type + "_from_" + startString + "_to_" + todayString + ".csv";

        // Return error report if requested type is "error"
        if(flagged){
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"")
                    .body(ds.reportHelperFlagged());
        }

        // Return normal report for given time period if type isn't "error"
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"")
                .body(ds.reportHelper(from, today));
    }

    /*
    * Supports the "/vaccines" endpoint.
    * Returns a list of all vaccine names.
    *
    * @see      DataStructure.getAllVaccines()
    *
    * @return   A JSONArray of all vaccine names in database
    */
    @CrossOrigin
    @GetMapping("/vaccines")
    public JSONArray returnVaccines() throws JSONException {
        Set<String> results = this.ds.getAllVaccines();
        return new JSONArray(results);
    }

    /*
    * Supports the "/facilities" endpoint.
    * Returns a list of all facility names.
    *
    * @see      DataStructure.getAllFacilities()
    *
    * @return   A JSONArray of all facility names in database
    */
    @CrossOrigin
    @GetMapping("/facilities")
    public JSONArray returnFacilities() throws JSONException {
        Set<String> results = this.ds.getAllFacilities();
        return new JSONArray(results);
    }

    /*
    * Supports the "/districts" endpoint.
    * Returns a list of all district names.
    *
    * @see      DataStructure.getAllDistricts()
    *
    * @return   A JSONArray of all district names in database
    */
    @CrossOrigin
    @GetMapping("/districts")
    public JSONArray returnDistricts() throws JSONException {
        Set<String> results = this.ds.getAllDistricts();
        return new JSONArray(results);
    }
}
