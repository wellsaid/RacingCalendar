package wellsaid.it.racingcalendar.app_widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NextEventsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new NextEventsWidgetFactory(this.getApplicationContext(),
                intent));
    }
}