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

  // create a promise to query the vaccine type
  var vaccinePromise = new Promise(function(resolve, reject) {
    odkData.query('vaccine_types', '_id = ?', [stockResultSet.get('vaccine_id')],
        null, null, null, null, null, null, true, resolve, reject);
  });

  // resolve promise and get result, putting it in field 1
  vaccinePromise.then(function (result) {
      $('#FIELD_1').prop('textContent', result.get('vaccine_name'));
  }, function(err) {
      console.log('promise failed with error: ' + err);
  });

  // fill in the rest of the data
  $('#FIELD_2').prop('textContent', stockResultSet.get('batch_num'));
  $('#FIELD_3').prop('textContent', stockResultSet.get('num_vial'));
  $('#FIELD_4').prop('textContent', stockResultSet.get('num_doses'));
  $('#FIELD_5').prop('textContent', stockResultSet.get('expiry_date').substring(0,10));

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