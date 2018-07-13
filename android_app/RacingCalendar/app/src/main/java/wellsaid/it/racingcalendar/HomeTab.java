package wellsaid.it.racingcalendar;

import android.os.Bundle;
import android.os.Handler;
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
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;

public class HomeTab extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.error_text_view)
    TextView errorTextView;

    @BindView(R.id.progressSpinner)
    ProgressBar progressSpinner;

    /* The adapter for the recycler view */
    private EventAdapter eventAdapter;

    /* required empty constructor */
    public HomeTab() {}

    /* helper method to toggle error text view and recycler view */
    private void errorDataToggle(){
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /* if you have no favorites */
                if (eventAdapter.getItemCount() == 0) {
                    /* show the error text view */
                    recyclerView.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    /* if you now have some favorites */
                } else {
                    /* show the recycler view */
                    recyclerView.setVisibility(View.VISIBLE);
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Called when you need to notify to this fragment that a series changed favorite status
     * @param series
     *     The series who changed status
     */
    public void notifyChangeFavoriteStatus(RacingCalendar.Series series){
        if(!series.favorite) {
            eventAdapter.notifyChangeFavoriteStatus();

            errorDataToggle();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_home_tab, container, false);

        /* TODO: Define on click listener for the card to open EventDetailActivity */

        /* Bind the views of this fragment */
        ButterKnife.bind(this, view);

        /* Associate the adapter and the layout manager to the recycler view */
        eventAdapter = new EventAdapter(getContext(), true);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /* Start retrieval of the events from the local database */
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<RacingCalendar.Event> eventList = RacingCalendarDatabase
                        .getDatabaseFromContext(getContext()).getEventDao().getAll();

                /* When the list has been retrieved */
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /* Stop the spinner */
                        progressSpinner.setVisibility(View.GONE);

                        /* if we have no favorite series ... */
                        if(eventList == null || eventList.size() == 0) {
                            /* ... show an info in the error text view1 */
                            errorTextView.setVisibility(View.VISIBLE);
                        }

                        /* if we have no favorite series ... */
                        if(eventList == null || eventList.size() == 0) {
                            /* ... show an info in the error text view1 */
                            errorTextView.setVisibility(View.VISIBLE);
                            /* ... otherwise */
                        } else {
                            /* ... show the recycler view */
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        /* pass the list to the adapter */
                        eventAdapter.add(eventList);
                    }
                });
            }
        }).start();

        /* Return the inflated fragment to the caller */
        return view;
    }
}
