package ru.practicum.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsHitDto {

    private String app;

    private String uri;

    private Integer hits;
}
