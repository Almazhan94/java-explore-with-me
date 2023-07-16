package ru.practicum.stats.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.stats.service.dto.CreateStatDto;
import ru.practicum.stats.service.dto.StatsHitDto;
import ru.practicum.stats.service.model.Stat;
import ru.practicum.stats.service.repository.StatRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatService {

    private final StatRepository statRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public void createHit(CreateStatDto createStatDto) {
        Stat stat = new Stat();
        stat.setApp(createStatDto.getApp());
        stat.setIp(createStatDto.getIp());
        stat.setUri(createStatDto.getUri());
        stat.setTimestamp(createStatDto.getTimestamp());
        statRepository.save(stat);
    }


    public List<StatsHitDto> getStats(String start, String end, List<String> uris, Boolean unique) {

        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);
        List<StatsHitDto> statsHitDto = new ArrayList<>();
        if (uris == null && unique.equals(false)) {
            statsHitDto = statRepository.findAllStats(startTime, endTime);
        }

        if (uris != null && unique.equals(false)) {
           statsHitDto = statRepository.findStatsWithUri(startTime, endTime, uris);
        }

        if (uris != null && unique.equals(true)) {
            statsHitDto = statRepository.findStatsWithUriAndUniqueIp(startTime, endTime, uris);
        }

        return statsHitDto;
    }
}
