package com.example.up_leveling.exception;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message) {
        super (message);
    }
}
