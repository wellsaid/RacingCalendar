package wellsaid.it.racingcalendar;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarGetter;
import wellsaid.it.racingcalendardata.RacingCalendarNotifier;

/**
 * The Adapter to show series "cards"
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    /* The view holder for this adapter */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.series_type_icon)
        ImageView seriesTypeIcon;

        @BindView(R.id.event_name_text_view)
        TextView eventNameTextView;

        @BindView(R.id.series_name_text_view)
        TextView seriesNameTextView;

        @BindView(R.id.event_times_text_view)
        TextView eventTimesTextView;

        @BindView(R.id.notify_image_button)
        ImageButton notifyImageButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /* The series which are shown */
    private List<RacingCalendar.Event> eventsList = null;

    /* The context in which the adapter is created */
    private Context context;

    /* the DAO objects used to interact with the database */
    private RacingCalendarDaos.EventDao eventDao;
    private RacingCalendarDaos.SeriesDao seriesDao;
    private RacingCalendarDaos.SessionDao sessionDao;

    /* the notifier object */
    private RacingCalendarNotifier racingCalendarNotifier;

    /* helper method to obtain the drawable based on series type */
    private int getResourceBasedOnType(String seriesType){
        switch (seriesType){
            /*case "Formula":
                return R.drawable.formula_type_icon;
            case "Motorcycle circuit":
                return R.drawable.moto_circuit_type_icon;*/
            default:
                return android.R.drawable.stat_notify_error;
        }
    }

    /**
     * Called when you need to notify to this fragment that a series changed favorite status
     * @param series
     *     The series who changed status
     */
    public void notifyChangeFavoriteStatus(RacingCalendar.Series series){
        if(series.favorite){
            /* get all events of this series */
            List<RacingCalendar.Event> eventList = eventDao.getAllOfSeries(series.shortName);

            /* for each event in the list */
            for(RacingCalendar.Event event : eventList){
                /* get all sessions of this event */
                List<RacingCalendar.Session> sessionList =
                        sessionDao.getAllOfEvent(event.ID, event.seriesShortName);

                /* subscribe to all this event */
                racingCalendarNotifier.addSessionsNotifications(context, sessionList);
            }
        }
    }

    /**
     * Constructor
     * @param context
     *     The context in which the adapter is created
     */
    public EventAdapter(Context context){
        this.context = context;

        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);
        this.eventDao = db.getEventDao();
        this.seriesDao = db.getSeriesDao();
        this.sessionDao = db.getSessionDao();

        this.racingCalendarNotifier = RacingCalendarNotifier.getInstance();
    }

    /**
     * Add a list of events to the adapter
     * @param newEventList
     *     The event list to add
     */
    public void add(List<RacingCalendar.Event> newEventList){
        if(eventsList == null){
            eventsList = newEventList;
        } else {
            eventsList.addAll(newEventList);
        }

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Clears all events from the adapter
     */
    public void clear(){
        if(eventsList != null) {
            eventsList.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        /* Take the series we have to show */
        final RacingCalendar.Event event = eventsList.get(position);

        new Thread(new Runnable() {
            @Override
            public void run() {
                /* Get the series related to the event */
                final RacingCalendar.Series series =
                        seriesDao.getByShortName(event.seriesShortName);

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /* Fill views of the element at position */
                        holder.seriesTypeIcon.setImageResource(
                                getResourceBasedOnType(series.seriesType));

                        holder.eventNameTextView.setText(event.eventName);

                        holder.seriesNameTextView.setText(series.completeName);

                        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
                        StringBuilder datesStringBuilder = new StringBuilder()
                                .append(context.getString(R.string.dates))
                                .append(": ")
                                .append(dateFormat.format(event.startDate))
                                .append(" - ")
                                .append(dateFormat.format(event.endDate));

                        holder.eventTimesTextView.setText(datesStringBuilder.toString());
                    }
                });

                /* Check if the event has some sessions to be notified */
                if(sessionDao.getAllOfEvent(event.ID, event.seriesShortName).size() > 0){
                    /* TODO: change icon to clock on */
                }
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return (eventsList == null)?0:eventsList.size();
    }

}
