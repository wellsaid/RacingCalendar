package wellsaid.it.racingcalendar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;

public class AllSeriesTab extends Fragment
        implements RacingCalendarGetter.Listener<RacingCalendar.Series> {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    /* The adapter for the recycler view */
    private SeriesAdapter seriesAdapter;

    /* required empty constructor */
    public AllSeriesTab() {}

    /**
     * Creates a new instance of this fragment
     */
    public static AllSeriesTab newInstance() {
        return new AllSeriesTab();
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

        /* Bind the views of this fragment */
        ButterKnife.bind(this, view);

        /* Associate the adapter and the layout manager to the recycler view */
        seriesAdapter = new SeriesAdapter(getContext());
        recyclerView.setAdapter(seriesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /* Start retrieval of the series from the server */
        RacingCalendarGetter.getSeries(null, this);

        /* Return the inflated fragment to the caller */
        return view;
    }

    @Override
    public void onRacingCalendarObjectsReceived(final List<RacingCalendar.Series> list) {
        /* When the list has been retrieved: pass it to the adapter */
        Handler mainHandler = new Handler(getContext().getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                seriesAdapter.add(list);
            }
        };
        mainHandler.post(myRunnable);
    }
}
