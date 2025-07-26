package com.pccth.minio.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private Integer status;

    private String message;

    private T payload;

    private T violations;

    private LocalDateTime timestamp;

    private String path;

    public Response(Integer status, T payload) {
        this.status = status;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }

    public Response(Integer status, T payload, T violations) {
        this.status = status;
        this.payload = null;
        this.violations = violations;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ResponseEntity<Response<T>> ok(T payload) {
        Response<T> response = new Response<>(200, payload);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<Response<T>> withStatusAndMessage(HttpStatus httpStatus, String message) {
        Response<T> response = new Response<>(httpStatus.value(), null);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        response.setMessage(message);
        return new ResponseEntity<>(response, httpStatus);
    }

    public static <T> ResponseEntity<Response<T>> violation(T violation) {
        Response<T> response = new Response<>(400, null, violation);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<Response<T>> empty() {
        Response<T> response = new Response<>(200, null);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<Response<T>> badLogic(T payload) {
        Response<T> response = new Response<>(422, payload);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<Response<T>> unauthorized(String message) {
        Response<T> response = new Response<>(401, null);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        response.setMessage(message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    public static <T> ResponseEntity<Response<T>> forbidden(String message) {
        Response<T> response = new Response<>(403, null);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        response.setPath(request.getRequestURI());
        response.setMessage(message);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    public static <T> ResponseEntity<Response<T>> withStatusAndPayload(HttpStatus httpStatus, T payload) {
        Response<T> response = new Response<>(httpStatus.value(), payload);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        response.setPath(request.getRequestURI());
        return new ResponseEntity<>(response, httpStatus);
    }

}
