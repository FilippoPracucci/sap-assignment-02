package delivery_service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeConverter {

    public static Calendar getNowAsCalendar() {
        return GregorianCalendar.from(getNowAsZonedDateTime());
    }

    public static ZonedDateTime getNowAsZonedDateTime() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }

    public static ZonedDateTime getZonedDateTime(final Calendar calendar) {
        return ZonedDateTime.ofInstant(
                calendar.toInstant(),
                calendar.getTimeZone().toZoneId()
        );
    }
}
