package com.codecat.catsurvey.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.codecat.catsurvey.utils.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice(annotations = {RestController.class, Controller.class, Service.class})
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return Result.validatedFailed(
                Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violationSet = e.getConstraintViolations();
        String message = violationSet.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(" "));

        return Result.validatedFailed(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return Result.validatedFailed("参数不存在或类型错误: " + e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        e.printStackTrace();
        return Result.validatedFailed("参数非法：" + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return Result.failedMsg("参数非法: " + e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleDataIntegrityViolationException(DataIntegrityViolationException e) { // 主键重复异常
        e.printStackTrace();
        return Result.failedMsg("内部异常");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result handleNoHandlerFoundException(NoHandlerFoundException e) {
        e.printStackTrace();
        return Result.failedMsg(e.getMessage());
    }

    @ExceptionHandler(CatValidationException.class)
    public Result handleValidationException(CatValidationException e) {
        e.printStackTrace();
        return Result.validatedFailed(e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public Result handleCatValidationException(ValidationException e) {
        return Result.validatedFailed(e.getMessage());
    }

    @ExceptionHandler(CatAuthorizedException.class)
    public Result handleCatAuthorizedException(CatAuthorizedException e) {
        return Result.unauthorized(e.getMessage());
    }

    @ExceptionHandler(CatForbiddenException.class)
    public Result handleCatForbiddenException(CatForbiddenException e) {
        return Result.forbidden(e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result handleNotPermissionException(NotPermissionException e) { return Result.forbidden(e.getMessage()); }

    @ExceptionHandler(NotLoginException.class)
    public Result handleNotLoginException(NotLoginException e) {
        return Result.unauthorized(e.getMessage());
    }

    @ExceptionHandler(NotRoleException.class)
    public Result handleNotRoleException(NotRoleException e) {
        return Result.forbidden(e.getMessage());
    }
}
