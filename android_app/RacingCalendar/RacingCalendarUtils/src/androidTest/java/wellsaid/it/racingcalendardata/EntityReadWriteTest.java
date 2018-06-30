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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class EntityReadWriteTest {
    private RacingCalendarDaos.SeriesTypeDao seriesTypeDao;
    private RacingCalendarDatabase database;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, RacingCalendarDatabase.class).build();
        seriesTypeDao = database.getSeriesTypeDao();
    }

    @Test
    public void writeReadSeriesType() {
        RacingCalendar.SeriesType seriesType = new RacingCalendar.SeriesType(
                "formula",
                "Formula open-wheel racing",
                "Words words... other words",
                null);

        seriesTypeDao.insert(seriesType);
        RacingCalendar.SeriesType byName = seriesTypeDao.getByShortName("formula");
        assertThat(byName.shortName, equalTo(seriesType.shortName));
    }

    @After
    public void closeDb() {
        database.close();
    }
}
