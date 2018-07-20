package wellsaid.it.racingcalendardata;

import android.arch.lifecycle.LiveData;
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
    public interface SeriesDao {
        @Query("SELECT * FROM Series")
        LiveData<List<Series>> getAll();

        @Query("SELECT * FROM Series WHERE favorite IS 1")
        List<Series> getAllFavorites();

        @Query("SELECT * FROM Series WHERE shortName IN (:shortName)")
        Series getByShortName(String shortName);

        @Insert
        void insert(Series seriesType);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(Series seriesType);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdateAll(List<Series> seriesType);

        @Delete
        void delete(Series seriesType);
    }

    @Dao
    public interface EventDao {
        @Query("SELECT * FROM Event")
        List<Event> getAll();

        @Query("SELECT * FROM Event WHERE seriesShortName IN (:seriesShortName)")
        LiveData<List<Event>> getAllOfSeries(String seriesShortName);

        @Query("SELECT * FROM Event WHERE ID IN (:ID) AND seriesShortName IN (:seriesShortName)")
        Event getByIDAndSeriesShortName(String ID, String seriesShortName);

        @Insert
        void insert(Event event);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(Event event);

        @Insert
        void insertAll(List<Event> eventList);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdateAll(List<Event> eventList);

        @Delete
        void delete(Event seriesType);

        @Query("DELETE FROM Event WHERE seriesShortName IN (:seriesShortName)")
        void deleteAllOfSeries(String seriesShortName);
    }

    @Dao
    public interface SessionDao {
        @Query("SELECT * FROM Session")
        LiveData<List<Session>> getAll();

        @Query("SELECT * FROM Session WHERE seriesShortName IN (:seriesShortName)")
        LiveData<List<Session>> getAllOfSeries(String seriesShortName);

        @Query("SELECT * FROM Session WHERE eventID IN (:eventID) " +
                "AND seriesShortName IN (:seriesShortName)")
        List<Session> getAllOfEvent(String eventID, String seriesShortName);

        @Query("SELECT * FROM Session WHERE eventID IN (:eventID) " +
                "AND seriesShortName IN (:seriesShortName)" +
                "AND notify IN (:notify)")
        List<Session> getAllOfEvent(String eventID, String seriesShortName, int notify);

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

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdateAll(List<Session> session);

        @Update
        void update(Session session);

        @Delete
        void delete(Session session);

        @Delete
        void deleteAll(List<Session> sessions);

        @Query("DELETE FROM Session WHERE seriesShortName IN (:seriesShortName)")
        void deleteAllOfSeries(String seriesShortName);
    }

}
