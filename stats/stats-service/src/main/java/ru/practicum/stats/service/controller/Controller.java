package ru.practicum.stats.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.CreateStatDto;
import ru.practicum.stats.dto.StatsHitDto;
import ru.practicum.stats.service.service.StatService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class Controller {

    private final StatService statService;

    @Autowired
    public Controller(StatService statService) {
        this.statService = statService;
    }

    @PostMapping(path = "/hit")
    public void createHit(@RequestBody @Valid CreateStatDto createStatDto) {
        log.info("Сохранение информации в базу статистики: {}", createStatDto);
        statService.createHit(createStatDto);
    }

    @GetMapping(path = "/stats")
    public List<StatsHitDto> getStats(@RequestParam(value = "start") String start,
                                      @RequestParam(value = "end") String end,
                                      @RequestParam(value = "uris", required = false) List<String> uris,
                                      @RequestParam(value = "unique", required = false, defaultValue = "false")
                                      Boolean unique) {
        log.info("Ищется статистика с параметрами start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statService.getStats(start, end, uris, unique);
    }
}
