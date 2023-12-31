package ru.practicum.main.service.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.main.service.request.RequestStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestStatusUpdateDto {

    List<Integer> requestIds;

    @Enumerated(EnumType.STRING)
    RequestStatus status;
}
