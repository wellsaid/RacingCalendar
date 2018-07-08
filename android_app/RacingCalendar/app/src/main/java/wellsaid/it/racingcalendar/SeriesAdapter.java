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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;

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
    private List<RacingCalendar.Series> seriesList = null;

    /* The context in which the adapter is created */
    private Context context;

    /* true if the create want just favorite series to stay in the adapter */
    private boolean onlyFavorites;

    /* the listener which will receive updates to favorites change status */
    private FavoritesChangeListener listener;

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
    }

    /**
     * Add a list of series to the adapter
     * @param newSeriesList
     *     The series list to add
     */
    public void add(List<RacingCalendar.Series> newSeriesList){
        if(seriesList == null){
            seriesList = newSeriesList;
        } else {
            seriesList.addAll(newSeriesList);
        }

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
                .placeholder(R.drawable.placeholder)
                .into(holder.backgroundThumbnail);

        /* Put series name and type in the text views */
        holder.seriesNameTextView.setText(series.completeName);
        holder.seriesTypeTextView.setText(series.seriesType);

        /* Set image for the favorite button based to on if the series is favorite */
        final RacingCalendarDaos.SeriesDao seriesDao =
                RacingCalendarDatabase.getDatabaseFromContext(context).getSeriesDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                RacingCalendar.Series seriesDb = seriesDao.getByShortName(series.shortName);
                if(series != null && series.favorite){
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
                        seriesDao.insertOrUpdate(series);

                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                /* Toggle icon image */
                                holder.favoriteImageButton.setImageResource(
                                        (series.favorite)?android.R.drawable.btn_star_big_on:
                                                android.R.drawable.btn_star_big_off);

                                /* if caller wants just favorites in the adapter */
                                if(onlyFavorites) {
                                    /* Remove the element */
                                    int position = holder.getAdapterPosition();
                                    seriesList.remove(position);
                                    notifyItemRemoved(position);
                                }

                                /* call the listener to signal a favorite status change */
                                listener.onFavoritesChanged(series);
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
