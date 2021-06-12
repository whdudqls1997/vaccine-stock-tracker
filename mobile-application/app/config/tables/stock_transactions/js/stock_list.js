/* global $, odkTables, odkData, odkCommon */
'use strict';

// The first function called on load
var resumeFn = function() {

    // Retrieves the query data from the database
    // Sets displayGroup as the success callback
    // and cbFailure as the fail callback
          odkData.getViewData(displayGroup, cbFailure);
}

// Display the list of census results
var displayGroup = function(stockResultSet) {

    // Set the function to call when a list item is clicked
    $('#list').click(function(e) {

        // Retrieve the row ID from the item_space attribute
                    var jqueryObject = $(e.target);
                    var containingDiv = jqueryObject.closest('.item_space');
                    var rowId = containingDiv.attr('rowId');

        // Retrieve the tableID from the query results
                    var tableId = stockResultSet.getTableId();

                    if (rowId !== null && rowId !== undefined) {

            // Opens the detail view from the file specified in
            // the properties worksheet
                                    odkTables.openDetailView(null, tableId, rowId, null);
                          }
                });

    // creates a list of promises to be resolved
    var promises = [];           
    for (var i = 0; i < stockResultSet.getCount(); i++){
        // for each result, get the facility name
        var facilityPromise = new Promise(function(resolve, reject) {
            odkData.query('facilities', '_id = ?', [stockResultSet.getData(i, 'facility_id')],
                null, null, null, null, null, null, true, resolve, reject);
        });
    
        // for each result, get the vaccine type name
        var vaccinePromise = new Promise(function(resolve, reject) {
        odkData.query('vaccine_types', '_id = ?', [stockResultSet.getData(i, 'vaccine_id')],
            null, null, null, null, null, null, true, resolve, reject);
        });

        promises.push(facilityPromise);
        promises.push(vaccinePromise);
    }

    // resolve all of the promises
    Promise.all(promises).then(function (resultArray) {
        for (var i = 0; i < stockResultSet.getCount(); i++) {
            console.log("i=" + i);
            // Creates the item space and stores the row ID in it
            var item = $('<li>');
            item.attr('id', stockResultSet.getRowId(i));
            item.attr('rowId', stockResultSet.getRowId(i));
            item.attr('class', 'item_space');
            // create a sublist to add to
            var subList = $('<ul>');
            // add facility name
            var li = $('<li>');
           
            li.text(resultArray[2 * i].get('facility_name'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // add reason for transaction
            li = $('<li>');
            li.text("Reason: " + stockResultSet.getData(i, 'reason'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // add the quantity and type
            li = $('<li>');
            var temp = "";
            if (stockResultSet.getData(i, 'number') > 0) {
                temp += "+";
            }
            li.text((temp + stockResultSet.getData(i, 'number') + ' of ') + resultArray[2 * i + 1].get('vaccine_name'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // add the start date and end date
            li = $('<li>');
            li.text(stockResultSet.getData(i, 'start_date').substring(0,10) + " -> " + stockResultSet.getData(i, 'end_date').substring(0,10));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            
            // add sublist to our list item
            item.append(subList);
            
    
            // Creates arrow icon
            var chevron = $('<img>');
            chevron.attr('src', odkCommon.getFileAsUrl('config/assets/img/little_arrow.png'));
            chevron.attr('class', 'chevron');
            item.append(chevron);
    
            // Add the item to the list
            $('#list').append(item);
    
            // Don't append the last one to avoid the fencepost problem
            var borderDiv = $('<div>');
            borderDiv.addClass('divider');
            $('#list').append(borderDiv);
          }
          if (i < stockResultSet.getCount()) {
              setTimeout(resumeFn, 0, i);
          }
    }, function (err) {
        console.log("promises failed with error: " + err);
    });

    // Iterate through the query results, rendering list items
    
};

var cbFailure = function(error) {
    console.log('stock getViewData CB error : ' + error);
};
