package com.environment.quiz.exception;

public class InvalidAiOutputException extends RuntimeException {
    public InvalidAiOutputException(String message) {
        super(message);
    }
}