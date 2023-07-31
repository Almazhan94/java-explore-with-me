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
        Map<String, StatsHitDto> statsMap = stat.stream()
            .collect(Collectors.toMap(StatsHitDto::getUri, statsHitDto -> statsHitDto));

        for (Event event : eventList) {
            EventShortDto eventShortDto = new EventShortDto();
            eventShortDto.setId(event.getId());
            eventShortDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
            eventShortDto.setAnnotation(event.getAnnotation());
            eventShortDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
            eventShortDto.setEventDate(event.getEventDate());
            eventShortDto.setPaid(event.getPaid());
            eventShortDto.setParticipantLimit(event.getParticipantLimit());
            eventShortDto.setPublishedOn(event.getPublishedOn());             //Публикует Админ
            eventShortDto.setRequestModeration(event.getRequestModeration());
            eventShortDto.setTitle(event.getTitle());
            //eventShortDto.setViews(views);
            eventShortDtoList.add(eventShortDto);
        }
        for (EventShortDto eventShortDto : eventShortDtoList) {
            if (!statsMap.isEmpty()) {
                String uri = "/events/" + eventShortDto.getId();
                if (statsMap.containsKey(uri)) {
                    eventShortDto.setViews(statsMap.get(uri).getHits());
                }
            } else {
                eventShortDto.setViews(0L);
            }
        }
        return eventShortDtoList;
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> eventList,
                                                        List<RequestCountDto> requestCountDtoList,
                                                        List<StatsHitDto> stat) {
        Map<Integer, Long> requestCountDtoMap = requestCountDtoList.stream()
                .collect(Collectors.toMap(RequestCountDto::getEventId, RequestCountDto::getRequestCount));

        Map<String, StatsHitDto> statsMap = stat.stream()
            .collect(Collectors.toMap(StatsHitDto::getUri, statsHitDto -> statsHitDto));

        List<EventFullDto> eventFullDtoList = new ArrayList<>();

        for (Event e : eventList) {
            if (!requestCountDtoMap.isEmpty()) {
                if (requestCountDtoMap.containsKey(e.getId())) {
                    eventFullDtoList.add(EventMapper.toEventFullDto(e, e.getInitiator(),
                        requestCountDtoMap.get(e.getId()), 0));
                }
            } else {
                eventFullDtoList.add(EventMapper.toEventFullDto(e, e.getInitiator(), 0, 0));
            }
        }

        for (EventFullDto eventFullDto : eventFullDtoList) {
            if (!statsMap.isEmpty()) {
                String uri = "/events/" + eventFullDto.getId();
                if (statsMap.containsKey(uri)) {
                    eventFullDto.setViews(statsMap.get(uri).getHits());
                }
            } else {
                eventFullDto.setViews(0L);
            }
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
