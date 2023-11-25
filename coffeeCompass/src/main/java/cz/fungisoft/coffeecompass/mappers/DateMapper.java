package cz.fungisoft.coffeecompass.mappers;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
class DateMapper {

    public OffsetDateTime asOffsetDateTime(Timestamp ts) {
        return ts.toInstant()
                 .atOffset(ZoneOffset.UTC);
    }

    public Timestamp asTimeStamp(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return Timestamp.valueOf(offsetDateTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        } else {
            return null;
        }
    }
}
