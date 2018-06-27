package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import wellsaid.it.racingcalendardata.RacingCalendar.*;

/**
 * Class containing all DAOs for the RacingCalendar database
 */
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

    @Dao
    public interface SeriesDao {
        @Query("SELECT * FROM Series")
        List<Series> getAll();

        @Query("SELECT * FROM Series WHERE shortName IN (:shortName)")
        Series getByShortName(String shortName);

        @Insert
        void insert(Series seriesType);

        @Delete
        void delete(Series seriesType);
    }

    @Dao
    public interface EventDao {
        @Query("SELECT * FROM Event")
        List<Event> getAll();

        @Query("SELECT * FROM Event WHERE ID IN (:ID) AND seriesShortName IN (:seriesShortName)")
        Event getByIDAndSeriesShortName(String ID, String seriesShortName);

        @Insert
        void insert(Event seriesType);

        @Delete
        void delete(Event seriesType);
    }

    @Dao
    public interface SessionTypeDao {
        @Query("SELECT * FROM SessionType")
        List<SessionType> getAll();

        @Query("SELECT * FROM SessionType WHERE shortName IN (:shortName)")
        SessionType getByShortName(String shortName);

        @Insert
        void insert(SessionType seriesType);

        @Delete
        void delete(SessionType seriesType);
    }

    @Dao
    public interface SessionDao {
        @Query("SELECT * FROM Session")
        List<Session> getAll();

        @Query("SELECT * FROM Session WHERE shortName IN (:shortName) " +
                "AND seriesShortName IN (:seriesShortName)")
        Event getByIDAndSeriesShortName(String shortName, String eventID, String seriesShortName);

        @Insert
        void insert(Event seriesType);

        @Delete
        void delete(Event seriesType);
    }

}
