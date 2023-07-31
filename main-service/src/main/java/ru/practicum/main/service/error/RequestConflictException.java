package ru.practicum.main.service.error;

public class RequestConflictException extends RuntimeException {

    public RequestConflictException(String massage) {
        super(massage);
    }
}
