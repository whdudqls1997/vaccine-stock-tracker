/* 
* Populates the chosen multiselect with specified options
* @param options: list of choices to be put in the select
*/
function populateSelect(options) {
  const sel = document.getElementById('line-chart-filter');
  let option = document.createElement("option");
  // adds each option to the multiselect
  for (const o of options) {
      option = document.createElement("option");
      option.innerHTML = o;
      option.setAttribute("value", o);
      sel.appendChild(option);
  }
}

/* 
*  Based on the filter type selected (by district or facility),
*  populates the Chosen multiselect with options queried from server.
*  As facilities and districts aren't likely to change often, it caches
*  the result and reuses the list.
*/
async function getLineChartFilter() {

  // clears any existing options out of multiselect
  const sel = document.getElementById('line-chart-filter');
   while (sel.lastElementChild) {
      sel.removeChild(sel.lastElementChild);
   }
  const filterType = document.getElementById("line-chart-group");

  if (filterType.value == 'district') {
    if (districts == null) {
      // fetches the list of all districts
      let url = new URL("http://axon-service.azurewebsites.net/districts");
      let response = await fetch(url);
      
      if (response.ok) {
        let options = await response.json();
        districts = options;
      } else {
        console.log("server error!");
      }
    }
    populateSelect(districts);
    
    $('.chosen-select').trigger("chosen:updated");

  } else if (filterType.value == 'facility') {
    // fetches the list of all facilities
    if (facilities == null) {
      let url = new URL("http://axon-service.azurewebsites.net/facilities");
      let response = await fetch(url);
      
      if (response.ok) {
        let options = await response.json();
        facilities = options;
      } else {
        console.log("server error!");
      }
    }

    populateSelect(facilities);
    
    $('.chosen-select').trigger("chosen:updated");
  }
}

// initial setup means neither facilities nor districts have been fetched
var facilities = null;
var districts = null;

$(document).ready(function () {
  $('.chosen-select').chosen();
  });

$('.chosen-select').chosen({ width: '20%' });

getLineChartFilter();
