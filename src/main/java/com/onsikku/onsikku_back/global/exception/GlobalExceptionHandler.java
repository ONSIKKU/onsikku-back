package com.onsikku.onsikku_back.global.exception;



import com.onsikku.onsikku_back.global.response.BaseResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    protected BaseResponse<String> handleBaseException(BaseException e) {
        System.out.println(e.getMessage());
        return new BaseResponse<>(e.getStatus());
    }

}
