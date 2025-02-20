package com.fwwb.vehicledetection.common.exception;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 使用getMessage()
        this.errorCode = errorCode;
    }

    public int getCode() {
        return errorCode.getCode(); // 使用getCode()
    }
}