// specify the columns
const columnDefs = [
   { field: "date" },
   { field: "number" },
   { field: "reason" },
   { field: "vaccine" },
   { field: "facility" },
];
let selectedPeriod; // represents the time period the user is interested in seeing

// allow the grid to be sortable and filterable
let gridOptions = {
   defaultColDef: {
      sortable: true,
      filter: true,
   },
   columnDefs: columnDefs,
};

let madeGrid = false; // allows us to keep track of whether or not we have made a grid yet

// this function is called when the user changes their choice of time period
function getPeriodChoice() {
   let x = document.getElementById("period");
   selectedPeriod = x.value;
   getTransactionData();
}

// this function gets the transaction data from the backend
async function getTransactionData() {
   let transactionArray;
   let url = new URL("http://axon-service.azurewebsites.net/all");
   let params = { period: selectedPeriod };
   if (!madeGrid) {
      params = { period: "week" }; // by default we want the week view
   }
   url.search = new URLSearchParams(params).toString();
   let response = await fetch(url); // get the transactions from server
   let data;
   if (response.ok) {
      data = await response.json();
   } else {
      console.log("response not ok!");
   }
   transactionArray = [];

   // convert the string data format to the Date format
   const dateParser = d3.timeParse("%Y-%m-%d");
   data.forEach(function (d) {
      transactionArray.push({
         date: dateParser(d.date),
         number: d.number,
         reason: d.reason,
         vaccine: d.vaccine,
         facility: d.facility,
      });
   });

   // lookup the container we want the Grid to use
   const eGridDiv = document.querySelector("#myGrid");

   if (!madeGrid) {
      // if we havent made a grid make one, else just update it
      new agGrid.Grid(eGridDiv, gridOptions);
      gridOptions.api.setRowData(transactionArray);
      madeGrid = true;
   } else {
      gridOptions.api.setRowData(transactionArray);
   }

   // sort the table chronologically by default
   gridOptions.columnApi.applyColumnState({
      state: [
         {
            colId: "date",
            sort: "asc",
         },
      ],
   });
}
getTransactionData();
