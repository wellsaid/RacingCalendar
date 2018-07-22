package wellsaid.it.racingcalendar.adapters;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.LifecycleOwner;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendar.appwidget.NextEventsWidgetProvider;
import wellsaid.it.racingcalendar.R;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarDaos;
import wellsaid.it.racingcalendardata.RacingCalendarDatabase;
import wellsaid.it.racingcalendardata.RacingCalendarNotifier;
import wellsaid.it.racingcalendardata.RacingCalendarUtils;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    /* The view holder for this adapter */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.session_name_text_view)
        TextView sessionNameTextView;

        @BindView(R.id.session_time_text_view)
        TextView sessionTimeTextView;

        @BindView(R.id.notify_image_button)
        ImageButton notifyImageButton;

        @BindView(R.id.date_text_view)
        TextView dateTextView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /* The class of elements which can be contained in this adapter */
    private class Element {
        static final int TYPE_HEADER = 0;
        static final int TYPE_SESSION = 1;

        int type;
        String headerDate = null;
        RacingCalendar.Session session = null;

        Element(int type, String headerDate, RacingCalendar.Session session){
            this.type = type;
            this.headerDate = headerDate;
            this.session = session;
        }
    }

    /* the elements which are shown */
    private List<Element> elemList;

    /* the notifier object */
    private RacingCalendarNotifier racingCalendarNotifier;

    /* the session dao */
    private RacingCalendarDaos.SessionDao sessionDao;

    /* the context from which the adapter is called */
    private Context context;

    private LifecycleOwner activity;

    /**
     * Constructor
     * @param context
     *    the context from which the adapter is called
     */
    public SessionAdapter(Context context, LifecycleOwner activity){
        this.context = context;
        this.activity = activity;

        this.elemList = new ArrayList<>();

        racingCalendarNotifier = RacingCalendarNotifier.getInstance();
        sessionDao =
                RacingCalendarDatabase.getDatabaseFromContext(context).getSessionDao();
    }

    /**
     * Replace the list of sessions to the adapter
     * @param newSessionList
     *     The event list to replace
     */
    public void replace(List<RacingCalendar.Session> newSessionList){
        /* clear the current element list */
        elemList.clear();

        /* sort all sessions by start time */
        newSessionList.sort(new Comparator<RacingCalendar.Session>() {
            @Override
            public int compare(RacingCalendar.Session session, RacingCalendar.Session session1) {
                return session.startDateTime.compareTo(session1.startDateTime);
            }
        });

        /* build the element list */
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        String curDate = null;

        /* for each session in the new list */
        for(RacingCalendar.Session session: newSessionList){
            String sessionDate = dateFormat.format(session.startDateTime);
            /* if this is a new date */
            if(curDate == null || !curDate.equals(sessionDate)) {
                /* update current date */
                curDate = sessionDate;

                /* put a header in the element list */
                elemList.add(new Element(Element.TYPE_HEADER, curDate, null));
            }

            /* put session in the list */
            elemList.add(new Element(Element.TYPE_SESSION, null, session));
        }

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
        /* Take the element we have to show */
        final Element element = elemList.get(position);

        switch (element.type){
            case Element.TYPE_HEADER:
                /* hide all other element */
                holder.sessionNameTextView.setVisibility(View.GONE);
                holder.sessionTimeTextView.setVisibility(View.GONE);
                holder.notifyImageButton.setVisibility(View.GONE);

                /* show the date textview */
                holder.dateTextView.setVisibility(View.VISIBLE);
                holder.dateTextView.setText(element.headerDate);
                break;
            case Element.TYPE_SESSION:
                final RacingCalendar.Session session = element.session;

                /* fill the element */
                holder.sessionNameTextView.setText(session.completeName);

                DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String sessionTimeString =
                        dateFormat.format(session.startDateTime) +
                        " - " +
                        dateFormat.format(session.endDateTime);

                holder.sessionTimeTextView.setText(sessionTimeString);

                holder.notifyImageButton
                        .setImageResource((session.notify)?R.mipmap.clock_on:R.mipmap.clock_off);

                /* Check if the session is past */
                if(session.endDateTime.before(Calendar.getInstance().getTime())){
                    holder.notifyImageButton.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    holder.sessionTimeTextView.setLayoutParams(params);
                } else {
                    /* set on click listener for notify image button */
                    holder.notifyImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /* toggle session notify status */
                            session.notify = !session.notify;
                            holder.notifyImageButton
                                    .setImageResource((session.notify) ? R.mipmap.clock_on : R.mipmap.clock_off);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    /* Perform operations on notify status change */
                                    RacingCalendarUtils.sessionNotifyStatusChanged(context, session);

                                    /* update the widgets */
                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                                            new ComponentName(context, NextEventsWidgetProvider.class));
                                    NextEventsWidgetProvider.updateAll(context, appWidgetManager, appWidgetIds);
                                }
                            }).start();
                        }
                    });
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (elemList == null)?0:elemList.size();
    }
}
