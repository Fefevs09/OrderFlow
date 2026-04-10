package com.order.service.domain.exception;

public class InvalidOrderException extends DomainException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
