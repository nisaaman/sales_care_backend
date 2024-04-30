package com.newgen.ntlsnc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author nisa
 * @date 8/10/22
 * @time 5:05 PM
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {
    private Long resourceId;
    private String message;

    public ObjectNotFoundException(Long resourceId, String message) {
        super(message);
        this.resourceId = resourceId;
        this.message = message;
    }

    public ObjectNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public long getResourceId(){
        return resourceId;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
