package com.server.axon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
* A class to launch our web application.
* Uses Spring for web support.
*
* @author   XY Lim, Young Bin Cho
* @version  1.0
*
* @see      AxonController.java
*/
@SpringBootApplication
public class AxonApplication {

	public static void main(String[] args) {
		SpringApplication.run(AxonApplication.class, args);
	}
}
