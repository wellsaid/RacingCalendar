package wellsaid.it.racingcalendardata;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RacingCalendarNotifierTest {

    private RacingCalendarDaos.SeriesTypeDao seriesTypeDao;
    private RacingCalendarDaos.SeriesDao seriesDao;
    private RacingCalendarDaos.SessionDao sessionDao;
    private RacingCalendarDatabase database;
    private Context context;

    @Before
    public void createDb() {
        context = InstrumentationRegistry.getTargetContext();
        database = RacingCalendarDatabase.getDatabaseFromContext(context);
        seriesDao = database.getSeriesDao();
        seriesTypeDao = database.getSeriesTypeDao();
        sessionDao = database.getSessionDao();
    }

    @Test
    public void addRemoveNotificationTest(){
        /* Add a favorite series */
        RacingCalendar.Series series = new RacingCalendar.Series(
                "f1",
                "Formula 1",
                "formula",
                "Words words... other words",
                null,
                null);
        if(!seriesDao.getAll().contains(series))
            seriesDao.insert(series);

        /* Add sessions to be notified for the favorite series */
        RacingCalendar.Session session1 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "f1",
                null,
                null);
        RacingCalendarNotifier.addSessionNotification(context, session1);

        /* Add session to be notified not for a favorite series */
        RacingCalendar.Session session2 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "motogp",
                null,
                null);
        RacingCalendarNotifier.addSessionNotification(context, session2);

        /* See content of the sessions table */
        List<RacingCalendar.Session> sessions = sessionDao.getAll();

        for(RacingCalendar.Session session: sessions){
            assertTrue(session.notify);
        }

        /* Remove sessions to be notified */
        RacingCalendarNotifier.removeSessionNotification(context,
                Arrays.asList(session1, session2));

        /* Get the two notifications */
        RacingCalendar.Session maybeSession1 = sessionDao.getByShortNameIDAndSeriesShortName(
                "fp1","1","f1");

        assertTrue((maybeSession1 != null) &&
                (maybeSession1.equals(session1)) && (!maybeSession1.notify));

        RacingCalendar.Session maybeSession2 = sessionDao.getByShortNameIDAndSeriesShortName(
                "fp1","1","motogp");

        assertTrue(maybeSession2 == null);
    }

    @After
    public void closeDb() {
        database.close();
    }
}
