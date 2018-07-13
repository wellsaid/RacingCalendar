package wellsaid.it.racingcalendar;

import android.content.Context;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarUtils;

public class SeriesDetailActivity extends AppCompatActivity {

    /* the series to show */
    private RacingCalendar.Series series;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_detail);

        /* bind the views */
        ButterKnife.bind(this);

        /* initialize the action bar */
        setSupportActionBar(toolbar);

        /* set up navigation button in the action bar */
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* retrieve the series to show in the bundle */
        Bundle inputBundle = getIntent().getExtras();
        if(inputBundle != null && !inputBundle.containsKey(SERIES_BUNDLE_KEY)) {
            throw new IllegalArgumentException();
        }

        series = Parcels.unwrap(inputBundle.getParcelable(SERIES_BUNDLE_KEY));

        /* fill the layout with content */
        if(actionBar != null){
            actionBar.setTitle(series.completeName);
        }

        Picasso.with(this)
               .load(series.thumbnailURL)
               .placeholder(R.drawable.placeholder)
               .resize(toolbarImageView.getWidth(),300)
               .into(toolbarImageView);

        descriptionTextView.setText(series.description);

        /* TODO: set adapter and layout manager for the recycler view */
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

                /* Perform operations on favorite status change */
                final Context context = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RacingCalendarUtils.seriesFavoriteStatusChanged(context, series);
                    }
                }).start();

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
