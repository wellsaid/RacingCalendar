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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

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

        @BindView(R.id.series_logo_image_view)
        ImageView seriesLogoIcon;

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

    /**
     * Called when you need to notify to this fragment that a series changed favorite status
     * @param series
     *     The series who changed status
     */
    public void notifyChangeFavoriteStatus(final RacingCalendar.Series series){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(series.favorite){
                    /* get all events of this series */
                    List<RacingCalendar.Event> tmpEventList = eventDao.getAllOfSeries(series.shortName);

                    /* for each event in the list */
                    for(RacingCalendar.Event event : tmpEventList){
                        /* get all sessions of this event */
                        List<RacingCalendar.Session> sessionList =
                                sessionDao.getAllOfEvent(event.ID, event.seriesShortName);

                        /* subscribe to all this event */
                        racingCalendarNotifier.addSessionsNotifications(context, sessionList);
                    }
                } else {
                    /* remove for the list all events of this series */
                    eventsList.removeIf(new Predicate<RacingCalendar.Event>() {
                        @Override
                        public boolean test(RacingCalendar.Event event) {
                            return event.seriesShortName.equals(series.shortName);
                        }
                    });
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();
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

        this.eventsList = new ArrayList<>();
    }

    /**
     * Add a list of events to the adapter
     * @param newEventList
     *     The event list to add
     */
    public void add(List<RacingCalendar.Event> newEventList){
        /* remove all past events from the list */
        newEventList.removeIf(new Predicate<RacingCalendar.Event>() {
            @Override
            public boolean test(RacingCalendar.Event event) {
                return event.startDate.before(Calendar.getInstance().getTime());
            }
        });

        /* add remaining event to the list */
        eventsList.addAll(newEventList);

        /* sort events in the adapter */
        eventsList.sort(new Comparator<RacingCalendar.Event>() {
            @Override
            public int compare(RacingCalendar.Event event, RacingCalendar.Event event1) {
                return event.startDate.compareTo(event1.startDate);
            }
        });

        /* notify the change */
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);
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
                if(series == null){
                    return;
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /* Fill views of the element at position */
                        Picasso.with(context)
                                .load(series.logoURL)
                                .resize(holder.seriesLogoIcon.getWidth(),
                                        (int) context.getResources().getDimension(R.dimen.event_card_height))
                                .centerInside()
                                .placeholder(R.drawable.placeholder)
                                .into(holder.seriesLogoIcon);

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

                /* TODO: Add on click listener for clock icon */
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return (eventsList == null)?0:eventsList.size();
    }

}
