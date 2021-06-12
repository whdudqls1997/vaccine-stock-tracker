package com.server.axon;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/*
* A class to handle wrong query requests.
* Throws WrongParameterException if query format is wrong.
*
* @author   XY Lim, Young Bin Cho
* @version  1.0
*
* @see      WrongParameterException.java
*/
@ControllerAdvice
class WrongParameterAdvice {

    @ResponseBody
    @ExceptionHandler(WrongParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String wrongParameterHandler(WrongParameterException ex) {
        return ex.getMessage();
    }
}
