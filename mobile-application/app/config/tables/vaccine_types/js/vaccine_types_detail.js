/* global $, odkTables, odkData */
'use strict';

var stockResultSet = {};
var typeData = {};

// Called when the page loads
var display = function() {

  // Runs the query that launched this view
  odkData.getViewData(cbSuccess, cbFailure);
};

// Called when the query returns successfully
function cbSuccess(result) {

  stockResultSet = result;
  // and update the document with the values for this record
  updateContent();
}

function cbFailure(error) {

  // a real application would perhaps clear the document fiels if there were an error
  console.log('stock_detail getViewData CB error : ' + error);
}

/**
 * Assumes censusResultSet has valid content.
 *
 * Updates the document content with the information from the censusResultSet
 */
function updateContent() {

  nullCaseHelper('name', '#TITLE');
  // populate the fields
  $('#FIELD_1').prop('textContent', stockResultSet.get('vaccine_name'));
  $('#FIELD_2').prop('textContent', stockResultSet.get('num_doses'));
  // if it's a campaign, check the box
  if ( stockResultSet.get('campaign') == 'campaign') {
    $('#FIELD_3').prop('checked', 'true');
  } else {
    $('#FIELD_3').prop('checked', 'false');
  }

}

/**
 * Assumes censusResultSet has valid content
 *
 * Updates document field with the value for the elementKey
 */
function nullCaseHelper(elementKey, documentSelector) {
  var temp = stockResultSet.get(elementKey);
  if (temp !== null && temp !== undefined) {
    $(documentSelector).text(temp);
  }
}