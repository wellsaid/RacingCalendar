package wellsaid.it.racingcalendardata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

    /*
     * ATTENZIONE:
     * Bisogna impostare le relazioni tra gli oggetti nella classe RacingCalendar allo scopo di
     * rendere più efficente l'associazione tra sessione e serie quando ti serve
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
     */
    public static void clearSessions(){
        /* TODO: 1. Get list of sessions and clear it */

        /* TODO: 2. Clear next scheduled wake up */
    }

    /**
     * Add a new session to the notifications list
     * @param session
     *     The session to add
     */
    public static void addSessionNotification(RacingCalendar.Session session){
        /* TODO: 1. Get list of session and add session to list <- FROM WHERE? */

        /* TODO: 2. Save new list to be retrieve afterwords <- HOW?? */
    }

    /**
     * Removes a session to the notifications list
     * @param session
     *     The session to remove
     */
    public static void removeSessionNotification(RacingCalendar.Session session){
        /* TODO: 1. Get list of session and remove session from list <- FROM WHERE? */

        /* TODO: 2. Save new list to be retrieve afterwords <- HOW?? */
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
