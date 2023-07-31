package ru.practicum.main.service.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.service.compilation.dto.CompilationDto;
import ru.practicum.main.service.compilation.dto.NewCompilationDto;
import ru.practicum.main.service.compilation.dto.UpdateCompilationDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class CompilationController {

    private final CompilationService compilationService;

    @Autowired
    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Админ добавляет подборку событий newCompilationDto = {} ", newCompilationDto);
        return compilationService.createCompilation(newCompilationDto);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable int compId, @RequestBody @Valid UpdateCompilationDto updateCompilationDto) {
        log.info("Админ обновляет подборку событий c compId = {} updateCompilationDto = {}",compId, updateCompilationDto);
        return compilationService.updateCompilation(compId, updateCompilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int compId) {
        log.info("Админ Удаляет подборка с идентификатором: {}", compId);
        compilationService.delete(compId);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> findAll(@RequestParam(value = "pinned", required = false) Boolean pinned,
                              @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                              @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<CompilationDto> compilationDtoList = compilationService.findAll(pinned, from, size);
        log.info("Количество подборок в текущий момент: {}", compilationDtoList.size());
        return compilationDtoList;
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto findAll(@PathVariable int compId) {
        log.info("КИщется подборка по compId = {}", compId);
        return compilationService.getCompilationById(compId);
    }
}
