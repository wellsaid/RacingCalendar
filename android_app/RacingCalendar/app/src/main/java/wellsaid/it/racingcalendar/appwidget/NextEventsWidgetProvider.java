package wellsaid.it.racingcalendar.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import wellsaid.it.racingcalendar.R;

public class NextEventsWidgetProvider extends AppWidgetProvider {

    public static void updateAll(Context context, AppWidgetManager appWidgetManager,
                                 int[] appWidgetIds){
        for(int appWidgetId : appWidgetIds){
            update(context, appWidgetManager, appWidgetId);
        }
    }

    public static void update(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId){
        Intent svcIntent = new Intent(context, NextEventsWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget = new RemoteViews(context.getPackageName(),
                R.layout.next_events_widget);

        widget.setRemoteAdapter(R.id.widget_list_view, svcIntent);

        /* Set pending intent templates for the elements */
        Intent intent = new Intent(context, NextEventsWidgetIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setPendingIntentTemplate(R.id.widget_list_view, pendingIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);

        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateAll(context, appWidgetManager, appWidgetIds);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}