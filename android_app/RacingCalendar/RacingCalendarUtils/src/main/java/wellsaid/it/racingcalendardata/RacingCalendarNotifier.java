package wellsaid.it.racingcalendardata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

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
    private static class RCAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
     */
    public static void startNotifications(){
        /* TODO: 1. Get list of sessions */

        /* TODO: 2. Schedule wake up for first element in the list */
    }

    /**
     * Stops the notification process
     */
    public static void stopNotifications(){
        /* TODO: 1. Clear next scheduled wake up */
    }

}
