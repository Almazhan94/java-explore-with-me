package ru.practicum.main.service.compilation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.service.compilation.dto.CompilationDto;
import ru.practicum.main.service.compilation.dto.NewCompilationDto;
import ru.practicum.main.service.compilation.dto.UpdateCompilationDto;
import ru.practicum.main.service.error.ObjectNotFoundException;
import ru.practicum.main.service.event.Event;
import ru.practicum.main.service.event.EventMapper;
import ru.practicum.main.service.event.EventRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsHitDto;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    private final StatsClient statsClient;

    @Autowired
    public CompilationService(CompilationRepository compilationRepository, EventRepository eventRepository,
                              StatsClient statsClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.statsClient = statsClient;
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getEvents() == null) {
            newCompilationDto.setEvents(new HashSet<>());
        }
        List<Event> eventList = eventRepository.findAllById(newCompilationDto.getEvents());
        Compilation compilation = new Compilation();
        compilation.setEventSet(new HashSet<>(eventList));
        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(newCompilationDto.getPinned());
        }
        compilation.setTitle(newCompilationDto.getTitle());
        Compilation saveCompilation = compilationRepository.save(compilation);

        if (newCompilationDto.getEvents().isEmpty()) {
            return CompilationMapper.toCompilationDto(saveCompilation, new ArrayList<>());
        }

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            EventMapper.toUriCollection(saveCompilation.getEventSet()), Boolean.TRUE);

        return CompilationMapper.toCompilationDto(compilation,stat);
    }

    @Transactional
    public CompilationDto updateCompilation(int compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
            .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        if (updateCompilationDto.getEvents() != null) {
            List<Event> eventList = eventRepository.findAllById(updateCompilationDto.getEvents());
            compilation.setEventSet(new HashSet<>(eventList));
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        Compilation saveCompilation = compilationRepository.save(compilation);

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            EventMapper.toUriCollection(saveCompilation.getEventSet()), Boolean.TRUE);

        return CompilationMapper.toCompilationDto(saveCompilation, stat);
    }

    @Transactional
    public void delete(int compId) {

        Compilation compilation = compilationRepository.findById(compId)
            .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        compilationRepository.delete(compilation);
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Compilation> compilationList = new ArrayList<>();
        if (pinned != null) {
            compilationList = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilationList = compilationRepository.findAll(pageable).getContent();
        }

        Set<Event> eventSet = new HashSet<>();
        for (Compilation c : compilationList) {
            eventSet.addAll(c.getEventSet());
        }

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            EventMapper.toUriCollection(eventSet), Boolean.FALSE);

        return CompilationMapper.toCompilationDtoList(compilationList, stat);
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
            .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            EventMapper.toUriCollection(compilation.getEventSet()), Boolean.TRUE);

        return CompilationMapper.toCompilationDto(compilation, stat);
    }

    public static Collection<String> toUriCollection(Collection<Event> eventCollection) {
        List<String> uriList = new ArrayList<>();
        for (Event e : eventCollection) {
            uriList.add("/events/".concat(e.getId().toString()));
        }
        return uriList;
    }
}
