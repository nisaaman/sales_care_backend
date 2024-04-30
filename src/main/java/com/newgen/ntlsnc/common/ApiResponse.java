package com.newgen.ntlsnc.common;

/**
 * @author liton
 * Created on 10/25/21 10:15 AM
 */

public class ApiResponse {
    public boolean success;
    public String message;
    public Object data;

    public ApiResponse() {

    }

    public ApiResponse(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setSuccess(Object data) {
        this.success = true;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setError(String message) {
        this.success = false;
        this.message = message;
        this.data = null;
    }
}
