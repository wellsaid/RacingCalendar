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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A Singleton class used to manage notification of events
 */
public class RacingCalendarNotifier {

    /* This class will be a singleton */
    private static RacingCalendarNotifier notifierInstance = null;

    /* How many minute before the session the user wants to be notified */
    private int minBefore = 15;

    /* The subscribe mode (possible values: SUB_ALL_MODE, SUB_QP_RAC_MODE, SUB_RACE_MODE) */
    private int subMode = SUB_QP_RAC_MODE;

    /* The unique id to assign to a notification */
    private static int nextNotificationId = 0;

    /* The next scheduled alarm (its PendingIntent) */
    private PendingIntent nextAlarm = null;

    /* helper method to check if a series has to be notified */
    private boolean hasToBeNotified(Context context, RacingCalendar.Session session, int subMode){
        switch (subMode){
            case SUB_ALL_MODE:
                return true;
            case SUB_QP_RAC_MODE:
                List<String> posValues =
                        Arrays.asList(context.getString(R.string.qualifying),
                                context.getString(R.string.race));
                return posValues.contains(session.sessionType);
            case SUB_RACE_MODE:
                return session.sessionType.equals(context.getString(R.string.race));
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Subscribes to all sessions of events
     */
    public static final int SUB_ALL_MODE = 1;

    /**
     * Subscribes just to qp and race sessions of events
     */
    public static final int SUB_QP_RAC_MODE = 2;

    /**
     * Subscribes just to race sessions of events
     */
    public static final int SUB_RACE_MODE = 3;

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

    /**
     * Method that sets the configuration for the notification
     * @param minBefore
     *     How many minute before the session the user wants to be notified
     * @param subMode
     *     The subscribe mode (possible values: SUB_ALL_MODE, SUB_QP_RAC_MODE, SUB_RACE_MODE)
     */
    public void setConfiguration(int minBefore, int subMode){
        List<Integer> subModes = Arrays.asList(SUB_ALL_MODE, SUB_QP_RAC_MODE, SUB_RACE_MODE);
        if(minBefore < 0 || !subModes.contains(subMode)){
            throw new IllegalArgumentException();
        }

        this.subMode = subMode;
        this.minBefore = minBefore;
    }

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
                                      final Context context,
                                      int minBefore) {
            createNotificationChannel(context);

            /* Prepare text to show in the notification */
            String textContent = event.eventName + ": " + session.completeName
                    + " " + context.getString(R.string.starting_in) + " "
                    + String.valueOf(minBefore) + " " +
                    context.getString(R.string.minutes);

            /* Prepare notification builder */
            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                            .setContentTitle(series.completeName)
                            .setContentText(textContent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSmallIcon(R.mipmap.ic_launcher);

            /* Show notification */
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);
            notificationManager.notify(nextNotificationId++, builder.build());
        }

        /**
         * Key required in the intent: Minutes before the session to notify
         */
        public static final String MINUTES_BEFORE_KEY = "minutes";

        /**
         * The method executed on scheduled alarm triggers
         * @param context
         *     The context in which the method is executed
         * @param intent
         *     The intent of this alarm
         */
        @Override
        public void onReceive(final Context context, Intent intent) {
            final int minBefore = intent.getIntExtra(MINUTES_BEFORE_KEY, 15);

            /* Get database instance from context */
            final RacingCalendarDatabase db =
                    RacingCalendarDatabase.getDatabaseFromContext(context);

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    /* Retrieve session and related objects */
                    List<RacingCalendar.Session> sessions = db.getSessionDao().getAllNotify();
                    if(sessions != null && sessions.size() > 0) {
                        RacingCalendar.Session nextSession = sessions.get(0);
                        RacingCalendar.Event event = db.getEventDao()
                                .getByIDAndSeriesShortName(
                                        nextSession.eventID, nextSession.seriesShortName);
                        RacingCalendar.Series series = db.getSeriesDao()
                                .getByShortName(nextSession.seriesShortName);

                        /* Show notification for the Series/Event/Session */
                        showNotification(series, event, nextSession, context, minBefore);

                        if (notifierInstance != null) {
                            /* Remove first session from the list of notify one */
                            notifierInstance.removeSessionNotification(context, nextSession);

                            /* Schedule wake up for next element in the list */
                            notifierInstance.startNotifications(context, minBefore);
                        }
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
        /* if this session ends in the past -> stop immediately */
        if(session.endDateTime.before(Calendar.getInstance().getTime())){
            return;
        }

        /* Get database instance from context */
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);

        /* Insert (or update) this session to the database as one to be notified */
        session.notify = hasToBeNotified(context, session, subMode);
        RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();
        sessionDao.insertOrUpdate(session);

        /* Immediately start notification process if this is the first session added */
        if(sessionDao.getAllNotify().size() == 1){
            startNotifications(context, minBefore);
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

        /* get initial number of sessions to notify */
        RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();
        int prevSessions = sessionDao.getAllNotify().size();

        /* Insert (or update) this sessions to the database as one to be notified */
        for(RacingCalendar.Session session : sessions){
            /* if this session ends in the past -> skip it */
            if(session.endDateTime.before(Calendar.getInstance().getTime())){
                continue;
            }

            session.notify = hasToBeNotified(context, session, subMode);
            sessionDao.insertOrUpdate(session);
        }

        /* Immediately start notification process if we had 0 session and now we have some*/
        if(prevSessions == 0 && sessionDao.getAllNotify().size() > 0){
            startNotifications(context, minBefore);
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

        /* mark session as not to be notified in the local database */
        session.notify = false;
        db.getSessionDao().update(session);
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

        /* For each session passed */
        for(RacingCalendar.Session session : sessions) {
            /* mark session as not to be notified in the local database */
            session.notify = false;
            db.getSessionDao().update(session);
        }
    }

    /**
     * Starts the notification process
     * @param context
     *     The context in which the method is executed
     * @param minBefore
     *     How many minute before the session the user wants to be notified
     */
    public void startNotifications(Context context, int minBefore) {
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

                /* add configuration to the intent */
                intentAlarm.putExtra(RCAlarmReceiver.MINUTES_BEFORE_KEY, minBefore);

                /* Schedule wake up */
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        nextSession.startDateTime.getTime() - minBefore*60*1000,
                                 nextAlarm);
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
