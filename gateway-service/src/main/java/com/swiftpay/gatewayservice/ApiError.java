package com.swiftpay.gatewayservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

 private Instant timestamp;
 private int status;
 private String errorCode;
 private String message;
 private String path;
}
//