async function getVaccines() {
   const sel = document.getElementById("vaccine");

   let url = new URL("http://axon-service.azurewebsites.net/vaccines");
   let response = await fetch(url);

   if (response.ok) {
      let options = await response.json();
      let option = document.createElement("option");
      option.innerHTML = "None";
      option.setAttribute("value", "None");
      sel.appendChild(option);
      for (const o of options) {
         option = document.createElement("option");
         option.innerHTML = o;
         option.setAttribute("value", o);
         sel.appendChild(option);
      }
   } else {
      console.log("server error!");
   }
}
getVaccines();
