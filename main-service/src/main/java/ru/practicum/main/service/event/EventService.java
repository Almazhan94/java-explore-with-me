package ru.practicum.main.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.service.category.Category;
import ru.practicum.main.service.category.CategoryRepository;
import ru.practicum.main.service.error.ObjectNotFoundException;
import ru.practicum.main.service.error.RequestConflictException;
import ru.practicum.main.service.error.RequestNotValidException;
import ru.practicum.main.service.event.dto.*;
import ru.practicum.main.service.location.Location;
import ru.practicum.main.service.location.LocationDto;
import ru.practicum.main.service.location.LocationRepository;
import ru.practicum.main.service.request.RequestRepository;
import ru.practicum.main.service.request.RequestStatus;
import ru.practicum.main.service.request.dto.RequestCountDto;
import ru.practicum.main.service.user.User;
import ru.practicum.main.service.user.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service

public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        CategoryRepository categoryRepository, LocationRepository locationRepository,
                        RequestRepository requestRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.requestRepository = requestRepository;
        this.statsClient = statsClient;
    }

    public EventFullDto createEvent(int userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));
        int categoryId = newEventDto.getCategory();
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + categoryId + " не найдена"));
        Location location = saveNewLocation(newEventDto.getLocation());
        Event event = new Event();
        event.setInitiator(user);
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setLocation(location);

        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        } else {
            event.setPaid(newEventDto.getPaid());
        }

        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        } else {
            event.setParticipantLimit(newEventDto.getParticipantLimit());
        }

        event.setPublishedOn(null);             //Публикует Админ

        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        } else {
            event.setRequestModeration(newEventDto.getRequestModeration());
        }

        event.setState(State.PENDING);
        event.setTitle(newEventDto.getTitle());
        eventRepository.save(event);

        return EventMapper.toEventFullDto(event, user, 0, 0);
    }

    public Location saveNewLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return locationRepository.save(location);
    }

    public EventFullDto updateEventByInitiator(int userId, int eventId, UpdateEventUserRequest updateEventUserRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.info("Обратите внимание: дата и время на которые намечено событие не может быть раньше, " +
                "чем через два часа от текущего момента: {}", event.getEventDate());
            throw new RequestNotValidException("Обратите внимание: дата и время на которые намечено событие не может быть раньше, " +
                "чем через два часа от текущего момента");
        }

        if (!event.getState().equals(State.REJECTED) && !event.getState().equals(State.PENDING)) {
            throw new RequestConflictException("изменить можно только отмененные события или события в состоянии ожидания модерации");
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            int categoryId = updateEventUserRequest.getCategory();
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + categoryId + " не найдена"));
            event.setCategory(category);
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            Location location = saveNewLocation(updateEventUserRequest.getLocation());
            event.setLocation(location);
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getStateAction() != null && updateEventUserRequest.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
            event.setState(State.PENDING);
        }
        if (updateEventUserRequest.getStateAction() != null && updateEventUserRequest.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
            event.setState(State.CANCELED);
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        Event updateEvent = eventRepository.save(event);

        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
           Collections.singleton("/events/" + eventId), Boolean.TRUE);

        if (stat.isEmpty()) {
            return EventMapper.toEventFullDto(updateEvent, user, requestCountDto.getRequestCount(), 0);
        } else {
            return EventMapper.toEventFullDto(updateEvent, user, requestCountDto.getRequestCount(),
                stat.get(0).getHits());
        }
    }

    public List<EventShortDto> getEventShortListByInitiator(int userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        List<Event> eventList = eventRepository.findByInitiatorId(userId, pageable);

        List<String> uriList = new ArrayList<>();
        for (Event e : eventList) {
            uriList.add("/events/" + e.getId());
        }

        List<StatsHitDto> stat = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            uriList, Boolean.TRUE);

        eventShortDtoList = EventMapper.toEventShortDtoList(eventList, stat);

        return eventShortDtoList;
    }

    public EventFullDto getEventFullByInitiator(int userId, int eventId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId +
                " не найдено"));

        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<StatsHitDto> stat  = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            Collections.singleton("/events/" + eventId), Boolean.TRUE);

        if (stat.isEmpty()) {
            return EventMapper.toEventFullDto(event, user, requestCountDto.getRequestCount(), 0);
        } else {
            return EventMapper.toEventFullDto(event, user, requestCountDto.getRequestCount(), stat.get(0).getHits());
        }
    }

    public List<EventFullDto> getEventFullByAdmin(List<Integer> users, List<String> states, List<Integer> categories,
                                                  String rangeStart, String rangeEnd, Integer from, Integer size) {
        List<EventFullDto> eventFullDtoList = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now().minusYears(20);
        LocalDateTime end = LocalDateTime.now().plusYears(100);
        if (rangeStart != null && rangeEnd != null) {
            start = LocalDateTime.parse(rangeStart, formatter);
            end = LocalDateTime.parse(rangeEnd, formatter);
        }
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<State> stateList = new ArrayList<>();
        if (states != null) {
            for (String s : states) {
                stateList.add(Enum.valueOf(State.class, s));
            }
        } else {
            stateList = null;
        }
        List<Event> eventList = eventRepository.findByUserStateCategoryStartEnd(users, stateList, categories, start, end, pageable);

        List<Integer> eventIdList = new ArrayList<>();
        for (Event event : eventList) {
            eventIdList.add(event.getId());
        }

        List<RequestCountDto> requestCountDtoList =
            requestRepository.findRequestCountDtoListByEventId(eventIdList, RequestStatus.CONFIRMED);

        List<String> uriList = new ArrayList<>();
        for (Event e : eventList) {
            uriList.add("/events/" + e.getId());
        }

        List<StatsHitDto> stat = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            uriList, Boolean.TRUE);

        eventFullDtoList = EventMapper.toEventFullDtoList(eventList, requestCountDtoList, stat);
        return eventFullDtoList;
    }

    public EventFullDto updateEventByAdmin(int eventId, UpdateEventAdminDto updateEventAdminDto) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            log.info("дата начала изменяемого события должна быть не ранее чем за час от даты публикации: {}", event.getEventDate());
            throw new RequestConflictException("дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
        }
        if (!event.getState().equals(State.PENDING)) {
            throw new RequestConflictException("событие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (updateEventAdminDto.getStateAction() != null && updateEventAdminDto.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
            event.setState(State.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
        if (updateEventAdminDto.getStateAction() != null && updateEventAdminDto.getStateAction().equals(StateAction.REJECT_EVENT)) {
            event.setState(State.REJECTED);
        }
        if (updateEventAdminDto.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminDto.getAnnotation());
        }
        if (updateEventAdminDto.getCategory() != null) {
            int categoryId = updateEventAdminDto.getCategory();
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + categoryId + " не найдена"));
            event.setCategory(category);
        }
        if (updateEventAdminDto.getDescription() != null) {
            event.setDescription(updateEventAdminDto.getDescription());
        }
        if (updateEventAdminDto.getEventDate() != null) {
            event.setEventDate(updateEventAdminDto.getEventDate());
        }
        if (updateEventAdminDto.getLocation() != null) {
            Location location = saveNewLocation(updateEventAdminDto.getLocation());
            event.setLocation(location);
        }
        if (updateEventAdminDto.getPaid() != null) {
            event.setPaid(updateEventAdminDto.getPaid());
        }
        if (updateEventAdminDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminDto.getParticipantLimit());
        }
        if (updateEventAdminDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminDto.getRequestModeration());
        }
        if (updateEventAdminDto.getTitle() != null) {
            event.setTitle(updateEventAdminDto.getTitle());
        }
        Event updateEvent = eventRepository.save(event);
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<StatsHitDto> stat  = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            Collections.singleton("/events/" + eventId), Boolean.TRUE);

        if (stat.isEmpty()) {
            return EventMapper.toEventFullDto(updateEvent, updateEvent.getInitiator(),
                requestCountDto.getRequestCount(), 0);
        } else {
            return EventMapper.toEventFullDto(updateEvent, updateEvent.getInitiator(),
                requestCountDto.getRequestCount(), stat.get(0).getHits());
        }
    }

    public List<EventFullDto> getEventFullWithFilter(String text, List<Integer> categories, Boolean paid,
                                                     String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                                     String sort, Integer from, Integer size) {
        List<Event> eventList = new ArrayList<>();
        List<EventFullDto> eventFullDtoList = new ArrayList<>();
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        if (rangeStart == null && rangeEnd == null) {
            eventList = eventRepository.findByTextCategoriesPaidEventDateAfter(text, categories, paid,
                LocalDateTime.now(), pageable);
        } else {
            LocalDateTime start = LocalDateTime.parse(rangeStart, formatter);
            LocalDateTime end = LocalDateTime.parse(rangeEnd, formatter);
            if (end.isBefore(start) || start.isEqual(end)) {
                throw new RequestNotValidException("Обратите внимание: дата и время не корректны");
            }
            eventList =
                eventRepository.findByTextCategoriesPaidStartEndSortByEventDate(text, categories, paid, start, end,
                    pageable);
        }
        List<Integer> eventIdList = new ArrayList<>();
        for (Event event : eventList) {
            eventIdList.add(event.getId());
        }
        List<RequestCountDto> requestCountDtoList =
            requestRepository.findAllRequestCountDtoByEventIdInAndStatus(eventIdList, RequestStatus.CONFIRMED);
        if (onlyAvailable) {
            for (Event event : eventList) {
                if (!requestCountDtoList.isEmpty()) {
                    for (RequestCountDto requestCountDto : requestCountDtoList) {
                        if ((requestCountDto.getEventId() == event.getId())) {
                            if (event.getParticipantLimit().equals(requestCountDto.getRequestCount())) {
                                eventList.remove(event);
                            }
                        }
                    }
                }
            }
        }

        List<String> uriList = new ArrayList<>();
        for (Event e : eventList) {
            uriList.add("/events/" + e.getId());
        }

        List<StatsHitDto> stat  = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            uriList, Boolean.TRUE);

        if (sort != null) {
            if (sort.equals("EVENT_DATE")) {
                eventFullDtoList = EventMapper.toEventFullDtoList(eventList, requestCountDtoList, stat);
                return eventFullDtoList = eventFullDtoList
                    .stream()
                    .sorted(Comparator.comparing(event -> event.getEventDate()))
                    .collect(Collectors.toList());

            } else if (sort.equals("VIEWS")) {
                eventFullDtoList = EventMapper.toEventFullDtoList(eventList, requestCountDtoList, stat);
                return eventFullDtoList = eventFullDtoList
                    .stream()
                    .sorted(Comparator.comparingLong(eventFullDto -> eventFullDto.getViews()))
                    .collect(Collectors.toList());
            }
        }
        return  EventMapper.toEventFullDtoList(eventList, requestCountDtoList, stat);
    }

    public RequestCountDto getRequestCountDto(int eventId, RequestStatus status) {
        RequestCountDto requestCountDto = requestRepository.findRequestCountDtoByEventIdAndStatus(eventId, status);
        if (requestCountDto == null) {
            return new RequestCountDto(eventId, 0L);
        } else {
            return requestCountDto;
        }
    }

    public List<StatsHitDto> getStat(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique) {
        return statsClient.getStat(start, end, uris, unique);
    }

    public EventFullDto getEventFullById(int eventId) {

        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + "и статусом" + State.PUBLISHED + " не найдено"));

        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<StatsHitDto> stat  = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            Collections.singleton("/events/" + eventId), Boolean.TRUE);


        if (stat.isEmpty()) {
            return EventMapper.toEventFullDto(event, event.getInitiator(),
                requestCountDto.getRequestCount(), 0);
        } else {
            return EventMapper.toEventFullDto(event, event.getInitiator(),
                requestCountDto.getRequestCount(), stat.get(0).getHits());
        }
    }
}