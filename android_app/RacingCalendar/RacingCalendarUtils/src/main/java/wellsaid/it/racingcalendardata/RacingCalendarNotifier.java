package wellsaid.it.racingcalendardata;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.List;

/**
 * A Singleton class used to manage notification of events
 */
public class RacingCalendarNotifier {

    /* This class will be a singleton */
    private static RacingCalendarNotifier notifierInstance = null;

    private RacingCalendarNotifier(){}

    /**
     * Get the singleton instance
     * @return
     *     The instance of this singleton
     */
    public static RacingCalendarNotifier getInstance(){
        if(notifierInstance == null){
            notifierInstance = new RacingCalendarNotifier();
        }

        return notifierInstance;
    }

    /* The unique id to assign to a notification */
    private static int nextNotificationId = 0;

    /* The next scheduled alarm (its PendingIntent) */
    private PendingIntent nextAlarm = null;

    /**
     * The BroadcastReceiver for scheduled alarms
     */
    public static class RCAlarmReceiver extends BroadcastReceiver {

        /* Helper method to create notification channel under android 8.0 or above */
        private void createNotificationChannel(Context context) {
            /* Create the NotificationChannel, but only on API 26+ */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.channel_name);
                String description = context.getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel =
                        new NotificationChannel(
                                context.getString(R.string.channel_id), name, importance);
                channel.setDescription(description);

                /* Register the channel with the system */
                NotificationManager notificationManager =
                        context.getSystemService(NotificationManager.class);
                if(notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }

        /* Helper method to show a notification */
        private void showNotification(RacingCalendar.Series series,
                                      RacingCalendar.Event event,
                                      RacingCalendar.Session session,
                                      final Context context) {
            createNotificationChannel(context);

            /* Prepare text to show in the notification */
            String textContent = event.eventName + ": " + session.completeName
                    + " " + context.getString(R.string.starting_in) + " "
                    + "15" + " " + /* TODO: put user selected time instead of 15 (taken from shared preference) */
                    context.getString(R.string.minutes);

            /* Prepare notification builder */
            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                            .setContentTitle(series.completeName)
                            .setContentText(textContent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSmallIcon(R.mipmap.ic_launcher);

            /* TODO: Set notification tap action (I think you will have it passed from app module) */

            /* Show notification */
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);
            notificationManager.notify(nextNotificationId++, builder.build());
        }

        /**
         * The method executed on scheduled alarm triggers
         * @param context
         *     The context in which the method is executed
         * @param intent
         *     The intent of this alarm
         */
        @Override
        public void onReceive(final Context context, Intent intent) {
            /* Get database instance from context */
            final RacingCalendarDatabase db =
                    RacingCalendarDatabase.getDatabaseFromContext(context);

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    /* Retrieve session and related objects */
                    List<RacingCalendar.Session> sessions = db.getSessionDao().getAllNotify();
                    RacingCalendar.Session nextSession = sessions.get(0);
                    RacingCalendar.Event event = db.getEventDao()
                            .getByIDAndSeriesShortName(
                                    nextSession.eventID, nextSession.seriesShortName);
                    RacingCalendar.Series series = db.getSeriesDao()
                            .getByShortName(nextSession.seriesShortName);

                    /* Show notification for the Series/Event/Session */
                    showNotification(series, event, nextSession, context);

                    if(notifierInstance != null){
                        /* Remove first session from the list of notify one */
                        notifierInstance.removeSessionNotification(context, nextSession);

                        /* Schedule wake up for next element in the list */
                        notifierInstance.startNotifications(context);
                    }
                }
            });
        }
    }

    /**
     * Clear all the session notifications
     * @param context
     *     The context in which the method is executed
     */
    public void clearSessionsNotification(Context context){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Get list of all sessions and clear it */
        removeSessionNotifications(context, db.getSessionDao().getAllNotify());

        /* Clear next scheduled alarm */
        stopNotifications(context);
    }

    /**
     * Add a new session to the notifications list
     * (it immediately starts notification process if it is the first session added)
     * @param context
     *     The context in which the method is executed
     * @param session
     *     The session to add
     */
    public void addSessionNotification(Context context, RacingCalendar.Session session){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Insert (or update) this session to the database as one to be notified */
        session.notify = true;
        RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();
        sessionDao.insertOrUpdate(session);

        /* Immediately start notification process if this is the first session added */
        if(sessionDao.getAllNotify().size() == 1){
            startNotifications(context);
        }
    }

    /**
     * Add a list of session to the notifications list
     * (it immediately starts notification process if it is the first session added)
     * @param context
     *     The context in which the method is executed
     * @param sessions
     *     The sessions to add
     */
    public void addSessionsNotifications(Context context,
                                               List<RacingCalendar.Session> sessions){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Insert (or update) this sessions to the database as one to be notified */
        for(RacingCalendar.Session session : sessions){
            session.notify = true;
        }
        RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();
        sessionDao.insertOrUpdateAll(sessions);

        /* Immediately start notification process if this is the first session added */
        if(sessionDao.getAllNotify().size() == 1){
            startNotifications(context);
        }
    }

    /**
     * Removes a session from the notifications list
     * @param context
     *     The context in which the method is executed
     * @param session
     *     The session to remove
     */
    public void removeSessionNotification(Context context, RacingCalendar.Session session){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Retrieve the list of favorite series */
        List<RacingCalendar.Series> favSeriesList = db.getSeriesDao().getAllFavorites();

        /* If it is of a favorite series simply put notify = false */
        if(favSeriesList.contains(
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

    /**
     * Removes a list of sessions from the notifications list
     * @param context
     *     The context in which the method is executed
     * @param sessions
     *     The list of session to remove
     */
    public void removeSessionNotifications(Context context,
                                                 List<RacingCalendar.Session> sessions){
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Retrieve the list of favorite series */
        List<RacingCalendar.Series> favSeriesList = db.getSeriesDao().getAllFavorites();

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
    public void startNotifications(Context context) {
        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Get the next session */
        List<RacingCalendar.Session> sessions = db.getSessionDao().getAllNotify();
        if(sessions.size() > 0) {
            RacingCalendar.Session nextSession = sessions.get(0);

            /* Get the Alarm Service */
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {

                /* Create an Intent and set the class that will execute when the Alarm triggers */
                Intent intentAlarm = new Intent(context, RCAlarmReceiver.class);
                nextAlarm = PendingIntent.getBroadcast(
                        context, 0,
                        intentAlarm,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                /* Schedule wake up */
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        nextSession.startDateTime.getTime(), nextAlarm);
            }
        }
    }

    /**
     * Stops the notification process
     * @param context
     *     The context in which the method is executed
     */
    public void stopNotifications(Context context){
        /* Clear next scheduled alarm */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null) {
            alarmManager.cancel(nextAlarm);
        }
    }

}
