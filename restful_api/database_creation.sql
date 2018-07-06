DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS series;

CREATE TABLE series (
	shortName VARCHAR(10) PRIMARY KEY,
	completeName VARCHAR(50) NOT NULL,
	seriesType VARCHAR(20) NOT NULL,
	description VARCHAR(300),
	logoURL VARCHAR(100),
	thumbnailURL VARCHAR(100)
);

CREATE TABLE events (
	ID INTEGER,
	seriesShortName VARCHAR(10),
	eventShortName VARCHAR(10),
	eventName VARCHAR(50),
	circuitName VARCHAR(50),
	startDate DATE,
	endDate DATE,
	FOREIGN KEY (seriesShortName) REFERENCES series(shortName),
	PRIMARY KEY (ID, seriesShortName)
);

CREATE TABLE sessions (
    shortName VARCHAR(10) NOT NULL,
	completeName VARCHAR(50) NOT NULL,
	sessionType VARCHAR(20) NOT NULL,
	eventID INTEGER NOT NULL,
	seriesShortName VARCHAR(10) NOT NULL,
	startDateTime DATETIME NOT NULL,
	endDateTime DATETIME,
	FOREIGN KEY (eventID, seriesShortName) REFERENCES events(ID, seriesShortName),
	PRIMARY KEY (shortName, eventID, seriesShortName)
);
