var map = L.map("map").setView([1.373333, 32.290275], 7); // set default Uganda view
L.tileLayer("http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png", {
   maxZoom: 18,
   attribution:
      '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
}).addTo(map);
let stock = new Map();
let mapData;
let selectedVaccine; // the user's vaccine choice
let markers = [];

// this function is called when the user makes a vaccine choice
function getVaccineChoice() {
   let x = document.getElementById("vaccine");
   selectedVaccine = x.value;
   getData();
}

// this function gathers the current stock levels from the backend server
async function getData() {
   if (selectedVaccine == undefined) {
      // if user hasnt made a choice, dont fetch data
      return;
   }

   document.getElementById("spinner").style.display = "inline-block"; // spinner for loading
   let url = new URL("http://axon-service.azurewebsites.net/current");
   let params = { by: "district", type: selectedVaccine };
   url.search = new URLSearchParams(params).toString();
   let response = await fetch(url); // fetch the district vaccine data from backend

   if (response.ok) {
      let array = await response.json();
      for (let i = 0; i < array.length; i++) {
         let obj = array[i];
         stock.set(obj["district"], obj[selectedVaccine]); // populate our dictionary
      }
   }
   //clear map
   if (mapData) {
      mapData.remove();
   }
   for (let i = 0; i < markers.length; i++) {
      // clear old markers from the map before adding new ones
      markers[i].remove();
   }
   markers = [];
   $.getJSON("./uganda.geojson", function (geojson) {
      // embed the stock amounts into the geoJson file
      geojson.features.forEach(function (item) {
         if (stock.has(item.properties[2020])) {
            item.properties.stock = stock.get(item.properties[2020]);
         } else {
            item.properties.stock = -1;
            stock.set(item.properties[2020], item.properties.stock);
         }
      });

      mapData = L.choropleth(geojson, {
         // color scale for choropleth
         valueProperty: "stock",
         scale: ["white", "red"],
         steps: 20,
         mode: "k",
         style: {
            color: "#fff",
            weight: 2,
            fillOpacity: 0.8,
         },
         onEachFeature: function (feature, layer) {
            // when the user clicks on a district
            if (feature.properties.stock >= 0) {
               layer.bindPopup(
                  feature.properties.d +
                     "<br>" +
                     feature.properties.stock +
                     " vaccines<br>"
               );
            } else {
               layer.bindPopup(
                  feature.properties.d + "<br>" + "No vaccine data <br>"
               );
            }
         },
      }).addTo(map);
   });

   params = { by: "facility", type: selectedVaccine };

   url.search = new URLSearchParams(params).toString();

   response = await fetch(url); // fetch the facility data from backend

   if (response.ok) {
      let array = await response.json();

      for (let i = 0; i < array.length; i++) {
         let obj = array[i];
         let x = obj["latitude"];
         let y = obj["longitude"];
         let amt = obj[selectedVaccine];
         if (amt >= 0) {
            // create the marker with the info from the backend
            let marker = L.marker([x, y]).addTo(map);
            marker
               .bindPopup(
                  "<b>" + obj["facility"] + "</b><br>" + amt + " vaccines"
               )
               .openPopup();
         } else {
            let marker = L.marker([x, y]).addTo(map);
            marker
               .bindPopup("<b>" + obj["facility"] + "</b><br>no vaccine data")
               .openPopup();
         }
      }
   }

   document.getElementById("spinner").style.display = "none";
}
