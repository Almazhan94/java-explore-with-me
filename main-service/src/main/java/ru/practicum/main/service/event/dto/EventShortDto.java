package ru.practicum.main.service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {

    Integer id;

    UserShortDto initiator;

    String annotation;

    CategoryDto category;

    LocalDateTime eventDate;

    Boolean paid;

    Integer participantLimit;

    LocalDateTime publishedOn;

    Boolean requestModeration;  // Если true, то все заявки будут ожидать подтверждения инициатором события. Если false
                                // - то будут подтверждаться автоматически.

    String title;

    Long views;
}
