package com.newgen.ntlsnc.common;

import org.springframework.batch.core.JobParameter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Newaz Sharif
 * @since 22th Aug,22
 */
public class CustomJobParameterService <T extends Serializable> extends JobParameter {

    private T customParam;
    public CustomJobParameterService(T customParam){
        super(UUID.randomUUID().toString());
        this.customParam = customParam;
    }
    public T getValue(){
        return customParam;
    }
}
