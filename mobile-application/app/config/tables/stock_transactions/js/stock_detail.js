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

  // gets a promise to use a query to get the facility name
  var facilityPromise = new Promise(function(resolve, reject) {
    odkData.query('facilities', '_id = ?', [stockResultSet.get('facility_id')],
        null, null, null, null, null, null, true, resolve, reject);
  });

  // wait for the promise and update the content
  facilityPromise.then(function (result) {
      $('#FIELD_1').prop('textContent', result.get('facility_name'));
  }, function(err) {
      console.log('promise failed with error: ' + err);
  });

  // gets a promise to use a query to get the vaccine type name
  var vaccinePromise = new Promise(function(resolve, reject) {
    odkData.query('vaccine_types', '_id = ?', [stockResultSet.get('vaccine_id')],
        null, null, null, null, null, null, true, resolve, reject);
  });

  // wait for the promise and update the content
  vaccinePromise.then(function (result) {
      $('#FIELD_2').prop('textContent', result.get('vaccine_name'));
  }, function(err) {
      console.log('promise failed with error: ' + err);
  });

  // update the rest
  $('#FIELD_3').prop('textContent', stockResultSet.get('batch_id'));
  $('#FIELD_4').prop('textContent', stockResultSet.get('number'));
  $('#FIELD_5').prop('textContent', stockResultSet.get('reason'));
  $('#FIELD_6').prop('textContent', stockResultSet.get('start_date').substring(0,10));
  $('#FIELD_7').prop('textContent', stockResultSet.get('end_date').substring(0,10));
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