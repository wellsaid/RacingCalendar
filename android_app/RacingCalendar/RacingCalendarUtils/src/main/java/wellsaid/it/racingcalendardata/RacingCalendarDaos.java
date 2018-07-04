package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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

        @Query("SELECT * FROM Series WHERE favorite IS 1")
        List<Series> getAllFavorites();

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

        @Query("SELECT * FROM Session WHERE notify IS 1 ORDER BY startDateTime")
        List<Session> getAllNotify();

        @Query("SELECT * FROM Session WHERE shortName IN (:shortName) " +
                "AND eventID IN (:eventID) " +
                "AND seriesShortName IN (:seriesShortName)")
        Session getByShortNameIDAndSeriesShortName(String shortName,
                                                   String eventID,
                                                   String seriesShortName);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(Session session);

        @Insert
        void insert(Session session);

        @Insert
        void insertAll(List<Session> sessions);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdateAll(List<Session> sessions);

        @Update
        void update(Session session);

        @Delete
        void delete(Session session);
    }

}
