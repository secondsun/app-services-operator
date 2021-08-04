package com.openshift.cloud.utils;

import com.openshift.cloud.api.kas.invoker.ApiException;

/**
 * This class is a wrapper for the different kinds of ApiException that we inherit from generated OpenAPI clients.
 */
public class AppServicesApiException extends RuntimeException {
    
    private final int code;

    public AppServicesApiException(int code,String message,Throwable cause) {
        super(message,cause);
        this.code = code;

    
    }

    public AppServicesApiException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public AppServicesApiException(ApiException cause) {
        super(cause);
        this.code = cause.getCode();
        
    }

    public AppServicesApiException(com.openshift.cloud.api.srs.invoker.ApiException cause) {
        super(cause);
        this.code = cause.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

public int getCode() {
    return code;
}

}
