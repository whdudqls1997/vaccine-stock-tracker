/*
* Taking in a date object, it will be formatted as yyyy-m-d
* @param date to format
*/

function formatDate(date) {
   return `${date.getFullYear().toString()}-${(date.getMonth() + 1).toString()}-${date.getDate()}`;
}

/*
* Draws donut chart based on the selected vaccine. Displays vaccines remaining of that type
* and number administered within the past 30 days.
*/
async function drawDonut() {
   // removing existing data from donut
   let myNode = document.getElementById("donut");
   while (myNode.lastElementChild) {
      myNode.removeChild(myNode.lastElementChild);
   }

   // set up donut chart
   let pie = d3
      .pie()
      .padAngle(0.005)
      .sort(null)
      .value((d) => d.value);

   let data = [];

   let selectedVaccine = document.getElementById("vaccine").value;
   if (selectedVaccine == "None" || selectedVaccine == "") {
      // no vaccine selected yet
      return;
   }

   // set up dates to be past 30 days
   let currentDate = new Date();
   let startDate = new Date();
   startDate.setDate(currentDate.getDate() - 30);

   // get the vaccines used in the past 30 days
   let url = new URL("http://axon-service.azurewebsites.net/used");
   let params = { by: "district", start: formatDate(startDate), end: formatDate(currentDate) };
   url.search = new URLSearchParams(params).toString();
   var response = await fetch(url);
   let usedData;
   if (response.ok) {
      usedData = await response.json();
   } else {
      console.log("received bad response");
      return;
   }
   // sum up vaccines used
   let totalAdministered = 0;
   for (let date in usedData) {
      let facilities = usedData[date];
      for (let facility in facilities) {
         let vaccines = facilities[facility];
         for (let vaccine in vaccines) {
            if (vaccine == selectedVaccine) {
               totalAdministered += vaccines[vaccine];
            }
         }
      }
   }
   // add to our chart data
   data.push({ name: "Vaccines Administered (30 days)", value: totalAdministered });

   // get the vaccines remaining
   url = new URL("http://axon-service.azurewebsites.net/current");
   params = { by: "district", type: selectedVaccine };
   url.search = new URLSearchParams(params).toString();
   var response2 = await fetch(url);
   let currData;
   if (response2.ok) {
      currData = await response2.json();
   } else {
      console.log("received bad response");
      return;
   }
   // sum up the total remaining
   let totalRemaining = 0;
   for (district in currData) {
      totalRemaining += currData[district][selectedVaccine];
   }
   // add chartData
   data.push({ name: "Vaccines Remaining", value: totalRemaining });

   const arcs = pie(data);
   let width = 1600;
   let height = Math.min(width, 500);
   // set up the colors for each option
   let color = d3
      .scaleOrdinal()
      .domain(data.map((d) => d.name))
      .range(
         d3
            .quantize((t) => d3.interpolateSpectral(t * 0.8 + 0.1), data.length)
            .reverse()
      );

   const radius = Math.min(width, height) / 2;
   let arc = d3
      .arc()
      .innerRadius(radius * 0.67)
      .outerRadius(radius - 1);
   const svg = d3
      .select("#donut")
      .append("svg")
      .attr("viewBox", [-width / 2, -height / 2, width, height]);

   // add slices of donut
   svg.selectAll("allSlices")
      .data(arcs)
      .enter()
      .append("path")
      .attr("d", arc)
      .attr("fill", function (d) {
         return color(d.data.name);
      })
      .attr("stroke", "white")
      .style("stroke-width", "2px")
      .style("opacity", 0.7);

   // add text to each slice of donut
   svg.append("g")
      .attr("font-family", "sans-serif")
      .attr("font-size", 12)
      .attr("text-anchor", "middle")
      .selectAll("text")
      .data(arcs)
      .join("text")
      .attr("transform", (d) => `translate(${arc.centroid(d)})`)
      .call((text) =>
         text
            .append("tspan")
            .attr("y", "-0.4em")
            .attr("font-weight", "bold")
            .text((d) => d.data.name)
      )
      .call((text) =>
         text
            .filter((d) => d.endAngle - d.startAngle > 0.25)
            .append("tspan")
            .attr("x", 0)
            .attr("y", "0.7em")
            .attr("fill-opacity", 0.7)
            .text((d) => d.data.value.toLocaleString())
      );

   // Add titles
   svg.append("text")
      .attr("x", 0)
      .attr("y", 0 - height / 5 + 100)
      .attr("text-anchor", "middle")
      .style("font-size", "16px")
      .style("text-decoration", "underline")
      .text("Current Quantity vs. Used (Past 30 Days)");

   return svg.node();
}

drawDonut();
