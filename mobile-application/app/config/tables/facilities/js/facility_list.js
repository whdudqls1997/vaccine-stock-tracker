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

        // Get the facility name
        var name = stockResultSet.getData(i, 'facility_name');
        if (name === null || name === undefined) {
            name = 'unknown name';
        }
        item.text(name);

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