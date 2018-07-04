package wellsaid.it.racingcalendardata;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RacingCalendarNotifierTest {

    private RacingCalendarDaos.SeriesTypeDao seriesTypeDao;
    private RacingCalendarDaos.SeriesDao seriesDao;
    private RacingCalendarDaos.SessionDao sessionDao;
    private RacingCalendarDaos.EventDao eventDao;
    private RacingCalendarDatabase database;
    private RacingCalendarNotifier racingCalendarNotifier;
    private Context context;

    @Before
    public void createDb() {
        context = InstrumentationRegistry.getTargetContext();
        database = RacingCalendarDatabase.getDatabaseFromContext(context);
        seriesDao = database.getSeriesDao();
        seriesTypeDao = database.getSeriesTypeDao();
        sessionDao = database.getSessionDao();
        eventDao = database.getEventDao();
        racingCalendarNotifier = RacingCalendarNotifier.getInstance();

        /* Add a favorite series */
        RacingCalendar.Series series = new RacingCalendar.Series(
                "f1",
                "Formula 1",
                "formula",
                "Words words... other words",
                "https://upload.wikimedia.org/wikipedia/en/thumb/4/45/F1_logo.svg/800px-F1_logo.svg.png",
                null);
        series.favorite = true;
        if(!seriesDao.getAll().contains(series))
            seriesDao.insert(series);

        /* Add a non favorite series */
        RacingCalendar.Series series1 = new RacingCalendar.Series(
                "motogp",
                "Moto Grand Prix",
                "moto",
                "Words words... other words",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Moto_Gp_logo.svg/800px-Moto_Gp_logo.svg.png",
                null);
        if(!seriesDao.getAll().contains(series1))
            seriesDao.insert(series1);

        /* Add event to a favorite series */
        RacingCalendar.Event event = new RacingCalendar.Event(
                "1",
                "f1",
                "australia",
                "Rolex Australian Grand Prix",
                "Melbourne Grand Prix Circuit",
                null,
                null);
        if(!eventDao.getAll().contains(event))
            eventDao.insert(event);

        /* Add event to a favorite series */
        RacingCalendar.Event event1 = new RacingCalendar.Event(
                "1",
                "motogp",
                "qatar",
                "Grand Prix of Qatar",
                "Losail International Circuit",
                null,
                null);
        if(!eventDao.getAll().contains(event1))
            eventDao.insert(event1);
    }

    @Test
    public void addRemoveNotificationTest(){
        /* Add sessions to be notified for the favorite series */
        RacingCalendar.Session session1 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "f1",
                null,
                null);
        racingCalendarNotifier.addSessionNotification(context, session1);

        /* Add session to be notified not for a favorite series */
        RacingCalendar.Session session2 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "motogp",
                null,
                null);
        racingCalendarNotifier.addSessionNotification(context, session2);

        /* See content of the sessions table */
        List<RacingCalendar.Session> sessions = sessionDao.getAll();

        for(RacingCalendar.Session session: sessions){
            assertTrue(session.notify);
        }

        /* Remove sessions to be notified */
        racingCalendarNotifier.removeSessionNotification(context,
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

    @Test
    public void startNotificationTest() throws InterruptedException {
        /* Add session to be notified for a non-favorite series */
        RacingCalendar.Session session2 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "motogp",
                new Date(System.currentTimeMillis() + 5*1000),
                null);
        racingCalendarNotifier.addSessionNotification(context, session2);

        /* Add session to be notified for a favorite series */
        RacingCalendar.Session session3 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "f1",
                new Date(System.currentTimeMillis() + 20*1000),
                null);
        racingCalendarNotifier.addSessionNotification(context, session3);

        /* Sleep for 25 seconds */
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Check if we have only session3 */
        List<RacingCalendar.Session> sessions = sessionDao.getAll();
        assertTrue(sessions.size() == 1 &&
                sessions.get(0).equals(session3) &&
                !sessions.get(0).notify);
    }

    @Test
    public void startStopNotificationTest() {
        /* Add session to be notified for a favorite series */
        RacingCalendar.Session session2 = new RacingCalendar.Session(
                "fp1",
                "Free Practice 1",
                "fp",
                "1",
                "f1",
                new Date(System.currentTimeMillis() + 10*1000),
                null);
        racingCalendarNotifier.addSessionNotification(context, session2);

        /* Start the notification process */
        racingCalendarNotifier.startNotifications(context);

        /* Sleep for 5 seconds */
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Stop the notification process */
        racingCalendarNotifier.clearSessionsNotification(context);
    }

    @After
    public void closeDb() {
        database.close();
    }
}
