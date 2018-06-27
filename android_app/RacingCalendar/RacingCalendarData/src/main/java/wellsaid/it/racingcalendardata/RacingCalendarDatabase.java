package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import wellsaid.it.racingcalendardata.RacingCalendar.*;
import wellsaid.it.racingcalendardata.RacingCalendarDaos.*;

/**
 * The RacingCalendar database class from which we can obtain all DAOs
 */
@Database(entities = {SeriesType.class}, version = 1, exportSchema = false)
public abstract class RacingCalendarDatabase extends RoomDatabase {

    public abstract SeriesTypeDao getSeriesTypeDao();

    public abstract SeriesDao getSeriesDao();

    public abstract EventDao getEventDao();

    public abstract SessionTypeDao getSessionTypeDao();

    public abstract SessionDao getSessionDao();
}
