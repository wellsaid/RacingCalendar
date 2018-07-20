package wellsaid.it.racingcalendar.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;

public class EventViewModel extends AndroidViewModel {

    private LiveData<List<RacingCalendar.Event>> eventList;

    public EventViewModel(@NonNull Application application, RacingCalendar.Series series) {
        super(application);
        eventList = RacingCalendarDatabase.getDatabaseFromContext(application)
                .getEventDao().getAllOfSeries(series.shortName);
    }

    public LiveData<List<RacingCalendar.Event>> getEvents(){
        return eventList;
    }
}
