package delivery_service.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class TimeConverter {

    public static Calendar getNowAsCalendar() {
        return new Calendar.Builder().setInstant(Date.from(getNowAsInstant())).build();
    }

    public static Instant getNowAsInstant() {
        return ZonedDateTime.now(ZoneId.systemDefault()).toInstant();
    }
}
