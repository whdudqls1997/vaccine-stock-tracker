drawLineChart();

/* Draws the x and y axes as well as centered text indicating an error
* @param text to be written
* @svg svg object to append to
* @startDate lower time bound for chart
* @endDate upper time bound for chart
* @width width of chart
* @height height of chart
*/
function drawAxesAndErrorText(text, svg, startDate, endDate, width, height) {
   var x = d3
   .scaleTime()
   .domain([startDate, endDate])
   .range([0, width]);
svg.append("g")
   .attr("transform", "translate(0," + height + ")")
   .call(d3.axisBottom(x));

var y = d3.scaleLinear().domain([0, 0]).range([height, 0]);
svg.append("g").call(d3.axisLeft(y));

svg.append("text")
   .attr("x", width / 2)
   .attr("y", height / 2)
   .attr("text-anchor", "middle")
   .style("font-size", "16px")
   .style("text-decoration", "bold")
   .text(text);
}


/*
 * Draws line chart based on selected options. It uses selected
 * start and end date as the time bounds for the chart. It 
 * then uses the selected filter (a list of districts or facilities)
 * to filter the data that is pulled from the backend. Finally, it
 * draws the chart.
 *
*/
async function drawLineChart() {
   // clear any data remaining on chart from before
   let myNode = document.getElementById("line-chart");
   while (myNode.lastElementChild) {
      myNode.removeChild(myNode.lastElementChild);
   }

   let serverData = {};

   const dateParser = d3.timeParse("%Y-%m-%d");

   // get input dates from user
   let startString = document.getElementById("start-date").value;
   let endString = document.getElementById("end-date").value;
   let startDate = new Date(startString);
   let endDate = new Date(endString);

   // get the vaccine type they have selected
   let selectedVaccine = document.getElementById("vaccine").value;

   let tempData = {};
   let maxUsed = 0;

   let margin = {
      top: 50,
      right: 340,
      bottom: 30,
      left: 60,
   };
   let dimensions = {
      width: 1000 - margin.left - margin.right,
      height: 400 - margin.top - margin.bottom,
   };

   var svg = d3
      .select("#line-chart")
      .append("svg")
      .attr("width", dimensions.width + margin.left + margin.right)
      .attr("height", dimensions.height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

   if (selectedVaccine == "None" || selectedVaccine == undefined || selectedVaccine == "") {
      // user hasn't selected a vaccine, can't do anything yet
      // draw chart and write text indicating so
      drawAxesAndErrorText("Select a vaccine and time period to display data", svg, startDate, endDate, dimensions.width, dimensions.height);
      return;
   }

   let filterOptions = $(".chosen-select").val();
   if (filterOptions == null) {
      // user hasn't selected any options for the facility/district filter
      // draw axes and text indicating so
      drawAxesAndErrorText("Empty filter produces no data", svg, startDate, endDate, dimensions.width, dimensions.height);
      return;
   }

   // fetch data from the server
   let url = new URL("http://axon-service.azurewebsites.net/used");
   let params = {
      by: document.getElementById("line-chart-group").value,
      start: startString,
      end: endString,
   };
   url.search = new URLSearchParams(params).toString();
   var response = await fetch(url);
   if (response.ok) {
      serverData = await response.json();
   } else {
      console.log("received bad response");
   }


   circleData = [];
   // parse data into our desired format
   for (let date in serverData) {
      // ensure date is in the proper date range
      if (dateParser(date) >= startDate && dateParser(date) <= endDate) {
         let facilities = serverData[date];
         for (let facility in facilities) {
            // ensure facility is in our filter
            if (filterOptions.includes(facility)) {
               let vaccines = facilities[facility];
               for (let vaccine in vaccines) {
                  if (vaccine == selectedVaccine) {
                     if (!(facility in tempData)) {
                        // first time we see this facility, add it to our tempData
                        tempData[facility] = [];
                     }
                     // keep track of maximum quantity used
                     maxUsed = Math.max(maxUsed, Number(vaccines[vaccine]));
                     // add to our data
                     tempData[facility].push({
                        date: dateParser(date),
                        used: Number(vaccines[vaccine]),
                     });
                     tempData[facility].sort((a, b) => b.date - a.date);
                     circleData.push({
                        date: dateParser(date),
                        used: Number(vaccines[vaccine]),
                        facility: facility,
                     });
                  }
               }
            }
         }
      }
   }

   

   // reformat data into what is needed for D3's lines
   let lineData = [];
   for (let facility in tempData) {
      lineData.push({ key: facility, values: tempData[facility] });
   }

   let facilities = lineData.map((d) => d.key);
   let color = d3.scaleOrdinal().domain(facilities).range(colorbrewer.Set2[6]);
   if (facilities.length == 0) {
      // no results, append text indicating so
      drawAxesAndErrorText("No Data For " + selectedVaccine + " in time period with selected filter", svg, startDate, endDate, dimensions.width, dimensions.height)
   }

   // set up axes
   var x = d3
      .scaleTime()
      .domain([startDate, endDate])
      .range([0, dimensions.width]);
   svg.append("g")
      .attr("transform", "translate(0," + dimensions.height + ")")
      .call(d3.axisBottom(x));

   var y = d3.scaleLinear().domain([0, maxUsed]).range([dimensions.height, 0]);
   svg.append("g").call(d3.axisLeft(y));

   // draw the lines
   svg.selectAll(".line")
      .append("g")
      .attr("class", "line")
      .data(lineData)
      .enter()
      .append("path")
      .attr("d", function (d) {
         return d3
            .line()
            .x((d) => x(d.date))
            .y((d) => y(d.used))(d.values);
      })
      .attr("fill", "none")
      .attr("stroke", (d) => color(d.key))
      .attr("stroke-width", 2);

   // draw the circles
   svg.selectAll(".circle")
      .append("g")
      .data(circleData)
      .enter()
      .append("circle")
      .attr("r", 4)
      .attr("cx", (d) => x(d.date))
      .attr("cy", (d) => y(d.used))
      .style("fill", (d) => color(d.facility));

   // add the title
   svg.append("text")
      .attr("x", dimensions.width / 2)
      .attr("y", 0 - margin.top / 2)
      .attr("text-anchor", "middle")
      .style("font-size", "16px")
      .style("text-decoration", "underline")
      .text("Administered Vaccines Over Time");

   // add the legend
   var legend = svg
      .selectAll("g.legend")
      .data(lineData)
      .enter()
      .append("g")
      .attr("class", "legend");

   // add colors to legend
   legend
      .append("circle")
      .attr("cx", dimensions.width)
      .attr("cy", (d, i) => i * 30)
      .attr("r", 6)
      .style("fill", (d) => color(d.key));

   // add labels to legend
   legend
      .append("text")
      .attr("x", dimensions.width + 20)
      .attr("y", (d, i) => i * 30 + 5)
      .text((d) => d.key);
}
