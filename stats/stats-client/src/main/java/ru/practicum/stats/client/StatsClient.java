package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.CreateStatDto;

import java.time.LocalDateTime;
import java.util.Map;

public class StatsClient extends BaseClient {


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

    public ResponseEntity<Object> getStat(LocalDateTime start, LocalDateTime end, String uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
            "start", start,
            "end", end,
            "uris", uris,
            "unique", unique
        );
        return get("/stats", null, parameters);
    }
}