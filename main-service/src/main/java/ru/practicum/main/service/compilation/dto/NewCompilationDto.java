package ru.practicum.main.service.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    @NotNull
    Set<Integer> events;

    @NotNull
    Boolean pinned;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    String title;
}
