package org.example.expensesservice.config;

import feign.FeignException;

public class CustomFeignException extends FeignException {
    protected CustomFeignException(String message, int status) {
        super(status, message);
    }
}
