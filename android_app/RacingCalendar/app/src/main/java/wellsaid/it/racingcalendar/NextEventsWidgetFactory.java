package wellsaid.it.racingcalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.function.Predicate;

import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;

public class NextEventsWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private List<RacingCalendar.Event> eventList = null;

    private Context context;
    private int[] appWidgetId;

    RacingCalendarDaos.EventDao eventDao = null;
    RacingCalendarDaos.SeriesDao seriesDao = null;
    RacingCalendarDaos.SessionDao sessionDao = null;

    public NextEventsWidgetFactory(Context context, Intent intent) {
        this.context=context;
        appWidgetId = new int[1];
        appWidgetId[0] = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);
        this.eventDao = db.getEventDao();
        this.seriesDao = db.getSeriesDao();
        this.sessionDao = db.getSessionDao();
    }

    @Override
    public void onCreate(){}

    @Override
    public void onDestroy(){}

    @Override
    public int getCount() {
        return (eventList == null)?0:eventList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        /* get event at position */
        RacingCalendar.Event event = eventList.get(position);

        /* get the series of the passed event */
        RacingCalendar.Series series = seriesDao.getByShortName(event.seriesShortName);

        /* get if this event has some session to notify */
        List<RacingCalendar.Session> sessionList =
                sessionDao.getAllOfEvent(event.ID, event.seriesShortName, 1);
        boolean hasSessionToNotify =  (sessionList != null) && (sessionList.size() > 0);

        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.event_card);

        row.setTextViewText(R.id.event_name_text_view, event.eventName);

        row.setTextViewText(R.id.circuit_name_text_view, event.circuitName);

        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        String datesString = context.getString(R.string.dates) + ": "
                + dateFormat.format(event.startDate) +
                " - " + dateFormat.format(event.endDate);

        row.setTextViewText(R.id.event_times_text_view, datesString);

        row.setImageViewResource(R.id.notify_image_button,
                (hasSessionToNotify)?R.mipmap.clock_on:R.mipmap.clock_off);

        /* fill pending intent template */
        Bundle extras = new Bundle();
        extras.putParcelable(EventDetailActivity.EVENT_BUNDLE_KEY, Parcels.wrap(event));
        Intent fillIntent = new Intent();
        fillIntent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.card, fillIntent);

        /* hide not required views */
        row.setViewVisibility(R.id.series_logo_image_view, View.GONE);
        row.setViewVisibility(R.id.notify_image_button, View.GONE);

        return(row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {
        eventList = eventDao.getAll();

        /* remove all past events from the list */
        eventList.removeIf(new Predicate<RacingCalendar.Event>() {
            @Override
            public boolean test(RacingCalendar.Event event) {
                return event.endDate.before(Calendar.getInstance().getTime());
            }
        });
    }
}