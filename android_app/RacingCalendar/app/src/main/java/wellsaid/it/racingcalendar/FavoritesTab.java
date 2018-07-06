package wellsaid.it.racingcalendar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;

public class FavoritesTab extends Fragment {

    /**
     * Interface to be implemented to listen on favorite status modification
     */
    public interface FavoritesChangeListener {

        /**
         * Called from the adapter when a series changes its favorite status
         * @param series
         *     The series which has changed favorite status
         */
        void onFavoritesChanged(RacingCalendar.Series series);
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.error_text_view)
    TextView errorTextView;

    @BindView(R.id.progressSpinner)
    ProgressBar progressSpinner;

    /* The adapter for the recycler view */
    private SeriesAdapter seriesAdapter;

    /* the listener which will receive updates to favorites change status */
    private FavoritesTab.FavoritesChangeListener listener;

    /* required empty constructor */
    public FavoritesTab() {}

    /**
     * Sets the listener for favorite status changes in this fragment
     * @param listener
     */
    public void setListener(FavoritesChangeListener listener){
        this.listener = listener;
    }

    /**
     * Called when you need to notify to this fragment that a series changed favorite status
     * @param series
     *     The series who changed status
     */
    public void notifyChangeFavoriteStatus(RacingCalendar.Series series){
        seriesAdapter.favoriteStatusChanged(series);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_all_series_tab, container, false);

        /* TODO: Define on click listener for the card to open SeriesDetailActivity */

        /* Bind the views of this fragment */
        ButterKnife.bind(this, view);

        /* Associate the adapter and the layout manager to the recycler view */
        seriesAdapter = new SeriesAdapter(getContext(), true,
                new SeriesAdapter.FavoritesChangeListener() {
            @Override
            public void onFavoritesChanged(RacingCalendar.Series series) {
                /* when a series in the adapter has changed its favorite status ...
                 * ... send it to the listener of this fragment */
                listener.onFavoritesChanged(series);
            }
        });
        recyclerView.setAdapter(seriesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /* Start retrieval of the series from the local database */
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<RacingCalendar.Series> seriesList = RacingCalendarDatabase
                        .getDatabaseFromContext(getContext()).getSeriesDao().getAllFavorites();

                /* When the list has been retrieved */
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /* Stop the spinner */
                        progressSpinner.setVisibility(View.GONE);

                        /* if there was an error in retrieving data */
                        if(seriesList == null){
                            recyclerView.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        /* pass the list to the adapter */
                        seriesAdapter.add(seriesList);
                    }
                });
            }
        }).start();

        /* Return the inflated fragment to the caller */
        return view;
    }
}
