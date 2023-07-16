package ru.practicum.stats.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsHitDto {

    private String app;

    private String uri;

    private long hits;
}
