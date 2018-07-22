package wellsaid.it.racingcalendar.appwidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.parceler.Parcels;

import java.util.List;

import wellsaid.it.racingcalendar.activities.EventDetailActivity;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarUtils;

public class NextEventsWidgetIntentService extends IntentService {

    public static final String LAUNCH_DETAIL_ACTION = "launch_detail";
    public static final String NOTIFY_TOGGLE_ACTION = "notify_toggle";

    public static final String EVENT_BUNDLE_KEY = "event";

    public NextEventsWidgetIntentService() {
        super(NextEventsWidgetIntentService.class.getSimpleName());
    }

    private static void handleNotifyToggleAction(final Context context,
                                          RacingCalendar.Event event){
        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);
        RacingCalendarDaos.SessionDao sessionDao = db.getSessionDao();

        List<RacingCalendar.Session> notifySessions =
                sessionDao.getAllOfEvent(event.ID, event.seriesShortName, 1);
        boolean hasSessionToNotify = notifySessions.size() > 0;

        /* Perform operations on notify status change */
        RacingCalendarUtils.eventNotifyStatusChanged(context, event, !hasSessionToNotify);


        /* update the widgets */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, NextEventsWidgetProvider.class));
        NextEventsWidgetProvider.updateAll(context, appWidgetManager, appWidgetIds);
    }

    private static void handleLaunchDetailAction(Context context, RacingCalendar.Event event){
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_BUNDLE_KEY, Parcels.wrap(event));
        context.startActivity(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null && intent.getAction() != null) {
            RacingCalendar.Event event = Parcels.unwrap(intent.getParcelableExtra(EVENT_BUNDLE_KEY));

            switch (intent.getAction()){
                case NOTIFY_TOGGLE_ACTION:
                    handleNotifyToggleAction(this, event);
                    break;
                case LAUNCH_DETAIL_ACTION:
                    handleLaunchDetailAction(this, event);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
