package ru.practicum.main.service.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.service.request.dto.RequestDto;
import ru.practicum.main.service.request.dto.RequestStatusUpdateDto;
import ru.practicum.main.service.request.dto.RequestStatusUpdateResultDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable int userId, @RequestParam int eventId) {
        log.info("Добавляется новая заявка от пользователя с userId = {} и eventId = {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getRequestList(@PathVariable int userId) {
        log.info("Пользователь с userId = {} ищет заявки на участия", userId);
        return requestService.getRequests(userId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto patchRequest(@PathVariable int userId, @PathVariable int requestId) {
        log.info("Пользователь с userId = {} отменяет заявку на с requestId = {} участия в событии", userId, requestId);
        return requestService.patch(userId, requestId);
    }

    @PatchMapping("/users/{userId}/requests")
    public RequestStatusUpdateResultDto patchRequestByUser(@PathVariable int userId, @PathVariable int eventId,
                                                           @RequestBody RequestStatusUpdateDto requestStatusUpdateDto) {
        log.info("Пользователь с userId = {} изменяет статусы заявок для события с eventId = {} requestStatusUpdateDto = {}", userId, eventId, requestStatusUpdateDto);
        return requestService.patchByUser(userId, eventId, requestStatusUpdateDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getRequestByUser(@PathVariable int userId, @PathVariable int eventId) {
        log.info("Пользователь с userId = {} ищет заявки на участия в событии с eventId = {}", userId, eventId);
        return requestService.getRequestsByUser(userId, eventId);
    }
}
