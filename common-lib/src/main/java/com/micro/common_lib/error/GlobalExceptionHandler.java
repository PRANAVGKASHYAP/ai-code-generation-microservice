package com.micro.common_lib.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice // this class is used to hadel exception that happens on any cotroller
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handelResourceNotFoundError(ResourceNotFoundException ex){
        String message = ex.getMessage();
        ApiError error = new ApiError(HttpStatus.NOT_FOUND , message , null);
        return new ResponseEntity<>(error , HttpStatus.NOT_FOUND);
    }

    // handelling validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handelMethodValidationError(MethodArgumentNotValidException ex){
        List<ApiError.ValidationError>erList = ex.getBindingResult().getFieldErrors().stream()
                .map(ele -> new ApiError.ValidationError(ele.getField() , ele.getDefaultMessage() ))
                .toList();

        ApiError err = new ApiError(HttpStatus.BAD_REQUEST , "Validations failed " , erList);
        return new ResponseEntity<>(err , HttpStatus.BAD_REQUEST);

    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError>handelBadRequests(BadRequestException ex){
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST , ex.getMessage() , null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}
