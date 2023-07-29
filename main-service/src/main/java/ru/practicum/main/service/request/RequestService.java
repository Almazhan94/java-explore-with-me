package ru.practicum.main.service.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public RequestDto createRequest(int userId, int eventId) {
        Request request = new Request();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Событие с eventId = " + eventId + " не найдено"));

        List <Request> doubleRequest = requestRepository.findByRequesterId(userId);

        if (!doubleRequest.isEmpty()) {
            throw new RuntimeException();                       /* TODO нельзя добавить повторный запрос (Ожидается код ошибки 409)*/
        }
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new RuntimeException();                       /* TODO инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409) */
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new RuntimeException();                   /* TODO нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409) */
        }
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);
        if (!(event.getParticipantLimit() > requestCountDto.getRequestCount() + 1)) {
            throw new RuntimeException();                       /*TODO если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)*/
        }
        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        request.setRequester(user);
        request.setEvent(event);
        Request createRequest = requestRepository.save(request);
        return RequestMapper.toRequestDto(createRequest);
    }

    public List<RequestDto> getRequests(int userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь с userId = " + userId + " не найден"));

        List <Request> requestList = requestRepository.findByRequesterId(userId);
        return RequestMapper.toRequestDtoList(requestList);
    }

    public RequestDto patch(int userId, int requestId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь с userId = " + userId + " не найден"));

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Запрос с requestId = " + requestId + " не найден"));

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    public RequestStatusUpdateResultDto patchByUser(int userId, int eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Событие с eventId = " + eventId + " не найдено"));

        if(event.getParticipantLimit() == 0 || event.getRequestModeration().equals(false)){
            throw new RuntimeException();                       /*TODO если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется*/
        }
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);
        if (!(event.getParticipantLimit() > requestCountDto.getRequestCount() + 1)) {
            throw new RuntimeException();                       /*TODO нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)*/
        }

        List<Request> requestList = requestRepository.findByIdIn(requestStatusUpdateDto.getRequestIds());
        for( Request request : requestList) {
            if(!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new RuntimeException();                       /*TODO статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)*/
            }
            if(requestStatusUpdateDto.getStatus().equals(RequestStatus.CONFIRMED)
                && (event.getParticipantLimit() > (requestCountDto.getRequestCount() + 1))) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(RequestMapper.toRequestDto(request));
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(RequestMapper.toRequestDto(request));
            }
        }
        return new RequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }

    public RequestCountDto getRequestCountDto(int eventId, RequestStatus status){
        RequestCountDto requestCountDto = requestRepository.findRequestCountDtoByEventIdAndStatus(eventId, status);
        if (requestCountDto == null) {
            return new RequestCountDto(eventId, 0L);
        } else {
            return requestCountDto;
        }
    }

    public List<RequestDto> getRequestsByUser(int userId, int eventId) {
        List<RequestDto> requestDtoList = new ArrayList<>();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Событие с eventId = " + eventId + " не найдено"));

        if(event.getInitiator().getId() != user.getId()){
            throw new RuntimeException();                                                        /*TODO пользователь не является владельцем события*/
        }
        List<Request> requestList = requestRepository.findByEventId(eventId);
        requestDtoList = RequestMapper.toRequestDtoList(requestList);
        return requestDtoList;
    }
}
