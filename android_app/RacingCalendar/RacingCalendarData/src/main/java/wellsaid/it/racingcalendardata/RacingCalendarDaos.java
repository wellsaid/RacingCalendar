package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import wellsaid.it.racingcalendardata.RacingCalendar.*;

public class RacingCalendarDaos {

    @Dao
    public interface SeriesTypeDao {
        @Query("SELECT * FROM SeriesType")
        List<SeriesType> getAll();

        @Query("SELECT * FROM SeriesType WHERE shortName IN (:shortName)")
        SeriesType getByShortName(String shortName);

        @Insert
        void insert(SeriesType seriesType);

        @Delete
        void delete(SeriesType seriesType);
    }

}
