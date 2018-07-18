package wellsaid.it.racingcalendar.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendar.R;
import wellsaid.it.racingcalendar.adapters.SeriesAdapter;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;

public class AllSeriesTab extends Fragment
        implements RacingCalendarGetter.Listener<RacingCalendar.Series> {

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

    /* The listener to call when we retrieve the data */
    private RacingCalendarGetter.Listener<RacingCalendar.Series> rcGetterListener;

    /* Will contain the previous network status */
    private boolean hasBeenConnected;

    private static final String RECYCLER_VIEW_SAVED_STATE = "scroll_position";

    /* helper method to check the network connection status */
    private boolean isConnected(){
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo != null && netInfo.isConnected());
        }

        return false;
    }

    /* the listener which will receive updates to favorites change status */
    private AllSeriesTab.FavoritesChangeListener fcListener;

    /* broadcast receiver to receive connectivity actions */
    private NetworkChangeReceiver networkChangeReceiver;

    /* required empty constructor */
    public AllSeriesTab() {}

    /**
     * Sets the listener for favorite status changes in this fragment
     * @param listener
     */
    public void setListener(AllSeriesTab.FavoritesChangeListener listener){
        this.fcListener = listener;
    }

    /**
     * Called when you need to notify to this fragment that a series changed favorite status
     * @param series
     *     The series who changed status
     */
    public void notifyChangeFavoriteStatus(RacingCalendar.Series series){
        seriesAdapter.favoriteStatusChanged(series);
    }

    /* Broadcast receiver used to restart loading data when network connection returns */
    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Check if we are now online (after being offline) */
            if(isConnected() && !hasBeenConnected){
                    /* clear the adapter */
                    seriesAdapter.clear();

                    /* we are! -> restart loading data */
                    errorTextView.setVisibility(View.GONE);
                    progressSpinner.setVisibility(View.VISIBLE);

                    /* Start retrieval of the series from the server */
                    RacingCalendarGetter.getSeries(null, rcGetterListener);
            }

            hasBeenConnected = isConnected();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* initialize the listener as this object */
        rcGetterListener = this;

        /* get the starting network status */
        hasBeenConnected = isConnected();

        /* create the broadcast receiver to receive connectivity actions */
        networkChangeReceiver = new NetworkChangeReceiver();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECYCLER_VIEW_SAVED_STATE,
                recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();

        /* register the broadcast receiver to receive connectivity actions */
        getContext().registerReceiver(networkChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();

        /* unregister the broadcast receiver to receive connectivity actions */
        getContext().unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_all_series_tab, container, false);

        /* Bind the views of this fragment */
        ButterKnife.bind(this, view);

        /* Associate the adapter and the layout manager to the recycler view */
        seriesAdapter = new SeriesAdapter(getContext(), false,
                new SeriesAdapter.FavoritesChangeListener() {
            @Override
            public void onFavoritesChanged(RacingCalendar.Series series) {
                /* when a series in the adapter has changed its favorite status ...
                 * ... send it to the listener of this fragment */
                fcListener.onFavoritesChanged(series);
            }
        });
        recyclerView.setAdapter(seriesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /* restore scroll position of the recycler view */
        if(savedInstanceState != null){
            recyclerView
                    .getLayoutManager()
                    .onRestoreInstanceState(
                            savedInstanceState.getParcelable(RECYCLER_VIEW_SAVED_STATE));
        }

        /* Start retrieval of the series from the server */
        RacingCalendarGetter.getSeries(null, rcGetterListener);

        /* Return the inflated fragment to the caller */
        return view;
    }

    @Override
    public void onRacingCalendarObjectsReceived(final List<RacingCalendar.Series> list) {
        /* When the list has been retrieved */
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /* Stop the spinner */
                progressSpinner.setVisibility(View.GONE);

                /* if there was an error in retrieving data */
                if(list == null){
                    recyclerView.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);

                    /* pass the list to the adapter */
                    seriesAdapter.add(list);
                }
            }
        });
    }
}
