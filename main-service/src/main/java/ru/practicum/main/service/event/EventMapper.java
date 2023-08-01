package ru.practicum.main.service.event;


import ru.practicum.main.service.category.CategoryMapper;
import ru.practicum.main.service.event.dto.EventFullDto;
import ru.practicum.main.service.event.dto.EventShortDto;
import ru.practicum.main.service.location.LocationMapper;
import ru.practicum.main.service.request.dto.RequestCountDto;
import ru.practicum.main.service.user.User;
import ru.practicum.main.service.user.UserMapper;
import ru.practicum.stats.dto.StatsHitDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventMapper {

    public static EventFullDto toEventFullDto(Event event, User user, long confirmedRequests, long views) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(UserMapper.toUserShortDto(user));
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setLocation(LocationMapper.toLocationDto(event.getLocation()));
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn());             //Публикует Админ
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setConfirmedRequests(confirmedRequests);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    public static List<EventShortDto> toEventShortDtoList(List<Event> eventList, List<StatsHitDto> stat) {
        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        Map<String, Long> statsMap = stat.stream()
            .collect(Collectors.toMap(StatsHitDto::getUri, StatsHitDto::getHits));

        for (Event event : eventList) {
            EventShortDto eventShortDto = new EventShortDto();
            eventShortDto.setId(event.getId());
            eventShortDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
            eventShortDto.setAnnotation(event.getAnnotation());
            eventShortDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
            eventShortDto.setEventDate(event.getEventDate());
            eventShortDto.setPaid(event.getPaid());
            eventShortDto.setParticipantLimit(event.getParticipantLimit());
            eventShortDto.setPublishedOn(event.getPublishedOn());
            eventShortDto.setRequestModeration(event.getRequestModeration());
            eventShortDto.setTitle(event.getTitle());
            eventShortDtoList.add(eventShortDto);
        }

        for (EventShortDto eventShortDto : eventShortDtoList) {
            String uri = "/events/" + eventShortDto.getId();
            eventShortDto.setViews(statsMap.getOrDefault(uri, 0L));
        }
        return eventShortDtoList;
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> eventList,
                                                        List<RequestCountDto> requestCountDtoList,
                                                        List<StatsHitDto> stat) {
        Map<Integer, Long> requestCountDtoMap = requestCountDtoList.stream()
                .collect(Collectors.toMap(RequestCountDto::getEventId, RequestCountDto::getRequestCount));

        Map<String, Long> statsMap = stat.stream()
            .collect(Collectors.toMap(StatsHitDto::getUri, StatsHitDto::getHits));

        List<EventFullDto> eventFullDtoList = new ArrayList<>();

        for (Event e : eventList) {
            eventFullDtoList.add(EventMapper.toEventFullDto(e, e.getInitiator(),
                requestCountDtoMap.getOrDefault(e.getId(), 0L), 0));
        }

        for (EventFullDto eventFullDto : eventFullDtoList) {
            String uri = "/events/" + eventFullDto.getId();
            eventFullDto.setViews(statsMap.getOrDefault(uri, 0L));
        }
        return eventFullDtoList;
    }

    public static Collection<String> toUriCollection(Collection<Event> eventCollection) {
        List<String> uriList = new ArrayList<>();
        for (Event e : eventCollection) {
            uriList.add("/events/".concat(e.getId().toString()));
        }
        return uriList;
    }
}
