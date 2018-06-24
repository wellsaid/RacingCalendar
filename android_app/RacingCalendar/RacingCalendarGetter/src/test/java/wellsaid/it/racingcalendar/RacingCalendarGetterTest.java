package wellsaid.it.racingcalendar;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import wellsaid.it.racingcalendargetter.RacingCalendar;
import wellsaid.it.racingcalendargetter.RacingCalendarGetter;

import static org.junit.Assert.assertTrue;

public class RacingCalendarGetterTest {

    RacingCalendar.SeriesType seriesType = null;

    @Test
    public void getSeriesTypeTest(){
        /* CountDownLatch used to wait for response */
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        /* Create a DataListener */
        RacingCalendarGetter.Listener<RacingCalendar.SeriesType> listener =
                new RacingCalendarGetter.Listener<RacingCalendar.SeriesType>() {
                    @Override
                    public void onRacingCalendarObjectsReceived(List<RacingCalendar.SeriesType> list) {
                        seriesType = list.get(0);

                        /* release the latch */
                        countDownLatch.countDown();
                    }
        };

        /* Test correct usage */
        RacingCalendarGetter.get("formula", listener);

        /* Wait for the response to arrive */
        try {
            countDownLatch.await();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        assertTrue(seriesType.shortName.equals("formula"));
    }
}
