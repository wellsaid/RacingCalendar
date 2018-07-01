package wellsaid.it.racingcalendardata;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class EntityReadWriteTest {
    private RacingCalendarDaos.SeriesTypeDao seriesTypeDao;
    private RacingCalendarDaos.EventDao eventDao;
    private RacingCalendarDaos.SeriesDao seriesDao;
    private RacingCalendarDatabase database;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, RacingCalendarDatabase.class).build();
        seriesTypeDao = database.getSeriesTypeDao();
        eventDao = database.getEventDao();
        seriesDao = database.getSeriesDao();
    }

    @Test
    public void writeReadTest() {
        RacingCalendar.SeriesType seriesType = new RacingCalendar.SeriesType(
                "formula",
                "Formula open-wheel racing",
                "Words words... other words",
                null);

        seriesTypeDao.insert(seriesType);

        RacingCalendar.Series series = new RacingCalendar.Series(
                "f1",
                "Formula 1",
                "formula",
                "Words words... other words",
                null,
                null);

        seriesDao.insert(series);

        RacingCalendar.Event event = new RacingCalendar.Event(
                "1",
                "f1",
                "monaco",
                "Monaco GP",
                "Monaco",
                new Date(0),
                new Date(0));

        eventDao.insert(event);
        RacingCalendar.Event byName = eventDao.getByIDAndSeriesShortName("1","f1");
        assertThat(byName.eventShortName, equalTo(event.eventShortName));
    }

    @After
    public void closeDb() {
        database.close();
    }
}
