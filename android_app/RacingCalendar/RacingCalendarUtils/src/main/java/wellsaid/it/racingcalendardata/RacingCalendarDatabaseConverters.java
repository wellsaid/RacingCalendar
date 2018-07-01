package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

class RacingCalendarDatabaseConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
