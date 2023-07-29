package ru.practicum.main.service.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByInitiatorId(int initiatorId, Pageable pageable);

    List<Event> findByInitiatorIdInOrStateInOrCategoryIdInOrEventDateBetween(List<Integer> users, List<State> state, List<Integer> categories,
                                                                             LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query (" select e from Event e " +
        " where (upper(e.annotation) like upper(concat('%', ?1, '%')) " +
        " or upper(e.description) like upper(concat('%', ?1, '%'))) " +
        " and (e.category.id IN (?2)) " +
        " and (e.paid = ?3) " +
        " and e.eventDate BETWEEN ?4 and ?5 " +
        " ORDER BY e.eventDate DESC")
    List<Event> findByTextCategoriesPaidStartEndSortByEventDate(String text, List<Integer> categories, Boolean paid, LocalDateTime start, LocalDateTime end,
                                                                Pageable pageable);

    @Query (" select e from Event e " +
        " where (upper(e.annotation) like upper(concat('%', ?1, '%')) " +
        " or upper(e.description) like upper(concat('%', ?1, '%'))) " +
        " and (e.category.id IN (?2)) " +
        " and (e.paid = ?3) " +
        " and e.eventDate >= ?4 ")
    List<Event> findByTextCategoriesPaidEventDateAfterSortByEventDate(String text, List<Integer> categories, Boolean paid,
                                                                      LocalDateTime now, Pageable pageable);

}
