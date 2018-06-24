package wellsaid.it.racingcalendar;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import wellsaid.it.racingcalendargetter.RacingCalendar;
import wellsaid.it.racingcalendargetter.RacingCalendarGetter;

import static junit.framework.TestCase.fail;

public class RacingCalendarGetterTest {

    @Test
    public void getSeriesDataTest(){
        /* CountDownLatch used to wait for response */
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        /* Create a DataListener */
        RacingCalendarGetter.DataListener dataListener = new RacingCalendarGetter.DataListener() {
            @Override
            public void onRacingCalendarData(List<Object> list) {
                /* TODO: Check the object returned for real */

                /* release the latch */
                countDownLatch.countDown();
            }
        };

        /* Test invalid table */
        try {
            RacingCalendarGetter.get("InvalidTable", null, null, dataListener);
            fail("IllegalArgumentException not trown on invalid input");
        } catch (IllegalArgumentException e) {
            /* Passed! */
        }

        /* Test wrong selection */
        try {
            RacingCalendarGetter.get(RacingCalendar.SERIES_TYPES,
                    Arrays.asList("prova","prova1"),
                    Arrays.asList("prova"),
                    dataListener);

            fail("IllegalArgumentException not trown on wrong selection");
        } catch (IllegalArgumentException e) {
            /* Passed! */
        }

        /* Test correct usage 1 */
        RacingCalendarGetter.get(RacingCalendar.SERIES_TYPES,
                /*Arrays.asList("shortName"),*/ null,
                /*Arrays.asList("formula"),*/ null,
                dataListener);

        /* Wait for the response to arrive */
        try {
            countDownLatch.await();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
