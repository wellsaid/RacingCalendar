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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarNotifier;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    /* The view holder for this adapter */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.session_name_text_view)
        TextView sessionNameTextView;

        @BindView(R.id.session_time_text_view)
        TextView sessionTimeTextView;

        @BindView(R.id.notify_image_button)
        ImageButton notifyImageButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /* the sessions which are shown */
    private List<RacingCalendar.Session> sessionList = null;

    /* the notifier object */
    private RacingCalendarNotifier racingCalendarNotifier;

    /* the session dao */
    private RacingCalendarDaos.SessionDao sessionDao;

    /* the context from which the adapter is called */
    private Context context;

    /**
     * Constructor
     * @param context
     *    the context from which the adapter is called
     */
    public SessionAdapter(Context context){
        this.context = context;

        this.sessionList = new ArrayList<>();

        racingCalendarNotifier = RacingCalendarNotifier.getInstance();
        sessionDao =
                RacingCalendarDatabase.getDatabaseFromContext(context).getSessionDao();
    }

    /**
     * Add a list of sessions to the adapter
     * @param newSessionList
     *     The event list to add
     */
    public void add(List<RacingCalendar.Session> newSessionList){
        sessionList.addAll(newSessionList);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        /* Take the session we have to show */
        final RacingCalendar.Session session = sessionList.get(position);

        /* fill the element */
        holder.sessionNameTextView.setText(session.completeName);

        DateFormat dateFormat = SimpleDateFormat.getTimeInstance();
        StringBuilder sessionTimeStringBuilder = new StringBuilder()
                .append(dateFormat.format(session.startDateTime))
                .append(" - ")
                .append(dateFormat.format(session.endDateTime));

        holder.sessionTimeTextView.setText(sessionTimeStringBuilder.toString());

        holder.notifyImageButton
                .setImageResource((session.notify)?R.mipmap.clock_on:R.mipmap.clock_off);

        /* set on click listener for notify image button */
        holder.notifyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* toggle session notify status */
                session.notify = !session.notify;
                holder.notifyImageButton
                        .setImageResource((session.notify)?R.mipmap.clock_on:R.mipmap.clock_off);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /* update the local database */
                        if(session.notify){
                            /* add it from notify sessions */
                            racingCalendarNotifier.addSessionNotification(context, session);
                        } else {
                            /* remove it from notify sessions */
                            racingCalendarNotifier.removeSessionNotification(context, session);
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return (sessionList == null)?0:sessionList.size();
    }
}
