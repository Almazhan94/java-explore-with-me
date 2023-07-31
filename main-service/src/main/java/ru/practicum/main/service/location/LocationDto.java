package ru.practicum.main.service.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {

    @NotNull
    private Float lat; //Широта

    @NotNull
    private Float lon; //Долгота

}
