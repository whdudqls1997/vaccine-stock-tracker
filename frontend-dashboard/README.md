# Vaccine Stock Tracker Dashboard
## Overview
This is the codebase for the web dashboard.

The directory can be broken down as follows:
- The first category are files of code that we didn't write
  - The `css` folder contains relevant css files for parts of the dashboard
  - the `js` folder contains javascript for certain imported scripts (such as bootstrap)
  - `choropleth.js` provides pre-set code for the choropleth map
- The second category are those that we wrote
  - `dropdown.js` provides the code to generate the dropdown of vaccines that are queried from the backend server
  - `map.js` creates the choropleth map and the markers on top of it
  - `lineChartFilter.js` creates the filter for the line chart. This is the drop down multiselect that can either be populated with facilities or districts.
  - `lineChart.js` is the code to generate the line chart with D3
  - `donut.js` provides the code to generate the donut chart with D3.
  - `transactions.js` generates the tables of transactions using AG Grid

Finally, there is `uganda.geojson` which is the provided shapefiles for Uganda given by Professor Anderson.

## Running the Dashboard
To run the dashboard, there are a couple of different ways.
1. We have been using the "Live Server" VSCode extension to launch a local server. Navigate to that port on your localhost and you will be able to view and interact with the dashboard.
2. The second way requires [NodeJs](https://nodejs.org/en/). Once you install this, navigate to the project directory in a terminal and execute `npm install`. This will install the dependencies necessary to launch the local server. Then, do `npm start` to launch the server. This will be launched on `localhost:8000`, so navigate to this URL to view and interact with the dashboard.

Something to note is that the first time launching the dashboard may take a bit to fetch the initial data as the backend web server warms up. After that, it should be responsive and fast. This is a limitation of the Azure server we are using.
