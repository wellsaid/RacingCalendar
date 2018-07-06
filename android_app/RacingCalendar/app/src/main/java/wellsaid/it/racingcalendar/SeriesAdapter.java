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

    /**
     * Constructor
     * @param context
     *     The context in which the adapter is created
     * @param onlyFavorites
     *     True if you want just favorite series to stay in the adapter
     */
    public SeriesAdapter(Context context, boolean onlyFavorites){
        this.context = context;
        this.onlyFavorites = onlyFavorites;
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
        notifyDataSetChanged();
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
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
                if(seriesDao.getByShortName(series.shortName).favorite){
                    /* mark the series as favorite */
                    series.favorite = true;

                    /* change its favorite icon (on main thread) */
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            holder.favoriteImageButton
                                    .setImageResource(android.R.drawable.btn_star_big_on);
                        }
                    });
                }
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
                                    seriesList.remove(position);
                                    notifyItemRemoved(position);
                                }
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
