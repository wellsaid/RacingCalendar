package wellsaid.it.racingcalendar;

import android.content.Context;
import android.content.Intent;
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

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
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

        @BindView(R.id.circuit_name_text_view)
        TextView circuitNameTextView;

        @BindView(R.id.event_times_text_view)
        TextView eventTimesTextView;

        @BindView(R.id.notify_image_button)
        ImageButton notifyImageButton;

        View view;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            this.view = view;
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

    /* helper method which contains the on click listener for a generic event alarm image button */
    private void notifyImageButtonOnClickListener(final RacingCalendar.Event event,
                                                  final ViewHolder holder){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<RacingCalendar.Session> notifySessions1 =
                        sessionDao.getAllOfEvent(event.ID, event.seriesShortName, 1);
                final boolean hasSessionToNotify1 = notifySessions1.size() > 0;

                /* if we had session to notify */
                if(hasSessionToNotify1){
                    /* remove them from the notifier */
                    racingCalendarNotifier.removeSessionNotifications(context, notifySessions1);

                    /* remove them from the database */
                    sessionDao.deleteAll(notifySessions1);

                    /* remove event from the local database */
                    eventDao.delete(event);
                } else {
                    /* add event to the local database */
                    eventDao.insert(event);

                    /* retrieve session to notify */
                    RacingCalendarGetter.getSessionOfEvent(
                            event.ID,
                            event.seriesShortName,
                            new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                                @Override
                                public void onRacingCalendarObjectsReceived(
                                        List<RacingCalendar.Session> list) {
                                    /* add them to the notifier */
                                    racingCalendarNotifier.addSessionsNotifications(context, list);

                                    /* add them to the database */
                                    sessionDao.insertAll(notifySessions1);
                                }
                            });
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /* change icon accordingly */
                        holder.notifyImageButton.setImageResource(
                                (hasSessionToNotify1)?R.mipmap.clock_off:
                                        R.mipmap.clock_on);
                    }
                });
            }
        }).start();
    }

    /* helper method to fill the element in onBindViewHolder */
    private void fillElement(final ViewHolder holder,
                             final RacingCalendar.Event event,
                             final RacingCalendar.Series series){
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

                holder.circuitNameTextView.setText(event.circuitName);

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
        final List<RacingCalendar.Session> notifySessions =
                sessionDao.getAllOfEvent(event.ID, event.seriesShortName, 1);
        final boolean hasSessionToNotify = notifySessions.size() > 0;

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /* change icon accordingly */
                holder.notifyImageButton.setImageResource(
                        (hasSessionToNotify) ? R.mipmap.clock_on : R.mipmap.clock_off);
            }
        });

        /* set on click listener for notify image button */
        holder.notifyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyImageButtonOnClickListener(event, holder);
            }
        });
    }

    /**
     * Called when you need to notify to this fragment that a series changed favorite status
     */
    public void notifyChangeFavoriteStatus(){
        /* notify the change */
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
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
                return event.endDate.before(Calendar.getInstance().getTime());
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

        /* TODO: Define on click listener for the card to open EventDetailActivity */
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* launch the event detail activity */
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EVENT_BUNDLE_KEY, Parcels.wrap(event));
                context.startActivity(intent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                /* Get the series related to the event
                 * from local database if series is favorite */
                RacingCalendar.Series series =
                        seriesDao.getByShortName(event.seriesShortName);
                if(series == null){
                    /* from the server if series is not favorite */
                    RacingCalendarGetter.getSeries(event.seriesShortName,
                            new RacingCalendarGetter.Listener<RacingCalendar.Series>() {
                        @Override
                        public void onRacingCalendarObjectsReceived(
                                List<RacingCalendar.Series> list) {
                            fillElement(holder, event, list.get(0));
                        }
                    });
                } else {
                    fillElement(holder, event, series);
                }
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return (eventsList == null)?0:eventsList.size();
    }

}
