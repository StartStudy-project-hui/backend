package com.study.studyproject.global.exception.advice;

import com.study.studyproject.global.exception.ExceptionResponse;
import com.study.studyproject.global.exception.ex.BusinessException;
import com.study.studyproject.global.exception.ex.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;
import static com.study.studyproject.global.exception.ex.ErrorCode.INTERNAL_SEVER_ERROR;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> exceptionHandler(BusinessException e) {
        return createErrorResponse(e.getErrorCode());
    }


    //500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> exceptionHandler(Exception e) {
        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .message(INTERNAL_SEVER_ERROR.getMessage())
                .code(INTERNAL_SEVER_ERROR.getCode())
                .status(INTERNAL_SEVER_ERROR.getStatus().value())
                .build();
        return ResponseEntity.internalServerError().body(errorResponse);
    }


    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        log.error("error :{}" ,exception.getParameterName());
        String message = MISSING_REQUEST_PARAM.getMessage(exception.getParameterName());

        return ResponseEntity.badRequest().body(ExceptionResponse.of(
                MISSING_REQUEST_PARAM.getStatus().value(),message, MISSING_REQUEST_PARAM.getCode()));
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(c -> errors.put(((FieldError) c).getField(), c.getDefaultMessage()));
        return ResponseEntity.status(status.value()).body(errors);
    }

    private ResponseEntity<ExceptionResponse> createErrorResponse(ErrorCode errorCode) {
        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .status(errorCode.getStatus().value())
                .build();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponse);
    }
}
