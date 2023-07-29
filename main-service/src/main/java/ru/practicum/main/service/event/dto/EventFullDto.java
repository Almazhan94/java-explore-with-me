package ru.practicum.main.service.event.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.service.category.Category;
import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.event.State;
import ru.practicum.main.service.location.Location;
import ru.practicum.main.service.location.LocationDto;
import ru.practicum.main.service.user.User;
import ru.practicum.main.service.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {

    Integer id;

    UserShortDto initiator;

    String annotation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    CategoryDto category;

    String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    LocationDto location;

    Boolean paid;           //Нужно ли оплачивать участие

    Integer participantLimit; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    LocalDateTime publishedOn;

    Boolean requestModeration;  // Если true, то все заявки будут ожидать подтверждения инициатором события. Если false
                                // - то будут подтверждаться автоматически.

    State state;

    String title;

    Long confirmedRequests; //Количество одобренных заявок на участие в данном событии

    Long views;
}
