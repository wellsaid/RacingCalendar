package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import wellsaid.it.racingcalendardata.RacingCalendar.*;
import wellsaid.it.racingcalendardata.RacingCalendarDaos.*;

/**
 * The RacingCalendar database class from which we can obtain all DAOs
 */
@Database(entities =
            {SeriesType.class, Series.class, Event.class, SessionType.class, Session.class},
        version = 1, exportSchema = false)
@TypeConverters({RacingCalendarDatabaseConverters.class})
public abstract class RacingCalendarDatabase extends RoomDatabase {

    private static final String DB_NAME = "RacingCalendarDb";

    public static RacingCalendarDatabase getDatabaseFromContext(Context context){
        RacingCalendarDatabase db =
                Room.databaseBuilder(
                        context, RacingCalendarDatabase.class, DB_NAME).build();

        return db;
    }

    public abstract SeriesTypeDao getSeriesTypeDao();

    public abstract SeriesDao getSeriesDao();

    public abstract EventDao getEventDao();

    public abstract SessionTypeDao getSessionTypeDao();

    public abstract SessionDao getSessionDao();
}
