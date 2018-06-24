package wellsaid.it.racingcalendargetter;

/**
 * Class the will contain the objets types stored on the server
 */
public class RacingCalendar {

    /* Table containing informations on all series types in the database */
    public static final String SERIES_TYPES = "series_types";

    /* Table containing informations on all series in the database */
    public static final String SERIES = "series";

    /* Table containing informations on all events in the database */
    public static final String EVENTS = "events";

    /* Table containing informations on all session types in the database */
    public static final String SESSION_TYPES = "session_types";

    /* Table containing informations on all sessions in the database */
    public static final String SESSIONS = "sessions";

    /**
     * Object containing information about a series type
     */
    public static class SeriesType {

        public String shortName;
        public String completeName;
        public String description;
        public String thumbnailURL;

        /**
         * Constructor
         * @param shortName
         * @param completeName
         * @param description
         * @param thumbnailURL
         */
        public SeriesType(String shortName,
                          String completeName,
                          String description,
                          String thumbnailURL){

            this.shortName = shortName;
            this.completeName = completeName;
            this.description = description;
            this.thumbnailURL = thumbnailURL;
        }

    }
}
