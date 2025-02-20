package com.fwwb.vehicledetection.common.exception;

public enum ErrorCode {
    // 示例枚举项，根据实际情况调整
    BAD_REQUEST(400, "请求错误"),
    UNAUTHORIZED(401, "未授权");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
