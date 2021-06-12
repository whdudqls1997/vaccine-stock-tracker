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

    // Iterate through the query results, rendering list items
    for (var i = 0; i < stockResultSet.getCount(); i++) {

        // Creates the item space and stores the row ID in it
        var item = $('<li>');
        item.attr('id', stockResultSet.getRowId(i));
        item.attr('rowId', stockResultSet.getRowId(i));
        item.attr('class', 'item_space');

        // create a sublist
        var subList = $('<ul>');
        // add the vaccine name
        var li = $('<li>');
        li.text("Name: " + stockResultSet.getData(i, 'vaccine_name'));
        li.attr("style", "font-size: 30px");
        subList.append(li);
        // add the number of doses to fully vaccinate
        li = $('<li>');
        li.text("Doses to Fully Vaccinate: " + stockResultSet.getData(i, 'num_doses'));
        li.attr("style", "font-size: 30px");
        subList.append(li);
        // add whther or not this is a campaign
        li = $('<li>');
        console.log(stockResultSet.getData(i, 'campaign'));
        if (stockResultSet.getData(i, 'campaign') == "campaign") {
            li.text("Campaign: Yes");
        } else {
            li.text("Campaign: No");
        }
        
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
};

var cbFailure = function(error) {
    console.log('stock getViewData CB error : ' + error);
};