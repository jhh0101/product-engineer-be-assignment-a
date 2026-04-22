package com.github.jhh0101.assignment.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ApiResponse<T> {

    private final Boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T>ApiResponse<T> success(T data){
        return new ApiResponse<>(true, "C000", "성공", data);
    }

    public static <T>ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, "C000", message, data);
    }

    public static ApiResponse<Void> error(String code, String message){
        return new ApiResponse<>(false, code, message, null);
    }
}
