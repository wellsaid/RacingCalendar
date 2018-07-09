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

import java.util.ArrayList;
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
public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.ViewHolder> {

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

    /* The view holder for this adapter */
    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.background_thumbnail)
        ImageView backgroundThumbnail;

        @BindView(R.id.series_name_text_view)
        TextView seriesNameTextView;

        @BindView(R.id.series_type_text_view)
        TextView seriesTypeTextView;

        @BindView(R.id.favorite_button)
        ImageButton favoriteImageButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /* The series which are shown */
    private List<RacingCalendar.Series> seriesList;

    /* The context in which the adapter is created */
    private Context context;

    /* true if the create want just favorite series to stay in the adapter */
    private boolean onlyFavorites;

    /* the listener which will receive updates to favorites change status */
    private FavoritesChangeListener listener;

    /* the DAO objects used to interact with the database */
    private RacingCalendarDaos.SeriesDao seriesDao;
    private RacingCalendarDaos.EventDao eventDao;
    private RacingCalendarDaos.SessionDao sessionDao;

    /* the notifier object */
    private RacingCalendarNotifier racingCalendarNotifier;

    /* helper method to perform operations when a series becomes favorite or un-favorite */
    private void seriesFavoriteStatusChanged(final RacingCalendar.Series series,
                                            final int position){
        /* if the series just becomed favorite */
        if(series.favorite){
            /* add it to the local database */
            seriesDao.insertOrUpdate(series);

            /* download and add all its events in the local database */
            RacingCalendarGetter.getEventsOfSeries(series.shortName,
                    new RacingCalendarGetter.Listener<RacingCalendar.Event>() {
                @Override
                public void onRacingCalendarObjectsReceived(List<RacingCalendar.Event> list) {
                    /* when ready load them into the database */
                    eventDao.insertAll(list);

                    /* download and add all its sessions in the local database */
                    RacingCalendarGetter.getSessionOfSeries(series.shortName,
                            new RacingCalendarGetter.Listener<RacingCalendar.Session>() {
                                @Override
                                public void onRacingCalendarObjectsReceived(
                                        List<RacingCalendar.Session> list) {
                                    /* when ready load the into the database */
                                    sessionDao.insertAll(list);

                                    /* subscribe to all */
                                    racingCalendarNotifier.addSessionsNotifications(context, list);
                                    /* TODO: Subsribe based on user settings (all, just race ...) */
                                }
                            });
                }
            });
        /* if the series just becomed un-favorite */
        } else {
            /* un-subscribe from all sessions */
            racingCalendarNotifier.removeSessionNotifications(context,
                    sessionDao.getAllOfSeries(series.shortName));

            /* remove all sessions of that series from local database */
            sessionDao.deleteAllOfSeries(series.shortName);

            /* remove all events of that series from local database */
            eventDao.deleteAllOfSeries(series.shortName);

            /* remove it from the local database */
            seriesDao.delete(series);

            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    /* if caller wants just favorites in the adapter */
                    if (onlyFavorites) {
                        /* Remove the element */
                        seriesList.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            });
        }

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /* call the listener to signal a favorite status change */
                listener.onFavoritesChanged(series);
            }
        });
    }

    /**
     * Constructor
     * @param context
     *     The context in which the adapter is created
     * @param onlyFavorites
     *     True if you want just favorite series to stay in the adapter
     * @param listener
     *     The listener which will receive updates to favorites change status
     */
    public SeriesAdapter(Context context, boolean onlyFavorites, FavoritesChangeListener listener){
        this.context = context;
        this.onlyFavorites = onlyFavorites;
        this.listener = listener;

        RacingCalendarDatabase db = RacingCalendarDatabase.getDatabaseFromContext(context);
        this.seriesDao = db.getSeriesDao();
        this.eventDao = db.getEventDao();
        this.sessionDao = db.getSessionDao();

        this.racingCalendarNotifier = RacingCalendarNotifier.getInstance();

        this.seriesList = new ArrayList<>();
    }

    /**
     * Add a list of series to the adapter
     * @param newSeriesList
     *     The series list to add
     */
    public void add(List<RacingCalendar.Series> newSeriesList){
        seriesList.addAll(newSeriesList);

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Modify the corresponding item in the adapter if exists
     * @param series
     */
    public void favoriteStatusChanged(RacingCalendar.Series series){
        /* Check if the element exists in the adapter */
        final int position = seriesList.indexOf(series);
        if(onlyFavorites){ /* case onlyFavorites */
            if(position != -1) {
                /* if it exists -> delete it */
                seriesList.remove(position);
            } else {
                /* if it wasn't there -> add it */
                /* if it exists -> delete it */
                seriesList.add(series);
            }
        } else { /* case not onlyFavorites */
            if(position != -1) {
                /* if it was there -> change it */
                seriesList.get(position).favorite = series.favorite;
            }
        }

        /* notify the change */
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Clears all series from the adapter
     */
    public void clear(){
        if(seriesList != null) {
            seriesList.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.series_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        /* Take the series we have to show */
        final RacingCalendar.Series series = seriesList.get(position);

        /* Load thumbnail image */
        Picasso.with(context)
                .load(series.thumbnailURL)
                .resize(holder.backgroundThumbnail.getWidth(),
                        (int) context.getResources().getDimension(R.dimen.series_card_height))
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(holder.backgroundThumbnail);

        /* Put series name and type in the text views */
        holder.seriesNameTextView.setText(series.completeName);
        holder.seriesTypeTextView.setText(series.seriesType);

        /* Set image for the favorite button based to on if the series is favorite */
        new Thread(new Runnable() {
            @Override
            public void run() {
                RacingCalendar.Series seriesDb = seriesDao.getByShortName(series.shortName);
                if(seriesDb != null && seriesDb.favorite){
                    /* mark the series as favorite */
                    series.favorite = true;
                }

                /* change its favorite icon (on main thread) */
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        holder.favoriteImageButton.setImageResource(
                                (series.favorite)?android.R.drawable.btn_star_big_on:
                                                  android.R.drawable.btn_star_big_off);
                    }
                });
            }
        }).start();


        /* Set on click listener for the favorite image button */
        holder.favoriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /* Toggle series favorite status */
                        series.favorite = !series.favorite;
                        seriesFavoriteStatusChanged(series, holder.getAdapterPosition());

                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                /* Toggle icon image */
                                holder.favoriteImageButton.setImageResource(
                                        (series.favorite) ? android.R.drawable.btn_star_big_on :
                                                android.R.drawable.btn_star_big_off);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (seriesList == null)?0:seriesList.size();
    }


}
