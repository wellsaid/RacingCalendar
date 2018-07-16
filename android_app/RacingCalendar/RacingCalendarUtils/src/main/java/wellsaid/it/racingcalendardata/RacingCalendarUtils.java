package wellsaid.it.racingcalendardata;

import android.content.Context;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class which will contain helper methods to perform
 * recurrent operations in the app
 */
public class RacingCalendarUtils {

    /**
     * Helper method to perform operations when a series becomes favorite or un-favorite
     *
     */
    public static void seriesFavoriteStatusChanged(final Context context,
                                                   final RacingCalendar.Series series){
        final RacingCalendarDatabase db =
                RacingCalendarDatabase.getDatabaseFromContext(context);
        final RacingCalendarNotifier racingCalendarNotifier =
                RacingCalendarNotifier.getInstance();

        /* if the series just becomed favorite */
        if(series.favorite){
            /* add it to the local database */
            db.getSeriesDao().insertOrUpdate(series);

            /* download and add all its events in the local database */
            final CountDownLatch latch = new CountDownLatch(1);
            RacingCalendarGetter.getEventsOfSeries(series.shortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Event>() {
                        @Override
                        public void onRacingCalendarObjectsReceived(List<RacingCalendar.Event> list) {
                            /* when ready load them into the database */
                            db.getEventDao().insertOrUpdateAll(list);

                            /* download and add all its sessions in the local database */
                            RacingCalendarGetter.getSessionOfSeries(series.shortName,
                                    new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                                        @Override
                                        public void onRacingCalendarObjectsReceived(
                                                List<RacingCalendar.Session> list) {
                                            /* when ready load the into the database */
                                            db.getSessionDao().insertOrUpdateAll(list);

                                            /* subscribe to all */
                                            racingCalendarNotifier
                                                    .addSessionsNotifications(context, list);

                                            latch.countDown();
                                        }
                                    });
                        }
                    });

            try {
                latch.await();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        /* if the series just becomed un-favorite */
        } else {
            RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();

            /* un-subscribe from all sessions */
            racingCalendarNotifier.removeSessionNotifications(context,
                    sessionDao.getAllOfSeries(series.shortName));

            /* remove all sessions of that series from local database */
            sessionDao.deleteAllOfSeries(series.shortName);

            /* remove all events of that series from local database */
            db.getEventDao().deleteAllOfSeries(series.shortName);

            /* remove it from the local database */
            db.getSeriesDao().delete(series);
        }
    }
}
