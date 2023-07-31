package ru.practicum.main.service.error;

public class RequestNotValidException extends RuntimeException {

    public RequestNotValidException(String massage) {
        super(massage);
    }
}
