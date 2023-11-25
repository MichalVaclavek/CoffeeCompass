package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.domain.weather.WeatherData;
import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

@Mapper
public interface WeatherMapper {

    @Mapping(target = "sunRiseTime", expression = "java(mapLongMillisToLocalTime(weatherData.getSys().getSunrise()))")
    @Mapping(target = "sunSetTime", expression = "java(mapLongMillisToLocalTime(weatherData.getSys().getSunset()))")
    WeatherDTO weatherDataToWeatherDTO(WeatherData weatherData);

    default LocalTime mapLongMillisToLocalTime(Long millis) {
        LocalDateTime sunTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis * 1000),
                TimeZone.getDefault().toZoneId());
        return sunTime.toLocalTime();
    }
}
