package ru.practicum.main.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.service.category.Category;
import ru.practicum.main.service.location.Location;
import ru.practicum.main.service.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    String annotation;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    String description;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    Location location;

    Boolean paid;           //Нужно ли оплачивать участие

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    Boolean requestModeration;  // Если true, то все заявки будут ожидать подтверждения инициатором события. Если false
                                // - то будут подтверждаться автоматически.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    State state;

    String title;
}
