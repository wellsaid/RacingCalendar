package wellsaid.it.racingcalendar;

import android.content.Context;
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
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarNotifier;

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

    /**
     * Constructor
     * @param context
     *     The context in which the adapter is created
     */
    public SeriesAdapter(Context context){
        this.context = context;
    }

    public void add(List<RacingCalendar.Series> newSeriesList){
        if(seriesList == null){
            seriesList = newSeriesList;
        } else {
            seriesList.addAll(newSeriesList);
        }
        notifyDataSetChanged();
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
                // TODO: Add a placeholder in the drawable directory
                //.placeholder(R.drawable.thumbnail_placeholder)
                .into(holder.backgroundThumbnail);

        /* Put series name and type in the text views */
        holder.seriesNameTextView.setText(series.completeName);
        /* TODO: put series type complete name in the series directly */
        holder.seriesTypeTextView.setText(series.seriesType);

        /* Set on click listener for the favorite image button */
        holder.favoriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /* Toggle series favorite status */
                        series.favorite = !series.favorite;
                        RacingCalendarDatabase.getDatabaseFromContext(context)
                                .getSeriesDao().insertOrUpdate(series);

                        /* Toggle icon */
                        holder.favoriteImageButton.setImageResource(
                                (series.favorite)?android.R.drawable.star_big_on:
                                        android.R.drawable.star_big_off);
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
