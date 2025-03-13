// File: src/main/java/com/fwwb/vehicledetection/exception/UnauthorizedException.java
package com.fwwb.vehicledetection.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}