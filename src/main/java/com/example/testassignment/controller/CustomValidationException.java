package com.example.testassignment.controller;

public class CustomValidationException extends RuntimeException{
    public CustomValidationException(String message){
        super(message);
    }
}
