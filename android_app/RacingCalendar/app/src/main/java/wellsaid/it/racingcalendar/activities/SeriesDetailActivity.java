package wellsaid.it.racingcalendar.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendar.viewmodels.EventViewModel;
import wellsaid.it.racingcalendar.adapters.EventAdapter;
import wellsaid.it.racingcalendar.R;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;
import wellsaid.it.racingcalendardata.RacingCalendarUtils;

public class SeriesDetailActivity extends AppCompatActivity {

    /* the series to show */
    private RacingCalendar.Series series;

    /* The adapter for the recycler view */
    private EventAdapter eventAdapter;

    /* the toolbar of the activity */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /* image view of the collapsing toolbar */
    @BindView(R.id.toolbar_image_view)
    ImageView toolbarImageView;

    /* the text view which will contain the description */
    @BindView(R.id.description_text_view)
    TextView descriptionTextView;

    /* the recycler view which will contain the list of events */
    @BindView(R.id.calendar_recycler_view)
    RecyclerView calendarRecyclerView;

    /**
     * The keys to required data in the bundle for the activity to start
     */
    public static final String SERIES_BUNDLE_KEY = "series";

    public static final String RECYCLER_VIEW_SAVED_STATE = "recycler_view_saved_state";

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECYCLER_VIEW_SAVED_STATE,
                calendarRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_detail);

        /* bind the views */
        ButterKnife.bind(this);

        /* Associate the adapter and the layout manager to the recycler view */
        eventAdapter = new EventAdapter(this, this, false);
        calendarRecyclerView.setAdapter(eventAdapter);
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        /* restore scroll position of the recycler view */
        if(savedInstanceState != null){
            calendarRecyclerView
                    .getLayoutManager()
                    .onRestoreInstanceState(
                            savedInstanceState.getParcelable(RECYCLER_VIEW_SAVED_STATE));
        }

        /* initialize the action bar */
        setSupportActionBar(toolbar);

        /* set up navigation button in the action bar */
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* retrieve the series to show in the bundle */
        Bundle inputBundle = getIntent().getExtras();
        if(inputBundle != null && !inputBundle.containsKey(SERIES_BUNDLE_KEY)) {
            throw new IllegalArgumentException();
        }

        series = Parcels.unwrap(inputBundle.getParcelable(SERIES_BUNDLE_KEY));

        /* from local database if series is favorite */
        final Context context = this;
        if(series.favorite) {
            EventViewModel eventViewModel =
                    ViewModelProviders.of(this).get(EventViewModel.class);
            eventViewModel.getEvents()
                    .observe(this, new Observer<List<RacingCalendar.Event>>() {
                @Override
                public void onChanged(final @Nullable List<RacingCalendar.Event> events) {
                    /* When the list has been retrieved */
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            /* pass the list to the adapter */
                            eventAdapter.add(events);
                        }
                    });
                }
            });
        /* from the server otherwise */
        } else {
            /* Start retrieval of the events */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RacingCalendarGetter.getEventsOfSeries(series.shortName,
                            new RacingCalendarGetter.Listener<RacingCalendar.Event>() {
                                @Override
                                public void onRacingCalendarObjectsReceived(
                                        final List<RacingCalendar.Event> list) {
                                    /* When the list has been retrieved */
                                    new Handler(context.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            /* pass the list to the adapter */
                                            eventAdapter.add(list);

                                            /* fill the layout with content */
                                            if(actionBar != null){
                                                actionBar.setTitle(series.completeName);
                                            }

                                            Picasso.with(context)
                                                    .load(series.thumbnailURL)
                                                    .placeholder(R.drawable.placeholder)
                                                    .resize(toolbarImageView.getWidth(),300)
                                                    .into(toolbarImageView);

                                            descriptionTextView.setText(series.description);
                                        }
                                    });
                                }
                            });
                }
            }).start();


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu (adds items to the action bar) */
        getMenuInflater().inflate(R.menu.menu_series_detail, menu);

        /* Initialize favorite action color correctly */
        MenuItem favoriteAction = ((MenuBuilder) menu).getActionItems().get(0);
        favoriteAction.setIcon((series.favorite)?R.mipmap.heart_on:R.mipmap.heart_off);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* get the option selected */
        int id = item.getItemId();
        switch (id){
            case R.id.action_favorite:
                /* toggle series favorite status */
                series.favorite = !series.favorite;

                final Context context = this;
                final AppCompatActivity activity = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /* Perform operations on favorite status change */
                        RacingCalendarUtils.seriesFavoriteStatusChanged(context, activity, series);

                        /* notify the adapter of the change */
                        eventAdapter.notifyChangeFavoriteStatus();
                    }
                }).start();

                /* set the icon of the action button */
                item.setIcon((series.favorite)?R.mipmap.heart_on:R.mipmap.heart_off);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        NavUtils.navigateUpFromSameTask(this);
    }
}
