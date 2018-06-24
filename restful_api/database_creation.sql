DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS session_types;

DROP TABLE IF EXISTS events;

DROP TABLE IF EXISTS series;
DROP TABLE IF EXISTS series_types;

CREATE TABLE series_types (
	shortName VARCHAR(20) PRIMARY KEY,
	completeName VARCHAR(50),
	description VARCHAR(500),
	thumbnailURL VARCHAR(100)
);

CREATE TABLE series (
	shortName VARCHAR(10) PRIMARY KEY,
	completeName VARCHAR(50) NOT NULL,
	seriesType VARCHAR(20) NOT NULL,
	description VARCHAR(300),
	logoURL VARCHAR(100),
	thumbnailURL VARCHAR(100),
	FOREIGN KEY (series_types) REFERENCES series_types(shortName)
);

CREATE TABLE events (
	ID INTEGER,
	seriesShortName VARCHAR(10),
	eventShortName VARCHAR(10),
	eventName VARCHAR(50),
	circuitName VARCHAR(50),
	startDate DATE,
	endDate DATE,
	FOREIGN KEY (series_short_name) REFERENCES series(shortName),
	PRIMARY KEY (ID, seriesShortName)
);

CREATE TABLE session_types (
	shortName VARCHAR(10) PRIMARY KEY,
	completeName VARCHAR(50) NOT NULL
);

CREATE TABLE sessions (
    shortName VARCHAR(10) NOT NULL,
	completeName VARCHAR(50) NOT NULL,
	sessionType VARCHAR(10) NOT NULL,
	eventID INTEGER NOT NULL,
	seriesShortName VARCHAR(10) NOT NULL,
	startDateTime DATETIME NOT NULL,
	endDateTime DATETIME,
	FOREIGN KEY (session_type) REFERENCES session_types(shortName),
	FOREIGN KEY (event_ID, seriesShortName) REFERENCES events(ID, seriesShortName),
	PRIMARY KEY (shortName, eventID, seriesShortName)
);
