package wellsaid.it.racingcalendardata;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

public class RacingCalendarGetterTest {

    RacingCalendar.Series series = null;
    RacingCalendar.Event event = null;
    RacingCalendar.Session session = null;

    @Test
    public void getSeriesTest(){
        /* CountDownLatch used to wait for response */
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        /* Create a DataListener */
        RacingCalendarGetter.Listener<RacingCalendar.Series> listener =
                new RacingCalendarGetter.Listener<RacingCalendar.Series>() {
                    @Override
                    public void onRacingCalendarObjectsReceived(List<RacingCalendar.Series> list) {
                        series = list.get(0);

                        /* release the latch */
                        countDownLatch.countDown();
                    }
                };

        /* Test correct usage */
        RacingCalendarGetter.getSeries("f1", listener);

        /* Wait for the response to arrive */
        try {
            countDownLatch.await();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        assertTrue(series.shortName.equals("f1"));
    }

    @Test
    public void getEventsTest(){
        /* CountDownLatch used to wait for response */
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        /* Create a DataListener */
        RacingCalendarGetter.Listener<RacingCalendar.Event> listener =
                new RacingCalendarGetter.Listener<RacingCalendar.Event>() {
                    @Override
                    public void onRacingCalendarObjectsReceived(List<RacingCalendar.Event> list) {
                        event = list.get(0);

                        /* release the latch */
                        countDownLatch.countDown();
                    }
                };

        /* Test correct usage */
        RacingCalendarGetter.getEvents("1", "f1", listener);

        /* Wait for the response to arrive */
        try {
            countDownLatch.await();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        assertTrue(event.circuitName.equals("Melbourne Grand Prix Circuit "));
    }

    @Test
    public void getSessionTest(){
        /* CountDownLatch used to wait for response */
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        /* Create a DataListener */
        RacingCalendarGetter.Listener<RacingCalendar.Session> listener =
                new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                    @Override
                    public void onRacingCalendarObjectsReceived(List<RacingCalendar.Session> list) {
                        session = list.get(0);

                        /* release the latch */
                        countDownLatch.countDown();
                    }
                };

        /* Test correct usage */
        RacingCalendarGetter.getSession ("fp1", "f1",
                "1", listener);

        /* Wait for the response to arrive */
        try {
            countDownLatch.await();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        assertTrue(session.completeName.equals("Free Practice 1"));
    }
}
