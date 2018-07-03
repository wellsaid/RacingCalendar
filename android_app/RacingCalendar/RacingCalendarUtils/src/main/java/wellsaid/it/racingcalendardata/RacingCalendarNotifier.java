package wellsaid.it.racingcalendardata;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class used to manage notification of events
 */
public class RacingCalendarNotifier {

    /*
     * IDEA PER SALVARE SESSIONI:
     * Utilizzare il database già esistente e impostare un field booleano "notify"
     * Quando abbiamo bisogno della lista possiamo richiederla ordinata per data/ora di inizio
     * Quando dobbiamo fare il "pop" possiamo:
     *   - Impostare "notify" a false se la sessione fa parte di una serie preferita
     *   - Cancellare la riga se la sessione non fa parte di una serie preferita
     */

    /* Helper class, it will receive the intent when a notification has to be triggered */
    public static class RCAlarmReceiver extends BroadcastReceiver {

        public static int SESSION_START_REQ = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Eccomi qua!", Toast.LENGTH_LONG).show();

            /* TODO: 1. Retrieve session and related objects */

            /* TODO: 2. Show notification for the Series/Event/Session */

            /* TODO: 3. Get list of sessions */

            /* TODO: 4. Pull element from the list */

            /* TODO: 5. Schedule wake up for first element in the list */
        }
    }

    /**
     * Clear all the session notifications
     * @param context
     *     The context in which the method is executed
     */
    public static void clearSessions(Context context){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Get list of all sessions and clear it */
        removeSessionNotification(context, db.getSessionDao().getAll());

        /* TODO: 2. Clear next scheduled wake up */
    }

    /**
     * Add a new session to the notifications list
     * @param context
     *     The context in which the method is executed
     * @param session
     *     The session to add
     */
    public static void addSessionNotification(Context context, RacingCalendar.Session session){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Insert (or update) this session to the database as one to be notified */
        session.notify = true;
        db.getSessionDao().insertOrUpdate(session);
    }

    /**
     * Add a list of session to the notifications list
     * @param context
     *     The context in which the method is executed
     * @param sessions
     *     The sessions to add
     */
    public static void addSessionsNotifications(Context context,
                                               List<RacingCalendar.Session> sessions){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Insert (or update) this sessions to the database as one to be notified */
        for(RacingCalendar.Session session : sessions){
            session.notify = true;
        }
        db.getSessionDao().insertOrUpdateAll(sessions);
    }

    /**
     * Removes a session from the notifications list
     * @param context
     *     The context in which the method is executed
     * @param session
     *     The session to remove
     */
    public static void removeSessionNotification(Context context, RacingCalendar.Session session){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Retrieve the list of favorite series */
        List<RacingCalendar.Series> favSeriesList = db.getSeriesDao().getAll();

        /* If it is of a favorite series simply put notify = false */
        if(favSeriesList.contains(
                new RacingCalendar.Series(session.seriesShortName,
                        null,
                        null,
                        null,
                        null,
                        null))) {

            session.notify = true;
            db.getSessionDao().update(session);
        /* Otherwise remove it from the database */
        } else {
            db.getSessionDao().delete(session);
        }
    }

    /**
     * Removes a list of sessions from the notifications list
     * @param context
     *     The context in which the method is executed
     * @param sessions
     *     The list of session to remove
     */
    public static void removeSessionNotification(Context context,
                                                 List<RacingCalendar.Session> sessions){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Retrieve the list of favorite series */
        List<RacingCalendar.Series> favSeriesList = db.getSeriesDao().getAll();

        /* For each session passed */
        for(RacingCalendar.Session session : sessions) {
            /* If it is of a favorite series simply put notify = false */
            if (favSeriesList.contains(
                    new RacingCalendar.Series(session.seriesShortName,
                            null,
                            null,
                            null,
                            null,
                            null))) {

                session.notify = false;
                db.getSessionDao().update(session);
                /* Otherwise remove it from the database */
            } else {
                db.getSessionDao().delete(session);
            }
        }
    }

    /**
     * Starts the notification process
     * @param context
     *     The context in which the method is executed
     */
    public static void startNotifications(Context context) throws InterruptedException{
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Get the next session */
        RacingCalendar.Session nextSession = db.getSessionDao().getAll().get(1);

        /* Get the Alarm Service */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null) {

            /* Create an Intent and set the class that will execute when the Alarm triggers */
            Intent intentAlarm = new Intent(context, RCAlarmReceiver.class);

            /* Schedule wake up */
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextSession.startDateTime.getTime(),
                    PendingIntent.getBroadcast(
                            context,
                            RCAlarmReceiver.SESSION_START_REQ,
                            intentAlarm,
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    /**
     * Stops the notification process
     * @param context
     *     The context in which the method is executed
     */
    public static void stopNotifications(Context context){
        /* TODO: 1. Clear next scheduled wake up */
    }

}
