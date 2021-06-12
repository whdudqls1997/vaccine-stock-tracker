package com.server.axon;

/*
* A class to handle wrong query requests.
* This class represents the exception to be thrown if so.
*
* @author   XY Lim, Young Bin Cho
* @version  1.0
*
* @see      WrongParameterAdvice.java
*/
public class WrongParameterException extends RuntimeException {
    WrongParameterException(Param paramType, String input) {
        super("Value: \"" + input + "\" is invalid for parameter: " + paramType);
    }
    WrongParameterException(Param paramType, String input, String message) {
        super("Value: \"" + input + "\" is invalid for parameter: " + paramType + ". " + message);
    }
}
