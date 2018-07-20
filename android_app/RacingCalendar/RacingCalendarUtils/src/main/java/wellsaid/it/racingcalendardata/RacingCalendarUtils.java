package wellsaid.it.racingcalendardata;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class which will contain helper methods to perform
 * recurrent operations in the app
 */
public class RacingCalendarUtils {

    /**
     * Helper method to perform operation when a session becomes notify or un-notify
     */
    public static void sessionNotifyStatusChanged(final Context context,
                                                  final RacingCalendar.Session session){
        final RacingCalendarDatabase db =
                RacingCalendarDatabase.getDatabaseFromContext(context);
        final RacingCalendarNotifier racingCalendarNotifier =
                RacingCalendarNotifier.getInstance();

        /* if the session just becomed notify */
        if(session.notify) {
            /* add it to the local database */
            db.getSessionDao().insertOrUpdate(session);

            /* subscribe to it */
            racingCalendarNotifier.addSessionNotification(context, session);

            final CountDownLatch countDownLatch = new CountDownLatch(2);

            /* add its event into the database (if not exists) */
            RacingCalendarGetter.getEvents(session.eventID, session.seriesShortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Event>() {
                        @Override
                        public void onRacingCalendarObjectsReceived(List<RacingCalendar.Event> list) {
                            /* when ready load them into the database */
                            db.getEventDao().insertOrUpdateAll(list);

                            countDownLatch.countDown();
                        }
                    });

            /* add its series into the database (if not exists) */
            RacingCalendarGetter.getSeries(session.seriesShortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Series>() {
                        @Override
                        public void onRacingCalendarObjectsReceived(List<RacingCalendar.Series> list) {
                            /* when ready load them into the database */
                            db.getSeriesDao().insertOrUpdateAll(list);

                            countDownLatch.countDown();
                        }
                    });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        /* if the session just becomed un-notify */
        } else {
            /* if it is from a favorite series */
            RacingCalendar.Series series = db.getSeriesDao().getByShortName(session.seriesShortName);
            if(series != null && series.favorite){
                /* simply mark it has unfavorite */
                db.getSessionDao().insertOrUpdate(session);
            /* if it is not from a favorite serie */
            } else {
                RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();

                /* remove it from the database */
                sessionDao.delete(session);

                /* if it was the last of the event */
                List<RacingCalendar.Session> sessionList =
                        sessionDao.getAllOfEvent(session.eventID, session.seriesShortName);
                if(sessionList == null || sessionList.size() == 0){
                    RacingCalendar.Event event = db.getEventDao()
                            .getByIDAndSeriesShortName(session.eventID, session.seriesShortName);

                    /* remove the event from the database */
                    eventNotifyStatusChanged(context, event, false);
                }
            }
        }
    }

    /**
     * Helper method to perform operation when an event becomes notify or un-notify
     */
    public static void eventNotifyStatusChanged(final Context context,
                                                final RacingCalendar.Event event,
                                                final boolean hasSessionToNotify){
        final RacingCalendarDatabase db =
                RacingCalendarDatabase.getDatabaseFromContext(context);
        final RacingCalendarNotifier racingCalendarNotifier =
                RacingCalendarNotifier.getInstance();

        /* if the event just becomed notify */
        if(hasSessionToNotify){
            /* add it to the local database */
            db.getEventDao().insertOrUpdate(event);

            final CountDownLatch countDownLatch = new CountDownLatch(2);

            /* add its series into the database (if not exists) */
            RacingCalendarGetter.getSeries(event.seriesShortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Series>() {
                @Override
                public void onRacingCalendarObjectsReceived(List<RacingCalendar.Series> list) {
                    /* when ready load them into the database */
                    db.getSeriesDao().insertOrUpdateAll(list);

                    countDownLatch.countDown();
                }
            });

            /* download and add all its sessions in the local database */
            RacingCalendarGetter.getSessionOfEvent(event.ID, event.seriesShortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                @Override
                public void onRacingCalendarObjectsReceived(List<RacingCalendar.Session> list) {
                    /* when ready load them into the database */
                    db.getSessionDao().insertOrUpdateAll(list);

                    /* subscribe to all */
                    racingCalendarNotifier.addSessionsNotifications(context, list);

                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        /* if the event just becomed un-notify */
        } else {
            final RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();
            List<RacingCalendar.Session> sessionList = sessionDao
                    .getAllOfEvent(event.ID, event.seriesShortName);
            /* un-subscribe from all sessions */
            racingCalendarNotifier.removeSessionNotifications(context, sessionList);

            /* remove event and session from the local database if not of a favorite series */
            if(!db.getSeriesDao().getAllFavorites().contains(
                    new RacingCalendar.Series(event.seriesShortName,
                            null,
                            null,
                            null,
                            null,
                            null))){
                db.getEventDao().delete(event);

                sessionDao.deleteAll(sessionList);
            }
        }
    }

    /**
     * Helper method to perform operations when a series becomes favorite or un-favorite
     *
     */
    public static void seriesFavoriteStatusChanged(final Context context,
                                                   final LifecycleOwner activity,
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
            final CountDownLatch countDownLatch = new CountDownLatch(1);
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

                                            countDownLatch.countDown();
                                        }
                                    });
                        }
                    });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        /* if the series just becomed un-favorite */
        } else {
            final RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();

            /* un-subscribe from all sessions */
            LiveData<List<RacingCalendar.Session>> sessionsList =
                    sessionDao.getAllOfSeries(series.shortName);
            sessionsList.observe(activity, new Observer<List<RacingCalendar.Session>>() {
                @Override
                public void onChanged(@Nullable List<RacingCalendar.Session> sessions) {
                    racingCalendarNotifier.removeSessionNotifications(context, sessions);

                    /* remove all sessions of that series from local database */
                    sessionDao.deleteAllOfSeries(series.shortName);

                    /* remove all events of that series from local database */
                    db.getEventDao().deleteAllOfSeries(series.shortName);

                    /* remove it from the local database */
                    db.getSeriesDao().delete(series);
                }
            });
        }
    }
}
