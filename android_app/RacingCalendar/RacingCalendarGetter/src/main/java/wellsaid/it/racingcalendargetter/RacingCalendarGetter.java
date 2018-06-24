package wellsaid.it.racingcalendargetter;

import com.google.gson.Gson;
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

import static wellsaid.it.racingcalendargetter.RacingCalendar.*;

public class RacingCalendarGetter {

    /* The URL to the server */
    private static final String SERVER_URL = "http://racingcalendar.altervista.org/select.php/";

    /* Helper method to retrieve RacingCalendar object from a JSON server response */
    private static List<Object> jsonArrayToRacingCalendarObject(String table, String responseBody){
        Gson gson = new Gson();

        /* TODO: Choose what objects to parse based on the table */
        Type collectionType = new TypeToken<ArrayList<RacingCalendar.SeriesType>>(){}.getType();

        return gson.fromJson(responseBody, collectionType);
    }

    /**
     * This interface is implemented by listener to retrieve data from get() method
     */
    public interface DataListener {
        /**
         * Called by RacingCalendarGetter when response is ready
         * @param list
         *     If successfull the list of objects representing the results. If failed null.
         */
        void onRacingCalendarData(List<Object> list);
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
        if (!Arrays.asList(SERIES_TYPES, SERIES, EVENTS, SESSION_TYPES,SESSIONS).contains(table)) {
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
            System.out.println("[RacingCalendarGetter] server url is invalid!");
            return;
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
                listener.onRacingCalendarData(null);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    /* pass null to the listener to signal a failure */
                    listener.onRacingCalendarData(null);
                    return;
                }

                ResponseBody body = response.body();
                if(body == null){
                    /* pass null to the listener to signal a failure */
                    listener.onRacingCalendarData(null);
                    return;
                }

                /* on data correctly received, pack them in objects and send them to listener */
                listener.onRacingCalendarData(
                        jsonArrayToRacingCalendarObject(table, body.string()));
            }
        });
    }
}
