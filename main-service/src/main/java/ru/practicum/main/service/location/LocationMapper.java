package ru.practicum.main.service.location;

public class LocationMapper {

    public static LocationDto toLocationDto (Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
