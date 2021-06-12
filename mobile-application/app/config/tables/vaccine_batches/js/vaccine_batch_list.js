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

    // create a list of promises
    var promises = [];           
    for (var i = 0; i < stockResultSet.getCount(); i++){
        // for each vaccine batch, query the vaccine type
        var vaccinePromise = new Promise(function(resolve, reject) {
        odkData.query('vaccine_types', '_id = ?', [stockResultSet.getData(i, 'vaccine_id')],
            null, null, null, null, null, null, true, resolve, reject);
        });

        promises.push(vaccinePromise);
    }

    // resolve all of the promises and populat the list
    Promise.all(promises).then(function (resultArray) {
        for (var i = 0; i < stockResultSet.getCount(); i++) {
            console.log("i=" + i);
            // Creates the item space and stores the row ID in it
            var item = $('<li>');
            item.attr('id', stockResultSet.getRowId(i));
            item.attr('rowId', stockResultSet.getRowId(i));
            item.attr('class', 'item_space');
            var subList = $('<ul>');
            // get facility name
            var li = $('<li>');
            li.text(resultArray[i].get('facility_name'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // get vaccine name from promise result
            li = $('<li>');
            li.text("Type: " + resultArray[i].get('vaccine_name'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // get batch number
            li = $('<li>');
            li.text("Batch Num:" + stockResultSet.getData(i, 'batch_num'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // get the number of vials
            li = $('<li>');
            li.text("Total # Vials: " + stockResultSet.getData(i, 'num_vial'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // get the number of doses per vial
            li = $('<li>');
            li.text("Doses Per Vial: " + stockResultSet.getData(i, 'num_doses'));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            // get the expiration date and trim it
            li = $('<li>');
            li.text("Expires on " + stockResultSet.getData(i, 'expiry_date').substring(0,10));
            li.attr("style", "font-size: 30px");
            subList.append(li);
            
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
