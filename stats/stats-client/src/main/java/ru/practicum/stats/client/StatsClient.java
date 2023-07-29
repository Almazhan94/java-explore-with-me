package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.CreateStatDto;
import ru.practicum.stats.dto.StatsHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class StatsClient extends BaseClient  {

  //  RestTemplate rest;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

       @Autowired
        public StatsClient(@Value("http://localhost:9090") String serverUrl, RestTemplateBuilder builder) {
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
            ResponseEntity<StatsHitDto[]> responseEntity = get("/stats", null, parameters);

            StatsHitDto[] stats = responseEntity.getBody();

            if (stats != null && stats.length > 0){
                return Arrays.asList(stats);
            }
            return new ArrayList<>();
        }


    /*@Autowired
    public StatsClient(@Value("http://localhost:9090") String serverUrl, RestTemplateBuilder builder) {

            builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
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
        ResponseEntity<StatsHitDto[]> responseEntity = get("/stats", parameters);

        StatsHitDto[] stats = responseEntity.getBody();

        if (stats != null && stats.length > 0){
            return Arrays.asList(stats);
        }
        return new ArrayList<>();
    }

    protected  ResponseEntity<StatsHitDto[]> get(String path, @Nullable Map<String, Object> parameters) {
        return getMakeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, parameters, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> statServerResponse;
        try {
            if (parameters != null) {
                statServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                statServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(statServerResponse);
    }

    private <T> ResponseEntity<StatsHitDto[]> getMakeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<StatsHitDto[]> statServerResponse;
        try {
            if (parameters != null) {
                statServerResponse = rest.exchange(path, method, requestEntity, StatsHitDto[].class, parameters);
            } else {
                statServerResponse = rest.exchange(path, method, requestEntity, StatsHitDto[].class);
            }
        } catch (HttpStatusCodeException e) {
            log.info(e.getMessage());
            e.getStackTrace();
            throw new RuntimeException();
        }
        return getPrepareGatewayResponse(statServerResponse);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    private static ResponseEntity<StatsHitDto[]> getPrepareGatewayResponse(ResponseEntity<StatsHitDto[]> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }*/
}
