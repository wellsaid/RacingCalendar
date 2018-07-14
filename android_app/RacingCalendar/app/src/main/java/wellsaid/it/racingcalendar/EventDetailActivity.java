package wellsaid.it.racingcalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DrawableUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;

public class EventDetailActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.error_text_view)
    TextView errorTextView;

    @BindView(R.id.progressSpinner)
    ProgressBar progressSpinner;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    /* the adapter for the recycler view */
    //SessionAdapter sessionAdapter;

    /* the event to show */
    RacingCalendar.Event event;

    /* the series of the event to show */
    RacingCalendar.Series series;

    /* the sessions of this event */
    List<RacingCalendar.Session> sessionList;

    /* helper method to set the toolbar */
    private void setToolbar(){
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(event.eventName);
            }
        });
    }

    /* helper method called when sessions has been retrieved */
    private void sessionsRetrieved(){
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                progressSpinner.setVisibility(View.GONE);

                if(sessionList == null){
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);

                    /* add them to the adapter */
                    // sessionAdapter.add(sessionList);
                }
            }
        });
    }

    /**
     * The keys to required data in the bundle for the activity to start
     */
    public static final String EVENT_BUNDLE_KEY = "event";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        /* bind the views */
        ButterKnife.bind(this);

        /* Associate the adapter and the layout manager to the recycler view */
        //sessionAdapter = new EventAdapter(this);
        //recyclerView.setAdapter(sessionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /* retrieve the event to show in the bundle */
        Bundle inputBundle = getIntent().getExtras();
        if(inputBundle != null && !inputBundle.containsKey(EVENT_BUNDLE_KEY)) {
            throw new IllegalArgumentException();
        }

        event = Parcels.unwrap(inputBundle.getParcelable(EVENT_BUNDLE_KEY));

        final Context context = this;
        /* get the series and sessions of this event
         * (from local database if favorite, otherwise from the server)
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                RacingCalendarDatabase db =
                        RacingCalendarDatabase.getDatabaseFromContext(context);

                series = db.getSeriesDao().getByShortName(event.seriesShortName);
                if(series != null){
                    setToolbar();
                } else {
                    RacingCalendarGetter.getSeries(event.seriesShortName,
                            new RacingCalendarGetter.Listener<RacingCalendar.Series>() {
                                @Override
                                public void onRacingCalendarObjectsReceived(
                                        List<RacingCalendar.Series> list) {
                                    setToolbar();
                                }
                            });
                }

                sessionList = db.getSessionDao()
                        .getAllOfEvent(event.ID, event.seriesShortName);
                if(sessionList != null){
                    sessionsRetrieved();
                } else {
                    RacingCalendarGetter.getSessionOfEvent(event.ID, event.seriesShortName,
                            new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                        @Override
                        public void onRacingCalendarObjectsReceived(List<RacingCalendar.Session> list) {
                            sessionsRetrieved();
                        }
                    });
                }
            }
        }).start();
    }
}
