package com.micro.common_lib.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(HttpStatus status, String message , Instant timeStamp , List<ValidationError>errors ) {

    public ApiError(HttpStatus status, String message, List<ValidationError> errors) {
        this(status , message , Instant.now() , errors);
    }

    public ApiError(HttpStatus status, String message) {
        this(status , message , Instant.now() , null);
    }

    public record ValidationError(
            String field , // the field is the actual resource that is requested
            String message
            //Instant timestamp
    ){}
}
