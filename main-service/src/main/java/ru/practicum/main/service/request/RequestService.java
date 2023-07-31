package ru.practicum.main.service.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.service.error.ObjectNotFoundException;
import ru.practicum.main.service.error.RequestConflictException;
import ru.practicum.main.service.event.Event;
import ru.practicum.main.service.event.EventRepository;
import ru.practicum.main.service.event.State;
import ru.practicum.main.service.request.dto.RequestCountDto;
import ru.practicum.main.service.request.dto.RequestDto;
import ru.practicum.main.service.request.dto.RequestStatusUpdateDto;
import ru.practicum.main.service.request.dto.RequestStatusUpdateResultDto;
import ru.practicum.main.service.user.User;
import ru.practicum.main.service.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RequestService {

    private final RequestRepository requestRepository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, UserRepository userRepository,
                          EventRepository eventRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public RequestDto createRequest(int userId, int eventId) {
        Request request = new Request();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        List<Request> doubleRequest = requestRepository.findByRequesterId(userId);

        if (!doubleRequest.isEmpty()) {
            throw new RequestConflictException("нельзя добавить повторный запрос");
        }
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new RequestConflictException("инициатор события не может добавить запрос на участие в своём событии");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new RequestConflictException("нельзя участвовать в неопубликованном событии");
        }
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= requestCountDto.getRequestCount()) {
            throw new RequestConflictException("у события достигнут лимит запросов на участие");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        request.setRequester(user);
        request.setEvent(event);
        Request createRequest = requestRepository.save(request);
        return RequestMapper.toRequestDto(createRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(int userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        List<Request> requestList = requestRepository.findByRequesterId(userId);
        return RequestMapper.toRequestDtoList(requestList);
    }

    @Transactional
    public RequestDto patch(int userId, int requestId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ObjectNotFoundException("Запрос с requestId = " + requestId + " не найден"));

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Transactional
    public RequestStatusUpdateResultDto patchByUser(int userId, int eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        if (event.getParticipantLimit() == 0 || event.getRequestModeration().equals(false)) {
            throw new RuntimeException();
        }
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() <= requestCountDto.getRequestCount()) {
            throw new RequestConflictException("нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие");
        }

        List<Request> requestList = requestRepository.findByIdIn(requestStatusUpdateDto.getRequestIds());
        for (Request request : requestList) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new RequestConflictException("статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }
            if (requestStatusUpdateDto.getStatus().equals(RequestStatus.CONFIRMED)
                && (event.getParticipantLimit() > requestCountDto.getRequestCount())) {
                request.setStatus(RequestStatus.CONFIRMED);
                requestRepository.save(request);
                confirmedRequests.add(RequestMapper.toRequestDto(request));
            } else {
                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
                rejectedRequests.add(RequestMapper.toRequestDto(request));
            }
        }
        return new RequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }

    @Transactional(readOnly = true)
    public RequestCountDto getRequestCountDto(int eventId, RequestStatus status) {
        RequestCountDto requestCountDto = requestRepository.findRequestCountDtoByEventIdAndStatus(eventId, status);
        if (requestCountDto == null) {
            return new RequestCountDto(eventId, 0L);
        } else {
            return requestCountDto;
        }
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsByUser(int userId, int eventId) {
        List<RequestDto> requestDtoList = new ArrayList<>();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new RequestConflictException("пользователь не является владельцем события");
        }
        List<Request> requestList = requestRepository.findByEventId(eventId);
        requestDtoList = RequestMapper.toRequestDtoList(requestList);
        return requestDtoList;
    }
}
