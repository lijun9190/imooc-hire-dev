package com.imooc.exceptions;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GraceExceptionHandler {

//    @ExceptionHandler(ArithmeticException.class)
//    public GraceJSONResult returnArithmeticException(ArithmeticException e) {
//        log.error("Arithmetic exception", e);
//        return GraceJSONResult.errorMsg(e.getMessage());
//    }

    @ExceptionHandler(KeeperException.BadVersionException.class)
    public GraceJSONResult returnBadVersionException(KeeperException.BadVersionException e) {
        log.warn("Zookeeper bad version", e);
        return GraceJSONResult.exception(ResponseStatusEnum.ZOOKEEPER_BAD_VERSION_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public GraceJSONResult returnMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("Uploaded file exceeds max size", e);
        return GraceJSONResult.exception(ResponseStatusEnum.FILE_MAX_SIZE_500KB_ERROR);
    }

    @ExceptionHandler(MyCustomException.class)
    public GraceJSONResult returnMyCustomException(MyCustomException e) {
        log.warn("Business exception: status={}, msg={}",
                e.getResponseStatusEnum().status(),
                e.getResponseStatusEnum().msg());
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public GraceJSONResult returnExpiredJwtException(ExpiredJwtException e) {
        log.warn("JWT expired", e);
        return GraceJSONResult.exception(ResponseStatusEnum.JWT_EXPIRE_ERROR);
    }

    @ExceptionHandler({
            SignatureException.class,
            UnsupportedJwtException.class,
            MalformedJwtException.class,
            io.jsonwebtoken.security.SignatureException.class
    })
    public GraceJSONResult returnSignatureException(Exception e) {
        log.warn("JWT signature or format invalid", e);
        return GraceJSONResult.exception(ResponseStatusEnum.JWT_SIGNATURE_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public GraceJSONResult returnNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> errors = getErrors(result);
        return GraceJSONResult.errorMap(errors);
    }

    @ExceptionHandler(BindException.class)
    public GraceJSONResult returnBindException(BindException e) {
        return GraceJSONResult.errorMap(getErrors(e.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public GraceJSONResult returnConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return GraceJSONResult.errorMap(errors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public GraceJSONResult returnRequestParamException(Exception e) {
        log.warn("Request parameter error", e);
        return GraceJSONResult.exception(ResponseStatusEnum.SYSTEM_PARAMS_SETTINGS_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public GraceJSONResult returnException(Exception e) {
        log.error("Unexpected system exception", e);
        return GraceJSONResult.exception(ResponseStatusEnum.SYSTEM_ERROR);
    }

    public Map<String, String> getErrors(BindingResult result) {

        Map<String, String> map = new HashMap<>();

        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError fe : errorList) {
            // 错误所对应的属性字段名
            String field = fe.getField();
            // 错误信息
            String message = fe.getDefaultMessage();

            map.put(field, message);
        }

        return map;
    }

}
