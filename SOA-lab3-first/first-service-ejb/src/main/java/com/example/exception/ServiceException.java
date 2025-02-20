package com.example.exception;

import javax.ejb.ApplicationException;
import java.io.Serializable;

@ApplicationException(rollback = true)
public class ServiceException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public ServiceException(String message) {
        super(message);
    }
} 