package org.example.groupsservice;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse {

    public ErrorResponse(Integer httpStatus, String message) {
        super();
        this.httpStatus = httpStatus;
        this.message = message;
    }

    private Integer httpStatus;
    private String message;
}