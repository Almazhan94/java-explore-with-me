package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.CreateStatDto;
import ru.practicum.stats.dto.StatsHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class StatsClient extends BaseClient  {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

       @Autowired
        public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
            super(
                builder
                    .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                    .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                    .build()
            );
        }

        public ResponseEntity<Object> addStat(CreateStatDto createStatDto) {
            return post("/hit", createStatDto);
        }

        public List<StatsHitDto> getStat(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique) {
            Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", String.join(",", uris),
                "unique", unique
            );
            String queryString = "?start={start}&end={end}&uris={uris}&unique={unique}";

            ResponseEntity<StatsHitDto[]> responseEntity = get("/stats" + queryString, null, parameters);

            StatsHitDto[] stats = responseEntity.getBody();

            if (stats != null && stats.length > 0) {
                return Arrays.asList(stats);
            }
            return new ArrayList<>();
        }
}
