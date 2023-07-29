package ru.practicum.main.service.compilation;

import ru.practicum.main.service.compilation.dto.CompilationDto;
import ru.practicum.main.service.event.Event;
import ru.practicum.main.service.event.EventMapper;
import ru.practicum.main.service.event.dto.EventShortDto;
import ru.practicum.stats.dto.StatsHitDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CompilationMapper {

    public static CompilationDto toCompilationDto (Compilation compilation, List<StatsHitDto> stat) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setEvents(new HashSet<>(EventMapper.toEventShortDtoList(new ArrayList<>(compilation.getEventSet()), stat)));
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        return compilationDto;
    }

    public static List<CompilationDto> toCompilationDtoList(List<Compilation> compilationList, List<StatsHitDto> stat) {
        List<CompilationDto> compilationDtoList = new ArrayList<>();
        for (Compilation c : compilationList) {
            CompilationDto compilationDto = new CompilationDto();
            compilationDto.setEvents(new HashSet<>(EventMapper.toEventShortDtoList(new ArrayList<>(c.getEventSet()), stat)));
            compilationDto.setId(c.getId());
            compilationDto.setPinned(c.getPinned());
            compilationDto.setTitle(c.getTitle());
            compilationDtoList.add(compilationDto);
        }
        return compilationDtoList;
    }
}
