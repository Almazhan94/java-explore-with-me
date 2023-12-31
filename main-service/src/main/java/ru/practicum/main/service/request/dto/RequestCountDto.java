package ru.practicum.main.service.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestCountDto {

    Integer eventId;

    Long requestCount;
}
