# Vaccine Stock Tracker
This project consists of 3 parts: 
- the mobile application
- the backend server
- the frontend dashboard

The goal of this project is to: 
- collect data through the mobile app
- process the data at the backend
- display the processed data at a web dashboard

The mobile app and datastream runs on ODK-X framework (https://odk-x.org/software/). The backend processes data 
in Java and fowards it to the frontend. Endpoints are setup with our own RESTful API. The frontend displays the data 
in graphs/charts/maps and provides exportation of those data in json/excel/txt format.