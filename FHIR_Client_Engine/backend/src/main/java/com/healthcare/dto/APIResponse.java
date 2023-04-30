package com.healthcare.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Data
@Scope(value="prototype")
@EqualsAndHashCode(callSuper = false)
@Component
public class APIResponse {

    int statusCode;
    String message;

    Object data;
    String errMessage;

    public APIResponse(int statusCode, String message, Object data, String errMessage) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.errMessage = errMessage;
    }

}
