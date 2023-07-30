package ru.practicum.main.service.compilation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.service.compilation.dto.CompilationDto;
import ru.practicum.main.service.compilation.dto.NewCompilationDto;
import ru.practicum.main.service.compilation.dto.UpdateCompilationDto;
import ru.practicum.main.service.error.ObjectNotFoundException;
import ru.practicum.main.service.event.Event;
import ru.practicum.main.service.event.EventRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsHitDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        compilationRepository.save(compilation);

        List<String> uriList = new ArrayList<>();
        if (newCompilationDto.getEvents().isEmpty()) {
            return CompilationMapper.toCompilationDto(compilation, new ArrayList<>());
        }

        for (Event e : eventList) {
            uriList.add("/events/" + e.getId());
        }
        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            uriList, Boolean.TRUE);

        return CompilationMapper.toCompilationDto(compilation,stat);
    }


    public CompilationDto updateCompilation(int compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
            .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        List<Event> eventList = new ArrayList<>();

        if (updateCompilationDto.getEvents() != null) {
            eventList = eventRepository.findAllById(updateCompilationDto.getEvents());
            compilation.setEventSet(new HashSet<>(eventList));
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }

        Compilation saveCompilation = compilationRepository.save(compilation);

        List<String> uriList = new ArrayList<>();
        for (Event e : saveCompilation.getEventSet()) {
            uriList.add("/events/" + e.getId());
        }

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            uriList, Boolean.TRUE);

        return CompilationMapper.toCompilationDto(saveCompilation, stat);
    }

    public void delete(int compId) {

        Compilation compilation = compilationRepository.findById(compId)
            .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        compilationRepository.delete(compilation);
    }

    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Compilation> compilationList = new ArrayList<>();
        if (pinned != null) {
            compilationList = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilationList = compilationRepository.findAll(pageable).getContent();
        }

        Set<Integer> eventIdSet = new HashSet<>();
        for (Compilation c : compilationList) {
            for (Event e : c.getEventSet()) {
                eventIdSet.add(e.getId());
            }
        }

        List<String> uriList = new ArrayList<>();
        for (Integer id : eventIdSet) {
            uriList.add("/events/" + id);
        }
         List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20),
            LocalDateTime.now().plusYears(100),
             uriList, Boolean.FALSE);

        return CompilationMapper.toCompilationDtoList(compilationList, stat);
    }

    public CompilationDto getCompilationById(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
            .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        List<String> uriList = new ArrayList<>();
        for (Event e : compilation.getEventSet()) {
            uriList.add("/events/" + e.getId());
        }

        List<StatsHitDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
            uriList, Boolean.TRUE);

        return CompilationMapper.toCompilationDto(compilation, stat);
    }
}
