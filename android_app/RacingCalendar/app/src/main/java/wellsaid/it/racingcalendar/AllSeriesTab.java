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
import wellsaid.it.racingcalendardata.RacingCalendarGetter;

public class AllSeriesTab extends Fragment
        implements RacingCalendarGetter.Listener<RacingCalendar.Series> {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.error_text_view)
    TextView errorTextView;

    @BindView(R.id.progressSpinner)
    ProgressBar progressSpinner;

    /* The adapter for the recycler view */
    private SeriesAdapter seriesAdapter;

    /* The listener to call when we retrieve the data */
    private RacingCalendarGetter.Listener<RacingCalendar.Series> listener;

    /* Will contain the previous network status */
    private boolean hasBeenConnected;

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

    /* required empty constructor */
    public AllSeriesTab() {}

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
                    RacingCalendarGetter.getSeries(null, listener);
            }

            hasBeenConnected = isConnected();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* initialize the listener as this object */
        listener = this;

        /* get the starting network status */
        hasBeenConnected = isConnected();

        /* register the broadcast receiver to receive connectivity actions
         * (done after the first response to avoid loading two times at start)
         */
        getContext().registerReceiver(new NetworkChangeReceiver(),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
        seriesAdapter = new SeriesAdapter(getContext(), false);
        recyclerView.setAdapter(seriesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /* Start retrieval of the series from the server */
        RacingCalendarGetter.getSeries(null, listener);

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
                }

                /* pass the list to the adapter */
                seriesAdapter.add(list);
            }
        });
    }
}
