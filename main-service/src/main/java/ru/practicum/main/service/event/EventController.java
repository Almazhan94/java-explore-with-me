package ru.practicum.main.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.service.event.dto.*;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.CreateStatDto;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class EventController {

    private final EventService eventService;

    private final StatsClient statsClient;

    @Autowired
    public EventController(EventService eventService, StatsClient statsClient) {
        this.eventService = eventService;
        this.statsClient = statsClient;
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable int userId, @RequestBody @Valid NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.info("Обратите внимание: дата и время на которые намечено событие не может быть раньше, " +
                "чем через два часа от текущего момента: {}", newEventDto.getEventDate());
            throw new RuntimeException();
        }
        log.info("Добавляется событие {} пользователем с id = {}", newEventDto, userId);
        return eventService.createEvent(userId, newEventDto);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByInitiator(@PathVariable int userId, @PathVariable int eventId,
                                               @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Пользователем с id = {} обновляет событие {} с id = {} ",userId, updateEventUserRequest, eventId);
        return eventService.updateEventByInitiator(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventShortByInitiator(@PathVariable int userId,
                                                   @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Ищутся события пользователем с id = {}", userId);
        return eventService.getEventShortListByInitiator(userId, from, size);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventFullByInitiator(@PathVariable int userId, @PathVariable int eventId) {
        log.info("Ищется событие с с id = {} пользователем с id = {}", eventId, userId);
        return eventService.getEventFullByInitiator(userId, eventId);
    }

    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventFullByAdmin(@RequestParam(value = "users", required = false) List<Integer> users,
                                                  @RequestParam(value = "states", required = false) List<String> states,
                                                  @RequestParam(value = "categories", required = false) List<Integer> categories,
                                                  @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                                  @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                                @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Админ ищет события  с параметрами поиска users = {}, states = {}, categories = {}, rangeStart = {}, rangeEnd = {}, from = {}, size = {}",
            users , states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventFullByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByAdmin( @PathVariable int eventId,  @RequestBody UpdateEventAdminDto updateEventAdminDto) {
        log.info("Админ обновляет событие с id = {}, UpdateEventAdminDto = {}  ", eventId, updateEventAdminDto);
        return eventService.updateEventByAdmin(eventId, updateEventAdminDto);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventFullWithFilter( HttpServletRequest request,
                                                      @RequestParam(value = "text", required = false) String text,
                                                     @RequestParam(value = "categories", required = false) List<Integer> categories,
                                                     @RequestParam(value = "paid", required = false) Boolean paid,
                                                     @RequestParam(value = "rangeStart", required = false, defaultValue = "") String rangeStart,
                                                     @RequestParam(value = "rangeEnd", required = false, defaultValue = "") String rangeEnd,
                                                     @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
                                                     @RequestParam(value = "sort", required = false) String sort,
                                                     @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        log.info("Получение событий  с параметрами поиска text = {}, categories = {}, paid = {}, rangeStart = {}, rangeEnd = {},  " +
                "onlyAvailable = {}, from = {}, sort = {}, size = {}",
            text , categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        statsClient.addStat(new CreateStatDto("main-service", "/events", request.getRemoteAddr(), LocalDateTime.now()));
        return eventService.getEventFullWithFilter(text , categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventFullById(HttpServletRequest request, @PathVariable int eventId) {
        log.info("Ищется событие с id = {} ", eventId);
        statsClient.addStat(new CreateStatDto("main-service", "/events/" + eventId, request.getRemoteAddr(), LocalDateTime.now()));
        return eventService.getEventFullById(eventId);
    }
}
