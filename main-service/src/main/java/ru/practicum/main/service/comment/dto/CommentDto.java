package ru.practicum.main.service.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    Integer id;

    String text;

    String authorName;

    LocalDateTime created;

    LocalDateTime lastUpdatedOn;
}
