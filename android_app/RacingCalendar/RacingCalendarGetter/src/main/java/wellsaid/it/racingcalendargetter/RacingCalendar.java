package wellsaid.it.racingcalendargetter;

import java.util.Date;

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

    /**
     * Object containing information about a series
     */
    public static class Series {

        public String shortName;
        public String completeName;
        public String seriesType;
        public String description;
        public String logoUrl;
        public String thumbnailURL;

        /**
         * Constructor
         * @param shortName
         * @param completeName
         * @param seriesType
         * @param description
         * @param logoUrl
         * @param thumbnailURL
         */
        public Series(String shortName,
                      String completeName,
                      String seriesType,
                      String description,
                      String logoUrl,
                      String thumbnailURL){

            this.shortName = shortName;
            this.completeName = completeName;
            this.seriesType = seriesType;
            this.description = description;
            this.logoUrl = logoUrl;
            this.thumbnailURL = thumbnailURL;
        }

    }

    /**
     * Object containing information about a event
     */
    public static class Event {

        public String ID;
        public String seriesShortName;
        public String eventShortName;
        public String eventName;
        public String circuitName;
        public Date startDate;
        public Date endDate;

        /**
         * Constructor
         * @param ID
         * @param seriesShortName
         * @param eventShortName
         * @param eventName
         * @param circuitName
         * @param startDate
         * @param endDate
         */
        public Event(String ID,
                      String seriesShortName,
                      String eventShortName,
                      String eventName,
                      String circuitName,
                      Date startDate,
                      Date endDate){

            this.ID = ID;
            this.seriesShortName = seriesShortName;
            this.eventShortName = eventShortName;
            this.eventName = eventName;
            this.circuitName = circuitName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

    }

    /**
     * Object containing information about a session type
     */
    public static class SessionType {

        public String shortName;
        public String completeName;

        /**
         * Constructor
         * @param shortName
         * @param completeName
         */
        public SessionType(String shortName,
                          String completeName){

            this.shortName = shortName;
            this.completeName = completeName;
        }

    }

    /**
     * Object containing information about a session type
     */
    public static class Session {

        public String shortName;
        public String completeName;
        public String sessionType;
        public String eventID;
        public String seriesShortName;
        public Date startDateTime;
        public Date endDateTime;

        /**
         * Constructor
         * @param shortName
         * @param completeName
         * @param sessionType
         * @param eventID
         * @param seriesShortName
         * @param startDateTime
         * @param endDateTime
         */
        public Session(String shortName,
                       String completeName,
                       String sessionType,
                       String eventID,
                       String seriesShortName,
                       Date startDateTime,
                       Date endDateTime){

            this.shortName = shortName;
            this.completeName = completeName;
            this.sessionType = sessionType;
            this.eventID = eventID;
            this.seriesShortName = seriesShortName;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }

    }
}
