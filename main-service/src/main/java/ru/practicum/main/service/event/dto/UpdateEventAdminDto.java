package ru.practicum.main.service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.service.event.StateAction;
import ru.practicum.main.service.location.LocationDto;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminDto {

    @Size(min = 20, max = 2000)
    String annotation;

    Integer category;

    @Size(min = 20, max = 7000)
    String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    LocationDto location;

    Boolean paid;           //Нужно ли оплачивать участие

    Integer participantLimit; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    Boolean requestModeration;  // Если true, то все заявки будут ожидать подтверждения инициатором события. Если false
    // - то будут подтверждаться автоматически.

    StateAction stateAction;

    @Size(min = 3, max = 120)
    String title;
}
