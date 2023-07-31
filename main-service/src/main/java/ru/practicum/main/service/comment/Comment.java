package ru.practicum.main.service.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.main.service.event.Event;
import ru.practicum.main.service.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String text;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @CreationTimestamp
    LocalDateTime created;
}

