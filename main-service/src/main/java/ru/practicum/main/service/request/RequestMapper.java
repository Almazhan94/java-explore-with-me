package ru.practicum.main.service.request;

import ru.practicum.main.service.request.dto.RequestDto;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {

    public static RequestDto toRequestDto(Request request) {
        return new RequestDto(request.getId(),
            request.getRequester().getId(),
            request.getEvent().getId(),
            request.getStatus(),
            request.getCreated());
    }

    public static List<RequestDto> toRequestDtoList(List<Request> requestList) {
        List<RequestDto> requestDtoList = new ArrayList<>();
        for (Request request : requestList) {
            requestDtoList.add(RequestMapper.toRequestDto(request));
        }
        return requestDtoList;
    }
}
