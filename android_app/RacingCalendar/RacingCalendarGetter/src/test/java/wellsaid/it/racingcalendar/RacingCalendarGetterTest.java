package wellsaid.it.racingcalendar;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import wellsaid.it.racingcalendargetter.RacingCalendarGetter;

import static junit.framework.TestCase.fail;

public class RacingCalendarGetterTest {

    @Test
    public void getSeriesDataTest(){
        /* Create a DataListener */
        RacingCalendarGetter.DataListener dataListener = new RacingCalendarGetter.DataListener() {
            @Override
            public void onRacingCalendarData(List<Object> list) {
                /* TODO: Check the object returned for real */
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
            RacingCalendarGetter.get(RacingCalendarGetter.SERIES_TABLE,
                    Arrays.asList("prova","prova1"),
                    Arrays.asList("prova"),
                    dataListener);

            fail("IllegalArgumentException not trown on wrong selection");
        } catch (IllegalArgumentException e) {
            /* Passed! */
        }

        /* Test correct usage */
        RacingCalendarGetter.get(RacingCalendarGetter.SERIES_TABLE,
                Arrays.asList("short_name"),
                Arrays.asList("motogp"),
                dataListener);
    }
}
