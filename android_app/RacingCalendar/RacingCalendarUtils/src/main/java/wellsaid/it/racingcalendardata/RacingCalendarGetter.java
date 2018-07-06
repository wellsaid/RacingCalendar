package wellsaid.it.racingcalendardata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static wellsaid.it.racingcalendardata.RacingCalendar.*;

/**
 * Class used to retrieve RacingCalendar objects from the online database
 */
public class RacingCalendarGetter {

    /* The URL to the server */
    private static final String SERVER_URL = "http://racingcalendar.altervista.org/select.php/";

    /* Helper method to retrieve RacingCalendar object from a JSON server response */
    private static List<Object> jsonArrayToObjectList(String table, String responseBody){
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        /* Choose what objects to parse based on the table */
        Type listType = null;
        switch(table){
            case SERIES:
                listType = new TypeToken<ArrayList<RacingCalendar.Series>>(){}.getType();
                break;
            case EVENTS:
                listType = new TypeToken<ArrayList<RacingCalendar.Event>>(){}.getType();
                break;
            case SESSIONS:
                listType = new TypeToken<ArrayList<RacingCalendar.Session>>(){}.getType();
                break;
            default:
                throw new IllegalArgumentException("Invalid table: " + table);
        }

        /* parse the json and return the list to the caller */
        try {
            return gson.fromJson(responseBody, listType);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * This interface is implemented by listeners to retrieve data from get() method
     */
    public interface DataListener {
        /**
         * Called by RacingCalendarGetter when response is ready
         * @param list
         *     If successfull the list of objects representing the results. If failed null.
         */
        void onRacingCalendarDataReceived(List<Object> list);
    }

    /**
     * Method to retrieve objects from a table
     * @param table
     *     Specify the table from which you want to retrieve objects (it will determine the
     *     object it will be returned).
     *     Possible values: SERIES_TYPES_TABLE, SERIES_TABLE, EVENTS_TABLE, SESSION_TYPES_TABLE,
     *                      SESSIONS_TABLE
     * @param selection
     *     The list of strings with fields among which to select the output
     * @param selectionValues
     *     The corresponding values of fields in selection
     * @param listener
     *     The listener to which to return the list of objects
     */
    public static void get(final String table, List<String> selection,
                           List<String> selectionValues, final DataListener listener) {

        /* Check if table is one of the available */
        if (!Arrays.asList(SERIES, EVENTS, SESSIONS).contains(table)) {
            /* if not throw an exception */
            throw new IllegalArgumentException("Invalid table: " + table);
        }

        /* Check if selection and selectionValues lists matches */
        if(selection != null && selectionValues != null) {
            if (selection.size() != selectionValues.size()) {
                /* if not throw an exception */
                throw new IllegalArgumentException(
                        "selection and selectionValues array must have the same size");
            }
        }

        /* Check if we have a listener to return to */
        if(listener == null){
            /* if not we have nothing to do */
            return;
        }

        /* Create client object to connect with the server */
        OkHttpClient client = new OkHttpClient();

        /* Parse the url of the request */
        HttpUrl httpUrl = HttpUrl.parse(SERVER_URL+table);
        if(httpUrl == null){
            throw new IllegalArgumentException("server url is invalid");
        }

        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        if(selection != null && selectionValues != null) {
            for (int i = 0; i < selectionValues.size(); i++) {
                urlBuilder.addQueryParameter(selection.get(i), selectionValues.get(i));
            }
        }
        String url = urlBuilder.build().toString();

        /* Create the request object to perform the request */
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /* pass null to the listener to signal a failure */
                listener.onRacingCalendarDataReceived(null);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    /* pass null to the listener to signal a failure */
                    listener.onRacingCalendarDataReceived(null);
                    return;
                }

                ResponseBody body = response.body();
                if(body == null){
                    /* pass null to the listener to signal a failure */
                    listener.onRacingCalendarDataReceived(null);
                    return;
                }

                /* on data correctly received, pack them in objects and send them to listener */
                listener.onRacingCalendarDataReceived(jsonArrayToObjectList(table, body.string()));
            }
        });
    }

    /**
     * This interface is implemented by listeners to retrieve data from get<T>() method
     */
    public interface Listener<T> {
        /**
         * Called by RacingCalendarGetter when response is ready
         * @param list
         *     If successfull the list of objects representing the results. If failed null.
         */
        void onRacingCalendarObjectsReceived(List<T> list);
    }

    /* Helper method to cast a list of objects into a list of T */
    private static <T> List<T> castList(List<Object> list) {
        if(list == null){
            return null;
        }

        ArrayList<T> toReturn = new ArrayList<>();

        for(Object obj : list){
            try {
                toReturn.add((T) obj);
            } catch (ClassCastException e){
                throw new IllegalArgumentException("Element in list cannot be cast");
            }
        }

        return toReturn;
    }

    /**
     * Method to retrieve series from the server
     * @param shortName
     *     The value of the primary key of the desired series (null for all)
     * @param listener
     *     The listener to which to return the list of objects
     */
    public static void getSeries(String shortName, final Listener<Series> listener) {
        List<String> selection = null;
        List<String> selectionValues = null;
        if(shortName != null){
            selection = Arrays.asList("shortName");
            selectionValues = Arrays.asList(shortName);
        }

        get(SERIES, selection, selectionValues, new DataListener() {
            @Override
            public void onRacingCalendarDataReceived(List<Object> list) {
                listener.onRacingCalendarObjectsReceived(
                        RacingCalendarGetter.<Series>castList(list));
            }
        });
    }

    /**
     * Method to retrieve events from the server
     * @param ID
     *     The ID of the of the desired series (null for all)
     *     (togheter with seriesShortName is the primary key)
     * @param seriesShortName
     *     The series of the of the desired series (null for all)
     *     (togheter with ID is the primary key)
     * @param listener
     *     The listener to which to return the list of objects
     */
    public static void getEvents(String ID, String seriesShortName,
                                 final Listener<Event> listener) {
        List<String> selection = null;
        List<String> selectionValues = null;
        if(ID != null && seriesShortName != null){
            selection = Arrays.asList("ID","seriesShortName");
            selectionValues = Arrays.asList(ID,seriesShortName);
        }

        get(EVENTS, selection, selectionValues, new DataListener() {
            @Override
            public void onRacingCalendarDataReceived(List<Object> list) {
                listener.onRacingCalendarObjectsReceived(
                        RacingCalendarGetter.<Event>castList(list));
            }
        });
    }

    /**
     * Method to retrieve series from the server
     * @param shortName
     *     The short name of the desired session (null for all)
     *     (togheter with eventID and seriesShortName is the primary key)
     * @param seriesShortName
     *     The short name of the series of desired session (null for all)
     *     (togheter with eventID and shortName is the primary key)
     * @param eventID
     *     The id of the event (null for all)
     *     (togheter with seriesShortName and shortName is the primary key)
     * @param listener
     *     The listener to which to return the list of objects
     */
    public static void getSession(String shortName, String seriesShortName, String eventID,
                                  final Listener<Session> listener) {
        List<String> selection = null;
        List<String> selectionValues = null;
        if(shortName != null && seriesShortName != null && eventID != null){
            selection = Arrays.asList("shortName" ,"seriesShortName","eventID");
            selectionValues = Arrays.asList(shortName, seriesShortName, eventID);
        }

        get(SESSIONS, selection, selectionValues, new DataListener() {
            @Override
            public void onRacingCalendarDataReceived(List<Object> list) {
                listener.onRacingCalendarObjectsReceived(
                        RacingCalendarGetter.<Session>castList(list));
            }
        });
    }
}
