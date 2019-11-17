package cz.fungisoft.coffeecompass.controller.models.rest;

import lombok.Data;

@Data
public class ApiRESTResponse
{
    private boolean success;
    private String message;

    public ApiRESTResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
