package ru.practicum.stats.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.service.dto.StatsHitDto;
import ru.practicum.stats.service.model.Stat;

import java.time.LocalDateTime;
import java.util.List;


public interface StatRepository extends JpaRepository<Stat, Integer> {

    List<Stat> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /*@Query(value = "select app, uri, count(uri) as hits " +
        "from stat " +
        "where time_stamp between ?1 " +
        "and ?2 " +
        "group by app, uri, ip " +
        "order by hits desc;", nativeQuery = true)
    List<StatsHitDto> findAllStatsWithoutUri(LocalDateTime start,  LocalDateTime end);
*/
    @Query(value = "select new ru.practicum.stats.service.dto.StatsHitDto(s.app, s.uri, count(s.ip)) " +
        "from Stat as s " +
        "where s.timestamp >= ?1 " +
        "and s.timestamp <= ?2 " +
        "group by s.app, s.uri " +
    "order by count(s.ip) DESC")
    List<StatsHitDto> findAllStats(LocalDateTime start,  LocalDateTime end);

    @Query(value = "select new ru.practicum.stats.service.dto.StatsHitDto(s.app, s.uri, count(s.ip)) " +
        "from Stat as s " +
        "where s.timestamp >= ?1 " +
        "and s.timestamp <= ?2 " +
        "and s.uri in ?3 " +
        "group by s.app, s.uri " +
        "order by count(s.ip) DESC")
    List<StatsHitDto> findStatsWithUri(LocalDateTime start,  LocalDateTime end, List<String> uris);

    @Query(value = "select new ru.practicum.stats.service.dto.StatsHitDto(s.app, s.uri, count(DISTINCT s.ip)) " +
        "from Stat as s " +
        "where s.timestamp >= ?1 " +
        "and s.timestamp <= ?2 " +
        "and s.uri in ?3 " +
        "group by s.app, s.uri " +
        "order by count(s.ip) DESC")
    List<StatsHitDto> findStatsWithUriAndUniqueIp(LocalDateTime start,  LocalDateTime end, List<String> uris);

}
