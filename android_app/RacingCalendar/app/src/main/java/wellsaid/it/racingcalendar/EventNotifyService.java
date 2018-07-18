package wellsaid.it.racingcalendar;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;

import org.parceler.Parcels;

import java.util.List;

import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;
import wellsaid.it.racingcalendardata.RacingCalendarNotifier;

public class EventNotifyService extends IntentService {

    public static String EVENT_BUNDLE_KEY = "event";

    public EventNotifyService() {
        super(EventNotifyService.class.getSimpleName());
    }

    private void notifyImageButtonOnClickListener(final Context context,
                                                  final RacingCalendar.Event event){
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);
        final RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();
        final RacingCalendarDaos.EventDao eventDao = db.getEventDao();

        final RacingCalendarNotifier racingCalendarNotifier = RacingCalendarNotifier.getInstance();

        final List<RacingCalendar.Session> notifySessions =
                sessionDao.getAllOfEvent(event.ID, event.seriesShortName, 1);
        final boolean hasSessionToNotify = notifySessions.size() > 0;

        /* if we had session to notify */
        if(hasSessionToNotify){
            /* remove them from the notifier */
            racingCalendarNotifier.removeSessionNotifications(context, notifySessions);
        } else {
            /* retrieve session to notify */
            RacingCalendarGetter.getSessionOfEvent(
                    event.ID,
                    event.seriesShortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                        @Override
                        public void onRacingCalendarObjectsReceived(
                                List<RacingCalendar.Session> list) {
                            /* add them to the notifier */
                            racingCalendarNotifier.addSessionsNotifications(context, list);
                        }
                    });
        }

        /* update the widgets */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, NextEventsWidgetProvider.class));
        NextEventsWidgetProvider.updateAll(context, appWidgetManager, appWidgetIds);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null) {
            RacingCalendar.Event event = Parcels.unwrap(intent.getParcelableExtra(EVENT_BUNDLE_KEY));
            notifyImageButtonOnClickListener(this, event);

            /* update the widgets */
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(this, NextEventsWidgetProvider.class));
            NextEventsWidgetProvider.updateAll(this, appWidgetManager, appWidgetIds);
        }
    }
}
